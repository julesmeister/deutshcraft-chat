package com.julesmeister.deutschcraft.feature.chat.data.correction

import com.julesmeister.deutschcraft.BuildConfig
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenApi
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenChatCompletionRequest
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenMessage
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenResponseFormat
import com.julesmeister.deutschcraft.feature.chat.data.cache.AiResultCache
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

@Singleton
class QwenMessageCorrectionGateway @Inject constructor(
    private val qwenApi: QwenApi,
    private val json: Json
) : MessageCorrectionGateway {

    private val corrections = AiResultCache<String, String?>()

    override suspend fun correctSentence(
        sentence: String
    ): String? {
        val cleanedSentence =
            sentence.trim()

        if (cleanedSentence.isBlank()) {
            return null
        }

        return corrections.getOrLoad(cleanedSentence) {
            requestCorrection(cleanedSentence)
        }
    }

    private suspend fun requestCorrection(
        cleanedSentence: String
    ): String? {
        val request =
            QwenChatCompletionRequest(
                model =
                    BuildConfig.QWEN_MODEL,
                messages = listOf(
                    QwenMessage(
                        role = SYSTEM_ROLE,
                        content = SYSTEM_PROMPT
                    ),
                    QwenMessage(
                        role = USER_ROLE,
                        content =
                            "Learner sentence:\n$cleanedSentence"
                    )
                ),
                enableThinking = false,
                temperature =
                    CORRECTION_TEMPERATURE,
                maxTokens =
                    MAXIMUM_RESPONSE_TOKENS,
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
                throw MessageCorrectionException(
                    message =
                        "Qwen returned an empty correction."
                )
            }

            val payload =
                decodePayload(
                    content = responseContent
                )

            when {
                !payload.needsCorrection -> {
                    null
                }

                payload.correctedSentence
                    .isNullOrBlank() -> {
                    throw MessageCorrectionException(
                        message = "Qwen requested a correction but omitted the corrected sentence."
                    )
                }

                sentencesAreEquivalent(
                    original =
                        cleanedSentence,
                    corrected =
                        payload.correctedSentence
                ) -> {
                    null
                }

                else -> {
                    payload.correctedSentence
                        .trim()
                }
            }
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (
            correctionException:
            MessageCorrectionException
        ) {
            throw correctionException
        } catch (
            serializationException:
            SerializationException
        ) {
            throw MessageCorrectionException(
                message =
                    "Qwen returned an invalid " +
                            "correction format.",
                cause =
                    serializationException
            )
        } catch (throwable: Throwable) {
            throw MessageCorrectionException(
                message =
                    throwable.message
                        ?: "The learner message could " +
                        "not be corrected.",
                cause =
                    throwable
            )
        }
    }

    private fun decodePayload(
        content: String
    ): QwenMessageCorrectionPayload {
        val jsonObject =
            extractJsonObject(
                content =
                    removeMarkdownCodeFence(
                        content = content
                    )
            )

        return json.decodeFromString(
            string = jsonObject
        )
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

    private fun extractJsonObject(
        content: String
    ): String {
        val openingBrace =
            content.indexOf('{')

        val closingBrace =
            content.lastIndexOf('}')

        if (
            openingBrace < 0 ||
            closingBrace <= openingBrace
        ) {
            throw MessageCorrectionException(
                message =
                    "Qwen did not return a valid " +
                            "correction object."
            )
        }

        return content.substring(
            startIndex = openingBrace,
            endIndex = closingBrace + 1
        )
    }

    private fun sentencesAreEquivalent(
        original: String,
        corrected: String
    ): Boolean {
        return normalizeSentence(original) ==
                normalizeSentence(corrected)
    }

    private fun normalizeSentence(
        sentence: String
    ): String {
        return sentence
            .trim()
            .lowercase()
            .removeSuffix(".")
            .removeSuffix("!")
            .removeSuffix("?")
            .replace(
                MULTIPLE_WHITESPACE_REGEX,
                " "
            )
    }

    private companion object {
        const val SYSTEM_ROLE =
            "system"

        const val USER_ROLE =
            "user"

        const val CORRECTION_TEMPERATURE =
            0.1

        const val MAXIMUM_RESPONSE_TOKENS =
            180

        val MULTIPLE_WHITESPACE_REGEX =
            Regex("\\s+")

        val SYSTEM_PROMPT =
            """
                You are DeutschCraft's German sentence correction assistant.
                Check the learner sentence for meaningful mistakes, preserving intended meaning.
                Correct only accidental English words, incorrect German grammar, verb conjugation,
                word order, missing words making a sentence incomplete, or clearly unnatural phrasing.
                Do not change proper names, harmless punctuation, capitalization alone, valid
                regional/conversational German, intended meaning, or style of acceptable German.
                If needed, return the complete corrected German sentence, not a fragment.
                Return only one valid JSON object, no Markdown or code fences:
                {"needsCorrection":true,"correctedSentence":"Complete corrected German sentence"}
                If no meaningful correction is needed:
                {"needsCorrection":false,"correctedSentence":null}
            """.trimIndent()
    }
}