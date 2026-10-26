package com.julesmeister.deutschcraft.feature.mission.data.cache

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionScenarioType
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendation
import javax.inject.Inject
import javax.inject.Singleton

/*
 * Process-local queue for featured mission recommendations.
 *
 * The first item is visible in the thread list.
 * The remaining items are prefetched and hidden.
 *
 * Target state:
 *
 * 1 visible recommendation
 * 4 hidden recommendations
 * 5 total recommendations
 */
@Singleton
class MissionThreadRecommendationCache
@Inject constructor() {

    private val recommendationQueue =
        ArrayDeque<MissionThreadRecommendation>()

    /*
     * Semantic recommendation history remains separate from
     * the queue.
     *
     * Clearing or consuming queue items must not erase this
     * history, otherwise café and bakery scenarios could
     * immediately return.
     */
    private val recentScenarioTypes =
        ArrayDeque<MissionScenarioType>()

    /*
     * Returns the recommendation currently displayed in the
     * thread-list mission cell.
     */
    @Synchronized
    fun get():
            MissionThreadRecommendation? {
        return recommendationQueue.firstOrNull()
    }

    /*
     * Returns all queued recommendations in display order.
     */
    @Synchronized
    fun getAll():
            List<MissionThreadRecommendation> {
        return recommendationQueue.toList()
    }

    @Synchronized
    fun size(): Int {
        return recommendationQueue.size
    }

    @Synchronized
    fun isEmpty(): Boolean {
        return recommendationQueue.isEmpty()
    }

    /*
     * Adds a prefetched recommendation to the end of the
     * queue.
     *
     * Duplicate IDs, semantic scenario types, and equivalent
     * recommendation content are rejected.
     */
    @Synchronized
    fun enqueue(
        recommendation:
        MissionThreadRecommendation
    ): Boolean {
        if (
            recommendationQueue.size >=
            TARGET_QUEUE_SIZE
        ) {
            return false
        }

        if (
            recommendationQueue.any { queued ->
                queued.id ==
                        recommendation.id
            }
        ) {
            return false
        }

        if (
            recommendationQueue.any { queued ->
                queued.scenarioType ==
                        recommendation.scenarioType
            }
        ) {
            return false
        }

        if (
            recommendationQueue.any { queued ->
                recommendationsAreEquivalent(
                    first =
                        queued,
                    second =
                        recommendation
                )
            }
        ) {
            return false
        }

        recommendationQueue.addLast(
            recommendation
        )

        recordScenarioTypeInternal(
            scenarioType =
                recommendation.scenarioType
        )

        return true
    }

    /*
     * Compatibility method for older callers.
     *
     * Replaces the complete queue with one recommendation.
     * New code should normally use enqueue().
     */
    @Synchronized
    fun replace(
        recommendation:
        MissionThreadRecommendation
    ) {
        recommendationQueue.clear()

        recommendationQueue.addLast(
            recommendation
        )

        recordScenarioTypeInternal(
            scenarioType =
                recommendation.scenarioType
        )
    }

    /*
     * Consumes the currently displayed recommendation.
     *
     * The next prefetched item immediately becomes visible.
     */
    @Synchronized
    fun advance():
            MissionThreadRecommendation? {
        if (recommendationQueue.isNotEmpty()) {
            recommendationQueue.removeFirst()
        }

        return recommendationQueue.firstOrNull()
    }

    /*
     * Removes a recommendation after it has been started.
     */
    @Synchronized
    fun remove(
        recommendationId: String
    ) {
        recommendationQueue.removeAll { recommendation ->
            recommendation.id ==
                    recommendationId
        }
    }

    /*
     * Returns scenario types that must not be selected while
     * prefetching.
     *
     * This includes:
     *
     * - Recently shown scenarios
     * - Every scenario already waiting in the queue
     */
    @Synchronized
    fun getExcludedScenarioTypes():
            List<MissionScenarioType> {
        return (
                recentScenarioTypes.toList() +
                        recommendationQueue.map { recommendation ->
                            recommendation.scenarioType
                        }
                )
            .distinct()
    }

    @Synchronized
    fun getRecentScenarioTypes():
            List<MissionScenarioType> {
        return recentScenarioTypes.toList()
    }

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
     * Checks whether the visible recommendation still matches
     * the requested learner level.
     */
    @Synchronized
    fun currentMatchesLevel(
        cefrLevel: CefrLevel
    ): Boolean {
        return recommendationQueue
            .firstOrNull()
            ?.opportunity
            ?.mission
            ?.difficulty
            ?.name ==
                cefrLevel.name
    }

    /*
     * Removes recommendations belonging to another CEFR
     * level.
     */
    @Synchronized
    fun retainLevel(
        cefrLevel: CefrLevel
    ) {
        recommendationQueue.removeAll { recommendation ->
            recommendation
                .opportunity
                .mission
                .difficulty
                .name !=
                    cefrLevel.name
        }
    }

    /*
     * Clears the visible and prefetched queue.
     *
     * Semantic scenario history remains intact.
     */
    @Synchronized
    fun clear() {
        recommendationQueue.clear()
    }

    /*
     * Full reset intended for tests or an explicit history
     * reset.
     */
    @Synchronized
    fun clearAll() {
        recommendationQueue.clear()
        recentScenarioTypes.clear()
    }

    private fun recordScenarioTypeInternal(
        scenarioType: MissionScenarioType
    ) {
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

    private fun recommendationsAreEquivalent(
        first: MissionThreadRecommendation,
        second: MissionThreadRecommendation
    ): Boolean {
        return normalize(
            first.title
        ) ==
                normalize(
                    second.title
                ) ||
                normalize(
                    first.previewText
                ) ==
                normalize(
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

    companion object {
        const val TARGET_QUEUE_SIZE =
            5

        private const val MAXIMUM_RECENT_SCENARIO_COUNT =
            10

        private val NON_ALPHANUMERIC_REGEX =
            Regex(
                "[^\\p{L}\\p{N}]+"
            )

        private val MULTIPLE_WHITESPACE_REGEX =
            Regex(
                "\\s+"
            )
    }
}