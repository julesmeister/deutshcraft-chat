package com.julesmeister.deutschcraft.feature.threads.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julesmeister.deutschcraft.feature.threads.domain.ConversationThread
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
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
class ThreadListViewModel @Inject constructor(
    private val threadController:
    ThreadListThreadController,
    private val missionController:
    ThreadListMissionController,
    private val threadSectionBuilder:
    ThreadSectionBuilder
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            ThreadListUiState()
        )

    val uiState: StateFlow<ThreadListUiState> =
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
        restoreMissionRecommendation()
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

    fun startRecommendedMission() {
        val currentState =
            _uiState.value

        val recommendation =
            currentState.missionRecommendation
                ?: return

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

        loadMissionRecommendationIfNeeded(
            activeThreads =
                snapshot.activeThreads
        )
    }

    private fun restoreMissionRecommendation() {
        val recommendation =
            missionController
                .restoreCachedRecommendation()

        _uiState.update { state ->
            state.recommendationRestored(
                recommendation =
                    recommendation
            )
        }
    }

    private fun loadMissionRecommendationIfNeeded(
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
            },
            onFailure = {
                _uiState.update { state ->
                    state.recommendationLoadingFailed()
                }
            }
        )
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
        super.onCleared()
    }

    private companion object {
        const val DEFAULT_CONVERSATION_TITLE =
            "Unterhaltung"
    }
}