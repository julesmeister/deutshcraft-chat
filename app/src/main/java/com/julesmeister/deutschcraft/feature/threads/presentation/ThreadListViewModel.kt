package com.julesmeister.deutschcraft.feature.threads.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julesmeister.deutschcraft.feature.chat.domain.usecase.ObserveCurrentStreakUseCase
import com.julesmeister.deutschcraft.feature.threads.domain.ConversationThread
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ThreadListViewModel
@Inject constructor(
    private val threadController:
    ThreadListThreadController,
    private val missionController:
    ThreadListMissionController,
    private val missionRecommendationCoordinator:
    ThreadMissionRecommendationCoordinator,
    private val threadSectionBuilder:
    ThreadSectionBuilder,

    /*
     * Same streak source previously used by the chat top bar.
     * It is Room-backed, so the value updates automatically
     * whenever a new activity day is recorded.
     */
    private val observeCurrentStreak:
    ObserveCurrentStreakUseCase
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            ThreadListUiState()
        )

    val uiState:
            StateFlow<ThreadListUiState> =
        _uiState.asStateFlow()

    private val navigationEventChannel =
        Channel<ThreadListNavigationEvent>(
            capacity =
                Channel.BUFFERED
        )

    val navigationEvents:
            Flow<ThreadListNavigationEvent> =
        navigationEventChannel
            .receiveAsFlow()

    private val _archiveSnackbarEvents =
        MutableSharedFlow<ArchiveSnackbarEvent>(
            extraBufferCapacity =
                1
        )

    val archiveSnackbarEvents =
        _archiveSnackbarEvents
            .asSharedFlow()

    init {
        observeStreak()
        observeThreads()
    }

    fun createThread() {
        val currentState =
            _uiState.value

        if (
            currentState.isCreatingThread ||
            currentState.isStartingRecommendedMission
        ) {
            return
        }

        _uiState.update { state ->
            state.threadCreationStarted()
        }

        threadController.createConversation(
            scope =
                viewModelScope,
            onSuccess = { conversation ->
                navigateToConversation(
                    conversationId =
                        conversation.id
                )
            },
            onFailure =
                ::showError,
            onFinished = {
                _uiState.update { state ->
                    state.threadCreationFinished()
                }
            }
        )
    }

    /*
     * Starts the recommendation currently displayed in the
     * top mission cell.
     */
    fun startRecommendedMission() {
        val currentState =
            _uiState.value

        val recommendation =
            currentState.missionRecommendation
                ?: return

        if (
            currentState.isCreatingThread ||
            currentState.isStartingRecommendedMission ||
            currentState.isLoadingMissionRecommendation
        ) {
            return
        }

        missionController.startRecommendation(
            recommendation =
                recommendation,
            currentState =
                currentState,
            scope =
                viewModelScope,
            onStarted = {
                _uiState.update { state ->
                    state.recommendedMissionStarted()
                }
            },
            onSuccess = { conversation ->
                _uiState.update { state ->
                    state.recommendedMissionSucceeded()
                }

                navigateToConversation(
                    conversationId =
                        conversation.id
                )
            },
            onFailure = { message ->
                _uiState.update { state ->
                    state.recommendedMissionFailed(
                        message =
                            message
                    )
                }
            }
        )
    }

    /*
     * Marks the visible recommendation SKIPPED and loads the
     * next one. After the last READY recommendation, the use
     * case wraps around to the oldest skipped one.
     */
    fun showAnotherMissionRecommendation() {
        val currentState =
            _uiState.value

        if (
            currentState.isLoadingMissionRecommendation ||
            currentState.isStartingRecommendedMission ||
            currentState.isCreatingThread
        ) {
            return
        }

        val previousRecommendation =
            currentState.missionRecommendation

        val activeThreads =
            activeThreadsFromState(
                state =
                    currentState
            )

        _uiState.update { state ->
            state.copy(
                isLoadingMissionRecommendation =
                    true,
                errorMessage =
                    null
            )
        }

        viewModelScope.launch {
            try {
                val nextRecommendation =
                    missionRecommendationCoordinator
                        .loadRecommendation(
                            activeThreads =
                                activeThreads,
                            forceRefresh =
                                true
                        )

                val displayedRecommendation =
                    nextRecommendation
                        ?: previousRecommendation

                _uiState.update { state ->
                    state.copy(
                        missionRecommendation =
                            displayedRecommendation,
                        isLoadingMissionRecommendation =
                            false,
                        errorMessage =
                            if (
                                nextRecommendation ==
                                null
                            ) {
                                DIFFERENT_MISSION_UNAVAILABLE
                            } else {
                                null
                            }
                    )
                }

                prefetchMissionRecommendations(
                    activeThreads =
                        activeThreads
                )
            } catch (
                cancellationException:
                CancellationException
            ) {
                throw cancellationException
            } catch (throwable: Throwable) {
                _uiState.update { state ->
                    state.copy(
                        missionRecommendation =
                            previousRecommendation,
                        isLoadingMissionRecommendation =
                            false,
                        errorMessage =
                            DIFFERENT_MISSION_ERROR
                    )
                }
            }
        }
    }

    fun archiveThread(
        conversationId: String
    ) {
        val conversationTitle =
            findActiveThread(
                conversationId =
                    conversationId
            )?.title
                ?: DEFAULT_CONVERSATION_TITLE

        threadController.archiveConversation(
            conversationId =
                conversationId,
            conversationTitle =
                conversationTitle,
            scope =
                viewModelScope,
            onSuccess = { event ->
                _archiveSnackbarEvents.emit(
                    event
                )
            },
            onFailure =
                ::showError
        )
    }

    fun restoreThread(
        conversationId: String
    ) {
        threadController.restoreConversation(
            conversationId =
                conversationId,
            scope =
                viewModelScope,
            onFailure =
                ::showError
        )
    }

    fun deleteThread(
        conversationId: String
    ) {
        threadController.deleteConversation(
            conversationId =
                conversationId,
            scope =
                viewModelScope,
            onFailure =
                ::showError
        )
    }

    fun dismissError() {
        _uiState.update { state ->
            state.withoutError()
        }
    }

    /*
     * Keeps ThreadListUiState.currentStreak in sync with the
     * persisted learning streak.
     *
     * A failure here only affects the streak badge; the thread
     * list keeps working.
     */
    private fun observeStreak() {
        viewModelScope.launch {
            try {
                observeCurrentStreak()
                    .collect { streakDays ->
                        _uiState.update { state ->
                            state.copy(
                                currentStreak =
                                    streakDays
                                        .coerceAtLeast(
                                            0
                                        )
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
                    VIEW_MODEL_TAG,
                    "Streak observation failed.",
                    throwable
                )
            }
        }
    }

    private fun observeThreads() {
        threadController.startObserving(
            scope =
                viewModelScope,
            onSnapshot =
                ::handleThreadSnapshot,
            onFailure = { message ->
                _uiState.update { state ->
                    state.threadLoadingFailed(
                        message =
                            message
                    )
                }
            }
        )
    }

    private fun handleThreadSnapshot(
        snapshot: ThreadListsSnapshot
    ) {
        _uiState.update { state ->
            state.withThreadLists(
                sections =
                    threadSectionBuilder.build(
                        threads =
                            snapshot.activeThreads
                    ),
                archivedThreads =
                    snapshot.archivedThreads
            )
        }

        val currentState =
            _uiState.value

        if (
            currentState.missionRecommendation !=
            null
        ) {
            prefetchMissionRecommendations(
                activeThreads =
                    snapshot.activeThreads
            )

            return
        }

        if (
            currentState
                .isLoadingMissionRecommendation
        ) {
            return
        }

        loadFirstMissionRecommendation(
            activeThreads =
                snapshot.activeThreads
        )
    }

    private fun loadFirstMissionRecommendation(
        activeThreads:
        List<ConversationThread>
    ) {
        missionController.loadRecommendationIfNeeded(
            activeThreads =
                activeThreads,
            currentState =
                _uiState.value,
            scope =
                viewModelScope,
            onStarted = {
                _uiState.update { state ->
                    state.recommendationLoadingStarted()
                }
            },
            onSuccess = { recommendation ->
                _uiState.update { state ->
                    state.recommendationLoadingFinished(
                        recommendation =
                            recommendation
                    )
                }

                prefetchMissionRecommendations(
                    activeThreads =
                        activeThreads
                )
            },
            onFailure = {
                _uiState.update { state ->
                    state.recommendationLoadingFailed()
                }

                prefetchMissionRecommendations(
                    activeThreads =
                        activeThreads
                )
            }
        )
    }

    private fun prefetchMissionRecommendations(
        activeThreads:
        List<ConversationThread>
    ) {
        missionRecommendationCoordinator
            .prefetchRecommendations(
                activeThreads =
                    activeThreads,
                scope =
                    viewModelScope,
                onFirstRecommendationAvailable = { recommendation ->
                    if (
                        _uiState
                            .value
                            .missionRecommendation ==
                        null
                    ) {
                        _uiState.update { state ->
                            state.copy(
                                missionRecommendation =
                                    recommendation,
                                isLoadingMissionRecommendation =
                                    false,
                                errorMessage =
                                    null
                            )
                        }
                    }
                },
                onFailure = {
                    if (
                        _uiState
                            .value
                            .missionRecommendation ==
                        null
                    ) {
                        _uiState.update { state ->
                            state.copy(
                                isLoadingMissionRecommendation =
                                    false
                            )
                        }
                    }
                }
            )
    }

    private fun activeThreadsFromState(
        state: ThreadListUiState
    ): List<ConversationThread> {
        return state
            .sections
            .asSequence()
            .flatMap { section ->
                section
                    .threads
                    .asSequence()
            }
            .toList()
    }

    private suspend fun navigateToConversation(
        conversationId: String
    ) {
        navigationEventChannel.send(
            ThreadListNavigationEvent
                .OpenConversation(
                    conversationId =
                        conversationId
                )
        )
    }

    private fun findActiveThread(
        conversationId: String
    ): ConversationThread? {
        return _uiState
            .value
            .sections
            .asSequence()
            .flatMap { section ->
                section
                    .threads
                    .asSequence()
            }
            .firstOrNull { thread ->
                thread.id ==
                        conversationId
            }
    }

    private fun showError(
        message: String
    ) {
        _uiState.update { state ->
            state.withError(
                message =
                    message
            )
        }
    }

    override fun onCleared() {
        missionController.cancel()

        missionRecommendationCoordinator
            .cancelPrefetch()

        super.onCleared()
    }

    private companion object {
        const val VIEW_MODEL_TAG =
            "ThreadListViewModel"

        const val DEFAULT_CONVERSATION_TITLE =
            "Unterhaltung"

        const val DIFFERENT_MISSION_ERROR =
            "Eine andere Mission konnte nicht geladen werden."

        const val DIFFERENT_MISSION_UNAVAILABLE =
            "Im Moment ist keine andere Mission verfügbar."
    }
}