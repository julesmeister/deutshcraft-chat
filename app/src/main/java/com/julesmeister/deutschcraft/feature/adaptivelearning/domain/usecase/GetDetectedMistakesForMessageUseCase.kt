package com.julesmeister.deutschcraft.feature.adaptivelearning.domain.usecase

import android.util.Log
import com.julesmeister.deutschcraft.core.database.dao.AdaptiveLearningMistakeDao
import com.julesmeister.deutschcraft.core.database.entity.DetectedMistakeEntity
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

/*
 * Loads mistakes already persisted by the assessment system
 * for one learner message.
 *
 * This does not perform another assessment.
 */
class GetDetectedMistakesForMessageUseCase
@Inject constructor(
    private val mistakeDao:
    AdaptiveLearningMistakeDao
) {

    suspend operator fun invoke(
        messageId: String
    ): List<DetectedMistakeEntity> {
        if (messageId.isBlank()) {
            return emptyList()
        }

        return try {
            mistakeDao.getMistakesForMessage(
                messageId =
                    messageId.trim()
            )
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.e(
                USE_CASE_TAG,
                "Detected mistakes could not be loaded for " +
                        "message $messageId.",
                throwable
            )

            emptyList()
        }
    }

    private companion object {
        const val USE_CASE_TAG =
            "AdaptiveMistakeLoader"
    }
}