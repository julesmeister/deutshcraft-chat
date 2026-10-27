package com.julesmeister.deutschcraft.feature.threads.presentation

import android.util.Log
import com.julesmeister.deutschcraft.feature.chat.domain.Conversation
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendation
import com.julesmeister.deutschcraft.feature.threads.domain.ConversationThread
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/*
 * Controls asynchronous mission-recommendation operations
 * owned by the thread-list screen.
 *
 * Room is now the recommendation source of truth.
 *
 * This controller no longer restores recommendations from
 * MissionThreadRecommendationCache.
 */
class ThreadListMissionController
@Inject constructor(
    private val missionRecommendationCoordinator:
    ThreadMissionRecommendationCoordinator
) {

    private var recommendationLoadingJob:
            Job? =
        null

    private var missionStartingJob:
            Job? =
        null

    /*
     * Prevents repeated initial-load requests while the
     * thread source emits multiple equivalent snapshots.
     */
    private var hasRequestedRecommendation =
        false

    /*
     * Loads the initial persisted recommendation when needed.
     *
     * The coordinator checks Room first. Qwen is contacted
     * only when no eligible persisted recommendation exists.
     */
    fun loadRecommendationIfNeeded(
        activeThreads:
        List<ConversationThread>,
        currentState:
        ThreadListUiState,
        scope:
        CoroutineScope,
        onStarted:
            () -> Unit,
        onSuccess:
            (MissionThreadRecommendation?) -> Unit,
        onFailure:
            () -> Unit
    ) {
        if (
            currentState.missionRecommendation !=
            null ||
            currentState.isLoadingMissionRecommendation ||
            currentState.isStartingRecommendedMission ||
            recommendationLoadingJob?.isActive ==
            true ||
            hasRequestedRecommendation
        ) {
            return
        }

        hasRequestedRecommendation =
            true

        onStarted()

        recommendationLoadingJob =
            scope.launch {
                try {
                    val recommendation =
                        missionRecommendationCoordinator
                            .loadRecommendation(
                                activeThreads =
                                    activeThreads,
                                forceRefresh =
                                    false
                            )

                    /*
                     * When the load returns null, allow another
                     * future thread snapshot to retry.
                     */
                    if (recommendation == null) {
                        hasRequestedRecommendation =
                            false
                    }

                    onSuccess(
                        recommendation
                    )
                } catch (
                    cancellationException:
                    CancellationException
                ) {
                    throw cancellationException
                } catch (throwable: Throwable) {
                    /*
                     * Recommendation loading is
                     * supplementary.
                     *
                     * Failure must not prevent the saved
                     * conversation list from working.
                     */
                    Log.e(
                        CONTROLLER_TAG,
                        "Failed to load mission " +
                                "recommendation.",
                        throwable
                    )

                    /*
                     * Permit another future loading attempt.
                     */
                    hasRequestedRecommendation =
                        false

                    onFailure()
                } finally {
                    recommendationLoadingJob =
                        null
                }
            }
    }

    /*
     * Converts the displayed persisted recommendation into a
     * real conversation and active mission.
     *
     * StartRecommendedMissionThreadUseCase marks the persisted
     * recommendation STARTED after successful mission
     * creation.
     */
    fun startRecommendation(
        recommendation:
        MissionThreadRecommendation,
        currentState:
        ThreadListUiState,
        scope:
        CoroutineScope,
        onStarted:
            () -> Unit,
        onSuccess:
        suspend (Conversation) -> Unit,
        onFailure:
            (String) -> Unit
    ) {
        if (
            currentState.isStartingRecommendedMission ||
            currentState.isCreatingThread ||
            currentState.isLoadingMissionRecommendation ||
            missionStartingJob?.isActive ==
            true
        ) {
            return
        }

        onStarted()

        missionStartingJob =
            scope.launch {
                try {
                    val conversation =
                        missionRecommendationCoordinator
                            .startRecommendation(
                                recommendation =
                                    recommendation
                            )

                    /*
                     * The recommendation has become STARTED in
                     * Room.
                     *
                     * Allow an initial recommendation to be
                     * loaded again when the learner returns to
                     * the thread list.
                     */
                    hasRequestedRecommendation =
                        false

                    onSuccess(
                        conversation
                    )
                } catch (
                    cancellationException:
                    CancellationException
                ) {
                    throw cancellationException
                } catch (throwable: Throwable) {
                    Log.e(
                        CONTROLLER_TAG,
                        "Failed to start recommended mission.",
                        throwable
                    )

                    onFailure(
                        START_MISSION_ERROR
                    )
                } finally {
                    missionStartingJob =
                        null
                }
            }
    }

    /*
     * Allows another initial recommendation request.
     *
     * This can be called after the visible recommendation is
     * consumed or cleared.
     */
    fun allowNewRecommendation() {
        hasRequestedRecommendation =
            false
    }

    fun cancel() {
        recommendationLoadingJob?.cancel()
        missionStartingJob?.cancel()

        recommendationLoadingJob =
            null

        missionStartingJob =
            null

        hasRequestedRecommendation =
            false
    }

    private companion object {
        const val CONTROLLER_TAG =
            "ThreadListMissionController"

        const val START_MISSION_ERROR =
            "Die Mission konnte nicht gestartet werden."
    }
}