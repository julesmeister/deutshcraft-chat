package com.julesmeister.deutschcraft.feature.mission.data.ai

import android.util.Log
import com.julesmeister.deutschcraft.BuildConfig
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenApi
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenChatCompletionRequest
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenMessage
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenResponseFormat
import com.julesmeister.deutschcraft.feature.mission.domain.MissionObjective
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull
import retrofit2.HttpException

@Singleton
class QwenMissionObjectiveEvaluationAiGateway
@Inject constructor(
    private val qwenApi: QwenApi,
    private val json: Json
) : MissionObjectiveEvaluationAiGateway {

    override suspend fun evaluateMessage(
        learnerMessage: String,
        recentConversation:
        List<MissionEvaluationTurn>,
        cefrLevel: CefrLevel,
        missionTitle: String,
        missionScenario: String,
        learnerRole: String,
        assistantRole: String,
        unfinishedObjectives:
        List<MissionObjective>
    ): MissionObjectiveEvaluationResult {
        if (
            learnerMessage.isBlank() ||
            unfinishedObjectives.isEmpty()
        ) {
            return emptyResult()
        }

        if (BuildConfig.QWEN_API_KEY.isBlank()) {
            Log.w(
                GATEWAY_TAG,
                "Mission evaluation skipped because " +
                        "the Qwen API key is missing."
            )

            return emptyResult()
        }

        return try {
            evaluateWithRepair(
                learnerMessage =
                    learnerMessage.trim(),
                recentConversation =
                    recentConversation,
                cefrLevel =
                    cefrLevel,
                missionTitle =
                    missionTitle,
                missionScenario =
                    missionScenario,
                learnerRole =
                    learnerRole,
                assistantRole =
                    assistantRole,
                unfinishedObjectives =
                    unfinishedObjectives
            )
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (
            httpException:
            HttpException
        ) {
            Log.e(
                GATEWAY_TAG,
                "Mission evaluation HTTP request failed.",
                httpException
            )

            emptyResult()
        } catch (
            ioException:
            IOException
        ) {
            Log.e(
                GATEWAY_TAG,
                "Mission evaluation could not reach Qwen.",
                ioException
            )

            emptyResult()
        } catch (throwable: Throwable) {
            Log.e(
                GATEWAY_TAG,
                "Mission objective evaluation failed.",
                throwable
            )

            emptyResult()
        }
    }

    private suspend fun evaluateWithRepair(
        learnerMessage: String,
        recentConversation:
        List<MissionEvaluationTurn>,
        cefrLevel: CefrLevel,
        missionTitle: String,
        missionScenario: String,
        learnerRole: String,
        assistantRole: String,
        unfinishedObjectives:
        List<MissionObjective>
    ): MissionObjectiveEvaluationResult {
        val initialContent =
            requestCompletion(
                messages =
                    buildEvaluationMessages(
                        learnerMessage =
                            learnerMessage,
                        recentConversation =
                            recentConversation,
                        cefrLevel =
                            cefrLevel,
                        missionTitle =
                            missionTitle,
                        missionScenario =
                            missionScenario,
                        learnerRole =
                            learnerRole,
                        assistantRole =
                            assistantRole,
                        unfinishedObjectives =
                            unfinishedObjectives
                    ),
                temperature =
                    EVALUATION_TEMPERATURE
            )

        val initialResult =
            decodeEvaluation(
                responseContent =
                    initialContent
            )

        if (initialResult != null) {
            return initialResult
        }

        Log.w(
            GATEWAY_TAG,
            "Qwen returned invalid mission evaluation " +
                    "JSON. Retrying once."
        )

        val repairedContent =
            requestCompletion(
                messages =
                    buildRepairMessages(
                        malformedResponse =
                            initialContent,
                        validObjectiveIds =
                            unfinishedObjectives.map { objective ->
                                objective.id
                            }
                    ),
                temperature =
                    REPAIR_TEMPERATURE
            )

        return decodeEvaluation(
            responseContent =
                repairedContent
        ) ?: emptyResult()
    }

    private suspend fun requestCompletion(
        messages: List<QwenMessage>,
        temperature: Double
    ): String {
        val response =
            qwenApi.createChatCompletion(
                request =
                    QwenChatCompletionRequest(
                        model =
                            BuildConfig.QWEN_MODEL,
                        messages =
                            messages,
                        enableThinking =
                            false,
                        temperature =
                            temperature,
                        maxTokens =
                            MAXIMUM_RESPONSE_TOKENS,
                        responseFormat =
                            QwenResponseFormat(
                                type =
                                    JSON_OBJECT_TYPE
                            )
                    )
            )

        return response
            .choices
            .firstOrNull()
            ?.message
            ?.content
            ?.trim()
            .orEmpty()
            .takeIf { content ->
                content.isNotBlank()
            }
            ?: throw IllegalStateException(
                "Qwen returned an empty mission " +
                        "evaluation response."
            )
    }

    private fun buildEvaluationMessages(
        learnerMessage: String,
        recentConversation:
        List<MissionEvaluationTurn>,
        cefrLevel: CefrLevel,
        missionTitle: String,
        missionScenario: String,
        learnerRole: String,
        assistantRole: String,
        unfinishedObjectives:
        List<MissionObjective>
    ): List<QwenMessage> {
        val objectiveJson =
            unfinishedObjectives.joinToString(
                separator =
                    ",\n"
            ) { objective ->
                """
            {
              "objectiveId": "${escapeJson(objective.id)}",
              "title": "${escapeJson(objective.title)}",
              "description": "${escapeJson(objective.description)}",
              "completionHint": "${escapeJson(objective.completionHint)}",
              "required": ${objective.isRequired}
            }
            """.trimIndent()
            }

        val conversationText =
            recentConversation
                .takeLast(
                    MAXIMUM_RECENT_TURN_COUNT
                )
                .joinToString(
                    separator =
                        "\n"
                ) { turn ->
                    val speaker =
                        when (turn.speaker) {
                            MissionEvaluationSpeaker.LEARNER -> {
                                "Learner"
                            }

                            MissionEvaluationSpeaker.ASSISTANT -> {
                                "Emil"
                            }
                        }

                    "$speaker: ${
                        turn.content
                            .trim()
                            .take(
                                MAXIMUM_TURN_LENGTH
                            )
                    }"
                }
                .ifBlank {
                    "No earlier conversation is available."
                }

        val safeLearnerMessage =
            learnerMessage
                .take(
                    MAXIMUM_MESSAGE_LENGTH
                )
                .let(
                    ::escapeJson
                )

        return listOf(
            QwenMessage(
                role =
                    SYSTEM_ROLE,
                content =
                    """
                You evaluate the current objective of an
                ongoing DeutschCraft German conversation.

                Return exactly:

                {
                  "completedObjectives": [
                    {
                      "objectiveId": "exact objective ID",
                      "confidence": 0.0,
                      "reason": "Short reason"
                    }
                  ]
                }

                If the objective is not complete:

                {
                  "completedObjectives": []
                }

                Evaluation rules:

                - Inspect the recent conversation and the
                  latest learner message together.
                - The conversation may already have been in
                  progress before the mission became active.
                - An objective may be completed by an earlier
                  learner turn in the recent conversation.
                - Do not force the learner to repeat an
                  action that is already clearly present.
                - Do not require the conversation to restart.
                - For a greeting objective, inspect whether
                  the learner already greeted Emil, addressed
                  Emil socially, introduced themselves, or
                  initiated friendly social contact.
                - If greeting already occurred earlier,
                  complete the greeting objective even when
                  the latest message discusses something else.
                - Evaluate learner messages only.
                - Emil's messages provide context but cannot
                  themselves complete an objective.
                - Use completionHint as the internal semantic
                  completion rule.
                - Exact keyword matching is not required.
                - Accept natural paraphrases with the same
                  communicative meaning.
                - Minor German grammar mistakes are allowed
                  when the intended communication is clear.
                - Topic relevance by itself is insufficient.
                - The learner must perform the required
                  communication action.
                - Do not infer information the learner never
                  communicated.
                - Return only IDs supplied in this request.
                - Confidence must be between 0 and 1.
                - Use confidence of at least 0.88 only when a
                  language teacher would reasonably agree the
                  objective was completed.
                - Return at most one objective.
                - Return JSON only.
                """.trimIndent()
            ),
            QwenMessage(
                role =
                    USER_ROLE,
                content =
                    """
                Learner CEFR level:
                ${cefrLevel.name}

                Mission:
                ${missionTitle.trim()}

                Scenario:
                ${missionScenario.trim()}

                Learner role:
                ${learnerRole.trim()}

                Emil role:
                ${assistantRole.trim()}

                Current objective:
                [
                $objectiveJson
                ]

                Recent conversation:
                $conversationText

                Latest learner message:
                "$safeLearnerMessage"

                Determine whether the current objective was
                completed anywhere in the learner's recent
                conversation turns.
                """.trimIndent()
            )
        )
    }

    private fun buildRepairMessages(
        malformedResponse: String,
        validObjectiveIds: List<String>
    ): List<QwenMessage> {
        val validIds =
            validObjectiveIds.joinToString(
                separator =
                    ", "
            )

        return listOf(
            QwenMessage(
                role =
                    SYSTEM_ROLE,
                content =
                    """
                    Repair malformed DeutschCraft mission
                    evaluation JSON.

                    Return exactly this structure:

                    {
                      "completedObjectives": [
                        {
                          "objectiveId": "exact valid ID",
                          "confidence": 0.0,
                          "reason": "Short reason"
                        }
                      ]
                    }

                    Valid objective IDs:
                    $validIds

                    Requirements:

                    - Return JSON only.
                    - Use only one of the listed IDs.
                    - Confidence must be between 0 and 1.
                    - Use an empty completedObjectives array
                      if completion is not clearly supported.
                    - Do not return Markdown.
                    """.trimIndent()
            ),
            QwenMessage(
                role =
                    USER_ROLE,
                content =
                    malformedResponse.take(
                        MAXIMUM_REPAIR_INPUT_LENGTH
                    )
            )
        )
    }

    private fun decodeEvaluation(
        responseContent: String
    ): MissionObjectiveEvaluationResult? {
        val root =
            decodeRootObject(
                responseContent =
                    responseContent
            )
                ?: return null

        val completedArray =
            root[
                COMPLETED_OBJECTIVES_KEY
            ] as? JsonArray
                ?: return null

        val evaluations =
            completedArray
                .mapNotNull { element ->
                    decodeCompletedObjective(
                        element =
                            element
                    )
                }
                .distinctBy { evaluation ->
                    evaluation.objectiveId
                }

        return MissionObjectiveEvaluationResult(
            completedObjectives =
                evaluations
        )
    }

    private fun decodeCompletedObjective(
        element: JsonElement
    ): EvaluatedMissionObjective? {
        val root =
            element as? JsonObject
                ?: return null

        val objectiveId =
            root.stringValue(
                key =
                    OBJECTIVE_ID_KEY
            )
                ?: return null

        val confidence =
            root.floatValue(
                key =
                    CONFIDENCE_KEY
            )
                ?.coerceIn(
                    minimumValue =
                        0f,
                    maximumValue =
                        1f
                )
                ?: return null

        val reason =
            root.stringValue(
                key =
                    REASON_KEY
            ).orEmpty()

        return EvaluatedMissionObjective(
            objectiveId =
                objectiveId,
            confidence =
                confidence,
            reason =
                reason
        )
    }

    private fun decodeRootObject(
        responseContent: String
    ): JsonObject? {
        val cleanedContent =
            removeMarkdownCodeFence(
                content =
                    responseContent
            )

        return try {
            json.parseToJsonElement(
                string =
                    cleanedContent
            ) as? JsonObject
        } catch (
            exception:
            SerializationException
        ) {
            Log.w(
                GATEWAY_TAG,
                "Qwen returned invalid mission " +
                        "evaluation JSON.",
                exception
            )

            null
        } catch (
            exception:
            IllegalArgumentException
        ) {
            Log.w(
                GATEWAY_TAG,
                "Qwen returned an unsupported mission " +
                        "evaluation payload.",
                exception
            )

            null
        }
    }

    private fun JsonObject.stringValue(
        key: String
    ): String? {
        return (
                this[key] as? JsonPrimitive
                )
            ?.contentOrNull
            ?.trim()
            ?.takeIf { value ->
                value.isNotBlank()
            }
    }

    private fun JsonObject.floatValue(
        key: String
    ): Float? {
        return (
                this[key] as? JsonPrimitive
                )?.floatOrNull
    }

    private fun removeMarkdownCodeFence(
        content: String
    ): String {
        return content
            .trim()
            .removePrefix(
                "```json"
            )
            .removePrefix(
                "```JSON"
            )
            .removePrefix(
                "```"
            )
            .removeSuffix(
                "```"
            )
            .trim()
    }

    private fun escapeJson(
        value: String
    ): String {
        return value
            .replace(
                "\\",
                "\\\\"
            )
            .replace(
                "\"",
                "\\\""
            )
            .replace(
                "\b",
                "\\b"
            )
            .replace(
                "\u000C",
                "\\f"
            )
            .replace(
                "\n",
                "\\n"
            )
            .replace(
                "\r",
                "\\r"
            )
            .replace(
                "\t",
                "\\t"
            )
    }

    private fun emptyResult():
            MissionObjectiveEvaluationResult {
        return MissionObjectiveEvaluationResult(
            completedObjectives =
                emptyList()
        )
    }

    private companion object {
        const val GATEWAY_TAG =
            "MissionObjectiveAi"

        const val SYSTEM_ROLE =
            "system"

        const val USER_ROLE =
            "user"

        const val JSON_OBJECT_TYPE =
            "json_object"

        const val COMPLETED_OBJECTIVES_KEY =
            "completedObjectives"

        const val OBJECTIVE_ID_KEY =
            "objectiveId"

        const val CONFIDENCE_KEY =
            "confidence"

        const val REASON_KEY =
            "reason"

        const val MAXIMUM_RESPONSE_TOKENS =
            500

        const val MAXIMUM_MESSAGE_LENGTH =
            2_000

        const val MAXIMUM_REPAIR_INPUT_LENGTH =
            6_000

        const val EVALUATION_TEMPERATURE =
            0.10

        const val REPAIR_TEMPERATURE =
            0.05

        const val MAXIMUM_RECENT_TURN_COUNT =
            12

        const val MAXIMUM_TURN_LENGTH =
            600
    }
}