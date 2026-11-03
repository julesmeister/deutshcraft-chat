// app/src/main/java/com/julesmeister/deutschcraft/feature/mission/data/ai/MissionVariationAiGenerator.kt

package com.julesmeister.deutschcraft.feature.mission.data.ai

import com.julesmeister.deutschcraft.feature.chat.data.ai.DeutschCraftAiGateway
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import com.julesmeister.deutschcraft.feature.mission.domain.MissionDifficulty
import com.julesmeister.deutschcraft.feature.mission.domain.MissionTemplate
import com.julesmeister.deutschcraft.feature.mission.domain.generated.GeneratedMissionDraft
import com.julesmeister.deutschcraft.feature.mission.domain.generated.GeneratedMissionObjectiveDraft
import com.julesmeister.deutschcraft.feature.mission.domain.variation.MissionVariationContext
import com.julesmeister.deutschcraft.feature.mission.domain.variation.MissionVariationType
import javax.inject.Inject
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class MissionVariationAiGenerator
@Inject constructor(
    private val aiGateway:
    DeutschCraftAiGateway
) {

    suspend fun generate(
        variationType: MissionVariationType,
        rootMission: MissionTemplate,
        currentMission: ConversationMission,
        conversationHistory: List<ChatMessage>,
        context: MissionVariationContext
    ): GeneratedMissionVariation {
        require(
            conversationHistory.isNotEmpty()
        ) {
            "Conversation history must not be empty."
        }

        val userPrompt =
            buildUserPrompt(
                variationType =
                    variationType,

                rootMission =
                    rootMission,

                currentMission =
                    currentMission,

                conversationHistory =
                    conversationHistory,

                context =
                    context
            )

        val initialResponse =
            aiGateway.generateStructuredText(
                systemPrompt =
                    SYSTEM_PROMPT,

                userPrompt =
                    userPrompt
            )

        return try {
            parseResponse(
                rawResponse =
                    initialResponse,

                expectedDifficulty =
                    rootMission.difficulty
            )
        } catch (throwable: Throwable) {
            if (
                !throwable.isRecoverableGenerationFailure()
            ) {
                throw throwable
            }

            val repairedResponse =
                aiGateway.generateStructuredText(
                    systemPrompt =
                        REPAIR_SYSTEM_PROMPT,

                    userPrompt =
                        buildRepairPrompt(
                            originalUserPrompt =
                                userPrompt,

                            invalidResponse =
                                initialResponse,

                            failureMessage =
                                throwable.message.orEmpty()
                        )
                )

            parseResponse(
                rawResponse =
                    repairedResponse,

                expectedDifficulty =
                    rootMission.difficulty
            )
        }
    }

    private fun buildUserPrompt(
        variationType: MissionVariationType,
        rootMission: MissionTemplate,
        currentMission: ConversationMission,
        conversationHistory: List<ChatMessage>,
        context: MissionVariationContext
    ): String {
        val completedObjectives =
            currentMission
                .objectiveProgress
                .filter { progress ->
                    progress.isCompleted
                }
                .joinToString(
                    separator =
                        "\n"
                ) { progress ->
                    buildString {
                        append(
                            "- "
                        )

                        append(
                            progress.objective.title
                        )

                        append(
                            ": "
                        )

                        append(
                            progress.objective.description
                        )
                    }
                }
                .ifBlank {
                    "- None recorded."
                }

        val remainingObjectives =
            currentMission
                .objectiveProgress
                .filterNot { progress ->
                    progress.isCompleted
                }
                .joinToString(
                    separator =
                        "\n"
                ) { progress ->
                    buildString {
                        append(
                            "- "
                        )

                        append(
                            progress.objective.title
                        )

                        append(
                            ": "
                        )

                        append(
                            progress.objective.description
                        )
                    }
                }
                .ifBlank {
                    "- None."
                }

        val transcript =
            conversationHistory
                .sortedBy { message ->
                    message.sequenceNumber
                }
                .takeLast(
                    MAXIMUM_TRANSCRIPT_MESSAGE_COUNT
                )
                .joinToString(
                    separator =
                        "\n"
                ) { message ->
                    val speaker =
                        when (message.sender) {
                            MessageSender.USER ->
                                "LEARNER"

                            MessageSender.ASSISTANT ->
                                "EMIL"
                        }

                    val content =
                        message
                            .content
                            .normalizeWhitespace()
                            .take(
                                MAXIMUM_MESSAGE_CHARACTER_COUNT
                            )

                    "$speaker: $content"
                }

        return """
            MODE:
            ${context.mode.name}

            SELECTED VARIATION:
            ${variationType.name}

            ROOT MISSION:
            Title: ${rootMission.title.normalizeWhitespace()}
            Description: ${rootMission.description.normalizeWhitespace()}
            Scenario: ${rootMission.scenario.normalizeWhitespace()}
            Learner role: ${rootMission.learnerRole.normalizeWhitespace()}
            Assistant role: ${rootMission.assistantRole.normalizeWhitespace()}
            CEFR level: ${rootMission.difficulty.name}

            CURRENT TEMPORARY BRANCH:
            Title: ${currentMission.template.title.normalizeWhitespace()}
            Description: ${currentMission.template.description.normalizeWhitespace()}
            Scenario: ${currentMission.template.scenario.normalizeWhitespace()}

            COMPLETED OBJECTIVES:
            $completedObjectives

            CURRENT UNFINISHED OBJECTIVES:
            $remainingObjectives

            STRONG AREAS:
            ${context.strongObjectiveTitles.toPromptList()}

            IMPROVEMENT AREAS:
            ${context.improvementObjectiveTitles.toPromptList()}

            VOCABULARY ALREADY DEMONSTRATED:
            ${context.demonstratedVocabulary.toPromptList()}

            TARGET VOCABULARY NOT YET DEMONSTRATED:
            ${context.unusedTargetVocabulary.toPromptList()}

            COMPLETE AVAILABLE CONVERSATION:
            $transcript

            The ROOT MISSION is permanent and authoritative.

            The CURRENT TEMPORARY BRANCH and the latest messages
            are evidence of what has already been explored. They
            must not become the new mission identity.

            Choose one concrete, substantially different,
            unexplored direction inside the ROOT MISSION.

            Choose the direction yourself.

            Do not ask the learner which direction, subject,
            activity, or plan the learner wants next.

            Do not continue plans, scheduling, hobbies, sports,
            or activities if those areas have already been
            substantially discussed.

            The openingMessage must immediately introduce the
            selected new direction and ask one concrete question.

            Generate exactly three concise objectives.

            Return only the complete JSON object.
        """.trimIndent()
    }

    private fun buildRepairPrompt(
        originalUserPrompt: String,
        invalidResponse: String,
        failureMessage: String
    ): String {
        val safelyTruncatedResponse =
            invalidResponse.take(
                MAXIMUM_REPAIR_RESPONSE_CHARACTER_COUNT
            )

        return """
            The previous response was invalid or incomplete.

            FAILURE:
            ${failureMessage.normalizeWhitespace()}

            ORIGINAL TASK:
            $originalUserPrompt

            INVALID RESPONSE:
            $safelyTruncatedResponse

            Generate the response again from the beginning.

            Do not continue the invalid response.

            Return one complete, compact JSON object.

            Generate exactly three objectives.

            Keep every string concise so the JSON is not
            truncated.

            Do not use Markdown or code fences.
        """.trimIndent()
    }

    private fun parseResponse(
        rawResponse: String,
        expectedDifficulty: MissionDifficulty
    ): GeneratedMissionVariation {
        val jsonText =
            extractJsonObject(
                rawResponse =
                    rawResponse
            )

        val root =
            try {
                JSONObject(
                    jsonText
                )
            } catch (exception: JSONException) {
                throw IllegalArgumentException(
                    "The AI returned incomplete or malformed JSON.",
                    exception
                )
            }

        val branchFocus =
            root
                .requiredString(
                    key =
                        KEY_BRANCH_FOCUS
                )
                .take(
                    MAXIMUM_BRANCH_FOCUS_CHARACTER_COUNT
                )

        val openingMessage =
            root
                .requiredString(
                    key =
                        KEY_OPENING_MESSAGE
                )
                .take(
                    MAXIMUM_OPENING_MESSAGE_CHARACTER_COUNT
                )

        validateOpeningMessage(
            openingMessage =
                openingMessage
        )

        val missionJson =
            root.requiredObject(
                key =
                    KEY_MISSION
            )

        val difficulty =
            parseDifficulty(
                value =
                    missionJson.requiredString(
                        key =
                            KEY_DIFFICULTY
                    )
            )

        require(
            difficulty ==
                    expectedDifficulty
        ) {
            "The generated mission difficulty does not match the root mission."
        }

        val objectives =
            missionJson
                .requiredArray(
                    key =
                        KEY_OBJECTIVES
                )
                .toObjectList()
                .mapNotNull { objectiveJson ->
                    objectiveJson.toObjectiveDraftOrNull()
                }
                .distinctBy { objective ->
                    objective
                        .title
                        .trim()
                        .lowercase()
                }
                .take(
                    REQUIRED_OBJECTIVE_COUNT
                )

        require(
            objectives.size ==
                    REQUIRED_OBJECTIVE_COUNT
        ) {
            "The generated mission must contain exactly three valid objectives."
        }

        require(
            objectives.count { objective ->
                objective.isRequired
            } >=
                    MINIMUM_REQUIRED_OBJECTIVE_COUNT
        ) {
            "The generated mission must contain at least two required objectives."
        }

        val mission =
            GeneratedMissionDraft(
                title =
                    missionJson
                        .requiredString(
                            key =
                                KEY_TITLE
                        )
                        .take(
                            MAXIMUM_TITLE_CHARACTER_COUNT
                        ),

                description =
                    missionJson
                        .requiredString(
                            key =
                                KEY_DESCRIPTION
                        )
                        .take(
                            MAXIMUM_DESCRIPTION_CHARACTER_COUNT
                        ),

                scenario =
                    missionJson
                        .requiredString(
                            key =
                                KEY_SCENARIO
                        )
                        .take(
                            MAXIMUM_SCENARIO_CHARACTER_COUNT
                        ),

                learnerRole =
                    missionJson
                        .requiredString(
                            key =
                                KEY_LEARNER_ROLE
                        )
                        .take(
                            MAXIMUM_ROLE_CHARACTER_COUNT
                        ),

                assistantRole =
                    missionJson
                        .requiredString(
                            key =
                                KEY_ASSISTANT_ROLE
                        )
                        .take(
                            MAXIMUM_ROLE_CHARACTER_COUNT
                        ),

                difficulty =
                    difficulty,

                icon =
                    missionJson
                        .optionalString(
                            key =
                                KEY_ICON
                        )
                        .ifBlank {
                            DEFAULT_MISSION_ICON
                        }
                        .take(
                            MAXIMUM_ICON_CHARACTER_COUNT
                        ),

                objectives =
                    objectives
            )

        return GeneratedMissionVariation(
            branchFocus =
                branchFocus,

            openingMessage =
                openingMessage,

            mission =
                mission
        )
    }

    private fun JSONObject.toObjectiveDraftOrNull():
            GeneratedMissionObjectiveDraft? {

        val title =
            optionalString(
                key =
                    KEY_TITLE
            )
                .take(
                    MAXIMUM_OBJECTIVE_TITLE_CHARACTER_COUNT
                )

        val description =
            optionalString(
                key =
                    KEY_DESCRIPTION
            )
                .take(
                    MAXIMUM_OBJECTIVE_DESCRIPTION_CHARACTER_COUNT
                )

        val completionHint =
            optionalString(
                key =
                    KEY_COMPLETION_HINT
            )
                .take(
                    MAXIMUM_COMPLETION_HINT_CHARACTER_COUNT
                )

        if (
            title.isBlank() ||
            description.isBlank() ||
            completionHint.isBlank()
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
                optionalStringList(
                    key =
                        KEY_COACHING_HINTS,

                    maximumSize =
                        MAXIMUM_COACHING_HINT_COUNT,

                    maximumCharacterCount =
                        MAXIMUM_COACHING_HINT_CHARACTER_COUNT
                ),

            targetVocabulary =
                optionalStringList(
                    key =
                        KEY_TARGET_VOCABULARY,

                    maximumSize =
                        MAXIMUM_TARGET_VOCABULARY_COUNT,

                    maximumCharacterCount =
                        MAXIMUM_VOCABULARY_CHARACTER_COUNT
                ),

            examplePhrases =
                optionalStringList(
                    key =
                        KEY_EXAMPLE_PHRASES,

                    maximumSize =
                        MAXIMUM_EXAMPLE_PHRASE_COUNT,

                    maximumCharacterCount =
                        MAXIMUM_EXAMPLE_PHRASE_CHARACTER_COUNT
                ),

            isRequired =
                optBoolean(
                    KEY_IS_REQUIRED,
                    true
                )
        )
    }

    private fun validateOpeningMessage(
        openingMessage: String
    ) {
        val normalized =
            openingMessage.lowercase()

        require(
            FORBIDDEN_OPENING_PATTERNS.none { pattern ->
                normalized.contains(
                    pattern
                )
            }
        ) {
            "The generated opening asks the learner to choose the next direction."
        }
    }

    private fun parseDifficulty(
        value: String
    ): MissionDifficulty {

        return runCatching {
            MissionDifficulty.valueOf(
                value
                    .trim()
                    .uppercase()
            )
        }
            .getOrElse {
                throw IllegalArgumentException(
                    "The generated mission difficulty is invalid.",
                    it
                )
            }
    }

    private fun extractJsonObject(
        rawResponse: String
    ): String {
        val normalized =
            rawResponse
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

        val firstBraceIndex =
            normalized.indexOf(
                '{'
            )

        val lastBraceIndex =
            normalized.lastIndexOf(
                '}'
            )

        require(
            firstBraceIndex >=
                    0
        ) {
            "The AI response does not contain a JSON object."
        }

        require(
            lastBraceIndex >
                    firstBraceIndex
        ) {
            "The AI response was truncated before the JSON object was completed."
        }

        val extracted =
            normalized.substring(
                startIndex =
                    firstBraceIndex,

                endIndex =
                    lastBraceIndex +
                            1
            )

        require(
            extracted.hasBalancedJsonDelimiters()
        ) {
            "The AI response contains unbalanced JSON delimiters."
        }

        return extracted
    }

    private fun String.hasBalancedJsonDelimiters():
            Boolean {

        var objectDepth =
            0

        var arrayDepth =
            0

        var insideString =
            false

        var escaping =
            false

        for (character in this) {
            if (escaping) {
                escaping =
                    false

                continue
            }

            if (
                insideString &&
                character ==
                '\\'
            ) {
                escaping =
                    true

                continue
            }

            if (character == '"') {
                insideString =
                    !insideString

                continue
            }

            if (insideString) {
                continue
            }

            when (character) {
                '{' -> {
                    objectDepth +=
                        1
                }

                '}' -> {
                    objectDepth -=
                        1

                    if (
                        objectDepth <
                        0
                    ) {
                        return false
                    }
                }

                '[' -> {
                    arrayDepth +=
                        1
                }

                ']' -> {
                    arrayDepth -=
                        1

                    if (
                        arrayDepth <
                        0
                    ) {
                        return false
                    }
                }
            }
        }

        return !insideString &&
                !escaping &&
                objectDepth ==
                0 &&
                arrayDepth ==
                0
    }

    private fun Throwable.isRecoverableGenerationFailure():
            Boolean {

        return this is JSONException ||
                this is IllegalArgumentException ||
                cause is JSONException
    }

    private fun JSONObject.requiredString(
        key: String
    ): String {
        val value =
            optionalString(
                key =
                    key
            )

        require(
            value.isNotBlank()
        ) {
            "Missing required string: $key"
        }

        return value
    }

    private fun JSONObject.optionalString(
        key: String
    ): String {
        if (
            !has(
                key
            ) ||
            isNull(
                key
            )
        ) {
            return ""
        }

        return optString(
            key,
            ""
        )
            .normalizeWhitespace()
    }

    private fun JSONObject.requiredObject(
        key: String
    ): JSONObject {

        return optJSONObject(
            key
        )
            ?: throw IllegalArgumentException(
                "Missing required object: $key"
            )
    }

    private fun JSONObject.requiredArray(
        key: String
    ): JSONArray {

        return optJSONArray(
            key
        )
            ?: throw IllegalArgumentException(
                "Missing required array: $key"
            )
    }

    private fun JSONObject.optionalStringList(
        key: String,
        maximumSize: Int,
        maximumCharacterCount: Int
    ): List<String> {
        val array =
            optJSONArray(
                key
            )
                ?: return emptyList()

        return buildList {
            for (
            index in
            0 until array.length()
            ) {
                val value =
                    array
                        .optString(
                            index,
                            ""
                        )
                        .normalizeWhitespace()
                        .take(
                            maximumCharacterCount
                        )

                if (value.isNotBlank()) {
                    add(
                        value
                    )
                }
            }
        }
            .distinct()
            .take(
                maximumSize
            )
    }

    private fun JSONArray.toObjectList():
            List<JSONObject> {

        return buildList {
            for (
            index in
            0 until length()
            ) {
                optJSONObject(
                    index
                )
                    ?.let(
                        ::add
                    )
            }
        }
    }

    private fun List<String>.toPromptList():
            String {

        val values =
            asSequence()
                .map { value ->
                    value.normalizeWhitespace()
                }
                .filter { value ->
                    value.isNotBlank()
                }
                .distinct()
                .take(
                    MAXIMUM_PROMPT_LIST_ITEM_COUNT
                )
                .toList()

        return if (values.isEmpty()) {
            "- None recorded."
        } else {
            values.joinToString(
                separator =
                    "\n"
            ) { value ->
                "- $value"
            }
        }
    }

    private fun String.normalizeWhitespace():
            String {

        return trim()
            .replace(
                MULTIPLE_WHITESPACE_REGEX,
                " "
            )
    }

    private companion object {

        val SYSTEM_PROMPT =
            """
            You generate contextual branches for German-learning missions.

            The root mission is permanent and authoritative.

            A generated branch must never replace or redefine the
            root mission.

            Read the complete supplied conversation and identify
            the meaningful dimensions already explored.

            The latest message is not automatically the next branch.

            Generate one substantially different unexplored
            dimension of the root mission.

            Choose the direction yourself.

            Never ask what else the learner wants to discuss,
            choose, do, plan, or talk about.

            The opening message must immediately introduce one
            concrete new direction and ask one concrete question.

            For COMPLICATION, a complication is optional. Prefer a
            natural unexplored direction over an artificial problem.

            For NEW_VOCABULARY, introduce useful vocabulary through
            a natural unexplored aspect of the root mission.

            For GRAMMAR_STRETCH, use meaningful communication that
            naturally elicits slightly richer grammar.

            Return only valid JSON.

            Do not use Markdown.

            Do not use code fences.

            Keep all strings concise.

            Use exactly this structure:

            {
              "branchFocus": "short English semantic focus",
              "openingMessage": "concise German opening",
              "mission": {
                "title": "concise German title",
                "description": "concise German description",
                "scenario": "concise English scenario",
                "learnerRole": "concise German role",
                "assistantRole": "concise German role",
                "difficulty": "A1",
                "icon": "one emoji",
                "objectives": [
                  {
                    "title": "concise German objective",
                    "description": "concise German description",
                    "completionHint": "concise English completion rule",
                    "coachingHints": ["German hint"],
                    "targetVocabulary": ["German expression"],
                    "examplePhrases": ["German example"],
                    "isRequired": true
                  }
                ]
              }
            }

            Generate exactly three objectives.

            At least two objectives must be required.

            Difficulty must exactly match the root mission.

            Ensure every bracket, brace, quote, and array is closed.
            """.trimIndent()

        val REPAIR_SYSTEM_PROMPT =
            """
            You repair failed structured-generation responses.

            Generate the entire response again from the beginning.

            Do not continue or patch the previous JSON.

            Return only one complete valid JSON object.

            Do not use Markdown or code fences.

            Keep all strings concise.

            Generate exactly three objectives.

            Ensure every bracket, brace, quote, and array is closed.
            """.trimIndent()

        const val KEY_BRANCH_FOCUS =
            "branchFocus"

        const val KEY_OPENING_MESSAGE =
            "openingMessage"

        const val KEY_MISSION =
            "mission"

        const val KEY_TITLE =
            "title"

        const val KEY_DESCRIPTION =
            "description"

        const val KEY_SCENARIO =
            "scenario"

        const val KEY_LEARNER_ROLE =
            "learnerRole"

        const val KEY_ASSISTANT_ROLE =
            "assistantRole"

        const val KEY_DIFFICULTY =
            "difficulty"

        const val KEY_ICON =
            "icon"

        const val KEY_OBJECTIVES =
            "objectives"

        const val KEY_COMPLETION_HINT =
            "completionHint"

        const val KEY_COACHING_HINTS =
            "coachingHints"

        const val KEY_TARGET_VOCABULARY =
            "targetVocabulary"

        const val KEY_EXAMPLE_PHRASES =
            "examplePhrases"

        const val KEY_IS_REQUIRED =
            "isRequired"

        const val REQUIRED_OBJECTIVE_COUNT =
            3

        const val MINIMUM_REQUIRED_OBJECTIVE_COUNT =
            2

        const val MAXIMUM_TRANSCRIPT_MESSAGE_COUNT =
            40

        const val MAXIMUM_MESSAGE_CHARACTER_COUNT =
            700

        const val MAXIMUM_PROMPT_LIST_ITEM_COUNT =
            12

        const val MAXIMUM_REPAIR_RESPONSE_CHARACTER_COUNT =
            4_000

        const val MAXIMUM_BRANCH_FOCUS_CHARACTER_COUNT =
            100

        const val MAXIMUM_OPENING_MESSAGE_CHARACTER_COUNT =
            350

        const val MAXIMUM_TITLE_CHARACTER_COUNT =
            100

        const val MAXIMUM_DESCRIPTION_CHARACTER_COUNT =
            350

        const val MAXIMUM_SCENARIO_CHARACTER_COUNT =
            700

        const val MAXIMUM_ROLE_CHARACTER_COUNT =
            300

        const val MAXIMUM_ICON_CHARACTER_COUNT =
            8

        const val MAXIMUM_OBJECTIVE_TITLE_CHARACTER_COUNT =
            100

        const val MAXIMUM_OBJECTIVE_DESCRIPTION_CHARACTER_COUNT =
            300

        const val MAXIMUM_COMPLETION_HINT_CHARACTER_COUNT =
            350

        const val MAXIMUM_COACHING_HINT_COUNT =
            2

        const val MAXIMUM_COACHING_HINT_CHARACTER_COUNT =
            180

        const val MAXIMUM_TARGET_VOCABULARY_COUNT =
            3

        const val MAXIMUM_VOCABULARY_CHARACTER_COUNT =
            80

        const val MAXIMUM_EXAMPLE_PHRASE_COUNT =
            1

        const val MAXIMUM_EXAMPLE_PHRASE_CHARACTER_COUNT =
            180

        const val DEFAULT_MISSION_ICON =
            "↻"

        val FORBIDDEN_OPENING_PATTERNS =
            listOf(
                "worüber möchtest du",
                "was möchtest du noch",
                "was willst du noch",
                "was möchtest du als nächstes",
                "welches thema möchtest du",
                "gibt es noch etwas",
                "was möchtest du machen",
                "was willst du machen"
            )

        val MULTIPLE_WHITESPACE_REGEX =
            Regex(
                "\\s+"
            )
    }
}

data class GeneratedMissionVariation(
    val branchFocus: String,

    val openingMessage: String,

    val mission: GeneratedMissionDraft
)