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

    /*
     * Mission recommendations are no longer synchronously
     * restored from MissionThreadRecommendationCache.
     *
     * The first thread snapshot triggers a Room-backed load.
     * If a persisted recommendation exists, that load returns
     * it immediately.
     */
    init {
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
     * Tapping the recommendation starts it.
     *
     * Swiping the recommendation left only displays the next
     * persisted suggestion.
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
     * Marks the currently displayed Room recommendation as
     * SKIPPED and loads the next eligible recommendation.
     *
     * Normally this is fast because recommendations two
     * through five are already persisted as READY.
     *
     * After advancing, background prefetch replenishes the
     * persisted pool to five.
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

                /*
                 * If Room has no next recommendation and
                 * fallback generation also fails, preserve the
                 * previous cell rather than making the top of
                 * the thread list blank.
                 */
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

                /*
                 * One persisted recommendation was consumed.
                 *
                 * Replenish the Room pool asynchronously.
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
                 * Keep the current recommendation visible when
                 * Room access, generation, or validation fails.
                 */
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

    /*
     * Handles the latest active and archived thread lists.
     *
     * When no recommendation is visible, the ViewModel loads
     * the first persisted Room recommendation.
     *
     * When a recommendation is already visible, it remains in
     * place and the background pool is replenished.
     */
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
            /*
             * Keep the visible recommendation stable during
             * ordinary thread-list emissions.
             *
             * Only replenish hidden Room recommendations.
             */
            prefetchMissionRecommendations(
                activeThreads =
                    snapshot.activeThreads
            )

            return
        }

        /*
         * Avoid launching duplicate initial recommendation
         * loads if the thread source emits another snapshot
         * while Room or Qwen is still loading.
         */
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

    /*
     * Loads the initial visible recommendation.
     *
     * The Room-backed use case checks in this order:
     *
     * 1. Existing DISPLAYED recommendation
     * 2. Oldest READY recommendation
     * 3. Generate and persist one recommendation
     *
     * The UI does not wait for the complete five-item pool.
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
                 * Recommendation #1 is visible.
                 *
                 * Generate and persist missing recommendations
                 * until Room contains five eligible drafts.
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
                 * Initial loading may fail because no persisted
                 * recommendation existed and the first Qwen
                 * request failed validation.
                 *
                 * The prefetch operation continues trying. Its
                 * first valid persisted item becomes visible.
                 */
                prefetchMissionRecommendations(
                    activeThreads =
                        activeThreads
                )
            }
        )
    }

    /*
     * Replenishes the persisted recommendation pool.
     *
     * The coordinator prevents duplicate prefetch jobs.
     *
     * When no recommendation is currently visible, the first
     * recommendation successfully persisted by the prefetcher
     * is applied to the UI immediately.
     *
     * Recommendations two through five remain hidden in Room.
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
                     * Background prefetch failure should not
                     * interrupt ordinary thread-list usage.
                     *
                     * When at least one recommendation remains
                     * visible, keep it unchanged.
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
     * Reconstructs active threads from the already-built
     * sections.
     *
     * The recommendation coordinator uses these threads for:
     *
     * - Temporary CEFR-level resolution
     * - Excluding recognizable scenarios already represented
     *   by visible threads
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