package com.julesmeister.deutschcraft.feature.messageactions.data

import com.julesmeister.deutschcraft.BuildConfig
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenApi
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenChatCompletionRequest
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenMessage
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenResponseFormat
import com.julesmeister.deutschcraft.feature.chat.data.cache.AiResultCache
import com.julesmeister.deutschcraft.feature.messageactions.domain.SentenceInsight
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class QwenSentenceInsightGateway @Inject constructor(
    private val qwenApi: QwenApi,
    private val json: Json
) : SentenceInsightGateway {

    private val insights = AiResultCache<String, SentenceInsight>()

    override suspend fun analyzeSentence(
        sentence: String
    ): SentenceInsight {
        require(sentence.isNotBlank()) {
            "The selected sentence must not be blank."
        }

        return insights.getOrLoad(sentence.trim()) {
            requestInsight(sentence.trim())
        }
    }

    private suspend fun requestInsight(sentence: String): SentenceInsight {
        val request =
            QwenChatCompletionRequest(
                model = BuildConfig.QWEN_MODEL,
                messages = listOf(
                    QwenMessage(
                        role = SYSTEM_ROLE,
                        content = SYSTEM_PROMPT
                    ),
                    QwenMessage(
                        role = USER_ROLE,
                        content = "German sentence:\n$sentence"
                    )
                ),
                enableThinking = false,
                temperature = RESPONSE_TEMPERATURE,
                maxTokens = MAXIMUM_RESPONSE_TOKENS,
                responseFormat = QwenResponseFormat(type = "json_object")
            )

        return try {
            val response =
                qwenApi.createChatCompletion(
                    request = request
                )

            val responseContent =
                response
                    .choices
                    .firstOrNull()
                    ?.message
                    ?.content
                    ?.trim()
                    .orEmpty()

            if (responseContent.isBlank()) {
                throw SentenceInsightException(
                    message =
                        "Qwen returned an empty sentence analysis."
                )
            }

            val insight = json.decodeFromString<SentenceInsight>(
                string =
                    removeMarkdownCodeFence(
                        content = responseContent
                    )
            )
            if (listOf(
                    insight.translation,
                    insight.explanation,
                    insight.simpleRephrase,
                    insight.naturalRephrase,
                    insight.advancedRephrase
                ).any(String::isBlank)
            ) {
                throw SentenceInsightException(
                    message = "Qwen returned an incomplete sentence analysis."
                )
            }
            insight
        } catch (
            cancellationException: CancellationException
        ) {
            throw cancellationException
        } catch (
            sentenceInsightException:
            SentenceInsightException
        ) {
            throw sentenceInsightException
        } catch (
            serializationException: SerializationException
        ) {
            throw SentenceInsightException(
                message =
                    "Qwen returned an invalid sentence analysis.",
                cause = serializationException
            )
        } catch (throwable: Throwable) {
            throw SentenceInsightException(
                message =
                    throwable.message
                        ?: "The sentence could not be analyzed.",
                cause = throwable
            )
        }
    }

    private fun removeMarkdownCodeFence(
        content: String
    ): String {
        return content
            .trim()
            .removePrefix("```json")
            .removePrefix("```JSON")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }

    private companion object {
        const val SYSTEM_ROLE = "system"
        const val USER_ROLE = "user"

        const val RESPONSE_TEMPERATURE = 0.3
        const val MAXIMUM_RESPONSE_TOKENS = 550

        val SYSTEM_PROMPT =
            """
                You are DeutschCraft's sentence-analysis assistant.
                Analyze the entire German sentence, not individual words; no vocabulary list.
                Give a natural English translation, a concise beginner-friendly English explanation
                of its overall meaning and how it is expressed, and three German rephrases.
                All rephrases must preserve the original meaning.
                Return only one valid JSON object with exactly these string fields, no Markdown or fences:
                {
                  "translation":"Natural English translation of the complete sentence",
                  "explanation":"Concise explanation of the complete sentence",
                  "simpleRephrase":"Simpler German sentence",
                  "naturalRephrase":"Natural conversational German alternative",
                  "advancedRephrase":"More advanced German alternative"
                }
            """.trimIndent()
    }
}