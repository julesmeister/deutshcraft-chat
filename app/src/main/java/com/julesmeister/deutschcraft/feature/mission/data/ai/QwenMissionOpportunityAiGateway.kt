package com.julesmeister.deutschcraft.feature.mission.data.ai

import com.julesmeister.deutschcraft.BuildConfig
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.chat.data.ai.AiGatewayException
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenApi
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenChatCompletionRequest
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenMessage
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenResponseFormat
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
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

@Singleton
class QwenMissionOpportunityAiGateway @Inject constructor(
    private val qwenApi: QwenApi,
    private val json: Json
) : MissionOpportunityAiGateway {

    override suspend fun detectMissionOpportunity(
        conversationHistory: List<ChatMessage>,
        cefrLevel: CefrLevel
    ): AiMissionOpportunity {
        if (BuildConfig.QWEN_API_KEY.isBlank()) {
            return noOpportunity(
                reason =
                    "Mission detection is unavailable."
            )
        }

        return try {
            requestAndDecode(
                conversationHistory =
                    conversationHistory,
                cefrLevel =
                    cefrLevel
            )
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (httpException: HttpException) {
            noOpportunity(
                reason =
                    "Mission detection request failed."
            )
        } catch (ioException: IOException) {
            noOpportunity(
                reason =
                    "Mission detection is offline."
            )
        } catch (throwable: Throwable) {
            noOpportunity(
                reason =
                    "No suitable mission was detected."
            )
        }
    }

    private suspend fun requestAndDecode(
        conversationHistory: List<ChatMessage>,
        cefrLevel: CefrLevel
    ): AiMissionOpportunity {
        val initialContent =
            requestCompletion(
                messages =
                    buildDetectionMessages(
                        conversationHistory =
                            conversationHistory,
                        cefrLevel =
                            cefrLevel
                    ),
                temperature =
                    DETECTION_TEMPERATURE
            )

        val initialResult =
            decodeOpportunity(
                content =
                    initialContent
            )

        if (initialResult != null) {
            return initialResult
        }

        val repairedContent =
            requestCompletion(
                messages =
                    buildRepairMessages(
                        malformedResponse =
                            initialContent,
                        cefrLevel =
                            cefrLevel
                    ),
                temperature =
                    REPAIR_TEMPERATURE
            )

        return decodeOpportunity(
            content =
                repairedContent
        ) ?: noOpportunity(
            reason =
                "No valid mission opportunity was produced."
        )
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
    }

    private fun buildDetectionMessages(
        conversationHistory: List<ChatMessage>,
        cefrLevel: CefrLevel
    ): List<QwenMessage> {
        val historyText =
            conversationHistory
                .takeLast(
                    MAXIMUM_HISTORY_MESSAGES
                )
                .filter { message ->
                    message.content.isNotBlank()
                }
                .joinToString(
                    separator = "\n"
                ) { message ->
                    val speaker =
                        when (message.sender) {
                            MessageSender.USER ->
                                "Learner"

                            MessageSender.ASSISTANT ->
                                "Emil"
                        }

                    "$speaker: ${message.content.trim()}"
                }

        return listOf(
            QwenMessage(
                role =
                    SYSTEM_ROLE,
                content =
                    createDetectionSystemPrompt(
                        cefrLevel =
                            cefrLevel
                    )
            ),
            QwenMessage(
                role =
                    USER_ROLE,
                content =
                    """
                    Review this recent conversation:

                    $historyText

                    Decide whether a practical German
                    conversation mission naturally follows
                    from the discussion.

                    Return JSON only.
                    """.trimIndent()
            )
        )
    }

    private fun createDetectionSystemPrompt(
        cefrLevel: CefrLevel
    ): String {
        return """
            You are DeutschCraft's mission opportunity
            detector.

            The learner's current CEFR level is
            ${cefrLevel.name}.

            Detect whether the conversation naturally
            suggests a practical German communication
            scenario.

            Offer a mission only when:
            - The scenario follows naturally from the topic.
            - It provides useful real-world communication.
            - It is appropriate for CEFR ${cefrLevel.name}.
            - It can be completed entirely through chat.
            - The conversation is not emotionally sensitive.
            - The situation is not an emergency.
            - The mission does not feel forced.

            If no mission is appropriate, return:

            {
              "shouldOfferMission": false,
              "confidence": 0.0,
              "reason": "Short explanation",
              "mission": null
            }

            If a mission is appropriate, return:

            {
              "shouldOfferMission": true,
              "confidence": 0.0,
              "reason": "Why this mission fits",
              "mission": {
                "title": "Concise mission title",
                "description": "Short learner-facing description",
                "scenario": "Role-play scenario",
                "learnerRole": "Learner role",
                "assistantRole": "Emil role",
                "difficulty": "${cefrLevel.name}",
                "icon": "One suitable emoji",
                "objectives": [
                  {
                    "title": "Observable objective",
                    "description": "Short description",
                    "completionHint": "Internal completion rule",
                    "required": true
                  }
                ]
              }
            }

            Requirements:
            - Return JSON only.
            - Aim for 10 distinct objectives; use 8 to 10.
            - Arrange objectives as a natural progression
              from opening the conversation to closing it.
            - Do not pad the list with repeated goals.
            - All objectives must be observable in learner
              messages.
            - All objectives must be achievable through chat.
            - Keep the mission at CEFR ${cefrLevel.name}.
            - Do not create quiz or test missions.
            - Do not reveal internal completion hints to the
              learner.
        """.trimIndent()
    }

    private fun buildRepairMessages(
        malformedResponse: String,
        cefrLevel: CefrLevel
    ): List<QwenMessage> {
        return listOf(
            QwenMessage(
                role =
                    SYSTEM_ROLE,
                content =
                    """
                    Repair the malformed mission JSON.

                    Preserve only valid mission information.

                    The mission difficulty must be
                    ${cefrLevel.name}.

                    If shouldOfferMission is true, provide
                    8 to 10 distinct, observable chat goals,
                    aiming for 10 in a natural progression.
                    Complete missing goals without repeating
                    existing ones. Every goal needs a title,
                    description, completionHint, and required.

                    Return exactly one JSON object and no
                    Markdown.
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

    private fun decodeOpportunity(
        content: String
    ): AiMissionOpportunity? {
        val cleanedContent =
            removeMarkdownCodeFence(
                content =
                    content
            )

        val root =
            try {
                json.parseToJsonElement(
                    string =
                        cleanedContent
                ) as? JsonObject
            } catch (
                exception:
                SerializationException
            ) {
                null
            }
                ?: return null

        val shouldOffer =
            root.booleanValue(
                key =
                    SHOULD_OFFER_KEY
            )
                ?: return null

        val confidence =
            root.floatValue(
                key =
                    CONFIDENCE_KEY
            )
                ?: 0f

        val reason =
            root.stringValue(
                key =
                    REASON_KEY
            ).orEmpty()

        if (!shouldOffer) {
            return AiMissionOpportunity(
                shouldOfferMission =
                    false,
                confidence =
                    confidence,
                reason =
                    reason,
                mission =
                    null
            )
        }

        val missionObject =
            root[MISSION_KEY] as? JsonObject
                ?: return null

        val objectives =
            readObjectives(
                element =
                    missionObject[
                        OBJECTIVES_KEY
                    ]
            )
                .filter { objective ->
                    objective.title.isNotBlank() &&
                        objective.description.isNotBlank() &&
                        objective.completionHint.isNotBlank()
                }
                .distinctBy { objective -> objective.title.lowercase() }

        if (objectives.size !in MINIMUM_OBJECTIVE_COUNT..MAXIMUM_OBJECTIVE_COUNT ||
            objectives.none { it.required }
        ) {
            return null
        }

        return AiMissionOpportunity(
            shouldOfferMission =
                true,
            confidence =
                confidence,
            reason =
                reason,
            mission =
                AiGeneratedMission(
                    title =
                        missionObject
                            .stringValue(
                                TITLE_KEY
                            )
                            .orEmpty(),
                    description =
                        missionObject
                            .stringValue(
                                DESCRIPTION_KEY
                            )
                            .orEmpty(),
                    scenario =
                        missionObject
                            .stringValue(
                                SCENARIO_KEY
                            )
                            .orEmpty(),
                    learnerRole =
                        missionObject
                            .stringValue(
                                LEARNER_ROLE_KEY
                            )
                            .orEmpty(),
                    assistantRole =
                        missionObject
                            .stringValue(
                                ASSISTANT_ROLE_KEY
                            )
                            .orEmpty(),
                    difficulty =
                        missionObject
                            .stringValue(
                                DIFFICULTY_KEY
                            )
                            .orEmpty(),
                    icon =
                        missionObject
                            .stringValue(
                                ICON_KEY
                            )
                            .orEmpty(),
                    objectives =
                        objectives
                )
        )
    }

    private fun readObjectives(
        element: JsonElement?
    ): List<AiGeneratedMissionObjective> {
        val array =
            element as? JsonArray
                ?: return emptyList()

        return array
            .mapNotNull { item ->
                val objective =
                    item as? JsonObject
                        ?: return@mapNotNull null

                AiGeneratedMissionObjective(
                    title =
                        objective
                            .stringValue(
                                TITLE_KEY
                            )
                            .orEmpty(),
                    description =
                        objective
                            .stringValue(
                                DESCRIPTION_KEY
                            )
                            .orEmpty(),
                    completionHint =
                        objective
                            .stringValue(
                                COMPLETION_HINT_KEY
                            )
                            .orEmpty(),
                    required =
                        objective.booleanValue(
                            REQUIRED_KEY
                        ) ?: true
                )
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
            ?.takeIf(String::isNotBlank)
    }

    private fun JsonObject.booleanValue(
        key: String
    ): Boolean? {
        return (
                this[key] as? JsonPrimitive
                )?.booleanOrNull
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
            .removePrefix("```json")
            .removePrefix("```JSON")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }

    private fun noOpportunity(
        reason: String
    ): AiMissionOpportunity {
        return AiMissionOpportunity(
            shouldOfferMission =
                false,
            confidence =
                0f,
            reason =
                reason,
            mission =
                null
        )
    }

    private companion object {
        const val SYSTEM_ROLE =
            "system"

        const val USER_ROLE =
            "user"

        const val JSON_OBJECT_TYPE =
            "json_object"

        const val SHOULD_OFFER_KEY =
            "shouldOfferMission"

        const val CONFIDENCE_KEY =
            "confidence"

        const val REASON_KEY =
            "reason"

        const val MISSION_KEY =
            "mission"

        const val TITLE_KEY =
            "title"

        const val DESCRIPTION_KEY =
            "description"

        const val SCENARIO_KEY =
            "scenario"

        const val LEARNER_ROLE_KEY =
            "learnerRole"

        const val ASSISTANT_ROLE_KEY =
            "assistantRole"

        const val DIFFICULTY_KEY =
            "difficulty"

        const val ICON_KEY =
            "icon"

        const val OBJECTIVES_KEY =
            "objectives"

        const val COMPLETION_HINT_KEY =
            "completionHint"

        const val REQUIRED_KEY =
            "required"

        const val MAXIMUM_HISTORY_MESSAGES =
            12

        const val MAXIMUM_RESPONSE_TOKENS =
            2_000

        const val MAXIMUM_REPAIR_INPUT_LENGTH =
            12_000

        const val MINIMUM_OBJECTIVE_COUNT =
            8

        const val MAXIMUM_OBJECTIVE_COUNT =
            10

        const val DETECTION_TEMPERATURE =
            0.35

        const val REPAIR_TEMPERATURE =
            0.10
    }
}