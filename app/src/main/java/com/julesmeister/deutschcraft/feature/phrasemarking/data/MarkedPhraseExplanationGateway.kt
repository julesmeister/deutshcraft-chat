package com.julesmeister.deutschcraft.feature.phrasemarking.data

import android.util.Log
import com.julesmeister.deutschcraft.BuildConfig
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenApi
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenChatCompletionRequest
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenMessage
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenResponseFormat
import com.julesmeister.deutschcraft.feature.chat.data.cache.AiResultCache
import com.julesmeister.deutschcraft.feature.phrasemarking.domain.MarkedPhraseType
import com.julesmeister.deutschcraft.feature.phrasemarking.domain.PhraseExplanation
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/*
 * Asks Qwen to explain one marked phrase in the context of
 * the sentence it came from.
 */
@Singleton
class MarkedPhraseExplanationGateway
@Inject constructor(
    private val qwenApi: QwenApi,
    private val json: Json
) {

    private data class ExplanationKey(
        val phrase: String,
        val sourceSentence: String,
        val type: MarkedPhraseType
    )

    private val explanations = AiResultCache<ExplanationKey, PhraseExplanation?>()

    suspend fun explain(
        phrase: String,
        sourceSentence: String,
        type: MarkedPhraseType
    ): PhraseExplanation? {
        if (BuildConfig.QWEN_API_KEY.isBlank()) {
            return null
        }

        return explanations.getOrLoad(
            key = ExplanationKey(phrase, sourceSentence, type),
            cacheable = { it != null }
        ) {
            requestExplanation(phrase, sourceSentence, type)
        }
    }

    private suspend fun requestExplanation(
        phrase: String,
        sourceSentence: String,
        type: MarkedPhraseType
    ): PhraseExplanation? {
        return try {
            val response =
                qwenApi.createChatCompletion(
                    request =
                        QwenChatCompletionRequest(
                            model = BuildConfig.QWEN_MODEL,
                            messages = buildMessages(phrase, sourceSentence, type),
                            enableThinking = false,
                            temperature = TEMPERATURE,
                            maxTokens = MAXIMUM_TOKENS,
                            responseFormat = QwenResponseFormat(type = "json_object")
                        )
                )

            val content =
                response
                    .choices
                    .firstOrNull()
                    ?.message
                    ?.content
                    ?.trim()
                    .orEmpty()

            parse(content) ?: throw IllegalStateException(
                "Qwen returned an invalid phrase explanation."
            )
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.e(GATEWAY_TAG, "Phrase explanation failed.", throwable)
            null
        }
    }

    private fun buildMessages(
        phrase: String,
        sourceSentence: String,
        type: MarkedPhraseType
    ): List<QwenMessage> {
        val focus =
            when (type) {
                MarkedPhraseType.DIFFICULT_WORD -> {
                    "Focus on the word or expression itself. " +
                            "For nouns give the article and plural. " +
                            "For verbs give the infinitive."
                }

                MarkedPhraseType.GRAMMAR -> {
                    "Focus on the grammar: case, verb position, " +
                            "tense, endings, or sentence structure."
                }
            }

        return listOf(
            QwenMessage(
                role = "system",
                content =
                    """
                    You are a friendly German tutor inside the
                    DeutschCraft app. A learner highlighted a
                    phrase from a German sentence because it is
                    difficult for them.

                    $focus

                    Return exactly one JSON object:

                    {
                      "meaning": "short English meaning of the phrase as used here",
                      "explanation": "one or two short, simple English sentences",
                      "example": "one new simple German sentence using it"
                    }

                    Keep everything short. Return JSON only.
                    """.trimIndent()
            ),
            QwenMessage(
                role = "user",
                content =
                    """
                    Sentence: "$sourceSentence"
                    Marked phrase: "$phrase"
                    """.trimIndent()
            )
        )
    }

    private fun parse(content: String): PhraseExplanation? {
        val cleaned =
            content
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

        val root =
            runCatching { json.parseToJsonElement(cleaned) as? JsonObject }
                .getOrNull()
                ?: return null

        fun field(key: String): String? =
            (root[key] as? JsonPrimitive)
                ?.contentOrNull
                ?.trim()
                ?.takeIf(String::isNotBlank)

        return PhraseExplanation(
            meaning = field("meaning") ?: return null,
            explanation = field("explanation") ?: return null,
            example = field("example").orEmpty()
        )
    }

    private companion object {
        const val GATEWAY_TAG = "PhraseExplanation"
        const val TEMPERATURE = 0.2
        const val MAXIMUM_TOKENS = 300
    }
}