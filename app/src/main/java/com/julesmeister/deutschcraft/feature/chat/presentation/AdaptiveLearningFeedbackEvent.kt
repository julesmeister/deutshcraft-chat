package com.julesmeister.deutschcraft.feature.chat.presentation

import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningFeedback
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningFeedbackType
import java.util.UUID

/*
 * One-time presentation event for adaptive-learning feedback.
 *
 * The underlying AdaptiveLearningFeedback is domain data.
 * This event adds a unique ID so Compose can display and
 * consume repeated feedback for the same learning focus.
 */
data class AdaptiveLearningFeedbackEvent(
    val eventId: String,
    val type: AdaptiveLearningFeedbackType,
    val message: String,
    val focusKey: String,
    val focusTitle: String?
) {

    companion object {

        fun from(
            feedback: AdaptiveLearningFeedback
        ): AdaptiveLearningFeedbackEvent? {
            val message =
                feedback.message
                    .trim()
                    .takeIf(
                        String::isNotBlank
                    )
                    ?: return null

            val focusKey =
                feedback.focusKey
                    .trim()
                    .takeIf(
                        String::isNotBlank
                    )
                    ?: return null

            return AdaptiveLearningFeedbackEvent(
                eventId =
                    UUID.randomUUID().toString(),
                type =
                    feedback.type,
                message =
                    message,
                focusKey =
                    focusKey,
                focusTitle =
                    feedback.focusTitle
                        ?.trim()
                        ?.takeIf(
                            String::isNotBlank
                        )
            )
        }
    }
}