package com.julesmeister.deutschcraft.feature.chat.presentation

import android.util.Log
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.usecase.ObserveActiveMissionUseCase
import com.julesmeister.deutschcraft.feature.chat.domain.usecase.ObserveChatMessagesUseCase
import com.julesmeister.deutschcraft.feature.chat.domain.usecase.ObserveCurrentStreakUseCase
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ChatSessionObservers @Inject constructor(
    private val observeChatMessages:
    ObserveChatMessagesUseCase,
    private val observeCurrentStreak:
    ObserveCurrentStreakUseCase,
    private val observeActiveMission:
    ObserveActiveMissionUseCase,
    private val missionTracker:
    ChatMissionTracker
) {

    private var messageObservationJob: Job? =
        null

    private var streakObservationJob: Job? =
        null

    private var missionObservationJob: Job? =
        null

    fun observeConversation(
        conversationId: String,
        scope: CoroutineScope,
        onMessagesChanged:
            (ChatMessagesUpdate) -> Unit,
        onMissionChanged:
            (ChatMissionUpdate) -> Unit,
        onMessageObservationFailed:
            () -> Unit
    ) {
        observeMessages(
            conversationId =
                conversationId,
            scope =
                scope,
            onMessagesChanged =
                onMessagesChanged,
            onObservationFailed =
                onMessageObservationFailed
        )

        observeMission(
            conversationId =
                conversationId,
            scope =
                scope,
            onMissionChanged =
                onMissionChanged
        )
    }

    fun observeStreak(
        scope: CoroutineScope,
        onStreakChanged: (Int) -> Unit
    ) {
        streakObservationJob?.cancel()

        streakObservationJob =
            scope.launch {
                try {
                    observeCurrentStreak()
                        .collect { streakDays ->
                            onStreakChanged(
                                streakDays
                            )
                        }
                } catch (
                    cancellationException:
                    CancellationException
                ) {
                    throw cancellationException
                } catch (throwable: Throwable) {
                    Log.e(
                        OBSERVERS_TAG,
                        "Streak observation failed.",
                        throwable
                    )
                }
            }
    }

    fun stop() {
        messageObservationJob?.cancel()
        streakObservationJob?.cancel()
        missionObservationJob?.cancel()

        messageObservationJob = null
        streakObservationJob = null
        missionObservationJob = null

        missionTracker.reset()
    }

    private fun observeMessages(
        conversationId: String,
        scope: CoroutineScope,
        onMessagesChanged:
            (ChatMessagesUpdate) -> Unit,
        onObservationFailed:
            () -> Unit
    ) {
        messageObservationJob?.cancel()

        messageObservationJob =
            scope.launch {
                try {
                    observeChatMessages(
                        conversationId =
                            conversationId
                    ).collect { messages ->
                        onMessagesChanged(
                            ChatMessagesUpdate(
                                messages =
                                    messages
                            )
                        )
                    }
                } catch (
                    cancellationException:
                    CancellationException
                ) {
                    throw cancellationException
                } catch (throwable: Throwable) {
                    Log.e(
                        OBSERVERS_TAG,
                        "Message observation failed.",
                        throwable
                    )

                    onObservationFailed()
                }
            }
    }

    private fun observeMission(
        conversationId: String,
        scope: CoroutineScope,
        onMissionChanged:
            (ChatMissionUpdate) -> Unit
    ) {
        missionObservationJob?.cancel()

        missionTracker.reset()

        missionObservationJob =
            scope.launch {
                try {
                    observeActiveMission(
                        conversationId =
                            conversationId
                    ).collect { mission ->
                        val update =
                            missionTracker.createUpdate(
                                mission =
                                    mission
                            )

                        onMissionChanged(
                            update
                        )
                    }
                } catch (
                    cancellationException:
                    CancellationException
                ) {
                    throw cancellationException
                } catch (throwable: Throwable) {
                    Log.e(
                        OBSERVERS_TAG,
                        "Mission observation failed.",
                        throwable
                    )
                }
            }
    }

    private companion object {
        const val OBSERVERS_TAG =
            "ChatSessionObservers"
    }
}

data class ChatMessagesUpdate(
    val messages: List<ChatMessage>
)