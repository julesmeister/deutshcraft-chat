package com.julesmeister.deutschcraft.feature.mission.domain.usecase

import android.util.Log
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionAnalytics
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.usecase.CompleteMissionObjectiveWithAnalyticsUseCase
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.usecase.ObjectiveCompletionResult
import com.julesmeister.deutschcraft.feature.mission.data.ai.MissionEvaluationSpeaker
import com.julesmeister.deutschcraft.feature.mission.data.ai.MissionEvaluationTurn
import com.julesmeister.deutschcraft.feature.mission.data.ai.MissionObjectiveEvaluationAiGateway
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import com.julesmeister.deutschcraft.feature.mission.domain.MissionObjective
import com.julesmeister.deutschcraft.feature.mission.domain.MissionStatus
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException

/*
 * Evaluates the learner's latest message against the current
 * active mission objective.
 *
 * Responsibilities:
 *
 * - Evaluate only the current unfinished objective
 * - Reject AI results below the completion threshold
 * - Persist successful objective completion
 * - Detect mission completion from the persisted Room state
 * - Trigger final mission analytics when the mission finishes
 *
 * Mission completion persistence is delegated to:
 *
 * CompleteMissionObjectiveWithAnalyticsUseCase
 *
 * This keeps analytics orchestration out of the evaluator.
 */
class EvaluateMissionMessageUseCase
@Inject constructor(
    private val aiGateway:
    MissionObjectiveEvaluationAiGateway,
    private val completeMissionObjectiveWithAnalytics:
    CompleteMissionObjectiveWithAnalyticsUseCase
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
                    MissionMessageEvaluationOutcome
                        .SKIPPED
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
                            MissionMessageEvaluationOutcome
                                .SKIPPED
                    )
                }

        val currentObjective =
            currentObjectiveProgress.objective

        val progressPercentageBefore =
            calculateCurrentProgressPercentage(
                mission =
                    mission
            )

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
                append(
                    "Evaluating mission objective. "
                )
                append(
                    "missionId="
                )
                append(
                    mission.id
                )
                append(
                    ", objectiveId="
                )
                append(
                    currentObjective.id
                )
                append(
                    ", objectiveTitle="
                )
                append(
                    currentObjective.title
                )
                append(
                    ", recentTurns="
                )
                append(
                    recentConversation.size
                )
                append(
                    ", currentProgress="
                )
                append(
                    progressPercentageBefore
                )
                append(
                    "%"
                )
            }
        )

        val evaluationResult =
            try {
                aiGateway.evaluateMessage(
                    learnerMessage =
                        learnerMessage
                            .content
                            .trim(),
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
                     * Deliberately evaluate only the current
                     * objective.
                     *
                     * One learner response therefore cannot
                     * complete several future mission
                     * objectives simultaneously.
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
                    progressPercentageBefore =
                        progressPercentageBefore,
                    projectedProgressPercentage =
                        progressPercentageBefore,
                    outcome =
                        MissionMessageEvaluationOutcome
                            .EVALUATION_FAILED
                )
            }

        /*
         * Qwen can theoretically return more than one result.
         *
         * Only results for the objective we explicitly asked
         * it to evaluate are accepted.
         */
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
                        MINIMUM_CONFIDENCE,
                    maximumValue =
                        MAXIMUM_CONFIDENCE
                )

        Log.d(
            USE_CASE_TAG,
            buildString {
                append(
                    "Mission AI evaluation received. "
                )
                append(
                    "objectiveId="
                )
                append(
                    currentObjective.id
                )
                append(
                    ", matchingResults="
                )
                append(
                    matchingEvaluations.size
                )
                append(
                    ", highestConfidence="
                )
                append(
                    highestConfidence
                )
            }
        )

        /*
         * No matching result means there is not enough
         * evidence that the current objective was completed.
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
         * A matching result may still have insufficient
         * confidence.
         *
         * Results between the partial and completion
         * thresholds are treated as partial attempts.
         */
        if (
            highestConfidence == null ||
            highestConfidence <
            MINIMUM_COMPLETION_CONFIDENCE
        ) {
            return createNotCompletedSummary(
                mission =
                    mission,
                currentObjective =
                    currentObjective,
                confidence =
                    highestConfidence
            )
        }

        /*
         * The AI considers the current objective completed.
         *
         * Do not calculate whether the mission will finish
         * from the old in-memory mission here.
         *
         * Persist the objective first and let the orchestration
         * use case reload the authoritative Room state.
         */
        return try {
            val completionResult =
                completeMissionObjectiveWithAnalytics(
                    conversationMissionId =
                        mission.id,
                    objectiveId =
                        currentObjective.id,
                    messageId =
                        learnerMessage.id,
                    confidence =
                        highestConfidence
                )

            when (completionResult) {
                is ObjectiveCompletionResult
                .ObjectiveCompleted -> {

                    Log.i(
                        USE_CASE_TAG,
                        buildString {
                            append(
                                "Mission objective completed. "
                            )
                            append(
                                "missionId="
                            )
                            append(
                                mission.id
                            )
                            append(
                                ", objectiveId="
                            )
                            append(
                                currentObjective.id
                            )
                            append(
                                ", objectiveTitle="
                            )
                            append(
                                currentObjective.title
                            )
                            append(
                                ", confidence="
                            )
                            append(
                                highestConfidence
                            )
                            append(
                                ", persistedProgress="
                            )
                            append(
                                completionResult
                                    .progressPercentage
                            )
                            append(
                                "%"
                            )
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
                            highestConfidence,
                        progressPercentageBefore =
                            progressPercentageBefore,
                        projectedProgressPercentage =
                            completionResult
                                .progressPercentage,
                        missionCompleted =
                            false,
                        outcome =
                            MissionMessageEvaluationOutcome
                                .OBJECTIVE_COMPLETED
                    )
                }

                is ObjectiveCompletionResult
                .MissionCompleted -> {

                    Log.i(
                        USE_CASE_TAG,
                        buildString {
                            append(
                                "Mission completed and analytics "
                            )
                            append(
                                "were finalized. "
                            )
                            append(
                                "missionId="
                            )
                            append(
                                mission.id
                            )
                            append(
                                ", objectiveId="
                            )
                            append(
                                currentObjective.id
                            )
                            append(
                                ", confidence="
                            )
                            append(
                                highestConfidence
                            )
                            append(
                                ", overallScore="
                            )
                            append(
                                completionResult
                                    .analytics
                                    .overallScore
                            )
                            append(
                                ", independenceScore="
                            )
                            append(
                                completionResult
                                    .analytics
                                    .independenceScore
                            )
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
                            highestConfidence,
                        progressPercentageBefore =
                            progressPercentageBefore,
                        projectedProgressPercentage =
                            MAXIMUM_PROGRESS_PERCENTAGE,
                        missionCompleted =
                            true,
                        analytics =
                            completionResult.analytics,
                        outcome =
                            MissionMessageEvaluationOutcome
                                .MISSION_COMPLETED
                    )
                }
            }
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
                    append(
                        "missionId="
                    )
                    append(
                        mission.id
                    )
                    append(
                        ", objectiveId="
                    )
                    append(
                        currentObjective.id
                    )
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
                    highestConfidence,
                progressPercentageBefore =
                    progressPercentageBefore,
                projectedProgressPercentage =
                    progressPercentageBefore,
                missionCompleted =
                    false,
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
        recentMessages:
        List<ChatMessage>,
        learnerMessage:
        ChatMessage
    ): List<MissionEvaluationTurn> {
        val contextMessages =
            recentMessages
                .asSequence()
                .filter { message ->
                    message.content.isNotBlank()
                }
                /*
                 * Do not include messages persisted after the
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
                        MissionEvaluationSpeaker
                            .LEARNER
                    } else {
                        MissionEvaluationSpeaker
                            .ASSISTANT
                    },
                content =
                    message
                        .content
                        .trim()
            )
        }
    }

    private fun createNotCompletedSummary(
        mission: ConversationMission,
        currentObjective:
        MissionObjective,
        confidence: Float?
    ): MissionMessageEvaluationSummary {
        val safeConfidence =
            confidence?.coerceIn(
                minimumValue =
                    MINIMUM_CONFIDENCE,
                maximumValue =
                    MAXIMUM_CONFIDENCE
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
                append(
                    "Mission objective not completed. "
                )
                append(
                    "objectiveId="
                )
                append(
                    currentObjective.id
                )
                append(
                    ", confidence="
                )
                append(
                    safeConfidence
                )
                append(
                    ", outcome="
                )
                append(
                    outcome
                )
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
        mission:
        ConversationMission
    ) =
        mission.currentObjective
            ?.takeIf { progress ->
                !progress.isCompleted
            }
            ?: mission
                .objectiveProgress
                .sortedBy { progress ->
                    progress
                        .objective
                        .sequenceNumber
                }
                .firstOrNull { progress ->
                    !progress.isCompleted
                }

    /*
     * This helper is retained for the pre-evaluation view of
     * progress.
     *
     * After successful persistence, the updated percentage
     * comes from the Room-backed ConversationMission instead.
     */
    private fun calculateCurrentProgressPercentage(
        mission:
        ConversationMission
    ): Int {
        val totalObjectiveCount =
            mission.objectiveProgress.size

        val completedObjectiveCount =
            mission.objectiveProgress
                .count { progress ->
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
                    MINIMUM_PROGRESS_PERCENTAGE,
                maximumValue =
                    MAXIMUM_PROGRESS_PERCENTAGE
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
         * A confidence of 0.78 or greater completes the
         * current objective.
         */
        const val MINIMUM_COMPLETION_CONFIDENCE =
            0.78f

        /*
         * Matching results between 0.55 and 0.78 count as
         * meaningful partial attempts.
         */
        const val MINIMUM_PARTIAL_ATTEMPT_CONFIDENCE =
            0.55f

        const val MAXIMUM_RECENT_MESSAGE_COUNT =
            12

        const val PERCENTAGE_MULTIPLIER =
            100f

        const val MINIMUM_PROGRESS_PERCENTAGE =
            0

        const val MAXIMUM_PROGRESS_PERCENTAGE =
            100

        const val MINIMUM_CONFIDENCE =
            0f

        const val MAXIMUM_CONFIDENCE =
            1f
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

/*
 * Result consumed by the conversation presentation pipeline.
 *
 * analytics is populated only when this learner message
 * completed the complete mission.
 */
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

    /*
     * Available immediately when this evaluation completed
     * the entire mission.
     *
     * The analytics have already been persisted in Room
     * before this summary is returned.
     */
    val analytics:
    MissionAnalytics? =
        null,

    val outcome:
    MissionMessageEvaluationOutcome =
        MissionMessageEvaluationOutcome
            .SKIPPED
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

    val hasFinalAnalytics: Boolean
        get() =
            analytics != null
}