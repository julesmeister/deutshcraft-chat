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
        } catch (
            httpException:
            HttpException
        ) {
            Log.e(
                RECOMMENDATION_TAG,
                "Mission recommendation HTTP request failed.",
                httpException
            )

            null
        } catch (
            ioException:
            IOException
        ) {
            Log.e(
                RECOMMENDATION_TAG,
                "Mission recommendation could not reach Qwen.",
                ioException
            )

            null
        } catch (
            throwable:
            Throwable
        ) {
            /*
             * Recommendation generation is supplementary.
             *
             * An AI, network, or parsing failure must never
             * prevent the normal thread list from loading.
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

                    Every objective must include its complete
                    learner-support content:

                    - Three progressive coaching hints
                    - Two to four German vocabulary items
                    - Two German example phrases
                    - One internal semantic completion rule

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

            The recommendation appears in a thread list and
            must look like an existing conversation that the
            learner can open.

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
                  "description": "Short learner-facing description",
                  "completionHint": "Internal semantic completion rule",
                  "coachingHints": [
                    "Gentle learner-facing hint",
                    "More specific contextual hint",
                    "Direct final hint"
                  ],
                  "targetVocabulary": [
                    "German target word",
                    "German target word"
                  ],
                  "examplePhrases": [
                    "CEFR-appropriate German example sentence.",
                    "Another CEFR-appropriate German example sentence."
                  ],
                  "required": true
                }
              ]
            }

            General requirements:

            - Return JSON only.
            - Do not wrap the response in Markdown.
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
            - Arrange objectives in a natural conversation
              order.
            - Objectives should not duplicate one another.
            - Each objective should represent one meaningful
              communication action.
            - The openingMessage must be written in German.
            - The openingMessage must immediately begin the
              role-play.
            - The openingMessage must suit CEFR
              ${cefrLevel.name}.
            - The title must be concise.
            - Do not include mission, challenge, exercise,
              quiz, lesson, Aufgabe, Übung, or Lektion in the
              title.
            - Do not create sensitive, dangerous, emergency,
              medical-crisis, or emotionally distressing
              scenarios.
            - Do not tell the learner the conversation was
              generated by AI.
            - Do not expose completionHint in learner-facing
              content.
            - Use only one emoji for icon.

            Objective requirements:

            - Every objective must contain exactly three
              coachingHints.
            - coachingHints are learner-facing.
            - Write coachingHints in English unless the hint
              itself demonstrates German wording.
            - Arrange coachingHints from gentle to
              increasingly specific.
            - The first hint should gently describe the next
              conversational direction.
            - The second hint should identify the information
              or communication action still needed.
            - The third hint may provide a direct German
              wording suggestion.
            - Coaching hints must adhere to the exact mission,
              scenario, and objective.
            - Do not merely repeat the objective title as a
              coaching hint.
            - Do not generate a greeting hint unless greeting
              is the objective currently being described.
            - Do not tell the learner to restart the
              conversation.
            - Hints must remain useful even after the role-play
              has already begun.
            - Each objective must contain 2 to 4
              targetVocabulary entries.
            - targetVocabulary entries must be useful German
              words or short expressions.
            - targetVocabulary must match CEFR
              ${cefrLevel.name}.
            - Every objective must contain exactly two
              examplePhrases.
            - examplePhrases must be written in German.
            - examplePhrases must match CEFR
              ${cefrLevel.name}.
            - Example phrases must demonstrate how the learner
              could satisfy that exact objective.
            - Example phrases should not contain placeholders
              such as NAME, COMPANY, or LOCATION.
            - completionHint is internal.
            - completionHint must be written as a semantic
              evaluation rule.
            - completionHint must clearly explain what the
              learner must communicate before the objective
              should be marked complete.
            - completionHint must not require an exact keyword
              when a correct paraphrase would communicate the
              same meaning.
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
                    createRepairPrompt(
                        cefrLevel =
                            cefrLevel
                    )
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

    private fun createRepairPrompt(
        cefrLevel: CefrLevel
    ): String {
        return """
            You repair malformed DeutschCraft mission
            recommendation JSON.

            Return exactly one JSON object using this shape:

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
                  "description": "Learner-facing description",
                  "completionHint": "Internal semantic rule",
                  "coachingHints": [
                    "Gentle contextual hint",
                    "More specific contextual hint",
                    "Direct final hint"
                  ],
                  "targetVocabulary": [
                    "German word",
                    "German word"
                  ],
                  "examplePhrases": [
                    "German example phrase.",
                    "Another German example phrase."
                  ],
                  "required": true
                }
              ]
            }

            Requirements:

            - Match CEFR ${cefrLevel.name}.
            - Return JSON only.
            - Do not use Markdown.
            - Preserve or create one complete scenario.
            - Use 3 to 5 objectives.
            - At least one objective must be required.
            - Every objective must be achievable through the
              learner's chat messages.
            - Every objective must include exactly three
              non-empty coachingHints.
            - Arrange coachingHints from gentle to specific.
            - Coaching hints must be relevant to the exact
              objective and scenario.
            - Do not use a greeting hint unless greeting is
              the objective.
            - Every objective must include 2 to 4 non-empty
              German targetVocabulary entries.
            - Every objective must include exactly two
              non-empty German examplePhrases.
            - The example phrases must demonstrate the exact
              objective.
            - The opening message must be written in German.
            - The opening message must begin the role-play.
            - completionHint is an internal semantic rule.
            - Do not expose completionHint in coachingHints,
              examplePhrases, or the opening message.
        """.trimIndent()
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
                    minimumValue =
                        0f,
                    maximumValue =
                        1f
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
        } catch (
            illegalArgumentException:
            IllegalArgumentException
        ) {
            Log.w(
                RECOMMENDATION_TAG,
                "Qwen returned an unsupported mission " +
                        "recommendation payload.",
                illegalArgumentException
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
                parseObjective(
                    element =
                        item
                )
            }
            .distinctBy { objective ->
                objective
                    .title
                    .trim()
                    .lowercase()
            }
            .take(
                MAXIMUM_OBJECTIVE_COUNT
            )
    }

    private fun parseObjective(
        element: JsonElement
    ): GeneratedMissionObjectiveDraft? {
        val objectiveObject =
            element as? JsonObject
                ?: return null

        val title =
            objectiveObject.stringValue(
                key =
                    TITLE_KEY
            )
                ?: return null

        val description =
            objectiveObject.stringValue(
                key =
                    DESCRIPTION_KEY
            )
                ?: return null

        val completionHint =
            objectiveObject.stringValue(
                key =
                    COMPLETION_HINT_KEY
            )
                ?: return null

        val coachingHints =
            objectiveObject.stringListValue(
                key =
                    COACHING_HINTS_KEY
            )

        val targetVocabulary =
            objectiveObject.stringListValue(
                key =
                    TARGET_VOCABULARY_KEY
            )

        val examplePhrases =
            objectiveObject.stringListValue(
                key =
                    EXAMPLE_PHRASES_KEY
            )

        if (
            coachingHints.size !=
            REQUIRED_COACHING_HINT_COUNT
        ) {
            return null
        }

        if (
            targetVocabulary.size !in
            MINIMUM_TARGET_VOCABULARY_COUNT..
            MAXIMUM_TARGET_VOCABULARY_COUNT
        ) {
            return null
        }

        if (
            examplePhrases.size !=
            REQUIRED_EXAMPLE_PHRASE_COUNT
        ) {
            return null
        }

        val isRequired =
            objectiveObject.booleanValue(
                key =
                    REQUIRED_KEY
            )
                ?: true

        return GeneratedMissionObjectiveDraft(
            title =
                title,
            description =
                description,
            completionHint =
                completionHint,
            coachingHints =
                coachingHints,
            targetVocabulary =
                targetVocabulary,
            examplePhrases =
                examplePhrases,
            isRequired =
                isRequired
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
            ?.sanitizeGeneratedText()
    }

    private fun JsonObject.stringListValue(
        key: String
    ): List<String> {
        val array =
            this[key] as? JsonArray
                ?: return emptyList()

        return array
            .mapNotNull { element ->
                (
                        element as? JsonPrimitive
                        )
                    ?.contentOrNull
                    ?.sanitizeGeneratedText()
            }
            .distinct()
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

    private fun String.sanitizeGeneratedText():
            String? {
        return trim()
            .replace(
                MULTIPLE_WHITESPACE_REGEX,
                " "
            )
            .takeIf { value ->
                value.isNotBlank()
            }
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

        const val COACHING_HINTS_KEY =
            "coachingHints"

        const val TARGET_VOCABULARY_KEY =
            "targetVocabulary"

        const val EXAMPLE_PHRASES_KEY =
            "examplePhrases"

        const val REQUIRED_KEY =
            "required"

        /*
         * The richer objective payload requires more output
         * tokens than the original objective-only response.
         */
        const val MAXIMUM_RESPONSE_TOKENS =
            2_000

        const val MAXIMUM_REPAIR_INPUT_LENGTH =
            10_000

        const val MINIMUM_OBJECTIVE_COUNT =
            3

        const val MAXIMUM_OBJECTIVE_COUNT =
            5

        const val REQUIRED_COACHING_HINT_COUNT =
            3

        const val MINIMUM_TARGET_VOCABULARY_COUNT =
            2

        const val MAXIMUM_TARGET_VOCABULARY_COUNT =
            4

        const val REQUIRED_EXAMPLE_PHRASE_COUNT =
            2

        const val MINIMUM_RECOMMENDATION_CONFIDENCE =
            0.80f

        const val GENERATION_TEMPERATURE =
            0.72

        const val REPAIR_TEMPERATURE =
            0.12

        const val DEFAULT_MISSION_ICON =
            "🎯"

        val MULTIPLE_WHITESPACE_REGEX =
            Regex(
                "\\s+"
            )

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