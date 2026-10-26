package com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model

/*
 * Result returned after attempting to record one adaptive
 * learning observation.
 */
sealed interface AdaptiveLearningRecordResult {

    /*
     * Evidence and the updated aggregate focus were saved.
     */
    data class Recorded(
        val focus: AdaptiveLearningFocus,
        val transition: AdaptiveLearningTransition,
        val feedback: AdaptiveLearningFeedback?
    ) : AdaptiveLearningRecordResult

    /*
     * The observation was intentionally ignored.
     *
     * Examples:
     *
     * - Blank message ID
     * - NOT_OBSERVED evidence
     * - Confidence was too low
     */
    data class Skipped(
        val reason: String
    ) : AdaptiveLearningRecordResult

    /*
     * Persistence or processing failed.
     */
    data class Failed(
        val message: String
    ) : AdaptiveLearningRecordResult
}

/*
 * Describes how the learner's aggregate focus changed after
 * the new observation.
 */
enum class AdaptiveLearningTransition {

    /*
     * Evidence was saved without a learner-significant state
     * change.
     */
    NONE,

    /*
     * The focus was observed for the first time.
     */
    NEW_FOCUS,

    /*
     * A known mistake or weakness appeared again.
     */
    REPEATED_MISTAKE,

    /*
     * The learner partially demonstrated the expected
     * behavior.
     */
    PARTIAL_SUCCESS,

    /*
     * One successful use was detected.
     */
    SUCCESSFUL_USE,

    /*
     * Recent evidence indicates consistent improvement.
     */
    IMPROVING,

    /*
     * The focus reached mastery requirements.
     */
    MASTERED,

    /*
     * A previously mastered focus now requires review.
     */
    MASTERY_LOST
}