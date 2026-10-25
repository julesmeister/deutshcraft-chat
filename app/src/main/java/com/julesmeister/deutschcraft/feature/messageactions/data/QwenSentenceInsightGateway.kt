package com.julesmeister.deutschcraft.feature.messageactions.data

import com.julesmeister.deutschcraft.BuildConfig
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenApi
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenChatCompletionRequest
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenMessage
import com.julesmeister.deutschcraft.feature.messageactions.domain.SentenceInsight
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class QwenSentenceInsightGateway @Inject constructor(
    private val qwenApi: QwenApi,
    private val json: Json
) : SentenceInsightGateway {

    override suspend fun analyzeSentence(
        sentence: String
    ): SentenceInsight {
        require(sentence.isNotBlank()) {
            "The selected sentence must not be blank."
        }

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
                        content = createUserPrompt(
                            sentence = sentence
                        )
                    )
                ),
                enableThinking = false,
                temperature = RESPONSE_TEMPERATURE,
                maxTokens = MAXIMUM_RESPONSE_TOKENS
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

            json.decodeFromString<SentenceInsight>(
                string =
                    removeMarkdownCodeFence(
                        content = responseContent
                    )
            )
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

    private fun createUserPrompt(
        sentence: String
    ): String {
        return """
            Analyze the complete German sentence below.

            Sentence:
            "$sentence"

            Return one JSON object with exactly these fields:

            {
              "translation": "Natural English translation of the complete sentence",
              "explanation": "A concise beginner-friendly explanation of the complete sentence and how it expresses its meaning",
              "simpleRephrase": "A simpler German sentence preserving the original meaning",
              "naturalRephrase": "A natural conversational German alternative preserving the original meaning",
              "advancedRephrase": "A more advanced German alternative preserving the original meaning"
            }

            Requirements:
            - Analyze the entire sentence, not individual vocabulary words.
            - Keep the explanation concise.
            - Preserve the original meaning in all rephrases.
            - Do not add Markdown.
            - Do not add code fences.
            - Return JSON only.
        """.trimIndent()
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

                Help German learners understand complete sentences.

                You provide:
                1. A natural English translation.
                2. A concise explanation of the sentence's overall meaning.
                3. Simple, natural, and advanced German rephrases.

                Do not provide a word-by-word vocabulary list.
                Return only valid JSON matching the requested schema.
            """.trimIndent()
    }
}