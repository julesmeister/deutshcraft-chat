package com.julesmeister.deutschcraft.feature.mission.data.cache

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionScenarioType
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendation
import javax.inject.Inject
import javax.inject.Singleton

/*
 * Process-local queue for thread-list mission
 * recommendations.
 *
 * Queue structure:
 *
 * Index 0:
 * Recommendation currently visible in the thread list.
 *
 * Index 1 to 4:
 * Hidden prefetched recommendations.
 *
 * The cache deliberately separates:
 *
 * - Queued scenarios
 * - Actually shown scenarios
 *
 * A hidden prefetched recommendation has not yet been shown
 * to the learner.
 */
@Singleton
class MissionThreadRecommendationCache
@Inject constructor() {

    private val recommendationQueue =
        ArrayDeque<MissionThreadRecommendation>()

    /*
     * Contains semantic scenario types that were actually
     * displayed in the featured mission cell.
     *
     * Hidden queue items are not recorded here until they
     * become visible.
     */
    private val shownScenarioTypes =
        ArrayDeque<MissionScenarioType>()

    private var currentRecommendationMarkedShown =
        false

    @Synchronized
    fun get():
            MissionThreadRecommendation? {
        return recommendationQueue.firstOrNull()
    }

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

    @Synchronized
    fun hasPrefetchedNext(): Boolean {
        return recommendationQueue.size >
                VISIBLE_RECOMMENDATION_COUNT
    }

    /*
     * Adds one generated recommendation to the end of the
     * queue.
     *
     * Rejected when:
     *
     * - The queue is already full
     * - The ID already exists
     * - The semantic scenario already exists in the queue
     * - Equivalent title or preview content already exists
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

        /*
         * If this is the first item, it becomes visible.
         *
         * It is not automatically marked shown here because
         * the presentation layer may not have displayed it
         * yet.
         */
        if (
            recommendationQueue.size ==
            VISIBLE_RECOMMENDATION_COUNT
        ) {
            currentRecommendationMarkedShown =
                false
        }

        return true
    }

    /*
     * Compatibility operation.
     *
     * Prefer enqueue() for normal queue management.
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

        currentRecommendationMarkedShown =
            false
    }

    /*
     * Records the current recommendation as actually shown.
     *
     * Repeated calls for the same visible recommendation do
     * not duplicate its history.
     */
    @Synchronized
    fun markCurrentShown() {
        if (currentRecommendationMarkedShown) {
            return
        }

        val currentRecommendation =
            recommendationQueue.firstOrNull()
                ?: return

        recordShownScenarioInternal(
            scenarioType =
                currentRecommendation.scenarioType
        )

        currentRecommendationMarkedShown =
            true
    }

    /*
     * Removes the current recommendation.
     *
     * The next prefetched recommendation becomes visible
     * immediately.
     */
    @Synchronized
    fun advance():
            MissionThreadRecommendation? {
        if (recommendationQueue.isNotEmpty()) {
            /*
             * Ensure the outgoing recommendation is recorded
             * even if the UI did not call markCurrentShown().
             */
            if (!currentRecommendationMarkedShown) {
                recommendationQueue
                    .firstOrNull()
                    ?.let { current ->
                        recordShownScenarioInternal(
                            scenarioType =
                                current.scenarioType
                        )
                    }
            }

            recommendationQueue.removeFirst()
        }

        currentRecommendationMarkedShown =
            false

        return recommendationQueue.firstOrNull()
    }

    @Synchronized
    fun remove(
        recommendationId: String
    ) {
        val removingCurrent =
            recommendationQueue
                .firstOrNull()
                ?.id ==
                    recommendationId

        recommendationQueue.removeAll { recommendation ->
            recommendation.id ==
                    recommendationId
        }

        if (removingCurrent) {
            currentRecommendationMarkedShown =
                false
        }
    }

    /*
     * Returns all scenario types that should be excluded from
     * new queue generation.
     *
     * Includes:
     *
     * - Recently shown scenarios
     * - Every scenario already reserved in the queue
     */
    @Synchronized
    fun getExcludedScenarioTypes():
            List<MissionScenarioType> {
        return (
                shownScenarioTypes.toList() +
                        recommendationQueue.map { recommendation ->
                            recommendation.scenarioType
                        }
                )
            .distinct()
    }

    @Synchronized
    fun getShownScenarioTypes():
            List<MissionScenarioType> {
        return shownScenarioTypes.toList()
    }

    @Synchronized
    fun getQueuedScenarioTypes():
            List<MissionScenarioType> {
        return recommendationQueue
            .map { recommendation ->
                recommendation.scenarioType
            }
            .distinct()
    }

    @Synchronized
    fun recordShownScenario(
        scenarioType: MissionScenarioType
    ) {
        recordShownScenarioInternal(
            scenarioType =
                scenarioType
        )
    }

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
     * Removes queue items belonging to a different learner
     * level.
     *
     * Shown history remains intact.
     */
    @Synchronized
    fun retainLevel(
        cefrLevel: CefrLevel
    ) {
        val previousCurrentId =
            recommendationQueue
                .firstOrNull()
                ?.id

        recommendationQueue.removeAll { recommendation ->
            recommendation
                .opportunity
                .mission
                .difficulty
                .name !=
                    cefrLevel.name
        }

        val currentIdAfterFiltering =
            recommendationQueue
                .firstOrNull()
                ?.id

        if (
            previousCurrentId !=
            currentIdAfterFiltering
        ) {
            currentRecommendationMarkedShown =
                false
        }
    }

    /*
     * Clears visible and prefetched recommendations.
     *
     * Shown history remains intact.
     */
    @Synchronized
    fun clear() {
        recommendationQueue.clear()

        currentRecommendationMarkedShown =
            false
    }

    /*
     * Complete reset intended for tests or an explicit user
     * history reset.
     */
    @Synchronized
    fun clearAll() {
        recommendationQueue.clear()
        shownScenarioTypes.clear()

        currentRecommendationMarkedShown =
            false
    }

    private fun recordShownScenarioInternal(
        scenarioType: MissionScenarioType
    ) {
        shownScenarioTypes.remove(
            scenarioType
        )

        shownScenarioTypes.addLast(
            scenarioType
        )

        while (
            shownScenarioTypes.size >
            MAXIMUM_SHOWN_SCENARIO_COUNT
        ) {
            shownScenarioTypes.removeFirst()
        }
    }

    private fun recommendationsAreEquivalent(
        first: MissionThreadRecommendation,
        second: MissionThreadRecommendation
    ): Boolean {
        val normalizedFirstTitle =
            normalize(
                value =
                    first.title
            )

        val normalizedSecondTitle =
            normalize(
                value =
                    second.title
            )

        val normalizedFirstPreview =
            normalize(
                value =
                    first.previewText
            )

        val normalizedSecondPreview =
            normalize(
                value =
                    second.previewText
            )

        return normalizedFirstTitle ==
                normalizedSecondTitle ||
                normalizedFirstPreview ==
                normalizedSecondPreview
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

        private const val VISIBLE_RECOMMENDATION_COUNT =
            1

        private const val MAXIMUM_SHOWN_SCENARIO_COUNT =
            12

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