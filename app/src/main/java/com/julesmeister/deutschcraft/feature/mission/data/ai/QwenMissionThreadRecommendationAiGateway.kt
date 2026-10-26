package com.julesmeister.deutschcraft.feature.mission.data.ai

import android.util.Log
import com.julesmeister.deutschcraft.BuildConfig
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenApi
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenChatCompletionRequest
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenMessage
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenResponseFormat
import com.julesmeister.deutschcraft.feature.mission.domain.MissionDifficulty
import com.julesmeister.deutschcraft.feature.mission.domain.generated.GeneratedMissionDraft
import com.julesmeister.deutschcraft.feature.mission.domain.generated.GeneratedMissionObjectiveDraft
import com.julesmeister.deutschcraft.feature.mission.domain.generated.MissionOpportunity
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendation
import java.io.IOException
import java.util.UUID
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
class QwenMissionThreadRecommendationAiGateway
@Inject constructor(
    private val qwenApi: QwenApi,
    private val json: Json
) : MissionThreadRecommendationAiGateway {

    override suspend fun generateRecommendation(
        cefrLevel: CefrLevel
    ): MissionThreadRecommendation? {
        if (BuildConfig.QWEN_API_KEY.isBlank()) {
            Log.w(
                RECOMMENDATION_TAG,
                "Mission recommendation skipped because " +
                        "the Qwen API key is missing."
            )

            return null
        }

        return try {
            generateWithRepair(
                cefrLevel =
                    cefrLevel
            )
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (httpException: HttpException) {
            Log.e(
                RECOMMENDATION_TAG,
                "Mission recommendation HTTP request failed.",
                httpException
            )

            null
        } catch (ioException: IOException) {
            Log.e(
                RECOMMENDATION_TAG,
                "Mission recommendation could not reach Qwen.",
                ioException
            )

            null
        } catch (throwable: Throwable) {
            /*
             * Recommendation generation is supplementary.
             *
             * A failure must never prevent the thread list
             * from loading normally.
             */
            Log.e(
                RECOMMENDATION_TAG,
                "Mission recommendation generation failed.",
                throwable
            )

            null
        }
    }

    private suspend fun generateWithRepair(
        cefrLevel: CefrLevel
    ): MissionThreadRecommendation? {
        val initialContent =
            requestCompletion(
                messages =
                    buildGenerationMessages(
                        cefrLevel =
                            cefrLevel
                    ),
                temperature =
                    GENERATION_TEMPERATURE
            )

        val initialRecommendation =
            decodeAndValidate(
                responseContent =
                    initialContent,
                expectedCefrLevel =
                    cefrLevel
            )

        if (initialRecommendation != null) {
            return initialRecommendation
        }

        Log.w(
            RECOMMENDATION_TAG,
            "Qwen returned an invalid mission-thread " +
                    "recommendation. Retrying once."
        )

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

        return decodeAndValidate(
            responseContent =
                repairedContent,
            expectedCefrLevel =
                cefrLevel
        )
    }

    private suspend fun requestCompletion(
        messages: List<QwenMessage>,
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
            .takeIf { content ->
                content.isNotBlank()
            }
            ?: throw IllegalStateException(
                "Qwen returned an empty mission " +
                        "recommendation."
            )
    }

    private fun buildGenerationMessages(
        cefrLevel: CefrLevel
    ): List<QwenMessage> {
        return listOf(
            QwenMessage(
                role =
                    SYSTEM_ROLE,
                content =
                    createGenerationPrompt(
                        cefrLevel =
                            cefrLevel
                    )
            ),
            QwenMessage(
                role =
                    USER_ROLE,
                content =
                    """
                    Generate one fresh German conversation
                    scenario for the learner.

                    It should look like a conversation already
                    waiting to be opened in a messaging app.

                    Return JSON only.
                    """.trimIndent()
            )
        )
    }

    private fun createGenerationPrompt(
        cefrLevel: CefrLevel
    ): String {
        return """
            You generate one contextual mission conversation
            for DeutschCraft.

            DeutschCraft is a conversation-first German
            learning application.

            The learner's current CEFR level is
            ${cefrLevel.name}.

            Generate one practical real-world German
            conversation scenario suitable for that level.

            The recommendation will appear in a thread list
            and must look like an existing conversation.

            Return exactly one JSON object using this shape:

            {
              "confidence": 0.0,
              "reason": "Why this scenario is useful",
              "title": "Short conversation title",
              "openingMessage": "Emil's opening German message",
              "description": "Short learner-facing description",
              "scenario": "The role-play situation",
              "learnerRole": "The learner's role",
              "assistantRole": "Emil's temporary role",
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

            Requirements:

            - Return JSON only.
            - Generate exactly one scenario.
            - Match CEFR level ${cefrLevel.name}.
            - Confidence must be between 0 and 1.
            - Confidence should be at least 0.8 only when the
              generated scenario is complete and useful.
            - Use 3 to 5 objectives.
            - At least one objective must be required.
            - Every objective must be observable from the
              learner's messages.
            - Every objective must be achievable entirely
              through chat.
            - The openingMessage must be written in German.
            - The openingMessage must immediately begin the
              role-play.
            - The openingMessage must suit CEFR
              ${cefrLevel.name}.
            - The title must be concise.
            - Do not include words such as mission, challenge,
              exercise, quiz, or lesson in the title.
            - Do not create sensitive, dangerous, emergency,
              medical-crisis, or emotionally distressing
              scenarios.
            - Do not tell the learner that the conversation
              was generated by AI.
            - Do not expose completionHint values in the
              opening message.
            - Use only one emoji for icon.
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
                    You repair malformed DeutschCraft mission
                    recommendation JSON.

                    Return exactly one JSON object.

                    Required fields:

                    {
                      "confidence": 0.0,
                      "reason": "Short reason",
                      "title": "Short title",
                      "openingMessage": "German role-play opener",
                      "description": "Short description",
                      "scenario": "Scenario",
                      "learnerRole": "Learner role",
                      "assistantRole": "Emil role",
                      "difficulty": "${cefrLevel.name}",
                      "icon": "One emoji",
                      "objectives": [
                        {
                          "title": "Objective",
                          "description": "Description",
                          "completionHint": "Internal rule",
                          "required": true
                        }
                      ]
                    }

                    Requirements:

                    - Match CEFR ${cefrLevel.name}.
                    - Use 3 to 5 objectives.
                    - Opening message must be German.
                    - Return JSON only.
                    - Do not use Markdown.
                    """.trimIndent()
            ),
            QwenMessage(
                role =
                    USER_ROLE,
                content =
                    """
                    Repair this response:

                    ${
                        malformedResponse.take(
                            MAXIMUM_REPAIR_INPUT_LENGTH
                        )
                    }
                    """.trimIndent()
            )
        )
    }

    private fun decodeAndValidate(
        responseContent: String,
        expectedCefrLevel: CefrLevel
    ): MissionThreadRecommendation? {
        val root =
            decodeRootObject(
                responseContent =
                    responseContent
            )
                ?: return null

        val confidence =
            root.floatValue(
                key =
                    CONFIDENCE_KEY
            )
                ?.coerceIn(
                    minimumValue = 0f,
                    maximumValue = 1f
                )
                ?: return null

        if (
            confidence <
            MINIMUM_RECOMMENDATION_CONFIDENCE
        ) {
            return null
        }

        val reason =
            root.stringValue(
                key =
                    REASON_KEY
            )
                ?: return null

        val title =
            root.stringValue(
                key =
                    TITLE_KEY
            )
                ?: return null

        val openingMessage =
            root.stringValue(
                key =
                    OPENING_MESSAGE_KEY
            )
                ?: return null

        val description =
            root.stringValue(
                key =
                    DESCRIPTION_KEY
            )
                ?: return null

        val scenario =
            root.stringValue(
                key =
                    SCENARIO_KEY
            )
                ?: return null

        val learnerRole =
            root.stringValue(
                key =
                    LEARNER_ROLE_KEY
            )
                ?: return null

        val assistantRole =
            root.stringValue(
                key =
                    ASSISTANT_ROLE_KEY
            )
                ?: return null

        val difficulty =
            parseDifficulty(
                value =
                    root.stringValue(
                        key =
                            DIFFICULTY_KEY
                    )
                        ?: return null
            )
                ?: return null

        if (
            difficulty.name !=
            expectedCefrLevel.name
        ) {
            return null
        }

        val icon =
            root.stringValue(
                key =
                    ICON_KEY
            )
                ?.takeIf { value ->
                    value.isNotBlank()
                }
                ?: DEFAULT_MISSION_ICON

        val objectives =
            parseObjectives(
                element =
                    root[OBJECTIVES_KEY]
            )

        if (
            objectives.size !in
            MINIMUM_OBJECTIVE_COUNT..
            MAXIMUM_OBJECTIVE_COUNT
        ) {
            return null
        }

        if (
            objectives.none { objective ->
                objective.isRequired
            }
        ) {
            return null
        }

        if (
            title.containsForbiddenTitleLanguage()
        ) {
            return null
        }

        val opportunity =
            MissionOpportunity(
                confidence =
                    confidence,
                reason =
                    reason,
                mission =
                    GeneratedMissionDraft(
                        title =
                            title,
                        description =
                            description,
                        scenario =
                            scenario,
                        learnerRole =
                            learnerRole,
                        assistantRole =
                            assistantRole,
                        difficulty =
                            difficulty,
                        icon =
                            icon,
                        objectives =
                            objectives
                    )
            )

        return MissionThreadRecommendation(
            id =
                UUID.randomUUID().toString(),
            title =
                title,
            previewText =
                openingMessage,
            opportunity =
                opportunity
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
            serializationException:
            SerializationException
        ) {
            Log.w(
                RECOMMENDATION_TAG,
                "Qwen returned invalid mission " +
                        "recommendation JSON.",
                serializationException
            )

            null
        }
    }

    private fun parseObjectives(
        element: JsonElement?
    ): List<GeneratedMissionObjectiveDraft> {
        val objectiveArray =
            element as? JsonArray
                ?: return emptyList()

        return objectiveArray
            .mapNotNull { item ->
                val objectiveObject =
                    item as? JsonObject
                        ?: return@mapNotNull null

                val title =
                    objectiveObject.stringValue(
                        key =
                            TITLE_KEY
                    )
                        ?: return@mapNotNull null

                val description =
                    objectiveObject.stringValue(
                        key =
                            DESCRIPTION_KEY
                    )
                        ?: return@mapNotNull null

                val completionHint =
                    objectiveObject.stringValue(
                        key =
                            COMPLETION_HINT_KEY
                    )
                        ?: return@mapNotNull null

                val isRequired =
                    objectiveObject.booleanValue(
                        key =
                            REQUIRED_KEY
                    )
                        ?: true

                GeneratedMissionObjectiveDraft(
                    title =
                        title,
                    description =
                        description,
                    completionHint =
                        completionHint,
                    isRequired =
                        isRequired
                )
            }
            .distinctBy { objective ->
                objective.title.lowercase()
            }
            .take(
                MAXIMUM_OBJECTIVE_COUNT
            )
    }

    private fun parseDifficulty(
        value: String
    ): MissionDifficulty? {
        return runCatching {
            MissionDifficulty.valueOf(
                value
                    .trim()
                    .uppercase()
            )
        }.getOrNull()
    }

    private fun JsonObject.stringValue(
        key: String
    ): String? {
        return (
                this[key] as? JsonPrimitive
                )
            ?.contentOrNull
            ?.trim()
            ?.replace(
                MULTIPLE_WHITESPACE_REGEX,
                " "
            )
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

    private fun JsonObject.booleanValue(
        key: String
    ): Boolean? {
        return (
                this[key] as? JsonPrimitive
                )?.booleanOrNull
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

    private fun String
            .containsForbiddenTitleLanguage(): Boolean {
        val normalizedTitle =
            lowercase()

        return FORBIDDEN_TITLE_WORDS.any { word ->
            word in normalizedTitle
        }
    }

    private companion object {
        const val RECOMMENDATION_TAG =
            "MissionThreadAi"

        const val SYSTEM_ROLE =
            "system"

        const val USER_ROLE =
            "user"

        const val JSON_OBJECT_TYPE =
            "json_object"

        const val CONFIDENCE_KEY =
            "confidence"

        const val REASON_KEY =
            "reason"

        const val TITLE_KEY =
            "title"

        const val OPENING_MESSAGE_KEY =
            "openingMessage"

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

        const val MAXIMUM_RESPONSE_TOKENS =
            1_000

        const val MAXIMUM_REPAIR_INPUT_LENGTH =
            6_000

        const val MINIMUM_OBJECTIVE_COUNT =
            3

        const val MAXIMUM_OBJECTIVE_COUNT =
            5

        const val MINIMUM_RECOMMENDATION_CONFIDENCE =
            0.80f

        const val GENERATION_TEMPERATURE =
            0.72

        const val REPAIR_TEMPERATURE =
            0.12

        const val DEFAULT_MISSION_ICON =
            "🎯"

        val MULTIPLE_WHITESPACE_REGEX =
            Regex("\\s+")

        val FORBIDDEN_TITLE_WORDS =
            setOf(
                "mission",
                "challenge",
                "exercise",
                "quiz",
                "lesson",
                "aufgabe",
                "übung",
                "lektion"
            )
    }
}