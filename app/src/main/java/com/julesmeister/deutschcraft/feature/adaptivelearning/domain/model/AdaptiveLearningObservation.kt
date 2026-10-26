package com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel

/*
 * Represents one adaptive-learning observation extracted
 * from an assessed learner message.
 *
 * This model is the bridge between:
 *
 * Assessment output
 *         ↓
 * Adaptive-learning memory
 */
data class AdaptiveLearningObservation(
    /*
     * Stable identifier that groups equivalent observations.
     *
     * Examples:
     *
     * grammar:dative_articles
     * grammar:verb_position
     * vocabulary:bestellen
     * communication:giving_reasons
     */
    val focusKey: String,

    val type: LearningFocusType,

    /*
     * Short learner-readable name.
     *
     * Examples:
     *
     * Dative articles
     * Verb position
     * Giving reasons
     */
    val title: String,

    /*
     * Explanation of what DeutschCraft is tracking.
     */
    val description: String,

    /*
     * CEFR level whose communication behavior is currently
     * expected.
     */
    val targetCefrLevel: CefrLevel,

    /*
     * Populated only for communication-focused observations.
     */
    val communicationSkill: CommunicationSkill? =
        null,

    /*
     * What this message demonstrated.
     */
    val result: LearningEvidenceResult,

    /*
     * Evaluator confidence from 0 to 1.
     */
    val confidence: Float,

    /*
     * Internal explanation of the classification.
     */
    val reason: String,

    /*
     * Instruction that Emil can later use to reinforce this
     * focus naturally.
     *
     * Example:
     *
     * Ask a follow-up question that encourages the learner
     * to explain why.
     */
    val reinforcementInstruction: String,

    /*
     * Short learner-facing feedback suitable for the chat
     * top bar.
     *
     * Example:
     *
     * Try explaining why.
     */
    val learnerGuidance: String,

    /*
     * Original learner message that produced the evidence.
     */
    val learnerText: String,

    /*
     * Direct grammatical or lexical correction.
     */
    val correctedText: String? =
        null,

    /*
     * Natural version demonstrating how the same thought
     * could be expressed at the learner's current CEFR
     * level.
     *
     * This is different from correctedText:
     *
     * correctedText:
     * Fixes the error.
     *
     * levelAppropriateText:
     * Demonstrates current-level communication behavior.
     */
    val levelAppropriateText: String? =
        null
) {

    val normalizedFocusKey: String
        get() =
            focusKey
                .trim()
                .lowercase()
                .replace(
                    WHITESPACE_REGEX,
                    "_"
                )
                .replace(
                    INVALID_KEY_CHARACTER_REGEX,
                    ""
                )

    val normalizedConfidence: Float
        get() =
            confidence.coerceIn(
                minimumValue =
                    0f,
                maximumValue =
                    1f
            )

    val sanitizedTitle: String
        get() =
            title.trim()

    val sanitizedDescription: String
        get() =
            description.trim()

    val sanitizedReason: String
        get() =
            reason.trim()

    val sanitizedReinforcementInstruction: String
        get() =
            reinforcementInstruction.trim()

    val sanitizedLearnerGuidance: String
        get() =
            learnerGuidance.trim()

    val sanitizedLearnerText: String
        get() =
            learnerText.trim()

    val sanitizedCorrectedText: String?
        get() =
            correctedText
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                )

    val sanitizedLevelAppropriateText: String?
        get() =
            levelAppropriateText
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                )

    val hasUsableEvidence: Boolean
        get() =
            result !=
                    LearningEvidenceResult.NOT_OBSERVED &&
                    normalizedFocusKey.isNotBlank() &&
                    sanitizedTitle.isNotBlank() &&
                    sanitizedLearnerText.isNotBlank() &&
                    normalizedConfidence >=
                    MINIMUM_USABLE_CONFIDENCE

    private companion object {
        const val MINIMUM_USABLE_CONFIDENCE =
            0.40f

        val WHITESPACE_REGEX =
            Regex(
                "\\s+"
            )

        val INVALID_KEY_CHARACTER_REGEX =
            Regex(
                "[^a-z0-9:_-]"
            )
    }
}