package com.julesmeister.deutschcraft.feature.chat.presentation

import android.util.Log
import com.julesmeister.deutschcraft.feature.mission.domain.generated.MissionOpportunity
import com.julesmeister.deutschcraft.feature.mission.presentation.guidance.MissionGuidanceController
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/*
 * Owns mission-specific chat-session behavior.
 *
 * Responsibilities:
 *
 * - Restore cached mission offers
 * - Detect contextual mission opportunities
 * - Accept mission offers
 * - Dismiss mission offers
 * - Apply Room-backed active-mission updates
 * - Keep ChatSessionRuntime synchronized
 * - Keep ChatUiState synchronized
 * - Keep MissionGuidanceController synchronized
 * - Consume objective-completion presentation events
 */
class ChatSessionMissionController
@Inject constructor(
    private val missionOfferCoordinator:
    ChatMissionOfferCoordinator,

    private val missionGuidanceController:
    MissionGuidanceController
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
     * Applies the authoritative mission state emitted by the
     * Room-backed session observer.
     *
     * This is also the correct place to update Mission
     * Guidance because it receives:
     *
     * - mission start
     * - objective progress changes
     * - current-objective changes
     * - mission completion
     *
     * independently of the UI lifecycle.
     */
    fun applyMissionUpdate(
        update:
        ChatMissionUpdate,
        runtime:
        ChatSessionRuntime,
        uiState:
        MutableStateFlow<ChatUiState>
    ) {
        val mission =
            update.mission

        /*
         * Retain the complete mission domain model.
         *
         * ChatMessageSender uses this exact snapshot when the
         * learner sends the next message.
         */
        runtime.activeMission =
            mission

        if (mission != null) {
            /*
             * A real active mission supersedes a cached
             * contextual mission offer.
             */
            runtime
                .activeConversationId
                ?.let { conversationId ->
                    missionOfferCoordinator.clearOffer(
                        conversationId =
                            conversationId
                    )
                }

            /*
             * Once a mission is active, normal contextual
             * mission detection must stop.
             */
            runtime.cancelMissionCheck()

            /*
             * Keep learner guidance synchronized with the
             * latest Room-backed mission.
             *
             * This updates:
             *
             * - mission title
             * - mission progress
             * - current objective
             * - coaching hints
             * - target vocabulary
             * - example phrases
             *
             * Existing revealed-hint state should be restored
             * by MissionGuidanceController from persisted hint
             * usage.
             */
            missionGuidanceController.updateMission(
                mission =
                    mission
            )

            Log.d(
                CONTROLLER_TAG,
                buildString {
                    append(
                        "Active mission updated. "
                    )
                    append(
                        "missionId="
                    )
                    append(
                        mission.id
                    )
                    append(
                        ", progress="
                    )
                    append(
                        mission.progress.percentage
                    )
                    append(
                        "%"
                    )
                    append(
                        ", currentObjective="
                    )
                    append(
                        mission.currentObjective
                            ?.objective
                            ?.title
                    )
                }
            )
        } else {
            /*
             * No active mission remains.
             *
             * This happens when:
             *
             * - the mission finishes
             * - the conversation has no mission
             * - the active mission is otherwise removed
             *
             * Close and clear guidance so stale objectives
             * cannot leak into the next mission.
             */
            missionGuidanceController.clear()

            Log.d(
                CONTROLLER_TAG,
                "No active mission. Mission guidance cleared."
            )
        }

        /*
         * Existing ChatUiState synchronization remains
         * unchanged.
         *
         * This continues to drive:
         *
         * - top-bar mission title
         * - objective title
         * - guidance marquee
         * - progress
         * - objective-completion animation
         */
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
        uiState:
        MutableStateFlow<ChatUiState>
    ) {
        val currentState =
            uiState.value

        if (
            runtime.activeMission !=
            null ||
            currentState.activeMissionId !=
            null ||
            currentState.missionOffer !=
            null ||
            currentState.isCheckingMissionOpportunity ||
            runtime.missionCheckJob?.isActive ==
            true
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
                     * Normal conversation has already
                     * completed successfully.
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
        uiState:
        MutableStateFlow<ChatUiState>
    ) {
        val conversationId =
            runtime.activeConversationId
                ?: return

        val currentState =
            uiState.value

        if (
            currentState.missionOffer ==
            null ||
            currentState.isStartingMission ||
            runtime.activeMission !=
            null ||
            currentState.activeMissionId !=
            null ||
            runtime.missionActionJob?.isActive ==
            true
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
                     * Do not construct MissionGuidance state
                     * here.
                     *
                     * Room will emit the newly created active
                     * ConversationMission and
                     * applyMissionUpdate() will configure both
                     * chat state and mission guidance from
                     * that authoritative result.
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
        runtime:
        ChatSessionRuntime,
        uiState:
        MutableStateFlow<ChatUiState>
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

    /*
     * Consumes the temporary objective-completion animation.
     *
     * MissionGuidanceController does not need to be manually
     * advanced here.
     *
     * Room already persists objective completion, and the
     * resulting mission emission updates guidance through
     * applyMissionUpdate().
     */
    fun consumeObjectiveCompletion(
        eventId: String,
        uiState:
        MutableStateFlow<ChatUiState>
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