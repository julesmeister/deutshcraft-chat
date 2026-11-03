package com.julesmeister.deutschcraft.feature.mission.domain.recommendation

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SelectMissionScenarioUseCaseTest {
    private val catalog = CefrMissionScenarioCatalog()
    private val selector = SelectMissionScenarioUseCase(catalog)

    @Test
    fun `exhausted catalog never relaxes explicit exclusions at any level`() {
        for (level in CefrLevel.entries) {
            val types = catalog.scenariosFor(level).map { it.type }
            val context = MissionRecommendationContext(
                cefrLevel = level,
                recentConversationText = "",
                excludedScenarioTypes = types.toSet(),
                recentlyOfferedScenarioTypes = types,
                recentlyDismissedScenarioTypes = types
            )
            assertTrue(level.name, selector.rank(context, Int.MAX_VALUE).isEmpty())
        }
    }

    @Test
    fun `recent dismissal fallbacks retain permanent exclusions`() {
        val scenarios = catalog.scenariosFor(CefrLevel.A2)
        val remaining = scenarios.last().type
        val ranked = selector.rank(
            MissionRecommendationContext(
                cefrLevel = CefrLevel.A2,
                recentConversationText = "",
                excludedScenarioTypes = scenarios.map { it.type }.toSet() - remaining,
                recentlyOfferedScenarioTypes = listOf(remaining),
                recentlyDismissedScenarioTypes = listOf(remaining)
            )
        )
        assertEquals(listOf(remaining), ranked.map { it.type })
    }

    @Test
    fun `ordinary conversation keywords lower novelty ranking without banning the topic`() {
        val topic = catalog.scenarioFor(MissionScenarioType.RESTAURANT_ORDERING)!!
        val context = MissionRecommendationContext(
            cefrLevel = CefrLevel.A2,
            recentConversationText = topic.topicKeywords.joinToString(" ")
        )
        val relevant = selector.rank(context, Int.MAX_VALUE).map { it.type }
        val novel = selector.rank(context.copy(preferConversationNovelty = true), Int.MAX_VALUE)
            .map { it.type }

        assertTrue(novel.contains(topic.type))
        assertTrue(novel.indexOf(topic.type) > relevant.indexOf(topic.type))
        assertEquals(relevant.toSet(), novel.toSet())
    }

    @Test
    fun `learning reinforcement remains eligible despite ordinary topic overlap`() {
        val topic = catalog.scenarioFor(MissionScenarioType.RESTAURANT_ORDERING)!!
        val ranked = selector.rank(
            MissionRecommendationContext(
                cefrLevel = CefrLevel.A2,
                recentConversationText = topic.topicKeywords.joinToString(" "),
                developingCapabilities = topic.targetSkills.toList(),
                preferConversationNovelty = true
            ),
            Int.MAX_VALUE
        )
        assertFalse(ranked.isEmpty())
        assertEquals(topic.type, ranked.first().type)
    }
}
