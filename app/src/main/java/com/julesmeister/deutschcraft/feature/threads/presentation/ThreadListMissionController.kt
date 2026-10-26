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

class ThreadListMissionController @Inject constructor(
    private val missionRecommendationCoordinator:
    ThreadMissionRecommendationCoordinator
) {

    private var recommendationLoadingJob: Job? =
        null

    private var missionStartingJob: Job? =
        null

    private var hasRequestedRecommendation =
        false

    fun restoreCachedRecommendation():
            MissionThreadRecommendation? {
        return missionRecommendationCoordinator
            .restoreCachedRecommendation()
    }

    fun loadRecommendationIfNeeded(
        activeThreads: List<ConversationThread>,
        currentState: ThreadListUiState,
        scope: CoroutineScope,
        onStarted: () -> Unit,
        onSuccess:
            (MissionThreadRecommendation?) -> Unit,
        onFailure: () -> Unit
    ) {
        if (
            currentState.missionRecommendation != null ||
            currentState.isLoadingMissionRecommendation ||
            currentState.isStartingRecommendedMission ||
            recommendationLoadingJob?.isActive == true ||
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
                                    activeThreads
                            )

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
                     * Recommendations are supplementary.
                     *
                     * Failure should not display a Snackbar
                     * or block the saved conversation list.
                     */
                    Log.e(
                        CONTROLLER_TAG,
                        "Failed to load mission " +
                                "recommendation.",
                        throwable
                    )

                    onFailure()
                } finally {
                    recommendationLoadingJob =
                        null
                }
            }
    }

    fun startRecommendation(
        recommendation:
        MissionThreadRecommendation,
        currentState: ThreadListUiState,
        scope: CoroutineScope,
        onStarted: () -> Unit,
        onSuccess:
        suspend (Conversation) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (
            currentState.isStartingRecommendedMission ||
            currentState.isCreatingThread ||
            missionStartingJob?.isActive == true
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
                     * The recommendation was consumed.
                     *
                     * Allow a new one to be requested when
                     * the repository emits again.
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
                        "Failed to start recommended " +
                                "mission.",
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
    }

    private companion object {
        const val CONTROLLER_TAG =
            "ThreadListMissionController"

        const val START_MISSION_ERROR =
            "Die Mission konnte nicht gestartet werden."
    }
}