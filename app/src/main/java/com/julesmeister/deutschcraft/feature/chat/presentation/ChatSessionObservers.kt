package com.julesmeister.deutschcraft.feature.chat.presentation

import android.util.Log
import com.julesmeister.deutschcraft.feature.besser.domain.usecase.ObserveConversationBesserChallengesUseCase
import com.julesmeister.deutschcraft.feature.besser.presentation.BesserChallengeUiModel
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.usecase.ObserveActiveMissionUseCase
import com.julesmeister.deutschcraft.feature.chat.domain.usecase.ObserveChatMessagesUseCase
import com.julesmeister.deutschcraft.feature.chat.domain.usecase.ObserveCurrentStreakUseCase
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ChatSessionObservers
@Inject constructor(
    private val observeChatMessages:
    ObserveChatMessagesUseCase,

    private val observeCurrentStreak:
    ObserveCurrentStreakUseCase,

    private val observeActiveMission:
    ObserveActiveMissionUseCase,

    private val observeBesserChallenges:
    ObserveConversationBesserChallengesUseCase,

    private val missionTracker:
    ChatMissionTracker
) {

    private var messageObservationJob: Job? =
        null

    private var streakObservationJob: Job? =
        null

    private var missionObservationJob: Job? =
        null

    private var besserObservationJob: Job? =
        null

    fun observeConversation(
        conversationId: String,
        scope: CoroutineScope,
        onMessagesChanged:
            (ChatMessagesUpdate) -> Unit,
        onMissionChanged:
            (ChatMissionUpdate) -> Unit,
        onBesserChallengesChanged:
            (Map<String, BesserChallengeUiModel>) -> Unit,
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

        observeBesser(
            conversationId =
                conversationId,
            scope =
                scope,
            onBesserChallengesChanged =
                onBesserChallengesChanged
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
        besserObservationJob?.cancel()

        messageObservationJob =
            null

        streakObservationJob =
            null

        missionObservationJob =
            null

        besserObservationJob =
            null

        missionTracker.reset()
    }

    /*
     * =======================================================
     * Messages
     * =======================================================
     */

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

    /*
     * =======================================================
     * Mission
     * =======================================================
     */

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

    /*
     * =======================================================
     * Besser
     * =======================================================
     *
     * Observe AVAILABLE / ACCEPTED challenges stored in Room.
     *
     * The use case already returns:
     *
     *     messageId -> BesserChallengeUiModel
     *
     * so presentation does not need to search challenge lists.
     */
    private fun observeBesser(
        conversationId: String,
        scope: CoroutineScope,
        onBesserChallengesChanged:
            (Map<String, BesserChallengeUiModel>) -> Unit
    ) {
        besserObservationJob?.cancel()

        besserObservationJob =
            scope.launch {
                try {
                    observeBesserChallenges(
                        conversationId =
                            conversationId
                    ).collect { challenges ->
                        onBesserChallengesChanged(
                            challenges
                        )
                    }
                } catch (
                    cancellationException:
                    CancellationException
                ) {
                    throw cancellationException
                } catch (throwable: Throwable) {
                    /*
                     * Chat itself remains usable if Besser
                     * observation fails.
                     */
                    Log.e(
                        OBSERVERS_TAG,
                        "Besser challenge observation failed.",
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