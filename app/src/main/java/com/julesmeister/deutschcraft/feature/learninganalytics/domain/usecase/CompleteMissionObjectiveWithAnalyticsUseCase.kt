package com.julesmeister.deutschcraft.feature.learninganalytics.domain.usecase

import android.util.Log
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionAnalytics
import com.julesmeister.deutschcraft.feature.mission.domain.MissionStatus
import com.julesmeister.deutschcraft.feature.mission.domain.repository.MissionRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

/*
 * Completes one mission objective and determines whether that
 * objective also completed the entire mission.
 *
 * LocalFirstMissionRepository currently performs:
 *
 * 1. Persist objective completion.
 * 2. Count unfinished required objectives.
 * 3. Mark the mission COMPLETED when no required objectives
 *    remain.
 *
 * This use case then reloads the persisted mission.
 *
 * If the mission became COMPLETED, final analytics are
 * generated and persisted.
 */
class CompleteMissionObjectiveWithAnalyticsUseCase
@Inject constructor(
    private val missionRepository:
    MissionRepository,
    private val finalizeCompletedMissionAnalytics:
    FinalizeCompletedMissionAnalyticsUseCase
) {

    suspend operator fun invoke(
        conversationMissionId: String,
        objectiveId: String,
        messageId: String,
        confidence: Float
    ): ObjectiveCompletionResult {
        validateInput(
            conversationMissionId =
                conversationMissionId,
            objectiveId =
                objectiveId,
            messageId =
                messageId
        )

        return try {
            /*
             * Persist objective completion.
             *
             * LocalFirstMissionRepository may also mark the
             * complete mission as COMPLETED if this was the
             * final required objective.
             */
            missionRepository.completeObjective(
                conversationMissionId =
                    conversationMissionId,
                objectiveId =
                    objectiveId,
                messageId =
                    messageId,
                confidence =
                    confidence.coerceIn(
                        minimumValue =
                            MINIMUM_CONFIDENCE,
                        maximumValue =
                            MAXIMUM_CONFIDENCE
                    )
            )

            /*
             * Reload the complete ConversationMission from
             * Room after the write.
             *
             * We do not guess whether the mission is finished.
             * The persisted mission state is authoritative.
             */
            val updatedMission =
                missionRepository
                    .getConversationMission(
                        conversationMissionId =
                            conversationMissionId
                    )
                    ?: throw IllegalStateException(
                        "Mission could not be found after " +
                                "objective completion: " +
                                conversationMissionId
                    )

            if (
                updatedMission.status !=
                MissionStatus.COMPLETED
            ) {
                val updatedProgress =
                    updatedMission
                        .progress
                        .percentage
                        .coerceIn(
                            minimumValue =
                                MINIMUM_PROGRESS_PERCENTAGE,
                            maximumValue =
                                MAXIMUM_PROGRESS_PERCENTAGE
                        )

                Log.d(
                    USE_CASE_TAG,
                    buildString {
                        append(
                            "Mission objective completed. "
                        )
                        append(
                            "missionId="
                        )
                        append(
                            conversationMissionId
                        )
                        append(
                            ", objectiveId="
                        )
                        append(
                            objectiveId
                        )
                        append(
                            ", status="
                        )
                        append(
                            updatedMission.status
                        )
                        append(
                            ", progress="
                        )
                        append(
                            updatedProgress
                        )
                        append(
                            "%"
                        )
                    }
                )

                return ObjectiveCompletionResult
                    .ObjectiveCompleted(
                        conversationMissionId =
                            conversationMissionId,
                        objectiveId =
                            objectiveId,
                        progressPercentage =
                            updatedProgress
                    )
            }

            /*
             * The persisted mission is now COMPLETED.
             *
             * FinalizeCompletedMissionAnalyticsUseCase should
             * be idempotent:
             *
             * - Existing analytics are returned when already
             *   present.
             * - Otherwise evidence is built, analytics are
             *   calculated, and the result is persisted.
             */
            val analytics =
                finalizeCompletedMissionAnalytics(
                    conversationMissionId =
                        conversationMissionId
                )

            Log.d(
                USE_CASE_TAG,
                buildString {
                    append(
                        "Mission completed with analytics. "
                    )
                    append(
                        "missionId="
                    )
                    append(
                        conversationMissionId
                    )
                    append(
                        ", finalObjectiveId="
                    )
                    append(
                        objectiveId
                    )
                    append(
                        ", overall="
                    )
                    append(
                        analytics.overallScore
                    )
                    append(
                        ", independence="
                    )
                    append(
                        analytics.independenceScore
                    )
                    append(
                        ", hints="
                    )
                    append(
                        analytics.hintsUsed
                    )
                }
            )

            ObjectiveCompletionResult
                .MissionCompleted(
                    conversationMissionId =
                        conversationMissionId,
                    finalObjectiveId =
                        objectiveId,
                    analytics =
                        analytics
                )
        } catch (
            cancellationException:
            CancellationException
        ) {
            /*
             * Coroutine cancellation must propagate.
             *
             * Cancellation is not an ordinary mission failure.
             */
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.e(
                USE_CASE_TAG,
                "Mission objective completion failed.",
                throwable
            )

            throw throwable
        }
    }

    private fun validateInput(
        conversationMissionId: String,
        objectiveId: String,
        messageId: String
    ) {
        require(
            conversationMissionId.isNotBlank()
        ) {
            "Conversation mission ID must not be blank."
        }

        require(
            objectiveId.isNotBlank()
        ) {
            "Objective ID must not be blank."
        }

        require(
            messageId.isNotBlank()
        ) {
            "Message ID must not be blank."
        }
    }

    private companion object {
        const val USE_CASE_TAG =
            "CompleteMissionObjective"

        const val MINIMUM_CONFIDENCE =
            0f

        const val MAXIMUM_CONFIDENCE =
            1f

        const val MINIMUM_PROGRESS_PERCENTAGE =
            0

        const val MAXIMUM_PROGRESS_PERCENTAGE =
            100
    }
}

/*
 * Result returned to the conversation/mission pipeline.
 *
 * This lets the caller distinguish between an ordinary
 * objective advancement and complete mission completion.
 */
sealed interface ObjectiveCompletionResult {

    val conversationMissionId: String

    /*
     * One objective was completed, but additional required
     * objectives remain.
     */
    data class ObjectiveCompleted(
        override val conversationMissionId: String,

        val objectiveId: String,

        /*
         * Updated required-objective progress after Room has
         * persisted the objective completion.
         */
        val progressPercentage: Int
    ) : ObjectiveCompletionResult

    /*
     * This objective caused the complete mission to finish.
     *
     * analytics has already been calculated and persisted
     * before this result is returned.
     */
    data class MissionCompleted(
        override val conversationMissionId: String,

        val finalObjectiveId: String,

        val analytics:
        MissionAnalytics
    ) : ObjectiveCompletionResult
}