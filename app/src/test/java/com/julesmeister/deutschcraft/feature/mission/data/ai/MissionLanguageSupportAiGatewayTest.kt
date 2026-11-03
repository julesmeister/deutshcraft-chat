package com.julesmeister.deutschcraft.feature.mission.data.ai

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenApi
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenChatCompletionRequest
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenChatCompletionResponse
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenChoice
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenResponseMessage
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class MissionLanguageSupportAiGatewayTest {
    private val support = MissionLanguageSupport(
        vocabulary = List(4) { index -> MissionVocabulary("Wort $index", "Meaning $index", "Beispiel $index.") },
        grammar = List(2) { index -> MissionGrammar("Pattern $index", "Explanation $index", "Beispiel $index.") }
    )
    private val request = MissionLanguageSupportRequest(
        "mission", "template", CefrLevel.A2, "Order a meal", "A restaurant in Leipzig.",
        "Customer", listOf("Ask for a menu.", "Order a drink.")
    )

    @Test
    fun `generation retains both support sections with a bounded single request`() = runBlocking {
        val api = RecordingApi(Json.encodeToString(support))
        val result = MissionLanguageSupportAiGateway(api, Json).generate(request)
        assertEquals(support, result)
        val sent = api.requests.single()
        assertFalse(sent.enableThinking)
        assertEquals(1_600, sent.maxTokens)
        assertEquals("json_object", sent.responseFormat?.type)
        assertEquals(listOf("system", "user"), sent.messages.map { it.role })
        assertTrue(sent.messages.first().content.contains("not complete objectives on their behalf"))
        assertTrue(sent.messages.last().content.contains(request.scenario))
        assertTrue(sent.messages.last().content.contains(request.objectives.last()))
    }

    @Test
    fun `every CEFR level constrains grammar without mixing mission data into rules`() {
        val gateway = MissionLanguageSupportAiGateway(RecordingApi(""), Json)
        for (level in CefrLevel.entries) {
            val messages = gateway.buildMessages(request.copy(cefrLevel = level))
            assertTrue(messages.first().content.contains("Match CEFR ${level.name}"))
            assertTrue(messages.first().content.contains("do not introduce grammar above this level"))
            assertFalse(messages.first().content.contains(request.scenario))
            assertFalse(messages.last().content.contains(request.missionId))
        }
    }

    @Test
    fun `invalid and duplicate entries are errors not valid help`() {
        for (invalid in listOf(
            support.copy(vocabulary = support.vocabulary.take(3)),
            support.copy(grammar = emptyList()),
            support.copy(grammar = listOf(support.grammar.first(), support.grammar.first())),
            support.copy(vocabulary = support.vocabulary.dropLast(1) + support.vocabulary.last().copy(example = " "))
        )) {
            try {
                invalid.validate()
                fail("Incomplete or repetitive support must not be displayed or cached")
            } catch (expected: IllegalArgumentException) {
                assertTrue(expected.message.orEmpty().isNotBlank())
            }
        }
    }

    @Test
    fun `malformed and empty AI output surface explicit errors without extra requests`() = runBlocking {
        for (content in listOf("", "not JSON")) {
            val api = RecordingApi(content)
            try {
                MissionLanguageSupportAiGateway(api, Json).generate(request)
                fail("Invalid AI output must not become success")
            } catch (expected: IllegalStateException) {
                assertTrue(content.isEmpty())
            } catch (expected: SerializationException) {
                assertTrue(content.isNotEmpty())
            }
            assertEquals(1, api.requests.size)
        }
    }

    private class RecordingApi(private val content: String) : QwenApi {
        val requests = mutableListOf<QwenChatCompletionRequest>()
        override suspend fun createChatCompletion(request: QwenChatCompletionRequest): QwenChatCompletionResponse {
            requests += request
            return QwenChatCompletionResponse(
                choices = listOf(QwenChoice(message = QwenResponseMessage(content = content)))
            )
        }
    }
}
