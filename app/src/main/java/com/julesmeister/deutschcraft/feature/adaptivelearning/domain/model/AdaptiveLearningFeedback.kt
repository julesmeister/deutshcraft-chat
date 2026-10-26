package com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model

/*
 * Describes a subtle learner-facing adaptive-learning event.
 *
 * This can later be shown briefly in the chat top bar.
 */
data class AdaptiveLearningFeedback(
    val type: AdaptiveLearningFeedbackType,

    /*
     * Short message displayed to the learner.
     *
     * Examples:
     *
     * Try explaining why.
     *
     * Dative articles are becoming more consistent.
     *
     * Giving reasons mastered.
     */
    val message: String,

    val focusKey: String,

    /*
     * Optional title for richer UI surfaces.
     */
    val focusTitle: String? =
        null
) {

    val safeMessage: String
        get() =
            message.trim()

    val safeFocusKey: String
        get() =
            focusKey.trim()

    val safeFocusTitle: String?
        get() =
            focusTitle
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                )

    val isUsable: Boolean
        get() =
            safeMessage.isNotBlank() &&
                    safeFocusKey.isNotBlank()
}

enum class AdaptiveLearningFeedbackType {

    /*
     * A new issue was observed, but the learner should not
     * be confronted with heavy feedback yet.
     */
    GENTLE_REMINDER,

    /*
     * The same mistake or weakness was observed again.
     */
    REPEATED_MISTAKE,

    /*
     * The learner demonstrated part of the expected skill.
     */
    PARTIAL_SUCCESS,

    /*
     * One successful use was detected.
     */
    SUCCESS,

    /*
     * Multiple recent successful uses indicate improvement.
     */
    IMPROVING,

    /*
     * Sufficient consistent evidence indicates mastery.
     */
    MASTERED,

    /*
     * A previously mastered focus appeared as a weakness
     * again.
     */
    NEEDS_REVIEW
}