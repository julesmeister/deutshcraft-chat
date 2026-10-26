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
import kotlinx.coroutines.CancellationException

class EvaluateMissionMessageUseCase @Inject constructor(
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
        if (
            mission.status != MissionStatus.ACTIVE ||
            learnerMessage.sender != MessageSender.USER ||
            learnerMessage.content.isBlank()
        ) {
            return MissionMessageEvaluationSummary()
        }

        val currentObjective =
            findCurrentObjective(
                mission =
                    mission
            )
                ?: return MissionMessageEvaluationSummary()

        val recentConversation =
            recentMessages
                .asSequence()
                .filter { message ->
                    message.content.isNotBlank()
                }
                .takeLastAsList(
                    maximumCount =
                        MAXIMUM_RECENT_MESSAGE_COUNT
                )
                .map { message ->
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
                            message.content
                    )
                }

        Log.d(
            USE_CASE_TAG,
            "Evaluating objective " +
                    "'${currentObjective.title}' using " +
                    "${recentConversation.size} recent turns."
        )

        val evaluationResult =
            try {
                aiGateway.evaluateMessage(
                    learnerMessage =
                        learnerMessage.content,
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
                    "Mission evaluation failed.",
                    throwable
                )

                return MissionMessageEvaluationSummary(
                    evaluatedObjectiveCount =
                        1
                )
            }

        Log.d(
            USE_CASE_TAG,
            "AI result=" +
                    evaluationResult.completedObjectives
        )

        val acceptedEvaluation =
            evaluationResult
                .completedObjectives
                .asSequence()
                .filter { evaluation ->
                    evaluation.objectiveId ==
                            currentObjective.id
                }
                .filter { evaluation ->
                    evaluation.confidence >=
                            MINIMUM_COMPLETION_CONFIDENCE
                }
                .maxByOrNull { evaluation ->
                    evaluation.confidence
                }
                ?: return MissionMessageEvaluationSummary(
                    evaluatedObjectiveCount =
                        1
                )

        return try {
            missionRepository.completeObjective(
                conversationMissionId =
                    mission.id,
                objectiveId =
                    acceptedEvaluation.objectiveId,
                messageId =
                    learnerMessage.id,
                confidence =
                    acceptedEvaluation.confidence
            )

            Log.i(
                USE_CASE_TAG,
                "Completed objective " +
                        "'${currentObjective.title}' using " +
                        "recent conversation context."
            )

            MissionMessageEvaluationSummary(
                evaluatedObjectiveCount =
                    1,
                completedObjectiveIds =
                    listOf(
                        acceptedEvaluation.objectiveId
                    ),
                highestConfidence =
                    acceptedEvaluation.confidence
            )
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.e(
                USE_CASE_TAG,
                "Could not save completed objective " +
                        acceptedEvaluation.objectiveId,
                throwable
            )

            MissionMessageEvaluationSummary(
                evaluatedObjectiveCount =
                    1,
                highestConfidence =
                    acceptedEvaluation.confidence
            )
        }
    }

    private fun findCurrentObjective(
        mission: ConversationMission
    ): MissionObjective? {
        val selectedObjective =
            mission.currentObjective
                ?.takeIf { progress ->
                    !progress.isCompleted
                }
                ?.objective

        if (selectedObjective != null) {
            return selectedObjective
        }

        return mission
            .objectiveProgress
            .firstOrNull { progress ->
                !progress.isCompleted
            }
            ?.objective
    }

    private fun <T> Sequence<T>.takeLastAsList(
        maximumCount: Int
    ): List<T> {
        return toList()
            .takeLast(
                maximumCount
            )
    }

    private companion object {
        const val USE_CASE_TAG =
            "EvaluateMissionMessage"

        const val MINIMUM_COMPLETION_CONFIDENCE =
            0.88f

        const val MAXIMUM_RECENT_MESSAGE_COUNT =
            12
    }
}

data class MissionMessageEvaluationSummary(
    val evaluatedObjectiveCount: Int =
        0,
    val completedObjectiveIds:
    List<String> =
        emptyList(),
    val highestConfidence: Float? =
        null
) {

    val completedObjectiveCount: Int
        get() =
            completedObjectiveIds.size

    val completedAnyObjective: Boolean
        get() =
            completedObjectiveIds.isNotEmpty()
}