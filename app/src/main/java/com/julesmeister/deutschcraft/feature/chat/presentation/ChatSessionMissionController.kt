package com.julesmeister.deutschcraft.feature.chat.presentation

import android.util.Log
import com.julesmeister.deutschcraft.feature.mission.domain.generated.MissionOpportunity
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatSessionMissionController @Inject constructor(
    private val missionOfferCoordinator:
    ChatMissionOfferCoordinator
) {

    fun restoreOffer(
        conversationId: String
    ): MissionOpportunity? {
        return missionOfferCoordinator.restoreOffer(
            conversationId =
                conversationId
        )
    }

    /*
     * Applies a mission update emitted by the Room-backed
     * session observer.
     */
    fun applyMissionUpdate(
        update: ChatMissionUpdate,
        runtime: ChatSessionRuntime,
        uiState: MutableStateFlow<ChatUiState>
    ) {
        /*
         * Retain the complete mission domain model.
         *
         * The next learner message will be evaluated against
         * this mission's unfinished objectives.
         */
        runtime.activeMission =
            update.mission

        val mission =
            update.mission

        if (mission != null) {
            runtime
                .activeConversationId
                ?.let { conversationId ->
                    missionOfferCoordinator.clearOffer(
                        conversationId =
                            conversationId
                    )
                }

            /*
             * Once a mission is active, contextual mission
             * detection should stop.
             */
            runtime.cancelMissionCheck()
        }

        uiState.update { state ->
            state.withActiveMission(
                mission =
                    mission,
                newlyCompletedObjective =
                    update.newlyCompletedObjective
            )
        }
    }

    fun checkForMissionOpportunity(
        conversationId: String,
        scope: CoroutineScope,
        runtime: ChatSessionRuntime,
        uiState: MutableStateFlow<ChatUiState>
    ) {
        val currentState =
            uiState.value

        if (
            runtime.activeMission != null ||
            currentState.activeMissionId != null ||
            currentState.missionOffer != null ||
            currentState.isCheckingMissionOpportunity ||
            runtime.missionCheckJob?.isActive == true
        ) {
            return
        }

        uiState.update { state ->
            state.missionCheckStarted()
        }

        runtime.missionCheckJob =
            scope.launch {
                try {
                    val offer =
                        missionOfferCoordinator
                            .detectOffer(
                                conversationId =
                                    conversationId,
                                hasActiveMission =
                                    runtime.activeMission !=
                                            null ||
                                            uiState
                                                .value
                                                .activeMissionId !=
                                            null
                            )

                    uiState.update { state ->
                        state.missionCheckFinished(
                            offer =
                                offer
                        )
                    }
                } catch (
                    cancellationException:
                    CancellationException
                ) {
                    throw cancellationException
                } catch (throwable: Throwable) {
                    /*
                     * Mission opportunity detection is
                     * supplementary.
                     *
                     * The learner's normal chat interaction
                     * has already completed successfully.
                     */
                    Log.e(
                        CONTROLLER_TAG,
                        "Mission opportunity check failed.",
                        throwable
                    )

                    uiState.update { state ->
                        state.missionCheckFinished(
                            offer =
                                null
                        )
                    }
                } finally {
                    runtime.missionCheckJob =
                        null
                }
            }
    }

    fun startOfferedMission(
        scope: CoroutineScope,
        runtime: ChatSessionRuntime,
        uiState: MutableStateFlow<ChatUiState>
    ) {
        val conversationId =
            runtime.activeConversationId
                ?: return

        val currentState =
            uiState.value

        if (
            currentState.missionOffer == null ||
            currentState.isStartingMission ||
            runtime.activeMission != null ||
            currentState.activeMissionId != null ||
            runtime.missionActionJob?.isActive == true
        ) {
            return
        }

        uiState.update { state ->
            state.missionStartStarted()
        }

        runtime.missionActionJob =
            scope.launch {
                try {
                    missionOfferCoordinator.acceptOffer(
                        conversationId =
                            conversationId
                    )

                    /*
                     * The Room observer will emit the complete
                     * active mission after it is persisted and
                     * started.
                     */
                    uiState.update { state ->
                        state.missionStartSucceeded()
                    }
                } catch (
                    cancellationException:
                    CancellationException
                ) {
                    throw cancellationException
                } catch (throwable: Throwable) {
                    Log.e(
                        CONTROLLER_TAG,
                        "Mission could not be started.",
                        throwable
                    )

                    uiState.update { state ->
                        state.missionStartFailed(
                            message =
                                MISSION_START_ERROR
                        )
                    }
                } finally {
                    runtime.missionActionJob =
                        null
                }
            }
    }

    fun dismissOffer(
        runtime: ChatSessionRuntime,
        uiState: MutableStateFlow<ChatUiState>
    ) {
        val conversationId =
            runtime.activeConversationId
                ?: return

        missionOfferCoordinator.dismissOffer(
            conversationId =
                conversationId
        )

        uiState.update { state ->
            state.missionOfferDismissed()
        }
    }

    fun consumeObjectiveCompletion(
        eventId: String,
        uiState: MutableStateFlow<ChatUiState>
    ) {
        uiState.update { state ->
            state.objectiveCompletionShown(
                eventId =
                    eventId
            )
        }
    }

    private companion object {
        const val CONTROLLER_TAG =
            "ChatSessionMission"

        const val MISSION_START_ERROR =
            "The mission could not be started."
    }
}