package com.julesmeister.deutschcraft.feature.mission.domain.usecase

import android.util.Log
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import com.julesmeister.deutschcraft.feature.mission.data.ai.MissionEvaluationSpeaker
import com.julesmeister.deutschcraft.feature.mission.data.ai.MissionEvaluationTurn
import com.julesmeister.deutschcraft.feature.mission.data.ai.MissionObjectiveEvaluationAiGateway
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import com.julesmeister.deutschcraft.feature.mission.domain.MissionObjective
import com.julesmeister.deutschcraft.feature.mission.domain.MissionStatus
import com.julesmeister.deutschcraft.feature.mission.domain.repository.MissionRepository
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException

class EvaluateMissionMessageUseCase
@Inject constructor(
    private val aiGateway:
    MissionObjectiveEvaluationAiGateway,
    private val missionRepository:
    MissionRepository
) {

    suspend operator fun invoke(
        mission: ConversationMission,
        learnerMessage: ChatMessage,
        recentMessages: List<ChatMessage>,
        cefrLevel: CefrLevel
    ): MissionMessageEvaluationSummary {
        val skipReason =
            determineSkipReason(
                mission =
                    mission,
                learnerMessage =
                    learnerMessage
            )

        if (skipReason != null) {
            Log.d(
                USE_CASE_TAG,
                "Mission evaluation skipped: $skipReason"
            )

            return MissionMessageEvaluationSummary(
                outcome =
                    MissionMessageEvaluationOutcome.SKIPPED
            )
        }

        val currentObjectiveProgress =
            findCurrentObjectiveProgress(
                mission =
                    mission
            )
                ?: run {
                    Log.w(
                        USE_CASE_TAG,
                        "Mission evaluation skipped because no " +
                                "unfinished objective was found. " +
                                "missionId=${mission.id}"
                    )

                    return MissionMessageEvaluationSummary(
                        outcome =
                            MissionMessageEvaluationOutcome.SKIPPED
                    )
                }

        val currentObjective =
            currentObjectiveProgress.objective

        val recentConversation =
            createRecentConversation(
                recentMessages =
                    recentMessages,
                learnerMessage =
                    learnerMessage
            )

        Log.d(
            USE_CASE_TAG,
            buildString {
                append("Evaluating mission objective. ")
                append("missionId=")
                append(mission.id)
                append(", objectiveId=")
                append(currentObjective.id)
                append(", objectiveTitle=")
                append(currentObjective.title)
                append(", recentTurns=")
                append(recentConversation.size)
                append(", currentProgress=")
                append(
                    calculateCurrentProgressPercentage(
                        mission =
                            mission
                    )
                )
                append("%")
            }
        )

        val evaluationResult =
            try {
                aiGateway.evaluateMessage(
                    learnerMessage =
                        learnerMessage.content.trim(),
                    recentConversation =
                        recentConversation,
                    cefrLevel =
                        cefrLevel,
                    missionTitle =
                        mission.template.title,
                    missionScenario =
                        mission.template.scenario,
                    learnerRole =
                        mission.template.learnerRole,
                    assistantRole =
                        mission.template.assistantRole,

                    /*
                     * Only evaluate the currently active
                     * objective.
                     *
                     * This prevents one learner response from
                     * skipping several mission steps at once.
                     */
                    unfinishedObjectives =
                        listOf(
                            currentObjective
                        )
                )
            } catch (
                cancellationException:
                CancellationException
            ) {
                throw cancellationException
            } catch (throwable: Throwable) {
                Log.e(
                    USE_CASE_TAG,
                    "Mission objective evaluation failed.",
                    throwable
                )

                return MissionMessageEvaluationSummary(
                    evaluatedObjectiveCount =
                        1,
                    currentObjectiveId =
                        currentObjective.id,
                    currentObjectiveTitle =
                        currentObjective.title,
                    outcome =
                        MissionMessageEvaluationOutcome
                            .EVALUATION_FAILED
                )
            }

        val matchingEvaluations =
            evaluationResult
                .completedObjectives
                .filter { evaluation ->
                    evaluation.objectiveId ==
                            currentObjective.id
                }
                .sortedByDescending { evaluation ->
                    evaluation.confidence
                }

        val highestMatchingEvaluation =
            matchingEvaluations.firstOrNull()

        val highestConfidence =
            highestMatchingEvaluation
                ?.confidence
                ?.coerceIn(
                    minimumValue =
                        0f,
                    maximumValue =
                        1f
                )

        Log.d(
            USE_CASE_TAG,
            buildString {
                append("Mission AI evaluation received. ")
                append("objectiveId=")
                append(currentObjective.id)
                append(", matchingResults=")
                append(matchingEvaluations.size)
                append(", highestConfidence=")
                append(highestConfidence)
            }
        )

        /*
         * No matching objective means Qwen did not find
         * enough evidence that the learner completed the
         * current goal.
         */
        if (highestMatchingEvaluation == null) {
            return createNotCompletedSummary(
                mission =
                    mission,
                currentObjective =
                    currentObjective,
                confidence =
                    null
            )
        }

        /*
         * The learner may have demonstrated the correct
         * intent, but Qwen may still be uncertain.
         *
         * Confidence below the completion threshold remains
         * incomplete. The next UI slice can present this as
         * "almost there" when it exceeds the partial threshold.
         */
        if (
            highestMatchingEvaluation.confidence <
            MINIMUM_COMPLETION_CONFIDENCE
        ) {
            return createNotCompletedSummary(
                mission =
                    mission,
                currentObjective =
                    currentObjective,
                confidence =
                    highestMatchingEvaluation.confidence
            )
        }

        val unfinishedObjectiveCountBeforeCompletion =
            mission.objectiveProgress.count { progress ->
                !progress.isCompleted
            }

        val completedObjectiveCountBeforeCompletion =
            mission.objectiveProgress.count { progress ->
                progress.isCompleted
            }

        val totalObjectiveCount =
            mission.objectiveProgress.size

        val missionWillBeCompleted =
            unfinishedObjectiveCountBeforeCompletion ==
                    FINAL_OBJECTIVE_REMAINING_COUNT

        val projectedCompletedCount =
            (
                    completedObjectiveCountBeforeCompletion +
                            1
                    )
                .coerceAtMost(
                    maximumValue =
                        totalObjectiveCount
                )

        val projectedProgressPercentage =
            calculateProgressPercentage(
                completedObjectiveCount =
                    projectedCompletedCount,
                totalObjectiveCount =
                    totalObjectiveCount
            )

        return try {
            missionRepository.completeObjective(
                conversationMissionId =
                    mission.id,
                objectiveId =
                    currentObjective.id,
                messageId =
                    learnerMessage.id,
                confidence =
                    highestMatchingEvaluation
                        .confidence
                        .coerceIn(
                            minimumValue =
                                0f,
                            maximumValue =
                                1f
                        )
            )

            Log.i(
                USE_CASE_TAG,
                buildString {
                    append("Mission objective completed. ")
                    append("missionId=")
                    append(mission.id)
                    append(", objectiveId=")
                    append(currentObjective.id)
                    append(", objectiveTitle=")
                    append(currentObjective.title)
                    append(", confidence=")
                    append(
                        highestMatchingEvaluation
                            .confidence
                    )
                    append(", projectedProgress=")
                    append(projectedProgressPercentage)
                    append("%")
                    append(", missionCompleted=")
                    append(missionWillBeCompleted)
                }
            )

            MissionMessageEvaluationSummary(
                evaluatedObjectiveCount =
                    1,
                completedObjectiveIds =
                    listOf(
                        currentObjective.id
                    ),
                completedObjectiveTitle =
                    currentObjective.title,
                currentObjectiveId =
                    currentObjective.id,
                currentObjectiveTitle =
                    currentObjective.title,
                highestConfidence =
                    highestMatchingEvaluation
                        .confidence
                        .coerceIn(
                            minimumValue =
                                0f,
                            maximumValue =
                                1f
                        ),
                progressPercentageBefore =
                    calculateCurrentProgressPercentage(
                        mission =
                            mission
                    ),
                projectedProgressPercentage =
                    projectedProgressPercentage,
                missionCompleted =
                    missionWillBeCompleted,
                outcome =
                    if (missionWillBeCompleted) {
                        MissionMessageEvaluationOutcome
                            .MISSION_COMPLETED
                    } else {
                        MissionMessageEvaluationOutcome
                            .OBJECTIVE_COMPLETED
                    }
            )
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.e(
                USE_CASE_TAG,
                buildString {
                    append(
                        "Could not persist completed objective. "
                    )
                    append("missionId=")
                    append(mission.id)
                    append(", objectiveId=")
                    append(currentObjective.id)
                },
                throwable
            )

            MissionMessageEvaluationSummary(
                evaluatedObjectiveCount =
                    1,
                currentObjectiveId =
                    currentObjective.id,
                currentObjectiveTitle =
                    currentObjective.title,
                highestConfidence =
                    highestMatchingEvaluation
                        .confidence
                        .coerceIn(
                            minimumValue =
                                0f,
                            maximumValue =
                                1f
                        ),
                progressPercentageBefore =
                    calculateCurrentProgressPercentage(
                        mission =
                            mission
                    ),
                projectedProgressPercentage =
                    calculateCurrentProgressPercentage(
                        mission =
                            mission
                    ),
                outcome =
                    MissionMessageEvaluationOutcome
                        .PERSISTENCE_FAILED
            )
        }
    }

    private fun determineSkipReason(
        mission: ConversationMission,
        learnerMessage: ChatMessage
    ): String? {
        return when {
            mission.status !=
                    MissionStatus.ACTIVE -> {

                "Mission is not active."
            }

            learnerMessage.sender !=
                    MessageSender.USER -> {

                "Message sender is not USER."
            }

            learnerMessage.id.isBlank() -> {
                "Learner message ID is blank."
            }

            learnerMessage.content.isBlank() -> {
                "Learner message content is blank."
            }

            else -> {
                null
            }
        }
    }

    private fun createRecentConversation(
        recentMessages: List<ChatMessage>,
        learnerMessage: ChatMessage
    ): List<MissionEvaluationTurn> {
        val contextMessages =
            recentMessages
                .asSequence()
                .filter { message ->
                    message.content.isNotBlank()
                }
                /*
                 * Exclude anything that was saved after the
                 * learner message currently being evaluated.
                 */
                .filter { message ->
                    message.sequenceNumber <=
                            learnerMessage.sequenceNumber
                }
                .sortedBy { message ->
                    message.sequenceNumber
                }
                .takeLastAsList(
                    maximumCount =
                        MAXIMUM_RECENT_MESSAGE_COUNT
                )

        val learnerMessageAlreadyIncluded =
            contextMessages.any { message ->
                message.id ==
                        learnerMessage.id
            }

        val completeContext =
            if (learnerMessageAlreadyIncluded) {
                contextMessages
            } else {
                (
                        contextMessages +
                                learnerMessage
                        )
                    .sortedBy { message ->
                        message.sequenceNumber
                    }
                    .takeLast(
                        MAXIMUM_RECENT_MESSAGE_COUNT
                    )
            }

        return completeContext.map { message ->
            MissionEvaluationTurn(
                speaker =
                    if (
                        message.sender ==
                        MessageSender.USER
                    ) {
                        MissionEvaluationSpeaker.LEARNER
                    } else {
                        MissionEvaluationSpeaker.ASSISTANT
                    },
                content =
                    message.content.trim()
            )
        }
    }

    private fun createNotCompletedSummary(
        mission: ConversationMission,
        currentObjective: MissionObjective,
        confidence: Float?
    ): MissionMessageEvaluationSummary {
        val safeConfidence =
            confidence?.coerceIn(
                minimumValue =
                    0f,
                maximumValue =
                    1f
            )

        val outcome =
            if (
                safeConfidence != null &&
                safeConfidence >=
                MINIMUM_PARTIAL_ATTEMPT_CONFIDENCE
            ) {
                MissionMessageEvaluationOutcome
                    .PARTIAL_ATTEMPT
            } else {
                MissionMessageEvaluationOutcome
                    .NOT_COMPLETED
            }

        Log.d(
            USE_CASE_TAG,
            buildString {
                append("Mission objective not completed. ")
                append("objectiveId=")
                append(currentObjective.id)
                append(", confidence=")
                append(safeConfidence)
                append(", outcome=")
                append(outcome)
            }
        )

        val currentProgress =
            calculateCurrentProgressPercentage(
                mission =
                    mission
            )

        return MissionMessageEvaluationSummary(
            evaluatedObjectiveCount =
                1,
            currentObjectiveId =
                currentObjective.id,
            currentObjectiveTitle =
                currentObjective.title,
            highestConfidence =
                safeConfidence,
            progressPercentageBefore =
                currentProgress,
            projectedProgressPercentage =
                currentProgress,
            missionCompleted =
                false,
            outcome =
                outcome
        )
    }

    private fun findCurrentObjectiveProgress(
        mission: ConversationMission
    ) =
        mission.currentObjective
            ?.takeIf { progress ->
                !progress.isCompleted
            }
            ?: mission.objectiveProgress
                .firstOrNull { progress ->
                    !progress.isCompleted
                }

    private fun calculateCurrentProgressPercentage(
        mission: ConversationMission
    ): Int {
        val totalObjectiveCount =
            mission.objectiveProgress.size

        val completedObjectiveCount =
            mission.objectiveProgress.count { progress ->
                progress.isCompleted
            }

        return calculateProgressPercentage(
            completedObjectiveCount =
                completedObjectiveCount,
            totalObjectiveCount =
                totalObjectiveCount
        )
    }

    private fun calculateProgressPercentage(
        completedObjectiveCount: Int,
        totalObjectiveCount: Int
    ): Int {
        if (totalObjectiveCount <= 0) {
            return 0
        }

        return (
                completedObjectiveCount.toFloat() /
                        totalObjectiveCount.toFloat() *
                        PERCENTAGE_MULTIPLIER
                )
            .roundToInt()
            .coerceIn(
                minimumValue =
                    0,
                maximumValue =
                    100
            )
    }

    private fun <T> Sequence<T>.takeLastAsList(
        maximumCount: Int
    ): List<T> {
        if (maximumCount <= 0) {
            return emptyList()
        }

        return toList()
            .takeLast(
                maximumCount
            )
    }

    private companion object {
        const val USE_CASE_TAG =
            "EvaluateMissionMessage"

        /*
         * Previously this was 0.88, which was too strict for
         * valid conversational paraphrases.
         *
         * 0.78 still requires meaningful evaluator
         * confidence while accepting natural alternatives.
         */
        const val MINIMUM_COMPLETION_CONFIDENCE =
            0.78f

        /*
         * A matching result above this threshold but below
         * completion is treated as a partial attempt.
         */
        const val MINIMUM_PARTIAL_ATTEMPT_CONFIDENCE =
            0.55f

        const val FINAL_OBJECTIVE_REMAINING_COUNT =
            1

        const val MAXIMUM_RECENT_MESSAGE_COUNT =
            12

        const val PERCENTAGE_MULTIPLIER =
            100f
    }
}

enum class MissionMessageEvaluationOutcome {
    SKIPPED,
    NOT_COMPLETED,
    PARTIAL_ATTEMPT,
    OBJECTIVE_COMPLETED,
    MISSION_COMPLETED,
    EVALUATION_FAILED,
    PERSISTENCE_FAILED
}

data class MissionMessageEvaluationSummary(
    val evaluatedObjectiveCount: Int =
        0,

    val completedObjectiveIds:
    List<String> =
        emptyList(),

    val completedObjectiveTitle: String? =
        null,

    val currentObjectiveId: String? =
        null,

    val currentObjectiveTitle: String? =
        null,

    val highestConfidence: Float? =
        null,

    val progressPercentageBefore: Int =
        0,

    val projectedProgressPercentage: Int =
        progressPercentageBefore,

    val missionCompleted: Boolean =
        false,

    val outcome:
    MissionMessageEvaluationOutcome =
        MissionMessageEvaluationOutcome.SKIPPED
) {

    val completedObjectiveCount: Int
        get() =
            completedObjectiveIds.size

    val completedAnyObjective: Boolean
        get() =
            completedObjectiveIds.isNotEmpty()

    val isPartialAttempt: Boolean
        get() =
            outcome ==
                    MissionMessageEvaluationOutcome
                        .PARTIAL_ATTEMPT

    val completedMission: Boolean
        get() =
            missionCompleted ||
                    outcome ==
                    MissionMessageEvaluationOutcome
                        .MISSION_COMPLETED

    val progressChanged: Boolean
        get() =
            projectedProgressPercentage >
                    progressPercentageBefore
}