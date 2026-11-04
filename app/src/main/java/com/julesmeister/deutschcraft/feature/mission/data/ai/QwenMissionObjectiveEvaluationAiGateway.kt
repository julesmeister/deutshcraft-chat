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
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull
import retrofit2.HttpException

/*
 * Uses one Qwen request to evaluate:
 *
 * 1. Completion of the currently active mission objective.
 * 2. Interaction and comprehension signals from the latest
 *    learner message.
 *
 * This avoids a second AI request for mission analytics.
 */
@Singleton
class QwenMissionObjectiveEvaluationAiGateway
@Inject constructor(
    private val qwenApi:
    QwenApi,
    private val json:
    Json
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

        if (
            BuildConfig.QWEN_BASE_URL.isBlank()
        ) {
            Log.w(
                GATEWAY_TAG,
                "Mission evaluation skipped because the " +
                        "Qwen API key is missing."
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
        val validObjectiveIds =
            unfinishedObjectives
                .map { objective ->
                    objective.id
                }
                .filter(
                    String::isNotBlank
                )
                .toSet()

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
                    initialContent,
                validObjectiveIds =
                    validObjectiveIds
            )

        if (initialResult != null) {
            return initialResult
        }

        Log.w(
            GATEWAY_TAG,
            "Qwen returned invalid mission evaluation JSON. " +
                    "Retrying once."
        )

        val repairedContent =
            requestCompletion(
                messages =
                    buildRepairMessages(
                        malformedResponse =
                            initialContent,
                        validObjectiveIds =
                            validObjectiveIds.toList()
                    ),
                temperature =
                    REPAIR_TEMPERATURE
            )

        return decodeEvaluation(
            responseContent =
                repairedContent,
            validObjectiveIds =
                validObjectiveIds
        )
            ?: emptyResult()
    }

    private suspend fun requestCompletion(
        messages:
        List<QwenMessage>,
        temperature: Double
    ): String {
        val request =
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

        val response =
            qwenApi.createChatCompletion(
                request =
                    request
            )

        return response
            .choices
            .firstOrNull()
            ?.message
            ?.content
            ?.trim()
            .orEmpty()
            .takeIf(
                String::isNotBlank
            )
            ?: throw IllegalStateException(
                "Qwen returned an empty mission evaluation " +
                        "response."
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
            unfinishedObjectives
                .joinToString(
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

                    val safeContent =
                        turn
                            .content
                            .trim()
                            .take(
                                MAXIMUM_TURN_LENGTH
                            )

                    "$speaker: $safeContent"
                }
                .ifBlank {
                    "No earlier conversation is available."
                }

        val safeLearnerMessage =
            learnerMessage
                .trim()
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
                    You evaluate the current objective and the
                    latest learner interaction in an ongoing
                    DeutschCraft German conversation.

                    Return exactly one JSON object with this
                    structure:

                    {
                      "completedObjectives": [
                        {
                          "objectiveId": "exact objective ID",
                          "confidence": 0.0,
                          "reason": "Short reason"
                        }
                      ],
                      "interaction": {
                        "relevantReply": false,
                        "followUpQuestion": false,
                        "clarification": false,
                        "replyExpected": false,
                        "confidence": 0.0
                      }
                    }

                    If the objective is not complete, return:

                    {
                      "completedObjectives": [],
                      "interaction": {
                        "relevantReply": false,
                        "followUpQuestion": false,
                        "clarification": false,
                        "replyExpected": false,
                        "confidence": 0.0
                      }
                    }

                    OBJECTIVE EVALUATION RULES

                    - Inspect the recent conversation and the
                      latest learner message together.
                    - The conversation may have started before
                      the mission became active.
                    - An objective may have been completed by an
                      earlier learner turn shown in the recent
                      conversation.
                    - Do not require the learner to repeat an
                      action that already clearly occurred.
                    - Do not require the conversation to restart.
                    - Evaluate learner messages only.
                    - Emil's messages provide context but cannot
                      complete an objective.
                    - Use completionHint as the internal semantic
                      completion rule.
                    - Exact keyword matching is not required.
                    - Accept natural paraphrases with the same
                      communicative meaning.
                    - Minor German grammar mistakes are allowed
                      when the intended communication is clear.
                    - Topic relevance alone is insufficient.
                    - The learner must perform the required
                      communication action.
                    - Do not infer information the learner never
                      communicated.
                    - Return only an objective ID supplied in
                      this request.
                    - Return at most one completed objective.
                    - Objective confidence must be between 0 and
                      1.
                    - Use objective confidence of 0.78 or above
                      only when a language teacher would
                      reasonably agree that the objective was
                      completed.

                    INTERACTION EVALUATION RULES

                    Evaluate only the latest learner message for
                    the interaction object.

                    relevantReply:

                    - true when the latest learner message
                      appropriately responds to the preceding
                      assistant message or meaningfully
                      continues the current scenario.
                    - false when the learner message is
                      unrelated, nonsensical, empty, or avoids
                      the conversational context.
                    - A grammatically imperfect response may
                      still be relevant when its meaning is
                      understandable.

                    followUpQuestion:

                    - true only when the learner independently
                      asks a useful question that meaningfully
                      advances the current scenario.
                    - A question mark alone is not sufficient.
                    - Repeating the assistant's question is not
                      a useful follow-up question.
                    - A question directly supplied as an
                      example by the system cannot be detected
                      here, so judge only the conversational
                      usefulness of the learner's message.

                    clarification:

                    - true when the latest learner message
                      successfully clarifies, corrects, repairs,
                      disambiguates, or restates something to
                      resolve uncertainty.
                    - Ordinary elaboration is not automatically
                      clarification.
                    - Saying "I mean...", "No, I mean...",
                      correcting a misunderstanding, or
                      explaining an ambiguous statement can
                      count.

                    replyExpected:

                    - true when the preceding assistant turn
                      asked a question, requested information,
                      requested clarification, or otherwise
                      clearly expected a learner response.
                    - false when there was no preceding
                      assistant turn or when no meaningful reply
                      was expected.
                    - Do not set replyExpected to false merely
                      because the learner's answer was poor.

                    interaction confidence:

                    - Confidence applies to the complete
                      interaction classification.
                    - Use a value between 0 and 1.
                    - Use a low value when context is
                      insufficient or ambiguous.

                    OUTPUT RULES

                    - Return every boolean explicitly.
                    - Return both top-level properties.
                    - Do not include additional properties.
                    - Return valid JSON only.
                    - Do not use Markdown.
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

                    Determine:

                    1. Whether the current objective was
                       completed anywhere in the learner's
                       recent conversation turns.

                    2. The interaction and comprehension
                       signals for the latest learner message.
                    """.trimIndent()
            )
        )
    }

    private fun buildRepairMessages(
        malformedResponse: String,
        validObjectiveIds:
        List<String>
    ): List<QwenMessage> {
        val validIds =
            validObjectiveIds
                .joinToString(
                    separator =
                        ", "
                )
                .ifBlank {
                    "No valid objective IDs."
                }

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
                      ],
                      "interaction": {
                        "relevantReply": false,
                        "followUpQuestion": false,
                        "clarification": false,
                        "replyExpected": false,
                        "confidence": 0.0
                      }
                    }

                    Valid objective IDs:
                    $validIds

                    Requirements:

                    - Return JSON only.
                    - Return both completedObjectives and
                      interaction.
                    - Use no more than one completed objective.
                    - Use only one of the listed IDs.
                    - Use an empty completedObjectives array
                      when objective completion is unsupported.
                    - Return all five interaction fields.
                    - Every interaction flag must be a JSON
                      boolean.
                    - Both confidence values must be between
                      0 and 1.
                    - Do not add other properties.
                    - Do not use Markdown.
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
        responseContent: String,
        validObjectiveIds:
        Set<String>
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

        val interactionObject =
            root[
                INTERACTION_KEY
            ] as? JsonObject
                ?: return null

        val evaluations =
            completedArray
                .mapNotNull { element ->
                    decodeCompletedObjective(
                        element =
                            element,
                        validObjectiveIds =
                            validObjectiveIds
                    )
                }
                .distinctBy { evaluation ->
                    evaluation.objectiveId
                }
                .sortedByDescending { evaluation ->
                    evaluation.confidence
                }
                .take(
                    MAXIMUM_COMPLETED_OBJECTIVE_COUNT
                )

        /*
         * A non-empty array where no entry survives validation
         * is considered malformed rather than silently
         * becoming an empty result.
         */
        if (
            completedArray.isNotEmpty() &&
            evaluations.isEmpty()
        ) {
            return null
        }

        val interaction =
            decodeInteractionEvaluation(
                root =
                    interactionObject
            )
                ?: return null

        return MissionObjectiveEvaluationResult(
            completedObjectives =
                evaluations,
            interaction =
                interaction
        )
    }

    private fun decodeCompletedObjective(
        element: JsonElement,
        validObjectiveIds:
        Set<String>
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

        if (
            objectiveId !in
            validObjectiveIds
        ) {
            Log.w(
                GATEWAY_TAG,
                "Qwen returned an unknown objective ID: " +
                        objectiveId
            )

            return null
        }

        val confidence =
            root.floatValue(
                key =
                    CONFIDENCE_KEY
            )
                ?.takeIf { value ->
                    value.isFinite()
                }
                ?.coerceIn(
                    minimumValue =
                        MINIMUM_CONFIDENCE,
                    maximumValue =
                        MAXIMUM_CONFIDENCE
                )
                ?: return null

        val reason =
            root.stringValue(
                key =
                    REASON_KEY
            )
                .orEmpty()
                .take(
                    MAXIMUM_REASON_LENGTH
                )

        return EvaluatedMissionObjective(
            objectiveId =
                objectiveId,
            confidence =
                confidence,
            reason =
                reason
        )
    }

    private fun decodeInteractionEvaluation(
        root: JsonObject
    ): MissionInteractionEvaluation? {
        val relevantReply =
            root.booleanValue(
                key =
                    RELEVANT_REPLY_KEY
            )
                ?: return null

        val followUpQuestion =
            root.booleanValue(
                key =
                    FOLLOW_UP_QUESTION_KEY
            )
                ?: return null

        val clarification =
            root.booleanValue(
                key =
                    CLARIFICATION_KEY
            )
                ?: return null

        val replyExpected =
            root.booleanValue(
                key =
                    REPLY_EXPECTED_KEY
            )
                ?: return null

        val confidence =
            root.floatValue(
                key =
                    CONFIDENCE_KEY
            )
                ?.takeIf { value ->
                    value.isFinite()
                }
                ?.coerceIn(
                    minimumValue =
                        MINIMUM_CONFIDENCE,
                    maximumValue =
                        MAXIMUM_CONFIDENCE
                )
                ?: return null

        return MissionInteractionEvaluation(
            relevantReply =
                relevantReply,
            followUpQuestion =
                followUpQuestion,
            clarification =
                clarification,
            replyExpected =
                replyExpected,
            confidence =
                confidence
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
                "Qwen returned invalid mission evaluation " +
                        "JSON.",
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
            ?.takeIf(
                String::isNotBlank
            )
    }

    private fun JsonObject.floatValue(
        key: String
    ): Float? {
        return (
                this[key] as? JsonPrimitive
                )
            ?.floatOrNull
    }

    private fun JsonObject.booleanValue(
        key: String
    ): Boolean? {
        return (
                this[key] as? JsonPrimitive
                )
            ?.booleanOrNull
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
                emptyList(),
            interaction =
                MissionInteractionEvaluation.EMPTY
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

        const val INTERACTION_KEY =
            "interaction"

        const val OBJECTIVE_ID_KEY =
            "objectiveId"

        const val CONFIDENCE_KEY =
            "confidence"

        const val REASON_KEY =
            "reason"

        const val RELEVANT_REPLY_KEY =
            "relevantReply"

        const val FOLLOW_UP_QUESTION_KEY =
            "followUpQuestion"

        const val CLARIFICATION_KEY =
            "clarification"

        const val REPLY_EXPECTED_KEY =
            "replyExpected"

        const val MAXIMUM_RESPONSE_TOKENS =
            650

        const val MAXIMUM_MESSAGE_LENGTH =
            2_000

        const val MAXIMUM_REPAIR_INPUT_LENGTH =
            6_000

        const val MAXIMUM_REASON_LENGTH =
            500

        const val MAXIMUM_COMPLETED_OBJECTIVE_COUNT =
            1

        const val EVALUATION_TEMPERATURE =
            0.10

        const val REPAIR_TEMPERATURE =
            0.05

        const val MAXIMUM_RECENT_TURN_COUNT =
            12

        const val MAXIMUM_TURN_LENGTH =
            600

        const val MINIMUM_CONFIDENCE =
            0f

        const val MAXIMUM_CONFIDENCE =
            1f
    }
}