package com.julesmeister.deutschcraft.feature.mission.data.ai

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenApi
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenChatCompletionRequest
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenChatCompletionResponse
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenChoice
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenResponseMessage
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QwenMissionOpportunityAiGatewayTest {
    private val history = listOf(
        ChatMessage("1", "chat", "I would like to practice ordering a meal.", MessageSender.USER, 1, 0)
    )

    @Test
    fun `valid ten goal offer requires one request with enough response room`() = runBlocking {
        val api = RecordingApi(response(10))
        val result = QwenMissionOpportunityAiGateway(api, Json)
            .detectMissionOpportunity(history, CefrLevel.A2)

        assertTrue(result.shouldOfferMission)
        assertEquals(10, result.mission!!.objectives.size)
        val request = api.requests.single()
        assertEquals(2_000, request.maxTokens)
        assertFalse(request.enableThinking)
        assertTrue(request.messages.first().content.contains("Aim for 10 distinct objectives"))
        assertTrue(request.messages.first().content.contains("8 to 10"))
        assertTrue(request.messages.last().content.contains(history.single().content))
    }

    @Test
    fun `short valid JSON is repaired to ten goals instead of offered as three`() = runBlocking {
        val api = RecordingApi(response(3), response(10))
        val result = QwenMissionOpportunityAiGateway(api, Json)
            .detectMissionOpportunity(history, CefrLevel.A2)

        assertTrue(result.shouldOfferMission)
        assertEquals(10, result.mission!!.objectives.size)
        assertEquals(2, api.requests.size)
        assertTrue(api.requests.last().messages.first().content.contains("8 to 10 distinct"))
        assertEquals(response(3).trim(), api.requests.last().messages.last().content)
    }

    @Test
    fun `repair remains bounded when both responses have too few goals`() = runBlocking {
        val api = RecordingApi(response(3), response(5))
        val result = QwenMissionOpportunityAiGateway(api, Json)
            .detectMissionOpportunity(history, CefrLevel.A2)

        assertFalse(result.shouldOfferMission)
        assertEquals(2, api.requests.size)
    }

    @Test
    fun `duplicates and incomplete goals do not satisfy the minimum`() = runBlocking {
        for (invalid in listOf(
            response(8).replace("Goal 7", "Goal 0"),
            response(8).replace("Description 7", "")
        )) {
            val api = RecordingApi(invalid, response(8))
            val result = QwenMissionOpportunityAiGateway(api, Json)
                .detectMissionOpportunity(history, CefrLevel.A2)
            assertEquals(8, result.mission!!.objectives.size)
            assertEquals(2, api.requests.size)
        }
    }

    @Test
    fun `no opportunity remains one request without unnecessary generation`() = runBlocking {
        val api = RecordingApi(
            """{"shouldOfferMission":false,"confidence":0.0,"reason":"Not appropriate.","mission":null}"""
        )
        val result = QwenMissionOpportunityAiGateway(api, Json)
            .detectMissionOpportunity(history, CefrLevel.A2)
        assertFalse(result.shouldOfferMission)
        assertEquals(1, api.requests.size)
    }

    private fun response(count: Int): String {
        val objectives = (0 until count).joinToString(",") { index ->
            """
            {"title":"Goal $index","description":"Description $index",
             "completionHint":"Rule $index","required":true}
            """.trimIndent()
        }
        return """
            {"shouldOfferMission":true,"confidence":0.95,"reason":"Natural follow-up.",
             "mission":{"title":"Order a meal","description":"Speak with a waiter.",
             "scenario":"At a restaurant.","learnerRole":"Customer","assistantRole":"Waiter",
             "difficulty":"A2","icon":"M","objectives":[$objectives]}}
        """.trimIndent()
    }

    private class RecordingApi(vararg responses: String) : QwenApi {
        private val responses = responses.toList()
        val requests = mutableListOf<QwenChatCompletionRequest>()

        override suspend fun createChatCompletion(request: QwenChatCompletionRequest): QwenChatCompletionResponse {
            val content = responses[requests.size]
            requests += request
            return QwenChatCompletionResponse(
                choices = listOf(QwenChoice(message = QwenResponseMessage(content = content)))
            )
        }
    }
}
