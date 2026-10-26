package com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model

/*
 * Combined adaptive-learning result for one learner message.
 *
 * One message may provide evidence for several focuses:
 *
 * - One grammar focus
 * - One vocabulary focus
 * - One communication focus
 *
 * Only one learner-facing feedback event is selected so the
 * chat interface does not overwhelm the learner.
 */
data class AdaptiveLearningMessageResult(
    val recordedFocuses:
    List<AdaptiveLearningFocus> =
        emptyList(),

    val skippedObservationCount: Int =
        0,

    val failedObservationCount: Int =
        0,

    val feedback:
    AdaptiveLearningFeedback? =
        null
) {

    val recordedFocusCount: Int
        get() =
            recordedFocuses.size

    val recordedAnyEvidence: Boolean
        get() =
            recordedFocuses.isNotEmpty()

    val producedFeedback: Boolean
        get() =
            feedback != null

    val masteredFocuses:
            List<AdaptiveLearningFocus>
        get() =
            recordedFocuses.filter { focus ->
                focus.masteryState ==
                        MasteryState.MASTERED
            }

    val improvingFocuses:
            List<AdaptiveLearningFocus>
        get() =
            recordedFocuses.filter { focus ->
                focus.masteryState ==
                        MasteryState.IMPROVING
            }

    val strugglingFocuses:
            List<AdaptiveLearningFocus>
        get() =
            recordedFocuses.filter { focus ->
                focus.masteryState ==
                        MasteryState.STRUGGLING ||
                        focus.masteryState ==
                        MasteryState.PRACTICING ||
                        focus.masteryState ==
                        MasteryState.NEEDS_REVIEW
            }
}