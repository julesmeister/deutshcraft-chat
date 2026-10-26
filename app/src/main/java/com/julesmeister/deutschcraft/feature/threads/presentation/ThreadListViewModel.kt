package com.julesmeister.deutschcraft.feature.threads.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    /*
     * Starts the recommendation currently displayed in the
     * top mission cell.
     *
     * The recommendation is normally started by tapping the
     * cell. Swiping left only displays another suggestion.
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
     * Advances to the next recommendation in the prefetched
     * queue.
     *
     * When the queue has been filled successfully, this does
     * not make a Qwen request. It removes the current item and
     * immediately returns the next cached recommendation.
     *
     * Afterward, background prefetch replenishes the queue to
     * five items.
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
                val recommendation =
                    missionRecommendationCoordinator
                        .loadRecommendation(
                            activeThreads =
                                activeThreads,
                            forceRefresh =
                                true
                        )

                _uiState.update { state ->
                    state.copy(
                        missionRecommendation =
                            recommendation,
                        isLoadingMissionRecommendation =
                            false,
                        errorMessage =
                            if (recommendation == null) {
                                DIFFERENT_MISSION_UNAVAILABLE
                            } else {
                                null
                            }
                    )
                }

                /*
                 * One queue item was consumed.
                 *
                 * Refill the hidden queue back to five without
                 * blocking the newly displayed suggestion.
                 */
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
                /*
                 * The existing recommendation remains in state
                 * if queue advancement fails.
                 */
                _uiState.update { state ->
                    state.copy(
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

        val visibleRecommendation =
            _uiState
                .value
                .missionRecommendation

        if (visibleRecommendation != null) {
            /*
             * A recommendation was restored from the queue.
             *
             * Keep it visible and fill any missing hidden queue
             * items in the background.
             */
            prefetchMissionRecommendations(
                activeThreads =
                    snapshot.activeThreads
            )

            return
        }

        /*
         * No visible recommendation exists.
         *
         * Load only the first recommendation now. The first
         * valid result is shown immediately. Recommendations
         * two through five are generated afterward.
         */
        loadFirstMissionRecommendation(
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

    /*
     * Loads the initial visible recommendation.
     *
     * The learner does not wait for the complete five-item
     * queue. The first recommendation is displayed as soon as
     * it is ready.
     */
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

                /*
                 * Recommendation #1 is now visible.
                 *
                 * Continue generating recommendations #2 to #5
                 * in the background.
                 */
                prefetchMissionRecommendations(
                    activeThreads =
                        activeThreads
                )
            },
            onFailure = {
                _uiState.update { state ->
                    state.recommendationLoadingFailed()
                }

                /*
                 * The existing initial loader may fail because
                 * of a temporary generation or validation
                 * issue.
                 *
                 * Let the queue prefetcher continue trying. Its
                 * first accepted item will be shown immediately.
                 */
                prefetchMissionRecommendations(
                    activeThreads =
                        activeThreads
                )
            }
        )
    }

    /*
     * Fills the recommendation queue to five.
     *
     * If no recommendation is currently displayed, the first
     * accepted prefetched recommendation is applied to the UI
     * immediately. Remaining recommendations stay hidden.
     */
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
                onFirstRecommendationAvailable = {
                        recommendation ->

                    val currentState =
                        _uiState.value

                    if (
                        currentState
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
                    /*
                     * Prefetch is supplementary.
                     *
                     * If one recommendation is already visible,
                     * background failure must not show a snackbar
                     * or remove the visible recommendation.
                     */
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

    /*
     * Reconstructs the current active thread list from the
     * already-built sections.
     *
     * This gives recommendation selection the most recently
     * observed CEFR level when the learner swipes.
     */
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
        const val DEFAULT_CONVERSATION_TITLE =
            "Unterhaltung"

        const val DIFFERENT_MISSION_ERROR =
            "Eine andere Mission konnte nicht geladen werden."

        const val DIFFERENT_MISSION_UNAVAILABLE =
            "Im Moment ist keine andere Mission verfügbar."
    }
}