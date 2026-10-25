package com.julesmeister.deutschcraft.feature.chat.data.correction

import com.julesmeister.deutschcraft.BuildConfig
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenApi
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenChatCompletionRequest
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenMessage
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

    override suspend fun correctSentence(
        sentence: String
    ): String? {
        val cleanedSentence =
            sentence.trim()

        if (cleanedSentence.isBlank()) {
            return null
        }

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
                            createUserPrompt(
                                sentence =
                                    cleanedSentence
                            )
                    )
                ),
                enableThinking = false,
                temperature =
                    CORRECTION_TEMPERATURE,
                maxTokens =
                    MAXIMUM_RESPONSE_TOKENS
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
                    null
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

    private fun createUserPrompt(
        sentence: String
    ): String {
        return """
            Check the following learner sentence:

            "$sentence"

            Decide whether it needs a meaningful correction.

            Correct only:
            - English words accidentally inserted into German
            - Incorrect German grammar
            - Incorrect verb conjugation
            - Incorrect word order
            - Missing words that make the sentence incomplete
            - Clearly unnatural German phrasing

            Do not correct:
            - Proper names
            - Harmless punctuation differences
            - Capitalization alone
            - Valid regional or conversational German
            - Stylistic preferences when the sentence is
              already correct
            - The learner's intended meaning

            If a correction is necessary, return the
            complete corrected German sentence.

            Return JSON only:

            {
              "needsCorrection": true,
              "correctedSentence": "Corrected German sentence"
            }

            If no meaningful correction is needed:

            {
              "needsCorrection": false,
              "correctedSentence": null
            }
        """.trimIndent()
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
                You are DeutschCraft's German sentence
                correction assistant.

                Correct meaningful language mistakes while
                preserving the learner's intended meaning.

                Do not unnecessarily rewrite sentences that
                are already acceptable German.

                Return only valid JSON matching the requested
                structure.
            """.trimIndent()
    }
}