package com.julesmeister.deutschcraft.feature.threads.presentation

import android.util.Log
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.chat.domain.Conversation
import com.julesmeister.deutschcraft.feature.mission.data.cache.MissionThreadRecommendationCache
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendation
import com.julesmeister.deutschcraft.feature.mission.domain.usecase.LoadMissionThreadRecommendationUseCase
import com.julesmeister.deutschcraft.feature.mission.domain.usecase.StartRecommendedMissionThreadUseCase
import com.julesmeister.deutschcraft.feature.threads.domain.ConversationThread
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ThreadMissionRecommendationCoordinator
@Inject constructor(
    private val loadRecommendation:
    LoadMissionThreadRecommendationUseCase,
    private val startRecommendedMission:
    StartRecommendedMissionThreadUseCase,
    private val recommendationCache:
    MissionThreadRecommendationCache
) {

    private var prefetchJob:
            Job? =
        null

    private var prefetchLevel:
            CefrLevel? =
        null

    fun restoreCachedRecommendation():
            MissionThreadRecommendation? {
        return recommendationCache.get()
    }

    /*
     * Loads or advances the visible recommendation.
     *
     * When forceRefresh is true, a prefetched item is used
     * immediately whenever available.
     */
    suspend fun loadRecommendation(
        activeThreads:
        List<ConversationThread>,
        forceRefresh: Boolean =
            false
    ): MissionThreadRecommendation? {
        val learnerLevel =
            determineLearnerLevel(
                activeThreads =
                    activeThreads
            )

        return if (forceRefresh) {
            loadRecommendation.advance(
                cefrLevel =
                    learnerLevel
            )
        } else {
            loadRecommendation(
                cefrLevel =
                    learnerLevel
            )
        }
    }

    /*
     * Ensures five recommendations are cached.
     *
     * This method launches background work and returns
     * immediately.
     *
     * If the queue is initially empty, each accepted item is
     * reported through onFirstRecommendationAvailable. The
     * first item can therefore be displayed without waiting
     * for items two through five.
     */
    fun prefetchRecommendations(
        activeThreads:
        List<ConversationThread>,
        scope: CoroutineScope,
        onFirstRecommendationAvailable:
            (MissionThreadRecommendation) -> Unit,
        onFailure:
            (String) -> Unit =
            {}
    ) {
        val learnerLevel =
            determineLearnerLevel(
                activeThreads =
                    activeThreads
            )

        if (
            prefetchJob?.isActive ==
            true &&
            prefetchLevel ==
            learnerLevel
        ) {
            return
        }

        if (
            prefetchLevel != null &&
            prefetchLevel !=
            learnerLevel
        ) {
            prefetchJob?.cancel()
        }

        prefetchLevel =
            learnerLevel

        prefetchJob =
            scope.launch {
                try {
                    loadRecommendation.prefetchUntilFull(
                        cefrLevel =
                            learnerLevel,
                        onRecommendationAvailable = {
                                _,
                                _ ->

                            /*
                             * The queue's first item is the
                             * visible recommendation.
                             */
                            recommendationCache
                                .get()
                                ?.let(
                                    onFirstRecommendationAvailable
                                )
                        }
                    )
                } catch (
                    cancellationException:
                    CancellationException
                ) {
                    throw cancellationException
                } catch (throwable: Throwable) {
                    Log.e(
                        COORDINATOR_TAG,
                        "Mission recommendation prefetch " +
                                "failed.",
                        throwable
                    )

                    onFailure(
                        PREFETCH_ERROR
                    )
                } finally {
                    prefetchJob =
                        null
                }
            }
    }

    suspend fun startRecommendation(
        recommendation:
        MissionThreadRecommendation
    ): Conversation {
        val conversation =
            startRecommendedMission(
                recommendation =
                    recommendation
            )

        recommendationCache.remove(
            recommendationId =
                recommendation.id
        )

        return conversation
    }

    fun clearCachedRecommendation() {
        recommendationCache.clear()
    }

    fun cancelPrefetch() {
        prefetchJob?.cancel()
        prefetchJob =
            null
        prefetchLevel =
            null
    }

    private fun determineLearnerLevel(
        activeThreads:
        List<ConversationThread>
    ): CefrLevel {
        return activeThreads
            .maxByOrNull { thread ->
                thread.updatedAt
            }
            ?.cefrLevel
            ?: CefrLevel.A1
    }

    private companion object {
        const val COORDINATOR_TAG =
            "ThreadMissionRecommendation"

        const val PREFETCH_ERROR =
            "Weitere Empfehlungen konnten nicht " +
                    "vorbereitet werden."
    }
}