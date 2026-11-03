// app/src/main/java/com/julesmeister/deutschcraft/feature/chat/presentation/ChatSessionMissionController.kt

package com.julesmeister.deutschcraft.feature.chat.presentation

import android.util.Log
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatPresentationTokens
import com.julesmeister.deutschcraft.feature.mission.domain.generated.MissionOpportunity
import com.julesmeister.deutschcraft.feature.mission.domain.usecase.StartMissionVariationUseCase
import com.julesmeister.deutschcraft.feature.mission.domain.variation.MissionVariationMode
import com.julesmeister.deutschcraft.feature.mission.domain.variation.MissionVariationType
import com.julesmeister.deutschcraft.feature.mission.presentation.guidance.MissionGuidanceController
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatSessionMissionController
@Inject constructor(
    private val missionOfferCoordinator:
    ChatMissionOfferCoordinator,

    private val missionGuidanceController:
    MissionGuidanceController,

    private val startMissionVariationUseCase:
    StartMissionVariationUseCase
) {

    fun restoreOffer(
        conversationId: String
    ): MissionOpportunity? {

        return missionOfferCoordinator.restoreOffer(
            conversationId =
                conversationId
        )
    }

    fun applyMissionUpdate(
        update: ChatMissionUpdate,
        runtime: ChatSessionRuntime,
        uiState: MutableStateFlow<ChatUiState>
    ) {
        val mission =
            update.mission

        runtime.activeMission =
            mission

        if (mission != null) {
            runtime
                .activeConversationId
                ?.let { conversationId ->
                    missionOfferCoordinator.clearOffer(
                        conversationId =
                            conversationId
                    )
                }

            runtime.cancelMissionCheck()

            missionGuidanceController.updateMission(
                mission =
                    mission
            )

            Log.d(
                ChatPresentationTokens.SessionMissionController.ControllerTag,
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
                        ", rootTemplateId="
                    )

                    append(
                        mission.resolvedRootMissionTemplateId
                    )

                    append(
                        ", currentTemplateId="
                    )

                    append(
                        mission.template.id
                    )

                    append(
                        ", progress="
                    )

                    append(
                        mission.progress.percentage
                    )
                }
            )
        } else {
            missionGuidanceController.clear()

            Log.d(
                ChatPresentationTokens.SessionMissionController.ControllerTag,
                "No active mission. Mission guidance cleared."
            )
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
            runtime.activeMission !=
            null ||
            currentState.activeMissionId !=
            null ||
            currentState.missionOffer !=
            null ||
            currentState.missionVariation.hasContinuation ||
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
                        missionOfferCoordinator.detectOffer(
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
                    Log.e(
                        ChatPresentationTokens.SessionMissionController.ControllerTag,
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
                        ChatPresentationTokens.SessionMissionController.ControllerTag,
                        "Mission could not be started.",
                        throwable
                    )

                    uiState.update { state ->
                        state.missionStartFailed(
                            message =
                                ChatPresentationTokens.SessionMissionController.MissionStartError
                        )
                    }
                } finally {
                    runtime.missionActionJob =
                        null
                }
            }
    }

    fun startMissionVariation(
        variationType: MissionVariationType,
        scope: CoroutineScope,
        runtime: ChatSessionRuntime,
        uiState: MutableStateFlow<ChatUiState>
    ) {
        val conversationId =
            runtime.activeConversationId
                ?: run {
                    uiState.update { state ->
                        state.missionVariationStartFailed(
                            message =
                                ChatPresentationTokens.SessionMissionController.ConversationUnavailableError
                        )
                    }

                    return
                }

        val currentState =
            uiState.value

        val context =
            currentState
                .missionVariation
                .context
                ?: run {
                    uiState.update { state ->
                        state.missionVariationStartFailed(
                            message =
                                ChatPresentationTokens.SessionMissionController.VariationContextUnavailableError
                        )
                    }

                    return
                }

        if (
            currentState
                .missionVariation
                .isStarting ||
            currentState.isStartingMission ||
            runtime.missionActionJob?.isActive ==
            true
        ) {
            return
        }

        /*
         * ===================================================
         * MODE-SPECIFIC VALIDATION
         * ===================================================
         *
         * This is the defect in the previous implementation.
         *
         * BRANCH_ACTIVE_MISSION requires an active mission.
         *
         * EXTEND_COMPLETED_MISSION requires there to be no
         * active mission.
         *
         * The previous implementation rejected every request
         * whenever runtime.activeMission != null, which made
         * active mission branching impossible.
         */

        when (context.mode) {
            MissionVariationMode.BRANCH_ACTIVE_MISSION -> {
                val activeMission =
                    runtime.activeMission

                if (
                    activeMission ==
                    null ||
                    currentState.activeMissionId ==
                    null
                ) {
                    uiState.update { state ->
                        state.missionVariationStartFailed(
                            message =
                                ChatPresentationTokens.SessionMissionController.ActiveMissionRequiredError
                        )
                    }

                    return
                }

                if (
                    activeMission.id !=
                    context.resolvedSourceMissionId ||
                    currentState.activeMissionId !=
                    context.resolvedSourceMissionId
                ) {
                    uiState.update { state ->
                        state.missionVariationStartFailed(
                            message =
                                ChatPresentationTokens.SessionMissionController.ActiveMissionChangedError
                        )
                    }

                    return
                }
            }

            MissionVariationMode.EXTEND_COMPLETED_MISSION -> {
                if (
                    runtime.activeMission !=
                    null ||
                    currentState.activeMissionId !=
                    null
                ) {
                    uiState.update { state ->
                        state.missionVariationStartFailed(
                            message =
                                ChatPresentationTokens.SessionMissionController.ActiveMissionExtensionError
                        )
                    }

                    return
                }
            }
        }

        runtime.cancelSuggestionJob()

        /*
         * Prevent an ordinary mission-opportunity check from
         * racing against the branch operation.
         */
        runtime.cancelMissionCheck()

        uiState.update { state ->
            state.missionVariationStartStarted()
        }

        Log.i(
            ChatPresentationTokens.SessionMissionController.ControllerTag,
            buildString {
                append(
                    "Starting mission variation. "
                )

                append(
                    "conversationId="
                )

                append(
                    conversationId
                )

                append(
                    ", sourceMissionId="
                )

                append(
                    context.resolvedSourceMissionId
                )

                append(
                    ", mode="
                )

                append(
                    context.mode.name
                )

                append(
                    ", type="
                )

                append(
                    variationType.name
                )
            }
        )

        runtime.missionActionJob =
            scope.launch {
                try {
                    val resultingMissionId =
                        startMissionVariationUseCase(
                            conversationId =
                                conversationId,

                            context =
                                context,

                            variationType =
                                variationType
                        )

                    /*
                     * Room is authoritative for active mission
                     * state.
                     *
                     * For an active branch, replacing the
                     * template causes observeActiveMission() to
                     * emit the updated ConversationMission.
                     *
                     * applyMissionUpdate() then refreshes:
                     *
                     * - runtime.activeMission
                     * - mission guidance
                     * - current objective
                     * - top-bar progress
                     */
                    uiState.update { state ->
                        state.missionVariationStartSucceeded()
                    }

                    Log.i(
                        ChatPresentationTokens.SessionMissionController.ControllerTag,
                        buildString {
                            append(
                                "Mission variation completed. "
                            )

                            append(
                                "conversationId="
                            )

                            append(
                                conversationId
                            )

                            append(
                                ", resultingMissionId="
                            )

                            append(
                                resultingMissionId
                            )

                            append(
                                ", mode="
                            )

                            append(
                                context.mode.name
                            )

                            append(
                                ", type="
                            )

                            append(
                                variationType.name
                            )
                        }
                    )
                } catch (
                    cancellationException:
                    CancellationException
                ) {
                    /*
                     * Cancellation must also release the local
                     * loading state. Otherwise the dialog /
                     * composer can remain permanently blocked.
                     */
                    uiState.update { state ->
                        state.missionVariationStartFailed(
                            message =
                                ChatPresentationTokens.SessionMissionController.VariationCancelledMessage
                        )
                    }

                    throw cancellationException
                } catch (throwable: Throwable) {
                    Log.e(
                        ChatPresentationTokens.SessionMissionController.ControllerTag,
                        buildString {
                            append(
                                "Mission variation could not "
                            )

                            append(
                                "be started. mode="
                            )

                            append(
                                context.mode.name
                            )

                            append(
                                ", type="
                            )

                            append(
                                variationType.name
                            )

                            append(
                                ", sourceMissionId="
                            )

                            append(
                                context.resolvedSourceMissionId
                            )
                        },
                        throwable
                    )

                    uiState.update { state ->
                        state.missionVariationStartFailed(
                            message =
                                throwable
                                    .message
                                    ?.trim()
                                    ?.takeIf(
                                        String::isNotBlank
                                    )
                                    ?: ChatPresentationTokens.SessionMissionController.MissionVariationStartError
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

}
