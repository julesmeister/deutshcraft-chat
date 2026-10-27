package com.julesmeister.deutschcraft.feature.mission.domain.usecase

import android.util.Log
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.mission.data.ai.MissionThreadRecommendationAiGateway
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionRecommendationContext
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionScenarioDefinition
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionScenarioType
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendation
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendationStore
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.SelectMissionScenarioUseCase
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/*
 * Loads and maintains the persistent mission recommendation
 * pool used by the thread-list featured mission cell.
 *
 * Room is the source of truth.
 *
 * Initial loading:
 *
 * 1. Return an existing persisted recommendation immediately.
 * 2. If Room has none, generate and persist one.
 * 3. The coordinator then fills the pool to five in the
 *    background.
 *
 * Left swipe:
 *
 * 1. Mark the current recommendation SKIPPED.
 * 2. Return the next persisted recommendation immediately.
 * 3. If no next recommendation exists, generate one fallback.
 * 4. Refill the pool to five in the background.
 */
class LoadMissionThreadRecommendationUseCase
@Inject constructor(
    private val aiGateway:
    MissionThreadRecommendationAiGateway,
    private val recommendationStore:
    MissionThreadRecommendationStore,
    private val selectMissionScenario:
    SelectMissionScenarioUseCase
) {

    /*
     * Protects visible recommendation loading and queue
     * advancement.
     *
     * A swipe cannot modify recommendation statuses while an
     * initial load is performing the same operation.
     */
    private val loadingMutex =
        Mutex()

    /*
     * Prevents multiple simultaneous pool-fill loops.
     */
    private val prefetchMutex =
        Mutex()

    /*
     * Serializes scenario selection and AI generation.
     *
     * This prevents two callers from selecting the same
     * scenario before either result is persisted.
     */
    private val generationMutex =
        Mutex()

    /*
     * Loads the recommendation that should be displayed.
     *
     * forceRefresh = false:
     *
     * - Restore a DISPLAYED recommendation when one exists.
     * - Otherwise display the oldest READY recommendation.
     * - Generate one if Room is empty.
     *
     * forceRefresh = true:
     *
     * - Mark the current recommendation SKIPPED.
     * - Load the next READY recommendation.
     * - Generate one only if the persisted pool is exhausted.
     */
    suspend operator fun invoke(
        cefrLevel: CefrLevel,
        forceRefresh: Boolean =
            false,
        externalExcludedScenarioTypes:
        Set<MissionScenarioType> =
            emptySet()
    ): MissionThreadRecommendation? {
        return loadingMutex.withLock {
            recommendationStore.performMaintenance(
                cefrLevel =
                    cefrLevel
            )

            val currentRecommendation =
                recommendationStore.getNextEligible(
                    cefrLevel =
                        cefrLevel
                )

            if (
                forceRefresh &&
                currentRecommendation != null
            ) {
                recommendationStore.markSkipped(
                    recommendationId =
                        currentRecommendation.id
                )

                Log.d(
                    USE_CASE_TAG,
                    buildString {
                        append(
                            "Skipped visible recommendation. "
                        )
                        append("id=")
                        append(
                            currentRecommendation.id
                        )
                        append(", scenario=")
                        append(
                            currentRecommendation.scenarioType
                        )
                    }
                )
            }

            /*
             * After a forced refresh, the skipped row is no
             * longer eligible, so this query returns the next
             * persisted recommendation.
             *
             * During ordinary loading, it returns the existing
             * DISPLAYED or oldest READY row.
             */
            val persistedRecommendation =
                recommendationStore.getNextEligible(
                    cefrLevel =
                        cefrLevel
                )

            if (persistedRecommendation != null) {
                recommendationStore.markDisplayed(
                    recommendationId =
                        persistedRecommendation.id
                )

                Log.d(
                    USE_CASE_TAG,
                    buildString {
                        append(
                            "Loaded persisted recommendation. "
                        )
                        append("id=")
                        append(
                            persistedRecommendation.id
                        )
                        append(", scenario=")
                        append(
                            persistedRecommendation.scenarioType
                        )
                        append(", forceRefresh=")
                        append(
                            forceRefresh
                        )
                    }
                )

                return@withLock persistedRecommendation
            }

            /*
             * No persisted recommendation is available.
             *
             * Generate only one now so the UI can show its
             * first recommendation without waiting for the
             * complete five-item pool.
             */
            generateAndPersistOne(
                cefrLevel =
                    cefrLevel,
                externalExcludedScenarioTypes =
                    externalExcludedScenarioTypes
            )

            val generatedRecommendation =
                recommendationStore.getNextEligible(
                    cefrLevel =
                        cefrLevel
                )

            if (generatedRecommendation != null) {
                recommendationStore.markDisplayed(
                    recommendationId =
                        generatedRecommendation.id
                )
            }

            generatedRecommendation
        }
    }

    /*
     * Explicit operation used by the coordinator when the
     * learner swipes left.
     */
    suspend fun advance(
        cefrLevel: CefrLevel,
        externalExcludedScenarioTypes:
        Set<MissionScenarioType> =
            emptySet()
    ): MissionThreadRecommendation? {
        return invoke(
            cefrLevel =
                cefrLevel,
            forceRefresh =
                true,
            externalExcludedScenarioTypes =
                externalExcludedScenarioTypes
        )
    }

    /*
     * Ensures Room contains five eligible recommendations.
     *
     * This runs after the first recommendation becomes
     * visible.
     *
     * Every successfully generated recommendation is saved
     * immediately. Therefore:
     *
     * - The pool survives process death.
     * - The pool survives app restarts.
     * - The learner can swipe through already-generated
     *   recommendations quickly.
     */
    suspend fun prefetchUntilFull(
        cefrLevel: CefrLevel,
        externalExcludedScenarioTypes:
        Set<MissionScenarioType> =
            emptySet(),
        onRecommendationAvailable:
            (
            recommendation:
            MissionThreadRecommendation,
            availableCount: Int
        ) -> Unit =
            { _, _ -> }
    ) {
        prefetchMutex.withLock {
            recommendationStore.performMaintenance(
                cefrLevel =
                    cefrLevel
            )

            var attemptCount =
                0

            var consecutiveFailureCount =
                0

            while (
                recommendationStore.countEligible(
                    cefrLevel =
                        cefrLevel
                ) <
                TARGET_RECOMMENDATION_COUNT &&
                attemptCount <
                MAXIMUM_GENERATION_ATTEMPTS &&
                consecutiveFailureCount <
                MAXIMUM_CONSECUTIVE_FAILURES
            ) {
                attemptCount +=
                    1

                val recommendation =
                    generateAndPersistOne(
                        cefrLevel =
                            cefrLevel,
                        externalExcludedScenarioTypes =
                            externalExcludedScenarioTypes
                    )

                if (recommendation == null) {
                    consecutiveFailureCount +=
                        1
                } else {
                    consecutiveFailureCount =
                        0

                    val availableCount =
                        recommendationStore.countEligible(
                            cefrLevel =
                                cefrLevel
                        )

                    onRecommendationAvailable(
                        recommendation,
                        availableCount
                    )

                    Log.d(
                        USE_CASE_TAG,
                        buildString {
                            append(
                                "Persisted prefetched recommendation. "
                            )
                            append("scenario=")
                            append(
                                recommendation.scenarioType
                            )
                            append(", available=")
                            append(
                                availableCount
                            )
                        }
                    )
                }
            }

            val finalCount =
                recommendationStore.countEligible(
                    cefrLevel =
                        cefrLevel
                )

            Log.d(
                USE_CASE_TAG,
                buildString {
                    append(
                        "Recommendation prefetch cycle finished. "
                    )
                    append("level=")
                    append(
                        cefrLevel.name
                    )
                    append(", available=")
                    append(
                        finalCount
                    )
                    append(", target=")
                    append(
                        TARGET_RECOMMENDATION_COUNT
                    )
                    append(", attempts=")
                    append(
                        attemptCount
                    )
                    append(", consecutiveFailures=")
                    append(
                        consecutiveFailureCount
                    )
                }
            )
        }
    }

    /*
     * Marks the persisted recommendation as STARTED after its
     * real Conversation and ConversationMission have been
     * created.
     */
    suspend fun markStarted(
        recommendationId: String,
        conversationId: String,
        conversationMissionId: String
    ) {
        recommendationStore.markStarted(
            recommendationId =
                recommendationId,
            conversationId =
                conversationId,
            conversationMissionId =
                conversationMissionId
        )
    }

    /*
     * Marks the recommendation COMPLETED when its linked
     * ConversationMission finishes.
     */
    suspend fun markCompleted(
        conversationMissionId: String
    ) {
        recommendationStore.markCompletedByMissionId(
            conversationMissionId =
                conversationMissionId
        )
    }

    /*
     * Exposed for diagnostics and coordinator decisions.
     */
    suspend fun availableCount(
        cefrLevel: CefrLevel
    ): Int {
        return recommendationStore.countEligible(
            cefrLevel =
                cefrLevel
        )
    }

    /*
     * Generates and persists one distinct recommendation.
     *
     * Scenario selection and generation are serialized so the
     * same scenario cannot be selected concurrently.
     */
    private suspend fun generateAndPersistOne(
        cefrLevel: CefrLevel,
        externalExcludedScenarioTypes:
        Set<MissionScenarioType>
    ): MissionThreadRecommendation? {
        return generationMutex.withLock {
            val availableCount =
                recommendationStore.countEligible(
                    cefrLevel =
                        cefrLevel
                )

            if (
                availableCount >=
                TARGET_RECOMMENDATION_COUNT
            ) {
                return@withLock null
            }

            val persistedScenarioTypes =
                recommendationStore
                    .getRecentScenarioTypes(
                        cefrLevel =
                            cefrLevel
                    )

            /*
             * Combine:
             *
             * - Persisted recommendation history
             * - Existing thread scenario types
             * - Active/completed mission scenario types when
             *   those are supplied by the coordinator
             */
            val completeExclusions =
                expandScenarioFamilyExclusions(
                    excludedScenarioTypes =
                        persistedScenarioTypes +
                                externalExcludedScenarioTypes
                )

            val selectedScenario =
                selectScenario(
                    cefrLevel =
                        cefrLevel,
                    excludedScenarioTypes =
                        completeExclusions
                )
                    ?: run {
                        Log.w(
                            USE_CASE_TAG,
                            buildString {
                                append(
                                    "No unexcluded scenario was "
                                )
                                append(
                                    "available. level="
                                )
                                append(
                                    cefrLevel.name
                                )
                                append(", exclusions=")
                                append(
                                    completeExclusions
                                )
                            }
                        )

                        return@withLock null
                    }

            /*
             * Defensive duplicate check.
             *
             * The selector already receives the exclusions,
             * but the store also protects against a scenario
             * already persisted as READY, DISPLAYED, or
             * STARTED.
             */
            val scenarioAlreadyActive =
                recommendationStore.hasActiveScenario(
                    cefrLevel =
                        cefrLevel,
                    scenarioType =
                        selectedScenario.type
                )

            if (scenarioAlreadyActive) {
                Log.d(
                    USE_CASE_TAG,
                    "Scenario was already active: " +
                            selectedScenario.type
                )

                return@withLock null
            }

            try {
                Log.d(
                    USE_CASE_TAG,
                    buildString {
                        append(
                            "Generating persisted recommendation. "
                        )
                        append("level=")
                        append(
                            cefrLevel.name
                        )
                        append(", scenario=")
                        append(
                            selectedScenario.type
                        )
                        append(", availableBefore=")
                        append(
                            availableCount
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

                recommendationStore.save(
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
                 * One failed recommendation must not break the
                 * thread list or discard already-persisted
                 * recommendations.
                 */
                Log.e(
                    USE_CASE_TAG,
                    "Recommendation generation or persistence " +
                            "failed.",
                    throwable
                )

                null
            }
        }
    }

    /*
     * Selects a CEFR curriculum scenario.
     *
     * Recent conversation text is intentionally empty because
     * the featured recommendation cell should not repeatedly
     * derive suggestions from coffee-related conversations.
     */
    private fun selectScenario(
        cefrLevel: CefrLevel,
        excludedScenarioTypes:
        Set<MissionScenarioType>
    ): MissionScenarioDefinition? {
        val selectedScenario =
            selectMissionScenario(
                context =
                    MissionRecommendationContext(
                        cefrLevel =
                            cefrLevel,
                        recentConversationText =
                            "",
                        recentlyOfferedScenarioTypes =
                            excludedScenarioTypes.toList(),
                        recentlyCompletedScenarioTypes =
                            emptyList(),
                        adaptiveLearningFocuses =
                            emptyList(),
                        activeScenarioType =
                            null
                    )
            )

        /*
         * This final guard prevents a selector fallback from
         * silently returning an excluded scenario.
         */
        return selectedScenario
            ?.takeIf { scenario ->
                scenario.type !in
                        excludedScenarioTypes
            }
    }

    /*
     * Temporarily excludes related scenario families.
     *
     * This prevents recommendation sequences such as:
     *
     * Café ordering
     * Bakery ordering
     * Restaurant ordering
     * Grocery shopping
     *
     * even though each item has a different title.
     */
    private fun expandScenarioFamilyExclusions(
        excludedScenarioTypes:
        Set<MissionScenarioType>
    ): Set<MissionScenarioType> {
        val expandedExclusions =
            excludedScenarioTypes
                .toMutableSet()

        if (
            excludedScenarioTypes.any { scenarioType ->
                scenarioType in
                        FOOD_SERVICE_SCENARIOS
            }
        ) {
            expandedExclusions.addAll(
                FOOD_SERVICE_SCENARIOS
            )
        }

        if (
            excludedScenarioTypes.any { scenarioType ->
                scenarioType in
                        SHOPPING_SCENARIOS
            }
        ) {
            expandedExclusions.addAll(
                SHOPPING_SCENARIOS
            )
        }

        return expandedExclusions
    }

    private fun isValidRecommendation(
        recommendation:
        MissionThreadRecommendation,
        cefrLevel: CefrLevel,
        selectedScenario:
        MissionScenarioDefinition
    ): Boolean {
        if (
            recommendation.scenarioType !=
            selectedScenario.type
        ) {
            Log.w(
                USE_CASE_TAG,
                buildString {
                    append(
                        "AI changed the locked scenario. "
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
                "Recommendation used an unexpected CEFR level."
            )

            return false
        }

        if (
            recommendation.id.isBlank() ||
            recommendation.title.isBlank() ||
            recommendation.previewText.isBlank() ||
            recommendation
                .opportunity
                .reason
                .isBlank()
        ) {
            Log.w(
                USE_CASE_TAG,
                "Recommendation contained blank required data."
            )

            return false
        }

        val mission =
            recommendation
                .opportunity
                .mission

        if (
            mission.description.isBlank() ||
            mission.scenario.isBlank() ||
            mission.learnerRole.isBlank() ||
            mission.assistantRole.isBlank()
        ) {
            Log.w(
                USE_CASE_TAG,
                "Recommendation contained incomplete mission " +
                        "data."
            )

            return false
        }

        val objectives =
            mission.objectives

        if (
            objectives.size !in
            MINIMUM_OBJECTIVE_COUNT..
            MAXIMUM_OBJECTIVE_COUNT
        ) {
            Log.w(
                USE_CASE_TAG,
                "Recommendation contained " +
                        "${objectives.size} objectives instead of " +
                        "$MINIMUM_OBJECTIVE_COUNT to " +
                        "$MAXIMUM_OBJECTIVE_COUNT."
            )

            return false
        }

        if (
            objectives.none { objective ->
                objective.isRequired
            }
        ) {
            Log.w(
                USE_CASE_TAG,
                "Recommendation had no required objective."
            )

            return false
        }

        val everyObjectiveIsComplete =
            objectives.all { objective ->
                objective.title.isNotBlank() &&
                        objective.description.isNotBlank() &&
                        objective.completionHint.isNotBlank() &&
                        objective
                            .sanitizedCoachingHints
                            .size ==
                        REQUIRED_COACHING_HINT_COUNT &&
                        objective
                            .sanitizedTargetVocabulary
                            .size in
                        MINIMUM_TARGET_VOCABULARY_COUNT..
                        MAXIMUM_TARGET_VOCABULARY_COUNT &&
                        objective
                            .sanitizedExamplePhrases
                            .size ==
                        REQUIRED_EXAMPLE_PHRASE_COUNT
            }

        if (!everyObjectiveIsComplete) {
            Log.w(
                USE_CASE_TAG,
                "Recommendation contained an incomplete " +
                        "objective."
            )

            return false
        }

        return true
    }

    private companion object {
        const val USE_CASE_TAG =
            "LoadMissionThread"

        const val TARGET_RECOMMENDATION_COUNT =
            5

        /*
         * Each generation attempt may fail JSON parsing,
         * validation, duplicate checks, or networking.
         */
        const val MAXIMUM_GENERATION_ATTEMPTS =
            15

        /*
         * Stop the cycle when repeated failures indicate an
         * unavailable service or insufficient scenarios.
         */
        const val MAXIMUM_CONSECUTIVE_FAILURES =
            5

        const val MINIMUM_OBJECTIVE_COUNT =
            8

        const val MAXIMUM_OBJECTIVE_COUNT =
            10

        const val REQUIRED_COACHING_HINT_COUNT =
            3

        const val MINIMUM_TARGET_VOCABULARY_COUNT =
            2

        const val MAXIMUM_TARGET_VOCABULARY_COUNT =
            4

        const val REQUIRED_EXAMPLE_PHRASE_COUNT =
            2

        val FOOD_SERVICE_SCENARIOS =
            setOf(
                MissionScenarioType.CAFE_ORDERING,
                MissionScenarioType.RESTAURANT_ORDERING
            )

        val SHOPPING_SCENARIOS =
            setOf(
                MissionScenarioType.GROCERY_SHOPPING,
                MissionScenarioType.CLOTHING_SHOPPING,
                MissionScenarioType.RETURNING_A_PRODUCT
            )
    }
}