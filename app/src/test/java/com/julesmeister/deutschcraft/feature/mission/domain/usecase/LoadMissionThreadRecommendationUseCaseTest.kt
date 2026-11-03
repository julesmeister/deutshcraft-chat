package com.julesmeister.deutschcraft.feature.mission.domain.usecase

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.repository.AdaptiveLearningRepository
import com.julesmeister.deutschcraft.feature.besser.domain.mastery.CapabilityMasteryCalculator
import com.julesmeister.deutschcraft.feature.besser.domain.progression.CapabilityProgressionEvaluator
import com.julesmeister.deutschcraft.feature.besser.domain.progression.CapabilityProgressionGraph
import com.julesmeister.deutschcraft.feature.besser.domain.repository.BesserChallengeRepository
import com.julesmeister.deutschcraft.feature.besser.domain.repository.CapabilityRepository
import com.julesmeister.deutschcraft.feature.besser.domain.repository.MissionCapabilityEvidenceRepository
import com.julesmeister.deutschcraft.feature.besser.domain.usecase.GetCapabilityMasteriesUseCase
import com.julesmeister.deutschcraft.feature.mission.data.ai.MissionThreadRecommendationAiGateway
import com.julesmeister.deutschcraft.feature.mission.domain.MissionDifficulty
import com.julesmeister.deutschcraft.feature.mission.domain.generated.GeneratedMissionDraft
import com.julesmeister.deutschcraft.feature.mission.domain.generated.GeneratedMissionObjectiveDraft
import com.julesmeister.deutschcraft.feature.mission.domain.generated.MissionOpportunity
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.CefrMissionScenarioCatalog
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionLearningContext
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionRecommendationHistory
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionScenarioDefinition
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionScenarioType
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendation
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendationStore
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.SelectMissionScenarioUseCase
import java.lang.reflect.Proxy
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LoadMissionThreadRecommendationUseCaseTest {
    private val catalog = CefrMissionScenarioCatalog()
    private val clock = Clock.fixed(Instant.ofEpochMilli(100_000), ZoneOffset.UTC)

    @Test
    fun `valid saved recommendation is reused without AI`() = runBlocking {
        val store = FakeStore()
        val saved = recommendation(catalog.scenariosFor(CefrLevel.A1).first())
        store.recommendations += saved
        var calls = 0
        val loader = loader(store) { calls++; null }

        assertEquals(saved, loader(CefrLevel.A1))
        assertEquals(0, calls)
        assertEquals(listOf(saved.id), store.displayedIds)
    }

    @Test
    fun `refill generates at most five distinct recommendations and reuses a full pool`() = runBlocking {
        val store = FakeStore()
        var calls = 0
        val published = mutableListOf<MissionThreadRecommendation>()
        val loader = loader(store) { scenario -> calls++; recommendation(scenario) }

        assertTrue(loader.prefetchUntilFull(CefrLevel.A1) { next, _ -> published += next })
        assertEquals(5, calls)
        assertEquals(5, store.recommendations.map { it.scenarioType }.toSet().size)
        assertTrue(published.all { it.id == store.recommendations.first().id })
        assertTrue(loader.prefetchUntilFull(CefrLevel.A1))
        assertEquals(5, calls)
    }

    @Test
    fun `all completed scenarios produce no AI calls or repeated refill callbacks`() = runBlocking {
        val store = FakeStore(
            MissionRecommendationHistory(
                blockedScenarioTypes = catalog.scenariosFor(CefrLevel.A1).map { it.type }.toSet()
            )
        )
        var calls = 0
        var callbacks = 0
        val loader = loader(store) { calls++; null }

        assertNull(loader(CefrLevel.A1))
        assertFalse(loader.prefetchUntilFull(CefrLevel.A1) { _, _ -> callbacks++ })
        assertFalse(loader.prefetchUntilFull(CefrLevel.A1) { _, _ -> callbacks++ })
        assertEquals(0, calls)
        assertEquals(0, callbacks)
    }

    @Test
    fun `failing candidates are attempted once then back off across load and refill`() = runBlocking {
        val store = FakeStore()
        val attempted = mutableListOf<MissionScenarioType>()
        val loader = loader(store) { attempted += it.type; null }

        assertNull(loader(CefrLevel.A1))
        val firstPass = attempted.toList()
        assertEquals(minOf(8, catalog.scenariosFor(CefrLevel.A1).size), firstPass.size)
        assertEquals(firstPass.size, firstPass.toSet().size)
        assertFalse(loader.prefetchUntilFull(CefrLevel.A1))
        assertNull(loader(CefrLevel.A1))
        assertEquals(firstPass, attempted)
    }

    @Test
    fun `completion during generation prevents persistence and publication`() = runBlocking {
        val store = FakeStore()
        val loader = loader(store) { scenario ->
            store.history = MissionRecommendationHistory(
                blockedScenarioTypes = catalog.scenariosFor(CefrLevel.A1).map { it.type }.toSet()
            )
            recommendation(scenario)
        }

        assertNull(loader(CefrLevel.A1))
        assertTrue(store.recommendations.isEmpty())
    }

    @Test
    fun `recent ordinary topics are used locally without changing generated support content`() = runBlocking {
        val topic = catalog.scenariosFor(CefrLevel.A1).first()
        val history = MissionRecommendationHistory(
            recentConversationText = topic.topicKeywords.joinToString(" ")
        )
        val store = FakeStore(history)
        var selected: MissionScenarioDefinition? = null
        val loader = loader(store) { scenario ->
            selected = scenario
            recommendation(scenario)
        }

        val result = loader(CefrLevel.A1)!!
        val expected = SelectMissionScenarioUseCase(catalog)(
            com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionRecommendationContext(
                cefrLevel = CefrLevel.A1,
                recentConversationText = history.recentConversationText,
                preferConversationNovelty = true
            )
        )!!
        assertEquals(expected.type, selected?.type)
        assertEquals(8, result.opportunity.mission.objectives.size)
        assertTrue(result.opportunity.mission.objectives.all {
            it.sanitizedCoachingHints.size == 3 && it.sanitizedExamplePhrases.size == 2 &&
                it.sanitizedTargetVocabulary.size == 2
        })
    }

    private fun loader(
        store: FakeStore,
        generate: (MissionScenarioDefinition) -> MissionThreadRecommendation?
    ): LoadMissionThreadRecommendationUseCase {
        val contextBuilder = BuildMissionLearningContextUseCase(
            proxy(CapabilityRepository::class.java) { method ->
                check(method == "getActiveForShortlist")
                emptyList<Nothing>()
            },
            GetCapabilityMasteriesUseCase(
                proxy(BesserChallengeRepository::class.java) { error("Unexpected call: $it") },
                proxy(MissionCapabilityEvidenceRepository::class.java) { error("Unexpected call: $it") },
                CapabilityMasteryCalculator()
            ),
            CapabilityProgressionEvaluator(CapabilityProgressionGraph()),
            proxy(AdaptiveLearningRepository::class.java) { method ->
                check(method == "getTopReinforcementFocuses")
                emptyList<Nothing>()
            }
        )
        return LoadMissionThreadRecommendationUseCase(
            object : MissionThreadRecommendationAiGateway {
                override suspend fun generateRecommendation(
                    cefrLevel: CefrLevel,
                    selectedScenario: MissionScenarioDefinition
                ) = generate(selectedScenario)
            },
            store,
            SelectMissionScenarioUseCase(catalog),
            contextBuilder,
            clock
        )
    }

    private fun <T> proxy(type: Class<T>, call: (String) -> Any?): T =
        type.cast(Proxy.newProxyInstance(type.classLoader, arrayOf(type)) { _, method, _ ->
            call(method.name)
        })

    private fun recommendation(scenario: MissionScenarioDefinition): MissionThreadRecommendation =
        MissionThreadRecommendation(
            id = scenario.type.name,
            scenarioType = scenario.type,
            title = scenario.title,
            previewText = "Welcome to this conversation.",
            opportunity = MissionOpportunity(
                confidence = 0.9f,
                reason = "Appropriate learning context.",
                mission = GeneratedMissionDraft(
                    title = scenario.title,
                    description = scenario.description,
                    scenario = scenario.description,
                    learnerRole = scenario.learnerRole,
                    assistantRole = scenario.assistantRole,
                    difficulty = MissionDifficulty.A1,
                    icon = "M",
                    objectives = List(8) { index ->
                        GeneratedMissionObjectiveDraft(
                            title = "Objective $index",
                            description = "Communicate naturally.",
                            completionHint = "Assess the learner's response.",
                            coachingHints = listOf("First hint", "Second hint", "Third hint"),
                            targetVocabulary = listOf("First word", "Second word"),
                            examplePhrases = listOf("First phrase", "Second phrase"),
                            isRequired = true
                        )
                    }
                )
            )
        )

    private class FakeStore(
        var history: MissionRecommendationHistory = MissionRecommendationHistory.Empty
    ) : MissionThreadRecommendationStore {
        val recommendations = mutableListOf<MissionThreadRecommendation>()
        val displayedIds = mutableListOf<String>()

        override fun observeNextEligible(cefrLevel: CefrLevel): Flow<MissionThreadRecommendation?> =
            flowOf(recommendations.firstOrNull())
        override suspend fun getNextEligible(cefrLevel: CefrLevel) =
            getEligibleRecommendations(cefrLevel, 1).firstOrNull()
        override suspend fun getEligibleRecommendations(cefrLevel: CefrLevel, limit: Int) =
            recommendations.filter { it.scenarioType !in history.blockedScenarioTypes }.take(limit)
        override suspend fun countEligible(cefrLevel: CefrLevel) =
            getEligibleRecommendations(cefrLevel, Int.MAX_VALUE).size
        override suspend fun getRecommendationHistory(cefrLevel: CefrLevel) = history
        override suspend fun hasActiveScenario(cefrLevel: CefrLevel, scenarioType: MissionScenarioType) =
            scenarioType in history.blockedScenarioTypes || recommendations.any { it.scenarioType == scenarioType }
        override suspend fun save(recommendation: MissionThreadRecommendation) {
            recommendations += recommendation
        }
        override suspend fun markDisplayed(recommendationId: String) {
            displayedIds += recommendationId
        }
        override suspend fun markSkipped(recommendationId: String) {
            recommendations.removeAll { it.id == recommendationId }
        }
        override suspend fun markStale(recommendationId: String) = markSkipped(recommendationId)
        override suspend fun invalidateStaleRecommendations(
            cefrLevel: CefrLevel,
            currentLearningContext: MissionLearningContext
        ) = 0
        override suspend fun markStarted(
            recommendationId: String,
            conversationId: String,
            conversationMissionId: String
        ) = markSkipped(recommendationId)
        override suspend fun markCompletedByMissionId(conversationMissionId: String) =
            error("Unexpected completion call")
        override suspend fun performMaintenance(cefrLevel: CefrLevel) = Unit
    }
}
