package com.julesmeister.deutschcraft.feature.chat.presentation

import android.util.Log
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import com.julesmeister.deutschcraft.feature.mission.domain.usecase.EvaluateMissionMessageUseCase
import com.julesmeister.deutschcraft.feature.mission.domain.usecase.MissionMessageEvaluationOutcome
import com.julesmeister.deutschcraft.feature.mission.domain.usecase.MissionMessageEvaluationSummary
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class ChatMissionEvaluationCoordinator
@Inject constructor(
    private val evaluateMissionMessage:
    EvaluateMissionMessageUseCase
) {

    suspend fun evaluate(
        mission: ConversationMission?,
        learnerMessage: ChatMessage,
        recentMessages: List<ChatMessage>,
        cefrLevel: CefrLevel
    ): MissionMessageEvaluationSummary {
        if (mission == null) {
            return MissionMessageEvaluationSummary(
                outcome =
                    MissionMessageEvaluationOutcome.SKIPPED
            )
        }

        return try {
            val result =
                evaluateMissionMessage(
                    mission =
                        mission,
                    learnerMessage =
                        learnerMessage,
                    recentMessages =
                        recentMessages,
                    cefrLevel =
                        cefrLevel
                )

            logResult(
                mission =
                    mission,
                result =
                    result
            )

            result
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.e(
                COORDINATOR_TAG,
                "Mission evaluation failed.",
                throwable
            )

            MissionMessageEvaluationSummary(
                outcome =
                    MissionMessageEvaluationOutcome
                        .EVALUATION_FAILED
            )
        }
    }

    private fun logResult(
        mission: ConversationMission,
        result: MissionMessageEvaluationSummary
    ) {
        Log.d(
            COORDINATOR_TAG,
            buildString {
                append("Mission evaluation completed. ")
                append("missionId=")
                append(mission.id)
                append(", outcome=")
                append(result.outcome)
                append(", currentObjective=")
                append(result.currentObjectiveTitle)
                append(", completedObjective=")
                append(result.completedObjectiveTitle)
                append(", confidence=")
                append(result.highestConfidence)
                append(", progress=")
                append(result.progressPercentageBefore)
                append("% -> ")
                append(result.projectedProgressPercentage)
                append("%")
                append(", missionCompleted=")
                append(result.completedMission)
            }
        )
    }

    private companion object {
        const val COORDINATOR_TAG =
            "ChatMissionEvaluation"
    }
}