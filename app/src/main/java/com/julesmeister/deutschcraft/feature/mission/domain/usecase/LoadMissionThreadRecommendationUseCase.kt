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
 * Loads the featured mission recommendation displayed at the
 * top of the thread-list screen.
 *
 * Kotlin selects a CEFR-appropriate semantic scenario before
 * Qwen is called.
 *
 * Qwen is then allowed to expand the selected scenario, but
 * it is not allowed to replace it.
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

    private val loadingMutex =
        Mutex()

    suspend operator fun invoke(
        cefrLevel: CefrLevel,
        forceRefresh: Boolean =
            false
    ): MissionThreadRecommendation? {
        /*
         * Normal screen restoration can use the cached value
         * when it still matches the learner's CEFR level.
         */
        if (!forceRefresh) {
            validCachedRecommendation(
                cefrLevel =
                    cefrLevel
            )?.let { recommendation ->
                return recommendation
            }
        }

        return loadingMutex.withLock {
            /*
             * Check the cache again after obtaining the lock.
             *
             * Another coroutine may already have loaded the
             * recommendation while this coroutine waited.
             */
            if (!forceRefresh) {
                validCachedRecommendation(
                    cefrLevel =
                        cefrLevel
                )?.let { recommendation ->
                    return@withLock recommendation
                }
            }

            val previousRecommendation =
                recommendationCache.get()

            val recentScenarioTypes =
                recommendationCache
                    .getRecentScenarioTypes()
                    .toMutableList()

            /*
             * A forced refresh means the currently visible
             * semantic scenario must not be selected again.
             */
            if (
                forceRefresh &&
                previousRecommendation != null
            ) {
                recentScenarioTypes +=
                    previousRecommendation.scenarioType
            }

            val selectedScenario =
                selectMissionScenario(
                    context =
                        MissionRecommendationContext(
                            cefrLevel =
                                cefrLevel,

                            /*
                             * The featured thread-list cell is
                             * curriculum driven.
                             *
                             * It is not derived from the latest
                             * coffee or bakery conversation.
                             */
                            recentConversationText =
                                "",
                            recentlyOfferedScenarioTypes =
                                recentScenarioTypes.distinct(),
                            recentlyCompletedScenarioTypes =
                                emptyList(),
                            adaptiveLearningFocuses =
                                emptyList(),
                            activeScenarioType =
                                null
                        )
                )
                    ?: run {
                        Log.w(
                            USE_CASE_TAG,
                            "No CEFR mission scenario could " +
                                    "be selected for " +
                                    cefrLevel.name
                        )

                        return@withLock previousRecommendation
                    }

            Log.d(
                USE_CASE_TAG,
                buildString {
                    append("Selected locked scenario. ")
                    append("level=")
                    append(cefrLevel.name)
                    append(", scenarioType=")
                    append(selectedScenario.type)
                    append(", title=")
                    append(selectedScenario.title)
                    append(", forceRefresh=")
                    append(forceRefresh)
                    append(", excluded=")
                    append(recentScenarioTypes.distinct())
                }
            )

            generateRecommendation(
                cefrLevel =
                    cefrLevel,
                selectedScenario =
                    selectedScenario,
                previousRecommendation =
                    previousRecommendation
            )
        }
    }

    private fun validCachedRecommendation(
        cefrLevel: CefrLevel
    ): MissionThreadRecommendation? {
        val cachedRecommendation =
            recommendationCache.get()
                ?: return null

        val cachedDifficulty =
            cachedRecommendation
                .opportunity
                .mission
                .difficulty
                .name

        if (
            cachedDifficulty ==
            cefrLevel.name
        ) {
            return cachedRecommendation
        }

        /*
         * Clear only the visible recommendation.
         *
         * Scenario history remains intact.
         */
        recommendationCache.clear()

        return null
    }

    private suspend fun generateRecommendation(
        cefrLevel: CefrLevel,
        selectedScenario:
        MissionScenarioDefinition,
        previousRecommendation:
        MissionThreadRecommendation?
    ): MissionThreadRecommendation? {
        return try {
            val recommendation =
                aiGateway.generateRecommendation(
                    cefrLevel =
                        cefrLevel,
                    selectedScenario =
                        selectedScenario
                )
                    ?: return previousRecommendation

            if (
                recommendation.scenarioType !=
                selectedScenario.type
            ) {
                Log.w(
                    USE_CASE_TAG,
                    buildString {
                        append(
                            "Rejected recommendation because "
                        )
                        append(
                            "the semantic scenario changed. "
                        )
                        append("expected=")
                        append(selectedScenario.type)
                        append(", actual=")
                        append(
                            recommendation.scenarioType
                        )
                    }
                )

                return previousRecommendation
            }

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

                return previousRecommendation
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

                return previousRecommendation
            }

            /*
             * Prevent the same generated object from replacing
             * itself during a forced refresh.
             */
            if (
                previousRecommendation != null &&
                recommendationsAreEquivalent(
                    first =
                        previousRecommendation,
                    second =
                        recommendation
                )
            ) {
                Log.w(
                    USE_CASE_TAG,
                    "The generated recommendation matched " +
                            "the previous recommendation."
                )

                recommendationCache.recordScenarioType(
                    scenarioType =
                        selectedScenario.type
                )

                return previousRecommendation
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
            Log.e(
                USE_CASE_TAG,
                "Could not load a mission-thread " +
                        "recommendation.",
                throwable
            )

            /*
             * Keep the current recommendation visible when
             * generating the next one fails.
             */
            previousRecommendation
        }
    }

    private fun recommendationsAreEquivalent(
        first: MissionThreadRecommendation,
        second: MissionThreadRecommendation
    ): Boolean {
        return normalize(
            value =
                first.title
        ) ==
                normalize(
                    value =
                        second.title
                ) &&
                normalize(
                    value =
                        first.previewText
                ) ==
                normalize(
                    value =
                        second.previewText
                )
    }

    private fun normalize(
        value: String
    ): String {
        return value
            .trim()
            .lowercase()
            .replace(
                NON_ALPHANUMERIC_REGEX,
                " "
            )
            .replace(
                MULTIPLE_WHITESPACE_REGEX,
                " "
            )
            .trim()
    }

    private companion object {
        const val USE_CASE_TAG =
            "LoadMissionThread"

        /*
         * Thread-list recommendations now use the richer
         * eight-to-ten-step mission design.
         */
        const val MINIMUM_OBJECTIVE_COUNT =
            8

        const val MAXIMUM_OBJECTIVE_COUNT =
            10

        val NON_ALPHANUMERIC_REGEX =
            Regex(
                "[^\\p{L}\\p{N}]+"
            )

        val MULTIPLE_WHITESPACE_REGEX =
            Regex(
                "\\s+"
            )
    }
}