package com.julesmeister.deutschcraft.feature.mission.domain.usecase

import android.util.Log
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.mission.data.ai.MissionThreadRecommendationAiGateway
import com.julesmeister.deutschcraft.feature.mission.data.cache.MissionThreadRecommendationCache
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionRecommendationContext
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionScenarioDefinition
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendation
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.SelectMissionScenarioUseCase
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/*
 * Manages the mission recommendations displayed in the
 * thread-list featured mission cell.
 *
 * Responsibilities:
 *
 * - Return the first available recommendation immediately
 * - Maintain a five-item recommendation queue
 * - Advance instantly to a prefetched recommendation
 * - Refill the queue in the background
 * - Prevent duplicate semantic scenario types
 * - Ensure Qwen expands a Kotlin-selected scenario instead
 *   of selecting another coffee or bakery variation
 */
class LoadMissionThreadRecommendationUseCase
@Inject constructor(
    private val aiGateway:
    MissionThreadRecommendationAiGateway,
    private val recommendationCache:
    MissionThreadRecommendationCache,
    private val selectMissionScenario:
    SelectMissionScenarioUseCase
) {

    /*
     * Protects visible recommendation loading and queue
     * advancement.
     */
    private val loadingMutex =
        Mutex()

    /*
     * Prevents multiple queue-fill operations from running
     * simultaneously.
     */
    private val prefetchMutex =
        Mutex()

    /*
     * Serializes actual recommendation generation.
     *
     * Without this lock, the initial loader and the
     * background prefetcher could select the same scenario
     * before either one has been added to the queue.
     */
    private val generationMutex =
        Mutex()

    /*
     * Returns the currently visible recommendation.
     *
     * Initial load:
     *
     * - Return a matching cached recommendation immediately.
     * - Otherwise generate one recommendation.
     * - Do not wait for all five recommendations.
     *
     * Forced refresh:
     *
     * - Remove the current item.
     * - Return the next prefetched item immediately.
     * - If the queue becomes empty, generate one fallback.
     */
    suspend operator fun invoke(
        cefrLevel: CefrLevel,
        forceRefresh: Boolean =
            false
    ): MissionThreadRecommendation? {
        return loadingMutex.withLock {
            recommendationCache.retainLevel(
                cefrLevel =
                    cefrLevel
            )

            if (forceRefresh) {
                val nextRecommendation =
                    recommendationCache.advance()

                if (nextRecommendation != null) {
                    Log.d(
                        USE_CASE_TAG,
                        buildString {
                            append(
                                "Advanced to prefetched mission. "
                            )
                            append("scenario=")
                            append(
                                nextRecommendation.scenarioType
                            )
                            append(", title=")
                            append(
                                nextRecommendation.title
                            )
                            append(", remainingQueue=")
                            append(
                                recommendationCache.size()
                            )
                        }
                    )

                    return@withLock nextRecommendation
                }

                /*
                 * The queue contained only the visible item.
                 *
                 * advance() consumed it, so generate one
                 * fallback recommendation.
                 */
                Log.d(
                    USE_CASE_TAG,
                    "Mission queue was exhausted during " +
                            "forced refresh."
                )
            } else {
                val cachedRecommendation =
                    recommendationCache.get()

                if (cachedRecommendation != null) {
                    return@withLock cachedRecommendation
                }
            }

            /*
             * Initial loading or queue-exhaustion fallback.
             *
             * Only one recommendation is generated here so
             * the UI can display it immediately.
             */
            generateAndEnqueueOne(
                cefrLevel =
                    cefrLevel
            )

            recommendationCache.get()
        }
    }

    /*
     * Explicit queue-advancement operation.
     *
     * The coordinator may call this method directly rather
     * than using invoke(forceRefresh = true).
     */
    suspend fun advance(
        cefrLevel: CefrLevel
    ): MissionThreadRecommendation? {
        return loadingMutex.withLock {
            recommendationCache.retainLevel(
                cefrLevel =
                    cefrLevel
            )

            val nextRecommendation =
                recommendationCache.advance()

            if (nextRecommendation != null) {
                Log.d(
                    USE_CASE_TAG,
                    buildString {
                        append(
                            "Mission queue advanced instantly. "
                        )
                        append("scenario=")
                        append(
                            nextRecommendation.scenarioType
                        )
                        append(", title=")
                        append(
                            nextRecommendation.title
                        )
                        append(", queueSize=")
                        append(
                            recommendationCache.size()
                        )
                    }
                )

                return@withLock nextRecommendation
            }

            /*
             * Queue exhaustion should be rare because the
             * coordinator continuously prefetches.
             *
             * Generate one recommendation as a fallback.
             */
            Log.d(
                USE_CASE_TAG,
                "Mission queue was empty. Generating one " +
                        "fallback recommendation."
            )

            generateAndEnqueueOne(
                cefrLevel =
                    cefrLevel
            )

            recommendationCache.get()
        }
    }

    /*
     * Fills the queue until it contains five valid,
     * semantically distinct recommendations.
     *
     * This method should run in a background coroutine after
     * the first visible recommendation has been delivered.
     *
     * The callback is invoked after each accepted item.
     *
     * If the visible queue was initially empty, the caller
     * may display recommendationCache.get() as soon as the
     * first callback occurs.
     */
    suspend fun prefetchUntilFull(
        cefrLevel: CefrLevel,
        onRecommendationAvailable:
            (
            recommendation:
            MissionThreadRecommendation,
            queueSize: Int
        ) -> Unit =
            { _, _ -> }
    ) {
        prefetchMutex.withLock {
            recommendationCache.retainLevel(
                cefrLevel =
                    cefrLevel
            )

            var generationAttemptCount =
                0

            while (
                recommendationCache.size() <
                MissionThreadRecommendationCache
                    .TARGET_QUEUE_SIZE &&
                generationAttemptCount <
                MAXIMUM_PREFETCH_ATTEMPTS
            ) {
                generationAttemptCount +=
                    1

                val generatedRecommendation =
                    generateAndEnqueueOne(
                        cefrLevel =
                            cefrLevel
                    )

                if (generatedRecommendation != null) {
                    onRecommendationAvailable(
                        generatedRecommendation,
                        recommendationCache.size()
                    )
                }
            }

            Log.d(
                USE_CASE_TAG,
                buildString {
                    append(
                        "Mission recommendation prefetch finished. "
                    )
                    append("level=")
                    append(
                        cefrLevel.name
                    )
                    append(", queueSize=")
                    append(
                        recommendationCache.size()
                    )
                    append(", attempts=")
                    append(
                        generationAttemptCount
                    )
                    append(", scenarios=")
                    append(
                        recommendationCache
                            .getAll()
                            .map { recommendation ->
                                recommendation.scenarioType
                            }
                    )
                }
            )
        }
    }

    /*
     * Returns the recommendation at the front of the queue.
     */
    fun current():
            MissionThreadRecommendation? {
        return recommendationCache.get()
    }

    /*
     * Returns the number of currently cached
     * recommendations.
     */
    fun queueSize(): Int {
        return recommendationCache.size()
    }

    /*
     * Selects, generates, validates, and enqueues one mission.
     *
     * Actual generation is serialized so two concurrent jobs
     * cannot select the same scenario from the same cache
     * state.
     */
    private suspend fun generateAndEnqueueOne(
        cefrLevel: CefrLevel
    ): MissionThreadRecommendation? {
        return generationMutex.withLock {
            /*
             * Another coroutine may have filled the queue
             * while this coroutine waited for the generation
             * lock.
             */
            if (
                recommendationCache.size() >=
                MissionThreadRecommendationCache
                    .TARGET_QUEUE_SIZE
            ) {
                return@withLock null
            }

            val selectedScenario =
                selectScenario(
                    cefrLevel =
                        cefrLevel
                )
                    ?: run {
                        Log.w(
                            USE_CASE_TAG,
                            "No available mission scenario " +
                                    "could be selected for " +
                                    cefrLevel.name
                        )

                        return@withLock null
                    }

            try {
                Log.d(
                    USE_CASE_TAG,
                    buildString {
                        append(
                            "Generating locked mission scenario. "
                        )
                        append("level=")
                        append(
                            cefrLevel.name
                        )
                        append(", scenario=")
                        append(
                            selectedScenario.type
                        )
                        append(", queueSize=")
                        append(
                            recommendationCache.size()
                        )
                    }
                )

                val recommendation =
                    aiGateway.generateRecommendation(
                        cefrLevel =
                            cefrLevel,
                        selectedScenario =
                            selectedScenario
                    )
                        ?: return@withLock null

                if (
                    !isValidRecommendation(
                        recommendation =
                            recommendation,
                        cefrLevel =
                            cefrLevel,
                        selectedScenario =
                            selectedScenario
                    )
                ) {
                    return@withLock null
                }

                val wasAdded =
                    recommendationCache.enqueue(
                        recommendation =
                            recommendation
                    )

                if (!wasAdded) {
                    Log.d(
                        USE_CASE_TAG,
                        buildString {
                            append(
                                "Discarded duplicate prefetched "
                            )
                            append("mission. scenario=")
                            append(
                                recommendation.scenarioType
                            )
                            append(", title=")
                            append(
                                recommendation.title
                            )
                        }
                    )

                    return@withLock null
                }

                Log.d(
                    USE_CASE_TAG,
                    buildString {
                        append(
                            "Prefetched mission recommendation. "
                        )
                        append("scenario=")
                        append(
                            recommendation.scenarioType
                        )
                        append(", title=")
                        append(
                            recommendation.title
                        )
                        append(", queueSize=")
                        append(
                            recommendationCache.size()
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
                    USE_CASE_TAG,
                    "Could not prefetch a mission-thread " +
                            "recommendation.",
                    throwable
                )

                null
            }
        }
    }

    /*
     * Selects one curriculum scenario while excluding:
     *
     * - Recently displayed scenarios
     * - Every scenario currently waiting in the queue
     *
     * Recent conversation text is intentionally empty.
     *
     * The featured mission cell should follow the CEFR
     * curriculum rather than repeatedly deriving café and
     * bakery missions from recent chats.
     */
    private fun selectScenario(
        cefrLevel: CefrLevel
    ): MissionScenarioDefinition? {
        val excludedScenarioTypes =
            recommendationCache
                .getExcludedScenarioTypes()

        val selectedScenario =
            selectMissionScenario(
                context =
                    MissionRecommendationContext(
                        cefrLevel =
                            cefrLevel,
                        recentConversationText =
                            "",
                        recentlyOfferedScenarioTypes =
                            excludedScenarioTypes,
                        recentlyCompletedScenarioTypes =
                            emptyList(),
                        adaptiveLearningFocuses =
                            emptyList(),
                        activeScenarioType =
                            null
                    )
            )

        if (selectedScenario != null) {
            Log.d(
                USE_CASE_TAG,
                buildString {
                    append(
                        "Selected scenario from CEFR catalog. "
                    )
                    append("level=")
                    append(
                        cefrLevel.name
                    )
                    append(", selected=")
                    append(
                        selectedScenario.type
                    )
                    append(", excluded=")
                    append(
                        excludedScenarioTypes
                    )
                }
            )
        }

        return selectedScenario
    }

    /*
     * Performs defensive validation even though the Qwen
     * gateway also validates its response.
     */
    private fun isValidRecommendation(
        recommendation:
        MissionThreadRecommendation,
        cefrLevel: CefrLevel,
        selectedScenario:
        MissionScenarioDefinition
    ): Boolean {
        /*
         * The gateway must preserve the Kotlin-selected
         * semantic scenario.
         */
        if (
            recommendation.scenarioType !=
            selectedScenario.type
        ) {
            Log.w(
                USE_CASE_TAG,
                buildString {
                    append(
                        "Qwen changed the locked mission scenario. "
                    )
                    append("expected=")
                    append(
                        selectedScenario.type
                    )
                    append(", actual=")
                    append(
                        recommendation.scenarioType
                    )
                }
            )

            return false
        }

        /*
         * Ensure the generated difficulty exactly matches the
         * requested CEFR level.
         */
        if (
            recommendation
                .opportunity
                .mission
                .difficulty
                .name !=
            cefrLevel.name
        ) {
            Log.w(
                USE_CASE_TAG,
                buildString {
                    append(
                        "Recommendation used the wrong CEFR level. "
                    )
                    append("expected=")
                    append(
                        cefrLevel.name
                    )
                    append(", actual=")
                    append(
                        recommendation
                            .opportunity
                            .mission
                            .difficulty
                            .name
                    )
                }
            )

            return false
        }

        if (
            recommendation.id.isBlank() ||
            recommendation.title.isBlank() ||
            recommendation.previewText.isBlank()
        ) {
            Log.w(
                USE_CASE_TAG,
                "Recommendation contained blank required data."
            )

            return false
        }

        val objectiveCount =
            recommendation
                .opportunity
                .mission
                .objectives
                .size

        if (
            objectiveCount !in
            MINIMUM_OBJECTIVE_COUNT..
            MAXIMUM_OBJECTIVE_COUNT
        ) {
            Log.w(
                USE_CASE_TAG,
                "Recommendation contained $objectiveCount " +
                        "objectives instead of 8 to 10."
            )

            return false
        }

        val hasRequiredObjective =
            recommendation
                .opportunity
                .mission
                .objectives
                .any { objective ->
                    objective.isRequired
                }

        if (!hasRequiredObjective) {
            Log.w(
                USE_CASE_TAG,
                "Recommendation did not contain a required " +
                        "objective."
            )

            return false
        }

        return true
    }

    private companion object {
        const val USE_CASE_TAG =
            "LoadMissionThread"

        const val MINIMUM_OBJECTIVE_COUNT =
            8

        const val MAXIMUM_OBJECTIVE_COUNT =
            10

        /*
         * Qwen responses can occasionally fail parsing or
         * validation.
         *
         * Fifteen attempts provide room to fill the queue
         * while preventing an endless retry loop.
         */
        const val MAXIMUM_PREFETCH_ATTEMPTS =
            15
    }
}