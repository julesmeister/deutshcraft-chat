package com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model

/*
 * Describes what a particular learner message demonstrated
 * about a tracked learning focus.
 */
enum class LearningEvidenceResult {

    /*
     * The learner repeated the tracked mistake or failed to
     * demonstrate the expected communication behavior.
     */
    STRUGGLED,

    /*
     * The learner attempted the target behavior, but the
     * evidence was incomplete or ambiguous.
     */
    PARTIAL_SUCCESS,

    /*
     * The learner demonstrated the behavior successfully.
     */
    SUCCESS,

    /*
     * The message did not provide useful evidence for this
     * focus.
     */
    NOT_OBSERVED
}