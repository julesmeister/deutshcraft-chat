package com.julesmeister.deutschcraft.feature.besser.application


enum class BesserChallengeSessionStatus {

    /*
     * Challenge is visible and Improve / Hint / Skip may be
     * used.
     */
    AVAILABLE,

    /*
     * Main conversation composer is being used to rewrite the
     * owning learner message.
     */
    REFINING,

    /*
     * Submitted refinement is currently being assessed.
     */
    EVALUATING,

    /*
     * Assessment completed but the selected capability has not
     * yet been sufficiently demonstrated.
     */
    NEEDS_WORK,

    /*
     * The capability was successfully demonstrated.
     */
    COMPLETED,

    /*
     * Learner explicitly chose not to practise this challenge.
     */
    SKIPPED,

    /*
     * Session was removed because its owning challenge/message
     * is no longer active.
     */
    CLOSED;

    val canRequestHint: Boolean
        get() =
            this == AVAILABLE ||
                    this == REFINING ||
                    this == NEEDS_WORK
}