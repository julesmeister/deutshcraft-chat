package com.julesmeister.deutschcraft.feature.mission.domain.usecase

import android.util.Log
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.mission.data.ai.MissionThreadRecommendationAiGateway
import com.julesmeister.deutschcraft.feature.mission.data.cache.MissionThreadRecommendationCache
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendation
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class LoadMissionThreadRecommendationUseCase
@Inject constructor(
    private val aiGateway:
    MissionThreadRecommendationAiGateway,
    private val recommendationCache:
    MissionThreadRecommendationCache
) {

    private val loadingMutex =
        Mutex()

    suspend operator fun invoke(
        cefrLevel: CefrLevel,
        forceRefresh: Boolean = false
    ): MissionThreadRecommendation? {
        if (!forceRefresh) {
            recommendationCache
                .get()
                ?.let { cachedRecommendation ->
                    if (
                        cachedRecommendation
                            .opportunity
                            .mission
                            .difficulty
                            .name ==
                        cefrLevel.name
                    ) {
                        return cachedRecommendation
                    }

                    /*
                     * The learner's level changed, so the
                     * existing recommendation is no longer
                     * guaranteed to be appropriate.
                     */
                    recommendationCache.clear()
                }
        }

        return loadingMutex.withLock {
            if (!forceRefresh) {
                recommendationCache
                    .get()
                    ?.let { cachedRecommendation ->
                        if (
                            cachedRecommendation
                                .opportunity
                                .mission
                                .difficulty
                                .name ==
                            cefrLevel.name
                        ) {
                            return@withLock cachedRecommendation
                        }

                        recommendationCache.clear()
                    }
            }

            generateRecommendation(
                cefrLevel =
                    cefrLevel
            )
        }
    }

    private suspend fun generateRecommendation(
        cefrLevel: CefrLevel
    ): MissionThreadRecommendation? {
        return try {
            val recommendation =
                aiGateway.generateRecommendation(
                    cefrLevel =
                        cefrLevel
                )
                    ?: return null

            /*
             * Defensive validation remains here even though
             * the Qwen gateway already validates its result.
             *
             * The use case protects callers if another
             * gateway implementation is added later.
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
                    "Rejected a recommendation with an " +
                            "unexpected CEFR level."
                )

                return null
            }

            if (
                recommendation.title.isBlank() ||
                recommendation.previewText.isBlank() ||
                recommendation
                    .opportunity
                    .mission
                    .objectives
                    .size !in
                MINIMUM_OBJECTIVE_COUNT..
                MAXIMUM_OBJECTIVE_COUNT
            ) {
                Log.w(
                    USE_CASE_TAG,
                    "Rejected an incomplete mission-thread " +
                            "recommendation."
                )

                return null
            }

            recommendationCache.replace(
                recommendation =
                    recommendation
            )

            recommendation
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            /*
             * The normal thread list must remain usable if
             * recommendation generation fails.
             */
            Log.e(
                USE_CASE_TAG,
                "Could not load a mission-thread " +
                        "recommendation.",
                throwable
            )

            null
        }
    }

    private companion object {
        const val USE_CASE_TAG =
            "LoadMissionThread"

        const val MINIMUM_OBJECTIVE_COUNT =
            3

        const val MAXIMUM_OBJECTIVE_COUNT =
            5
    }
}