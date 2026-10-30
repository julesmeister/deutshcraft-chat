// app/src/main/java/com/julesmeister/deutschcraft/feature/mission/domain/usecase/LoadMissionThreadRecommendationUseCase.kt

package com.julesmeister.deutschcraft.feature.mission.domain.usecase

import android.util.Log
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.mission.data.ai.MissionThreadRecommendationAiGateway
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionLearningContext
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionRecommendationContext
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionRecommendationHistory
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionScenarioDefinition
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionScenarioType
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendation
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendationStore
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.SelectMissionScenarioUseCase
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class LoadMissionThreadRecommendationUseCase
@Inject constructor(
    private val aiGateway:
    MissionThreadRecommendationAiGateway,

    private val recommendationStore:
    MissionThreadRecommendationStore,

    private val selectMissionScenario:
    SelectMissionScenarioUseCase,

    private val buildMissionLearningContext:
    BuildMissionLearningContextUseCase
) {

    private val loadingMutex =
        Mutex()

    private val prefetchMutex =
        Mutex()

    private val generationMutex =
        Mutex()

    suspend operator fun invoke(
        cefrLevel:
        CefrLevel,

        forceRefresh:
        Boolean =
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

            val learningContext =
                loadLearningContext(
                    cefrLevel =
                        cefrLevel
                )

            recommendationStore
                .invalidateStaleRecommendations(
                    cefrLevel =
                        cefrLevel,

                    currentLearningContext =
                        learningContext
                )

            val currentRecommendation =
                recommendationStore.getNextEligible(
                    cefrLevel =
                        cefrLevel
                )

            if (
                forceRefresh &&
                currentRecommendation !=
                null
            ) {
                recommendationStore.markSkipped(
                    recommendationId =
                        currentRecommendation.id
                )
            }

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

                return@withLock persistedRecommendation
            }

            generateAndPersistOne(
                cefrLevel =
                    cefrLevel,

                externalExcludedScenarioTypes =
                    externalExcludedScenarioTypes,

                suppliedLearningContext =
                    learningContext
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

    suspend fun advance(
        cefrLevel:
        CefrLevel,

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

    suspend fun prefetchUntilFull(
        cefrLevel:
        CefrLevel,

        externalExcludedScenarioTypes:
        Set<MissionScenarioType> =
            emptySet(),

        onRecommendationAvailable:
            (
            recommendation:
            MissionThreadRecommendation,

            availableCount:
            Int
        ) -> Unit =
            { _, _ -> }
    ) {
        prefetchMutex.withLock {
            recommendationStore.performMaintenance(
                cefrLevel =
                    cefrLevel
            )

            val learningContext =
                loadLearningContext(
                    cefrLevel =
                        cefrLevel
                )

            recommendationStore
                .invalidateStaleRecommendations(
                    cefrLevel =
                        cefrLevel,

                    currentLearningContext =
                        learningContext
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
                            externalExcludedScenarioTypes,

                        suppliedLearningContext =
                            learningContext
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
                }
            }
        }
    }

    suspend fun markStarted(
        recommendationId:
        String,

        conversationId:
        String,

        conversationMissionId:
        String
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

    suspend fun markCompleted(
        conversationMissionId:
        String
    ) {
        recommendationStore.markCompletedByMissionId(
            conversationMissionId =
                conversationMissionId
        )
    }

    suspend fun availableCount(
        cefrLevel:
        CefrLevel
    ): Int {

        return recommendationStore.countEligible(
            cefrLevel =
                cefrLevel
        )
    }

    private suspend fun generateAndPersistOne(
        cefrLevel:
        CefrLevel,

        externalExcludedScenarioTypes:
        Set<MissionScenarioType>,

        suppliedLearningContext:
        MissionLearningContext? =
            null
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

            val recommendationHistory =
                recommendationStore
                    .getRecommendationHistory(
                        cefrLevel =
                            cefrLevel
                    )

            val activeScenarioTypes =
                recommendationStore
                    .getEligibleRecommendations(
                        cefrLevel =
                            cefrLevel,

                        limit =
                            TARGET_RECOMMENDATION_COUNT
                    )
                    .map(
                        MissionThreadRecommendation::scenarioType
                    )
                    .toSet()

            val exactScenarioExclusions =
                activeScenarioTypes +
                        externalExcludedScenarioTypes

            val learningContext =
                suppliedLearningContext
                    ?: loadLearningContext(
                        cefrLevel =
                            cefrLevel
                    )

            val rankedScenarios =
                selectScenarios(
                    cefrLevel =
                        cefrLevel,

                    exactScenarioExclusions =
                        exactScenarioExclusions,

                    learningContext =
                        learningContext,

                    recommendationHistory =
                        recommendationHistory
                )

            if (rankedScenarios.isEmpty()) {
                return@withLock null
            }

            for (selectedScenario in rankedScenarios) {
                if (
                    selectedScenario.type in
                    exactScenarioExclusions
                ) {
                    continue
                }

                if (
                    recommendationStore.hasActiveScenario(
                        cefrLevel =
                            cefrLevel,

                        scenarioType =
                            selectedScenario.type
                    )
                ) {
                    continue
                }

                val adaptiveScenario =
                    selectedScenario.withLearningContext(
                        learningContext =
                            learningContext
                    )

                val generatedRecommendation =
                    try {
                        aiGateway.generateRecommendation(
                            cefrLevel =
                                cefrLevel,

                            selectedScenario =
                                adaptiveScenario
                        )
                    } catch (
                        cancellationException:
                        CancellationException
                    ) {
                        throw cancellationException
                    } catch (throwable: Throwable) {
                        Log.e(
                            USE_CASE_TAG,
                            "Generation failed for scenario " +
                                    selectedScenario.type.name +
                                    ".",
                            throwable
                        )

                        null
                    }
                        ?: continue

                val recommendation =
                    generatedRecommendation.copy(
                        learningContext =
                            learningContext
                    )

                if (
                    !isValidRecommendation(
                        recommendation =
                            recommendation,

                        cefrLevel =
                            cefrLevel,

                        selectedScenario =
                            adaptiveScenario
                    )
                ) {
                    continue
                }

                try {
                    recommendationStore.save(
                        recommendation =
                            recommendation
                    )

                    return@withLock recommendation
                } catch (
                    cancellationException:
                    CancellationException
                ) {
                    throw cancellationException
                } catch (throwable: Throwable) {
                    Log.e(
                        USE_CASE_TAG,
                        "Recommendation persistence failed for " +
                                selectedScenario.type.name +
                                ".",
                        throwable
                    )
                }
            }

            null
        }
    }

    private suspend fun loadLearningContext(
        cefrLevel:
        CefrLevel
    ): MissionLearningContext {

        return try {
            buildMissionLearningContext(
                cefrLevel =
                    cefrLevel
            )
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.w(
                USE_CASE_TAG,
                "Mission learning context unavailable.",
                throwable
            )

            MissionLearningContext.Empty
        }
    }

    private fun selectScenarios(
        cefrLevel:
        CefrLevel,

        exactScenarioExclusions:
        Set<MissionScenarioType>,

        learningContext:
        MissionLearningContext,

        recommendationHistory:
        MissionRecommendationHistory
    ): List<MissionScenarioDefinition> {

        val context =
            MissionRecommendationContext(
                cefrLevel =
                    cefrLevel,

                recentConversationText =
                    "",

                recentlyOfferedScenarioTypes =
                    recommendationHistory
                        .scenarioTypes,

                recentlyCompletedScenarioTypes =
                    recommendationHistory
                        .completedScenarioTypes,

                recentlyDismissedScenarioTypes =
                    recommendationHistory
                        .dismissedScenarioTypes,

                adaptiveLearningFocuses =
                    learningContext
                        .reinforcementFocuses,

                progressionReadyCapabilities =
                    learningContext
                        .progressionReadyCapabilities,

                developingCapabilities =
                    learningContext
                        .developingCapabilities,

                masteredCapabilities =
                    learningContext
                        .masteredCapabilities,

                activeScenarioType =
                    null
            )

        return selectMissionScenario
            .rank(
                context =
                    context,

                limit =
                    MAXIMUM_RANKED_SCENARIO_COUNT
            )
            .filter { scenario ->
                scenario.type !in
                        exactScenarioExclusions
            }
    }

    private fun MissionScenarioDefinition.withLearningContext(
        learningContext:
        MissionLearningContext
    ): MissionScenarioDefinition {

        if (!learningContext.hasLearningSignals) {
            return this
        }

        val adaptiveTargets =
            learningContext
                .generationTargetSkills
                .take(
                    MAXIMUM_INJECTED_TARGET_SKILLS
                )

        val retainedBlueprintCount =
            (
                    MAXIMUM_OBJECTIVE_COUNT -
                            ADAPTIVE_OBJECTIVE_COUNT
                    )
                .coerceAtLeast(
                    minimumValue =
                        MINIMUM_OBJECTIVE_COUNT
                )

        val adaptiveObjectiveBlueprints =
            buildList {
                addAll(
                    objectiveBlueprints.take(
                        retainedBlueprintCount
                    )
                )

                learningContext
                    .progressionReadyCapabilities
                    .firstOrNull()
                    ?.let { capability ->
                        add(
                            "Create one natural conversational " +
                                    "moment where the learner demonstrates " +
                                    "this next-step capability: " +
                                    capability
                        )
                    }

                (
                        learningContext
                            .developingCapabilities
                            .firstOrNull()
                            ?: learningContext
                                .reinforcementFocuses
                                .firstOrNull()
                        )
                    ?.let { focus ->
                        add(
                            "Create one natural conversational " +
                                    "moment that reinforces this learner " +
                                    "need without naming it as an exercise: " +
                                    focus
                        )
                    }
            }
                .distinct()
                .take(
                    MAXIMUM_OBJECTIVE_COUNT
                )

        return copy(
            targetSkills =
                (
                        targetSkills +
                                adaptiveTargets
                        )
                    .take(
                        MAXIMUM_TARGET_SKILL_COUNT
                    )
                    .toSet(),

            objectiveBlueprints =
                if (
                    adaptiveObjectiveBlueprints.size >=
                    MINIMUM_OBJECTIVE_COUNT
                ) {
                    adaptiveObjectiveBlueprints
                } else {
                    objectiveBlueprints
                }
        )
    }

    private fun isValidRecommendation(
        recommendation:
        MissionThreadRecommendation,

        cefrLevel:
        CefrLevel,

        selectedScenario:
        MissionScenarioDefinition
    ): Boolean {

        if (
            recommendation.scenarioType !=
            selectedScenario.type
        ) {
            return false
        }

        val mission =
            recommendation
                .opportunity
                .mission

        if (
            mission.difficulty.name !=
            cefrLevel.name
        ) {
            return false
        }

        if (
            recommendation.id.isBlank() ||
            recommendation.title.isBlank() ||
            recommendation.previewText.isBlank() ||
            recommendation
                .opportunity
                .reason
                .isBlank() ||
            mission.description.isBlank() ||
            mission.scenario.isBlank() ||
            mission.learnerRole.isBlank() ||
            mission.assistantRole.isBlank()
        ) {
            return false
        }

        val objectives =
            mission.objectives

        if (
            objectives.size !in
            MINIMUM_OBJECTIVE_COUNT..
            MAXIMUM_OBJECTIVE_COUNT
        ) {
            return false
        }

        if (
            objectives.none { objective ->
                objective.isRequired
            }
        ) {
            return false
        }

        return objectives.all { objective ->
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

        const val MAXIMUM_RANKED_SCENARIO_COUNT =
            8

        const val MINIMUM_OBJECTIVE_COUNT =
            8

        const val MAXIMUM_OBJECTIVE_COUNT =
            10

        const val ADAPTIVE_OBJECTIVE_COUNT =
            2

        const val MAXIMUM_INJECTED_TARGET_SKILLS =
            5

        const val MAXIMUM_TARGET_SKILL_COUNT =
            10

        const val REQUIRED_COACHING_HINT_COUNT =
            3

        const val MINIMUM_TARGET_VOCABULARY_COUNT =
            2

        const val MAXIMUM_TARGET_VOCABULARY_COUNT =
            4

        const val REQUIRED_EXAMPLE_PHRASE_COUNT =
            2
    }
}