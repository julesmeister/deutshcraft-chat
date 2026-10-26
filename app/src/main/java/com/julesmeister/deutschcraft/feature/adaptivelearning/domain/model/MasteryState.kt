package com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model

/*
 * Represents the learner's current relationship with a
 * tracked grammar, vocabulary, or communication skill.
 */
enum class MasteryState(
    val displayName: String
) {

    /*
     * DeutschCraft has noticed the behavior but does not yet
     * have enough evidence to classify it confidently.
     */
    OBSERVING(
        displayName =
            "Observing"
    ),

    /*
     * The learner has repeatedly struggled with the focus.
     */
    STRUGGLING(
        displayName =
            "Needs attention"
    ),

    /*
     * DeutschCraft is deliberately creating opportunities
     * for the learner to use the focus again.
     */
    PRACTICING(
        displayName =
            "Practicing"
    ),

    /*
     * Recent successful evidence outweighs recent mistakes,
     * but more evidence is still required.
     */
    IMPROVING(
        displayName =
            "Improving"
    ),

    /*
     * The learner has demonstrated the focus successfully
     * and consistently across multiple conversations.
     */
    MASTERED(
        displayName =
            "Mastered"
    ),

    /*
     * A previously mastered focus has started appearing as a
     * recurring problem again.
     */
    NEEDS_REVIEW(
        displayName =
            "Needs review"
    )
}