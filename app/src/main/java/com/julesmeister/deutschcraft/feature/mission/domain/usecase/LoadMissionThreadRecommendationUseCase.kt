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
 * 1. Return an existing DISPLAYED / READY recommendation.
 * 2. Otherwise reactivate the oldest SKIPPED recommendation.
 * 3. Only if Room has none at all, generate and persist one.
 * 4. The coordinator then fills the pool to five in the
 *    background.
 *
 * Left swipe:
 *
 * 1. Mark the current recommendation SKIPPED.
 * 2. Return the next READY recommendation immediately.
 * 3. If none remain, wrap around to the oldest SKIPPED one
 *    (round-robin through what is already in Room).
 * 4. Generate only when Room has no recommendations at all.
 * 5. Refill the pool to five in the background.
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
     */
    private val generationMutex =
        Mutex()

    /*
     * Loads the recommendation that should be displayed.
     *
     * Order:
     *
     * 1. Existing DISPLAYED / oldest READY recommendation
     * 2. Oldest SKIPPED recommendation (wrap-around)
     * 3. Generate a new one, only when Room has nothing
     *
     * forceRefresh = true first marks the visible
     * recommendation SKIPPED, so a swipe moves to the next one
     * and, after the last, back to the beginning.
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

            var justSkippedId: String? =
                null

            if (
                forceRefresh &&
                currentRecommendation != null
            ) {
                recommendationStore.markSkipped(
                    recommendationId =
                        currentRecommendation.id
                )

                justSkippedId =
                    currentRecommendation.id

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
             * 1. Next READY / DISPLAYED recommendation.
             *
             * After a forced refresh, the skipped row is no
             * longer eligible, so this returns the next one.
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
             * 2. No fresh recommendation left: wrap around to
             *    the oldest skipped one. Instant, no Qwen.
             */
            val recycledRecommendation =
                recommendationStore.reactivateNextSkipped(
                    cefrLevel =
                        cefrLevel,
                    excludedRecommendationId =
                        justSkippedId
                )

            if (recycledRecommendation != null) {
                Log.d(
                    USE_CASE_TAG,
                    buildString {
                        append(
                            "Wrapped around to skipped "
                        )
                        append(
                            "recommendation. id="
                        )
                        append(
                            recycledRecommendation.id
                        )
                        append(", scenario=")
                        append(
                            recycledRecommendation.scenarioType
                        )
                    }
                )

                return@withLock recycledRecommendation
            }

            /*
             * 3. Room has no recommendations at all for this
             *    level. Generate one so the cell is not empty.
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
     * Every successfully generated recommendation is saved
     * immediately, so the pool survives process death and app
     * restarts.
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

        return selectedScenario
            ?.takeIf { scenario ->
                scenario.type !in
                        excludedScenarioTypes
            }
    }

    /*
     * Temporarily excludes related scenario families.
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

        const val MAXIMUM_GENERATION_ATTEMPTS =
            15

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