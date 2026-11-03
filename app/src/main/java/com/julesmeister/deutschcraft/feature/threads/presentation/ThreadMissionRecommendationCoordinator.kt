// app/src/main/java/com/julesmeister/deutschcraft/feature/threads/presentation/ThreadMissionRecommendationCoordinator.kt

package com.julesmeister.deutschcraft.feature.threads.presentation

import android.util.Log
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.assessment.domain.GetCurrentLearnerCefrLevelUseCase
import com.julesmeister.deutschcraft.feature.chat.domain.Conversation
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendation
import com.julesmeister.deutschcraft.feature.mission.domain.usecase.LoadMissionThreadRecommendationUseCase
import com.julesmeister.deutschcraft.feature.mission.domain.usecase.StartRecommendedMissionThreadUseCase
import com.julesmeister.deutschcraft.feature.threads.domain.ConversationThread
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class ThreadMissionRecommendationCoordinator
@Inject constructor(
    private val loadRecommendation:
    LoadMissionThreadRecommendationUseCase,

    private val startRecommendedMission:
    StartRecommendedMissionThreadUseCase,

    private val getCurrentLearnerCefrLevel:
    GetCurrentLearnerCefrLevelUseCase
) {

    private var prefetchJob:
            Job? =
        null

    private var prefetchLevel:
            CefrLevel? =
        null

    private var refillRequestedWhileActive:
            Boolean =
        false

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeRecommendation(): Flow<MissionThreadRecommendation?> =
        getCurrentLearnerCefrLevel.observe().flatMapLatest(loadRecommendation::observeNextEligible)

    suspend fun getSavedRecommendation(): MissionThreadRecommendation? =
        loadRecommendation.getSavedRecommendation(getCurrentLearnerCefrLevel())

    suspend fun loadRecommendation(
        activeThreads:
        List<ConversationThread>,

        forceRefresh:
        Boolean =
            false
    ): MissionThreadRecommendation? {

        val learnerLevel =
            getCurrentLearnerCefrLevel()

        return try {
            val recommendation =
                if (forceRefresh) {
                    loadRecommendation.advance(
                        cefrLevel =
                            learnerLevel,

                        externalExcludedScenarioTypes =
                            emptySet()
                    )
                } else {
                    loadRecommendation(
                        cefrLevel =
                            learnerLevel,

                        forceRefresh =
                            false,

                        externalExcludedScenarioTypes =
                            emptySet()
                    )
                }

            Log.d(
                COORDINATOR_TAG,
                buildString {
                    append(
                        "Loaded mission recommendation. "
                    )

                    append(
                        "profileLevel="
                    )

                    append(
                        learnerLevel.name
                    )

                    append(
                        ", forceRefresh="
                    )

                    append(
                        forceRefresh
                    )

                    append(
                        ", activeThreadCount="
                    )

                    append(
                        activeThreads.size
                    )

                    append(
                        ", scenario="
                    )

                    append(
                        recommendation?.scenarioType
                    )

                    append(
                        ", title="
                    )

                    append(
                        recommendation?.title
                    )
                }
            )

            recommendation
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.e(
                COORDINATOR_TAG,
                "Mission recommendation could not be loaded.",
                throwable
            )

            null
        }
    }

    fun prefetchRecommendations(
        activeThreads:
        List<ConversationThread>,

        scope:
        CoroutineScope,

        onFirstRecommendationAvailable:
            (MissionThreadRecommendation) -> Unit,

        onFailure:
            (String) -> Unit =
            {}
    ) {
        scope.launch {
            val learnerLevel =
                try {
                    getCurrentLearnerCefrLevel()
                } catch (
                    cancellationException:
                    CancellationException
                ) {
                    throw cancellationException
                } catch (throwable: Throwable) {
                    Log.e(
                        COORDINATOR_TAG,
                        "Learner CEFR level could not be loaded.",
                        throwable
                    )

                    onFailure(
                        PREFETCH_ERROR
                    )

                    return@launch
                }

            requestPrefetch(
                learnerLevel =
                    learnerLevel,

                activeThreadCount =
                    activeThreads.size,

                scope =
                    scope,

                onFirstRecommendationAvailable =
                    onFirstRecommendationAvailable,

                onFailure =
                    onFailure
            )
        }
    }

    suspend fun startRecommendation(
        recommendation:
        MissionThreadRecommendation
    ): Conversation {

        require(
            recommendation.id.isNotBlank()
        ) {
            "Recommendation ID must not be blank."
        }

        return try {
            val conversation =
                startRecommendedMission(
                    recommendation =
                        recommendation
                )

            Log.d(
                COORDINATOR_TAG,
                buildString {
                    append(
                        "Started recommended mission thread. "
                    )

                    append(
                        "recommendationId="
                    )

                    append(
                        recommendation.id
                    )

                    append(
                        ", scenario="
                    )

                    append(
                        recommendation.scenarioType
                    )

                    append(
                        ", conversationId="
                    )

                    append(
                        conversation.id
                    )
                }
            )

            conversation
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.e(
                COORDINATOR_TAG,
                "Recommended mission thread could not be started.",
                throwable
            )

            throw throwable
        }
    }

    fun cancelPrefetch() {
        prefetchJob?.cancel()

        prefetchJob =
            null

        prefetchLevel =
            null

        refillRequestedWhileActive =
            false
    }

    private fun requestPrefetch(
        learnerLevel:
        CefrLevel,

        activeThreadCount:
        Int,

        scope:
        CoroutineScope,

        onFirstRecommendationAvailable:
            (MissionThreadRecommendation) -> Unit,

        onFailure:
            (String) -> Unit
    ) {
        if (
            prefetchJob?.isActive ==
            true &&
            prefetchLevel ==
            learnerLevel
        ) {
            refillRequestedWhileActive =
                true

            Log.d(
                COORDINATOR_TAG,
                "Recommendation prefetch is already active. " +
                        "A follow-up refill was requested."
            )

            return
        }

        if (
            prefetchJob?.isActive ==
            true &&
            prefetchLevel !=
            learnerLevel
        ) {
            prefetchJob?.cancel()

            prefetchJob =
                null

            refillRequestedWhileActive =
                false
        }

        prefetchLevel =
            learnerLevel

        startPrefetchJob(
            learnerLevel =
                learnerLevel,

            activeThreadCount =
                activeThreadCount,

            scope =
                scope,

            onFirstRecommendationAvailable =
                onFirstRecommendationAvailable,

            onFailure =
                onFailure
        )
    }

    private fun startPrefetchJob(
        learnerLevel:
        CefrLevel,

        activeThreadCount:
        Int,

        scope:
        CoroutineScope,

        onFirstRecommendationAvailable:
            (MissionThreadRecommendation) -> Unit,

        onFailure:
            (String) -> Unit
    ) {
        val job =
            scope.launch(start = CoroutineStart.LAZY) {
                var poolFilled = false
                try {
                    Log.d(
                        COORDINATOR_TAG,
                        buildString {
                            append(
                                "Starting recommendation prefetch. "
                            )

                            append(
                                "profileLevel="
                            )

                            append(
                                learnerLevel.name
                            )

                            append(
                                ", activeThreadCount="
                            )

                            append(
                                activeThreadCount
                            )
                        }
                    )

                    poolFilled = loadRecommendation.prefetchUntilFull(
                        cefrLevel =
                            learnerLevel,

                        externalExcludedScenarioTypes =
                            emptySet(),

                        onRecommendationAvailable = {
                                recommendation,
                                availableCount ->

                            onFirstRecommendationAvailable(
                                recommendation
                            )

                            Log.d(
                                COORDINATOR_TAG,
                                buildString {
                                    append(
                                        "Persisted recommendation available. "
                                    )

                                    append(
                                        "scenario="
                                    )

                                    append(
                                        recommendation.scenarioType
                                    )

                                    append(
                                        ", available="
                                    )

                                    append(
                                        availableCount
                                    )
                                }
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
                        "Mission recommendation prefetch failed.",
                        throwable
                    )

                    onFailure(
                        PREFETCH_ERROR
                    )
                } finally {
                    if (prefetchJob === coroutineContext[Job]) {
                        prefetchJob =
                            null

                        val followUpRequested =
                            refillRequestedWhileActive

                        refillRequestedWhileActive =
                            false

                        val shouldRunFollowUp =
                            if (followUpRequested && poolFilled) {
                                try {
                                    loadRecommendation.availableCount(
                                        cefrLevel =
                                            learnerLevel
                                    ) <
                                            TARGET_RECOMMENDATION_COUNT
                                } catch (
                                    cancellationException:
                                    CancellationException
                                ) {
                                    throw cancellationException
                                } catch (throwable: Throwable) {
                                    Log.e(
                                        COORDINATOR_TAG,
                                        "Persisted recommendation pool " +
                                                "could not be inspected.",
                                        throwable
                                    )

                                    false
                                }
                            } else {
                                false
                            }

                        if (shouldRunFollowUp) {
                            startPrefetchJob(
                                learnerLevel =
                                    learnerLevel,

                                activeThreadCount =
                                    activeThreadCount,

                                scope =
                                    scope,

                                onFirstRecommendationAvailable =
                                    onFirstRecommendationAvailable,

                                onFailure =
                                    onFailure
                            )
                        }
                    }
                }
            }
        prefetchJob = job
        job.start()
    }

    private companion object {

        const val COORDINATOR_TAG =
            "ThreadMissionRecommendation"

        const val TARGET_RECOMMENDATION_COUNT =
            5

        const val PREFETCH_ERROR =
            "Weitere Missionsempfehlungen konnten nicht " +
                    "vorbereitet werden."
    }
}