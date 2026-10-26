package com.julesmeister.deutschcraft.feature.chat.presentation

import android.util.Log
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import com.julesmeister.deutschcraft.feature.mission.domain.usecase.EvaluateMissionMessageUseCase
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class ChatMissionEvaluationCoordinator @Inject constructor(
    private val evaluateMissionMessage:
    EvaluateMissionMessageUseCase
) {

    suspend fun evaluate(
        mission: ConversationMission?,
        learnerMessage: ChatMessage,
        recentMessages: List<ChatMessage>,
        cefrLevel: CefrLevel
    ) {
        if (mission == null) {
            return
        }

        try {
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
        }
    }

    private companion object {
        const val COORDINATOR_TAG =
            "ChatMissionEvaluation"
    }
}