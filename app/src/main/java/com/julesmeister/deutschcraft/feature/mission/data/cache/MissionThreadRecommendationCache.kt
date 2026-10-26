package com.julesmeister.deutschcraft.feature.mission.data.cache

import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionScenarioType
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendation
import javax.inject.Inject
import javax.inject.Singleton

/*
 * Process-local cache for the featured mission recommendation.
 *
 * It stores:
 *
 * - The currently visible recommendation
 * - Recently shown semantic scenario types
 *
 * Semantic history prevents renamed versions of the same
 * scenario from appearing repeatedly.
 */
@Singleton
class MissionThreadRecommendationCache
@Inject constructor() {

    private var cachedRecommendation:
            MissionThreadRecommendation? =
        null

    private val recentScenarioTypes =
        ArrayDeque<MissionScenarioType>()

    @Synchronized
    fun get():
            MissionThreadRecommendation? {
        return cachedRecommendation
    }

    /*
     * Replaces the visible recommendation and records its
     * semantic scenario type.
     */
    @Synchronized
    fun replace(
        recommendation:
        MissionThreadRecommendation
    ) {
        cachedRecommendation =
            recommendation

        recordScenarioTypeInternal(
            scenarioType =
                recommendation.scenarioType
        )
    }

    /*
     * Returns recent types from oldest to newest.
     */
    @Synchronized
    fun getRecentScenarioTypes():
            List<MissionScenarioType> {
        return recentScenarioTypes.toList()
    }

    /*
     * Explicitly records a type without replacing the visible
     * recommendation.
     */
    @Synchronized
    fun recordScenarioType(
        scenarioType: MissionScenarioType
    ) {
        recordScenarioTypeInternal(
            scenarioType =
                scenarioType
        )
    }

    /*
     * Clears only the visible recommendation.
     *
     * Recent scenario history remains available so the next
     * recommendation cannot immediately repeat the same
     * scenario family.
     */
    @Synchronized
    fun clear() {
        cachedRecommendation =
            null
    }

    /*
     * Use only for testing or an explicit recommendation
     * history reset.
     */
    @Synchronized
    fun clearAll() {
        cachedRecommendation =
            null

        recentScenarioTypes.clear()
    }

    private fun recordScenarioTypeInternal(
        scenarioType: MissionScenarioType
    ) {
        /*
         * Move an existing type to the newest position.
         */
        recentScenarioTypes.remove(
            scenarioType
        )

        recentScenarioTypes.addLast(
            scenarioType
        )

        while (
            recentScenarioTypes.size >
            MAXIMUM_RECENT_SCENARIO_COUNT
        ) {
            recentScenarioTypes.removeFirst()
        }
    }

    private companion object {
        /*
         * The last six semantic scenario types are excluded
         * from normal selection.
         */
        const val MAXIMUM_RECENT_SCENARIO_COUNT =
            6
    }
}