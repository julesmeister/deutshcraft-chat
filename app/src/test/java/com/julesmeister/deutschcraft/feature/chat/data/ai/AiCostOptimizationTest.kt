package com.julesmeister.deutschcraft.feature.chat.data.ai

import com.julesmeister.deutschcraft.BuildConfig
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.chat.data.correction.MessageCorrectionException
import com.julesmeister.deutschcraft.feature.chat.data.correction.QwenMessageCorrectionGateway
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import com.julesmeister.deutschcraft.feature.messageactions.data.QwenSentenceInsightGateway
import com.julesmeister.deutschcraft.feature.messageactions.data.SentenceInsightException
import com.julesmeister.deutschcraft.feature.mission.domain.ActiveMissionConversationContext
import com.julesmeister.deutschcraft.feature.mission.domain.MissionConversationObjective
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class AiCostOptimizationTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun correctionReusesResultsAndKeepsSentenceDataSeparateFromStaticRules() = runBlocking {
        val api = RecordingApi("""{"needsCorrection":true,"correctedSentence":"Ich gehe heute zum Markt."}""")
        val gateway = QwenMessageCorrectionGateway(api, json)

        assertEquals("Ich gehe heute zum Markt.", gateway.correctSentence("Ich go heute zum Markt."))
        assertEquals("Ich gehe heute zum Markt.", gateway.correctSentence("  Ich go heute zum Markt.  "))
        assertEquals(1, api.requests.size)
        val request = api.requests.single()
        assertRequestSettings(request, temperature = 0.1, maximumTokens = 180)
        assertEquals("Learner sentence:\nIch go heute zum Markt.", request.messages.last().content)
        assertFalse(request.messages.first().content.contains("Ich go heute zum Markt."))
        assertTrue(request.messages.sumOf { it.content.length } < 1_500)

        gateway.correctSentence("ich go heute zum Markt.")
        gateway.correctSentence("Ich go heute zum Markt!")
        assertEquals(3, api.requests.size) // Case and punctuation are not normalized away.
    }

    @Test
    fun cachesNoCorrectionButRetriesMalformedCorrection() = runBlocking {
        val api = RecordingApi("""{"needsCorrection":false,"correctedSentence":null}""")
        val gateway = QwenMessageCorrectionGateway(api, json)
        assertNull(gateway.correctSentence("Guten Morgen!"))
        assertNull(gateway.correctSentence("Guten Morgen!"))
        assertEquals(1, api.requests.size)

        api.content = """{"needsCorrection":true,"correctedSentence":null}"""
        try {
            gateway.correctSentence("Ich go.")
            fail("A missing required correction must be an error, not a cached success.")
        } catch (_: MessageCorrectionException) {
            // The next call must retry rather than return a cached null.
        }
        api.content = """{"needsCorrection":true,"correctedSentence":"Ich gehe."}"""
        assertEquals("Ich gehe.", gateway.correctSentence("Ich go."))
        assertEquals(3, api.requests.size)
    }

    @Test
    fun sentenceAnalysisKeepsAllFiveFeaturesAndReusesConcurrentRequests() = runBlocking {
        val api = RecordingApi(VALID_INSIGHT)
        val release = CompletableDeferred<Unit>()
        api.release = release
        val gateway = QwenSentenceInsightGateway(api, json)
        val first = async(start = CoroutineStart.UNDISPATCHED) {
            gateway.analyzeSentence("Ich gehe.")
        }
        val second = async(start = CoroutineStart.UNDISPATCHED) {
            gateway.analyzeSentence(" Ich gehe. ")
        }
        assertEquals(1, api.requests.size)
        release.complete(Unit)
        val insight = first.await()
        assertEquals(insight, second.await())
        assertEquals("I am going.", insight.translation)
        assertEquals("A statement about going.", insight.explanation)
        assertEquals("Ich gehe.", insight.simpleRephrase)
        assertEquals("Ich bin unterwegs.", insight.naturalRephrase)
        assertEquals("Ich begebe mich auf den Weg.", insight.advancedRephrase)
        assertEquals(insight, gateway.analyzeSentence("Ich gehe."))
        assertEquals(1, api.requests.size)

        val request = api.requests.single()
        assertRequestSettings(request, temperature = 0.3, maximumTokens = 550)
        assertEquals("German sentence:\nIch gehe.", request.messages.last().content)
        assertTrue(request.messages.sumOf { it.content.length } < 1_300)
    }

    @Test
    fun incompleteSentenceAnalysisIsNotCached() = runBlocking {
        val api = RecordingApi(VALID_INSIGHT.replace("I am going.", ""))
        val gateway = QwenSentenceInsightGateway(api, json)
        try {
            gateway.analyzeSentence("Ich gehe.")
            fail("Incomplete analysis must not become a cached success.")
        } catch (_: SentenceInsightException) {
            // All five fields are required before a result is reusable.
        }
        api.content = VALID_INSIGHT
        assertEquals("I am going.", gateway.analyzeSentence("Ich gehe.").translation)
        assertEquals(2, api.requests.size)
    }

    @Test
    fun compactPersonaRetainsIdentityLevelAndOutputContractForEveryLevel() {
        val persona = DeutschCraftCompanionPersona()
        for (level in CefrLevel.entries) {
            val prompt = persona.createSystemPrompt(level)
            assertTrue(prompt.length < 3_500)
            assertTrue(prompt.contains("estimated CEFR level: ${level.name}"))
            assertTrue(prompt.contains("Leipzig"))
            assertTrue(prompt.contains("Lukas"))
            assertTrue(prompt.contains("independent bookstore"))
            assertTrue(prompt.contains("Never falsely claim to be human"))
            assertTrue(prompt.contains("at most one question"))
            assertTrue(prompt.contains("array of exactly three strings"))
            assertTrue(prompt.contains("separate Explain action"))
        }
    }

    @Test
    fun missionPromptRetainsFullContextHistoryAndLearnerSuggestions() {
        val gateway = QwenAiGateway(RecordingApi(""), json, DeutschCraftCompanionPersona())
        val objective = MissionConversationObjective(
            id = "order",
            title = "Order a drink",
            description = "Ask the waiter for a drink.",
            coachingHints = listOf("Offer the drinks menu."),
            targetVocabulary = listOf("Wasser", "Kaffee")
        )
        val next = MissionConversationObjective(
            id = "pay",
            title = "Ask for the bill",
            description = "Politely request the bill."
        )
        val mission = ActiveMissionConversationContext(
            missionId = "restaurant",
            title = "At the restaurant",
            scenario = "A restaurant in Leipzig.",
            learnerRole = "Guest",
            assistantRole = "Waiter",
            progressPercentage = 40,
            currentObjective = objective,
            remainingObjectives = listOf(objective, next)
        )
        val history = listOf(
            ChatMessage("1", "chat", "Was möchten Sie trinken?", MessageSender.ASSISTANT, 1, 0),
            ChatMessage("2", "chat", "Ein Wasser, bitte.", MessageSender.USER, 2, 1)
        )

        val messages = gateway.buildPromptMessages(
            learnerMessage = "Ein Wasser, bitte.",
            conversationHistory = history,
            cefrLevel = CefrLevel.A2,
            activeMissionContext = mission
        )
        val system = messages.first().content
        for (value in listOf(
                mission.title, mission.scenario, mission.learnerRole, mission.assistantRole,
                "40%", objective.title, objective.description, objective.coachingHints.single(),
                "Wasser, Kaffee", next.title, next.description,
                "override Emil's casual-friend behavior", "Never perform it for them",
                "do not restart a progressed scenario", "from the learner's role",
                "without exposing mission logic", "CEFR A2", "exactly three strings"
            )
        ) {
            assertTrue("Missing mission instruction or context: $value", system.contains(value))
        }
        assertTrue(system.length < 7_000)
        assertEquals(listOf("system", "assistant", "user"), messages.map { it.role })
        assertEquals(history.map { it.content }, messages.drop(1).map { it.content })

        val ordinaryMessages = gateway.buildPromptMessages(
            learnerMessage = "Und einen Kaffee.",
            conversationHistory = history,
            cefrLevel = CefrLevel.B1,
            activeMissionContext = null
        )
        assertFalse(ordinaryMessages.first().content.contains("ACTIVE MISSION OVERRIDE"))
        assertEquals("Und einen Kaffee.", ordinaryMessages.last().content)
        assertEquals(history.map { it.content }, ordinaryMessages.drop(1).dropLast(1).map { it.content })
    }

    private fun assertRequestSettings(
        request: QwenChatCompletionRequest,
        temperature: Double,
        maximumTokens: Int
    ) {
        assertEquals(BuildConfig.QWEN_MODEL, request.model)
        assertFalse(request.enableThinking)
        assertEquals(temperature, request.temperature, 0.0)
        assertEquals(maximumTokens, request.maxTokens)
        assertEquals("json_object", request.responseFormat?.type)
        assertEquals(listOf("system", "user"), request.messages.map { it.role })
    }

    private class RecordingApi(var content: String) : QwenApi {
        val requests = mutableListOf<QwenChatCompletionRequest>()
        var release: CompletableDeferred<Unit>? = null

        override suspend fun createChatCompletion(
            request: QwenChatCompletionRequest
        ): QwenChatCompletionResponse {
            requests += request
            release?.await()
            return QwenChatCompletionResponse(
                choices = listOf(QwenChoice(message = QwenResponseMessage(content = content)))
            )
        }
    }

    private companion object {
        val VALID_INSIGHT = """
            {
              "translation":"I am going.",
              "explanation":"A statement about going.",
              "simpleRephrase":"Ich gehe.",
              "naturalRephrase":"Ich bin unterwegs.",
              "advancedRephrase":"Ich begebe mich auf den Weg."
            }
        """.trimIndent()
    }
}
