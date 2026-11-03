package com.julesmeister.deutschcraft.feature.mission.domain.usecase

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import com.julesmeister.deutschcraft.feature.mission.data.ai.AiGeneratedMission
import com.julesmeister.deutschcraft.feature.mission.data.ai.AiGeneratedMissionObjective
import com.julesmeister.deutschcraft.feature.mission.data.ai.AiMissionOpportunity
import com.julesmeister.deutschcraft.feature.mission.data.ai.MissionOpportunityAiGateway
import com.julesmeister.deutschcraft.feature.mission.data.cache.MissionOfferCache
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DetectMissionOpportunityUseCaseTest {
    private val history = List(3) { index ->
        ChatMessage("$index", "chat", "A learner message.", MessageSender.USER, index.toLong(), 0)
    }

    @Test
    fun `eight nine and ten unique goals are retained in the cached offer`() = runBlocking {
        for (count in 8..10) {
            val cache = MissionOfferCache()
            var calls = 0
            val useCase = DetectMissionOpportunityUseCase(gateway { calls++; opportunity(count) }, cache)
            val result = useCase("chat", history, CefrLevel.A2, false)!!

            assertEquals(count, result.mission.objectives.size)
            assertEquals(result, cache.getOffer("chat"))
            assertEquals(result, useCase("chat", history, CefrLevel.A2, false))
            assertEquals(1, calls)
        }
    }

    @Test
    fun `short and oversized mission lists never become offers`() = runBlocking {
        for (count in listOf(3, 5, 7, 11)) {
            val cache = MissionOfferCache()
            val useCase = DetectMissionOpportunityUseCase(gateway { opportunity(count) }, cache)
            assertNull(useCase("chat", history, CefrLevel.A2, false))
            assertNull(cache.getOffer("chat"))
        }
    }

    @Test
    fun `minimum applies after removing duplicate and incomplete goals`() = runBlocking {
        val original = opportunity(8)
        val mission = original.mission!!
        for (objectives in listOf(
            mission.objectives.dropLast(1) + mission.objectives.first().copy(title = "  goal 0  "),
            mission.objectives.dropLast(1) + mission.objectives.last().copy(completionHint = " ")
        )) {
            val useCase = DetectMissionOpportunityUseCase(
                gateway { original.copy(mission = mission.copy(objectives = objectives)) },
                MissionOfferCache()
            )
            assertNull(useCase("chat", history, CefrLevel.A2, false))
        }
    }

    @Test
    fun `active mission and detection cadence still prevent unnecessary AI calls`() = runBlocking {
        var calls = 0
        val useCase = DetectMissionOpportunityUseCase(gateway { calls++; opportunity(10) }, MissionOfferCache())
        assertNull(useCase("chat", history, CefrLevel.A2, true))
        assertNull(useCase("chat", history.take(2), CefrLevel.A2, false))
        assertEquals(0, calls)
    }

    private fun gateway(result: () -> AiMissionOpportunity) = object : MissionOpportunityAiGateway {
        override suspend fun detectMissionOpportunity(
            conversationHistory: List<ChatMessage>,
            cefrLevel: CefrLevel
        ) = result()
    }

    private fun opportunity(count: Int) = AiMissionOpportunity(
        shouldOfferMission = true,
        confidence = 0.95f,
        reason = "A natural follow-up.",
        mission = AiGeneratedMission(
            title = "Order a meal",
            description = "Speak with a waiter.",
            scenario = "At a restaurant.",
            learnerRole = "Customer",
            assistantRole = "Waiter",
            difficulty = "A2",
            icon = "M",
            objectives = List(count) { index ->
                AiGeneratedMissionObjective("Goal $index", "Description $index", "Rule $index", true)
            }
        )
    )
}
