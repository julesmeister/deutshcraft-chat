package com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel

/*
 * Domain representation of one persistent learning focus.
 *
 * In the next implementation slice, this model will be
 * persisted in Room and updated after every assessment.
 */
data class AdaptiveLearningFocus(
    val id: String,

    /*
     * Stable identifier used to group equivalent evidence.
     *
     * Examples:
     *
     * grammar:dative_articles
     * vocabulary:bestellen
     * communication:giving_reasons
     */
    val focusKey: String,

    val type: LearningFocusType,

    val title: String,

    val description: String,

    /*
     * CEFR level at which this behavior is currently being
     * reinforced.
     *
     * Example:
     *
     * A B1 learner may receive reinforcement for giving
     * reasons and connecting ideas.
     */
    val targetCefrLevel: CefrLevel,

    val communicationSkill: CommunicationSkill? =
        null,

    val masteryState: MasteryState =
        MasteryState.OBSERVING,

    /*
     * Total number of messages that provided usable evidence.
     */
    val evidenceCount: Int =
        0,

    /*
     * Number of messages where the learner repeated the
     * tracked problem.
     */
    val struggleCount: Int =
        0,

    /*
     * Number of messages where the learner demonstrated the
     * tracked focus successfully.
     */
    val successCount: Int =
        0,

    /*
     * Number of consecutive successful demonstrations.
     *
     * This helps distinguish one lucky success from stable
     * learning.
     */
    val consecutiveSuccessCount: Int =
        0,

    /*
     * Normalized confidence from 0 to 1 indicating how
     * strongly DeutschCraft believes this focus represents a
     * real learner pattern.
     */
    val confidence: Float =
        0f,

    /*
     * Normalized mastery score from 0 to 1.
     */
    val masteryScore: Float =
        0f,

    /*
     * Internal guidance for Emil.
     *
     * Example:
     *
     * "Ask questions that encourage the learner to justify
     * preferences using weil, denn, or deshalb."
     */
    val reinforcementInstruction: String,

    /*
     * Learner-facing feedback displayed subtly in the chat.
     *
     * Example:
     *
     * "Try explaining why."
     */
    val learnerGuidance: String,

    val firstObservedAt: Long,

    val lastObservedAt: Long,

    val lastPracticedAt: Long? =
        null,

    val masteredAt: Long? =
        null
) {

    val safeEvidenceCount: Int
        get() =
            evidenceCount.coerceAtLeast(0)

    val safeStruggleCount: Int
        get() =
            struggleCount.coerceAtLeast(0)

    val safeSuccessCount: Int
        get() =
            successCount.coerceAtLeast(0)

    val safeConsecutiveSuccessCount: Int
        get() =
            consecutiveSuccessCount.coerceAtLeast(0)

    val safeConfidence: Float
        get() =
            confidence.coerceIn(
                minimumValue =
                    0f,
                maximumValue =
                    1f
            )

    val safeMasteryScore: Float
        get() =
            masteryScore.coerceIn(
                minimumValue =
                    0f,
                maximumValue =
                    1f
            )

    val masteryPercentage: Int
        get() =
            (
                    safeMasteryScore *
                            PERCENTAGE_MULTIPLIER
                    )
                .toInt()
                .coerceIn(
                    minimumValue =
                        0,
                    maximumValue =
                        100
                )

    val shouldReinforce: Boolean
        get() =
            masteryState ==
                    MasteryState.STRUGGLING ||
                    masteryState ==
                    MasteryState.PRACTICING ||
                    masteryState ==
                    MasteryState.NEEDS_REVIEW

    val isMastered: Boolean
        get() =
            masteryState ==
                    MasteryState.MASTERED

    private companion object {
        const val PERCENTAGE_MULTIPLIER =
            100f
    }
}