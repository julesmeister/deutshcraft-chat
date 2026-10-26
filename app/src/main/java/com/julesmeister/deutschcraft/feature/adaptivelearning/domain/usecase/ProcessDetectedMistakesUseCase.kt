package com.julesmeister.deutschcraft.feature.adaptivelearning.domain.usecase

import android.util.Log
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.core.database.dao.AdaptiveLearningMistakeDao
import com.julesmeister.deutschcraft.core.database.entity.DetectedMistakeEntity
import com.julesmeister.deutschcraft.feature.adaptivelearning.data.mapper.DetectedMistakeAdaptiveLearningMapper
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningMessageResult
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

/*
 * Loads saved assessment mistakes and passes them into the
 * persistent adaptive-learning pipeline.
 *
 * This use case does not run another AI request.
 *
 * It reuses DetectedMistakeEntity records already created by
 * DeutschCraft's assessment system.
 */
class ProcessDetectedMistakesUseCase
@Inject constructor(
    private val mistakeDao:
    AdaptiveLearningMistakeDao,
    private val mistakeMapper:
    DetectedMistakeAdaptiveLearningMapper,
    private val recordObservations:
    RecordAdaptiveLearningObservationsUseCase
) {

    /*
     * Preferred entry point when the assessment-evidence ID
     * is available.
     */
    suspend operator fun invoke(
        evidenceId: String,
        messageId: String,
        conversationId: String?,
        targetCefrLevel: CefrLevel
    ): AdaptiveLearningMessageResult {
        if (evidenceId.isBlank()) {
            Log.w(
                USE_CASE_TAG,
                "Adaptive-learning processing skipped because " +
                        "the assessment evidence ID was blank."
            )

            return AdaptiveLearningMessageResult()
        }

        if (messageId.isBlank()) {
            Log.w(
                USE_CASE_TAG,
                "Adaptive-learning processing skipped because " +
                        "the learner message ID was blank."
            )

            return AdaptiveLearningMessageResult()
        }

        return try {
            val mistakes =
                mistakeDao.getMistakesForEvidence(
                    evidenceId =
                        evidenceId.trim()
                )

            processMistakes(
                messageId =
                    messageId,
                conversationId =
                    conversationId,
                targetCefrLevel =
                    targetCefrLevel,
                mistakes =
                    mistakes
            )
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.e(
                USE_CASE_TAG,
                "Detected mistakes could not be processed.",
                throwable
            )

            AdaptiveLearningMessageResult(
                failedObservationCount =
                    1
            )
        }
    }

    /*
     * Secondary entry point for callers that already have
     * the saved DetectedMistakeEntity objects.
     */
    suspend fun processMistakes(
        messageId: String,
        conversationId: String?,
        targetCefrLevel: CefrLevel,
        mistakes: List<DetectedMistakeEntity>
    ): AdaptiveLearningMessageResult {
        if (messageId.isBlank()) {
            return AdaptiveLearningMessageResult(
                skippedObservationCount =
                    mistakes.size
            )
        }

        if (mistakes.isEmpty()) {
            Log.d(
                USE_CASE_TAG,
                "No detected mistakes were available for " +
                        "adaptive-learning processing."
            )

            return AdaptiveLearningMessageResult()
        }

        val observations =
            mistakeMapper.mapAll(
                mistakes =
                    mistakes,
                targetCefrLevel =
                    targetCefrLevel
            )

        if (observations.isEmpty()) {
            Log.d(
                USE_CASE_TAG,
                "Detected mistakes did not produce usable " +
                        "adaptive-learning observations."
            )

            return AdaptiveLearningMessageResult(
                skippedObservationCount =
                    mistakes.size
            )
        }

        val result =
            recordObservations(
                messageId =
                    messageId.trim(),
                conversationId =
                    conversationId
                        ?.trim()
                        ?.takeIf(
                            String::isNotBlank
                        ),
                observations =
                    observations
            )

        Log.d(
            USE_CASE_TAG,
            buildString {
                append("Detected mistakes processed. ")
                append("mistakes=")
                append(mistakes.size)
                append(", observations=")
                append(observations.size)
                append(", recorded=")
                append(result.recordedFocusCount)
                append(", feedback=")
                append(result.feedback?.type)
            }
        )

        return result
    }

    private companion object {
        const val USE_CASE_TAG =
            "ProcessDetectedMistakes"
    }
}