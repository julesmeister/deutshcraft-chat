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
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionScenarioDefinition
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
        cefrLevel: CefrLevel,
        selectedScenario:
        MissionScenarioDefinition
    ): MissionThreadRecommendation? {
        if (
            BuildConfig.QWEN_BASE_URL.isBlank()
        ) {
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
                    cefrLevel,
                selectedScenario =
                    selectedScenario
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
        } catch (throwable: Throwable) {
            Log.e(
                RECOMMENDATION_TAG,
                "Mission recommendation generation failed.",
                throwable
            )

            null
        }
    }

    private suspend fun generateWithRepair(
        cefrLevel: CefrLevel,
        selectedScenario:
        MissionScenarioDefinition
    ): MissionThreadRecommendation? {
        val initialContent =
            requestCompletion(
                messages =
                    buildGenerationMessages(
                        cefrLevel =
                            cefrLevel,
                        selectedScenario =
                            selectedScenario
                    ),
                temperature =
                    GENERATION_TEMPERATURE
            )

        val initialRecommendation =
            decodeAndValidate(
                responseContent =
                    initialContent,
                expectedCefrLevel =
                    cefrLevel,
                selectedScenario =
                    selectedScenario
            )

        if (initialRecommendation != null) {
            return initialRecommendation
        }

        Log.w(
            RECOMMENDATION_TAG,
            "Qwen returned an invalid locked-scenario " +
                    "recommendation. Retrying once."
        )

        val repairedContent =
            requestCompletion(
                messages =
                    buildRepairMessages(
                        malformedResponse =
                            initialContent,
                        cefrLevel =
                            cefrLevel,
                        selectedScenario =
                            selectedScenario
                    ),
                temperature =
                    REPAIR_TEMPERATURE
            )

        return decodeAndValidate(
            responseContent =
                repairedContent,
            expectedCefrLevel =
                cefrLevel,
            selectedScenario =
                selectedScenario
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
            .takeIf(
                String::isNotBlank
            )
            ?: throw IllegalStateException(
                "Qwen returned an empty mission " +
                        "recommendation."
            )
    }

    private fun buildGenerationMessages(
        cefrLevel: CefrLevel,
        selectedScenario:
        MissionScenarioDefinition
    ): List<QwenMessage> {
        return listOf(
            QwenMessage(
                role =
                    SYSTEM_ROLE,
                content =
                    createGenerationPrompt(
                        cefrLevel =
                            cefrLevel,
                        selectedScenario =
                            selectedScenario
                    )
            ),
            QwenMessage(
                role =
                    USER_ROLE,
                content =
                    """
                    Expand the locked scenario into one complete
                    German role-play conversation.

                    Do not select another scenario.

                    Generate exactly ten progressive objectives.

                    Return JSON only.
                    """.trimIndent()
            )
        )
    }

    private fun createGenerationPrompt(
        cefrLevel: CefrLevel,
        selectedScenario:
        MissionScenarioDefinition
    ): String {
        val skillText =
            selectedScenario
                .targetSkills
                .joinToString(
                    separator =
                        ", "
                )

        val objectivesText =
            selectedScenario
                .objectiveBlueprints
                .mapIndexed { index, objective ->
                    "${index + 1}. $objective"
                }
                .joinToString(
                    separator =
                        "\n"
                )

        return """
            You generate one conversation for DeutschCraft.

            IMPORTANT LOCKED-SCENARIO RULE

            The application already selected the scenario.

            You must not choose, replace, reinterpret, or migrate
            to another scenario family.

            Locked scenario type:
            ${selectedScenario.type.name}

            Required title direction:
            ${selectedScenario.title}

            Required scenario:
            ${selectedScenario.description}

            Learner role:
            ${selectedScenario.learnerRole}

            Emil's temporary role:
            ${selectedScenario.assistantRole}

            CEFR level:
            ${cefrLevel.name}

            Target communication skills:
            $skillText

            Required objective progression:
            $objectivesText

            Return exactly:

            {
              "confidence": 0.0,
              "reason": "Why this scenario is useful",
              "title": "Short conversation title",
              "openingMessage": "German role-play opener",
              "description": "Short learner-facing description",
              "scenario": "Detailed role-play situation",
              "learnerRole": "${selectedScenario.learnerRole}",
              "assistantRole": "${selectedScenario.assistantRole}",
              "difficulty": "${cefrLevel.name}",
              "icon": "One emoji",
              "objectives": [
                {
                  "title": "Observable objective",
                  "description": "Learner-facing description",
                  "completionHint": "Internal semantic rule",
                  "coachingHints": [
                    "Gentle contextual hint",
                    "More specific hint",
                    "Direct final hint"
                  ],
                  "targetVocabulary": [
                    "German expression",
                    "German expression"
                  ],
                  "examplePhrases": [
                    "German learner phrase.",
                    "Another German learner phrase."
                  ],
                  "required": true
                }
              ]
            }

            Requirements:

            - Return JSON only.
            - Do not use Markdown.
            - Generate exactly ten objectives.
            - Preserve the supplied objective progression.
            - Every objective must contain exactly three hints.
            - Every objective must contain 2 to 4 German
              vocabulary entries.
            - Every objective must contain exactly two German
              example phrases.
            - Match CEFR ${cefrLevel.name}.
            - The opening message must begin the role-play.
            - Emil must speak as:
              ${selectedScenario.assistantRole}
            - The learner must speak as:
              ${selectedScenario.learnerRole}
            - Do not generate a café, bakery, restaurant,
              shopping, or ordering scenario unless the locked
              scenario explicitly requires it.
            - Do not use exact-keyword completion rules when a
              valid paraphrase communicates the same intent.
            - Confidence must be between 0 and 1.
        """.trimIndent()
    }

    private fun buildRepairMessages(
        malformedResponse: String,
        cefrLevel: CefrLevel,
        selectedScenario:
        MissionScenarioDefinition
    ): List<QwenMessage> {
        return listOf(
            QwenMessage(
                role =
                    SYSTEM_ROLE,
                content =
                    createGenerationPrompt(
                        cefrLevel =
                            cefrLevel,
                        selectedScenario =
                            selectedScenario
                    ) +
                            """

                        Repair the malformed response while
                        preserving the locked scenario.

                        Return JSON only.
                        """.trimIndent()
            ),
            QwenMessage(
                role =
                    USER_ROLE,
                content =
                    """
                    Repair:

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
        expectedCefrLevel: CefrLevel,
        selectedScenario:
        MissionScenarioDefinition
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

                        /*
                         * Roles come from the locked Kotlin
                         * scenario, not from generated JSON.
                         */
                        learnerRole =
                            selectedScenario.learnerRole,
                        assistantRole =
                            selectedScenario.assistantRole,
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
            scenarioType =
                selectedScenario.type,
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
        return try {
            json.parseToJsonElement(
                string =
                    removeMarkdownCodeFence(
                        content =
                            responseContent
                    )
            ) as? JsonObject
        } catch (
            serializationException:
            SerializationException
        ) {
            Log.w(
                RECOMMENDATION_TAG,
                "Qwen returned invalid recommendation JSON.",
                serializationException
            )

            null
        } catch (
            illegalArgumentException:
            IllegalArgumentException
        ) {
            null
        }
    }

    private fun parseObjectives(
        element: JsonElement?
    ): List<GeneratedMissionObjectiveDraft> {
        val array =
            element as? JsonArray
                ?: return emptyList()

        return array
            .mapNotNull(
                ::parseObjective
            )
            .distinctBy { objective ->
                objective.title
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
        val value =
            element as? JsonObject
                ?: return null

        val title =
            value.stringValue(
                TITLE_KEY
            )
                ?: return null

        val description =
            value.stringValue(
                DESCRIPTION_KEY
            )
                ?: return null

        val completionHint =
            value.stringValue(
                COMPLETION_HINT_KEY
            )
                ?: return null

        val coachingHints =
            value.stringListValue(
                COACHING_HINTS_KEY
            )

        val targetVocabulary =
            value.stringListValue(
                TARGET_VOCABULARY_KEY
            )

        val examplePhrases =
            value.stringListValue(
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
                value.booleanValue(
                    REQUIRED_KEY
                )
                    ?: true
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
            .takeIf(
                String::isNotBlank
            )
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
            .containsForbiddenTitleLanguage():
            Boolean {
        val normalized =
            lowercase()

        return FORBIDDEN_TITLE_WORDS.any { word ->
            word in normalized
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

        const val MAXIMUM_RESPONSE_TOKENS =
            4_000

        const val MAXIMUM_REPAIR_INPUT_LENGTH =
            14_000

        const val MINIMUM_OBJECTIVE_COUNT =
            8

        const val MAXIMUM_OBJECTIVE_COUNT =
            10

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
            0.48

        const val REPAIR_TEMPERATURE =
            0.10

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