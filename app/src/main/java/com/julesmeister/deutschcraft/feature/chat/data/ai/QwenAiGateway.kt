package com.julesmeister.deutschcraft.feature.chat.data.ai

import android.util.Log
import com.julesmeister.deutschcraft.BuildConfig
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import retrofit2.HttpException

@Singleton
class QwenAiGateway @Inject constructor(
    private val qwenApi: QwenApi,
    private val json: Json
) : DeutschCraftAiGateway {

    override suspend fun generateReply(
        learnerMessage: String,
        conversationHistory: List<ChatMessage>,
        cefrLevel: CefrLevel
    ): AiReply {
        if (BuildConfig.QWEN_API_KEY.isBlank()) {
            throw AiGatewayException(
                message = "The Qwen API key is missing."
            )
        }

        return try {
            val initialContent =
                requestCompletion(
                    messages = buildPromptMessages(
                        learnerMessage = learnerMessage,
                        conversationHistory =
                            conversationHistory,
                        cefrLevel = cefrLevel
                    )
                )

            val initialPayload =
                decodePayload(
                    responseContent = initialContent
                )

            val validInitialReply =
                validatePayload(
                    payload = initialPayload,
                    learnerMessage = learnerMessage
                )

            if (validInitialReply != null) {
                return validInitialReply
            }

            Log.w(
                QWEN_GATEWAY_TAG,
                "Qwen returned an invalid or echoed reply. " +
                        "Retrying once."
            )

            val repairedContent =
                requestCompletion(
                    messages = buildRepairMessages(
                        learnerMessage = learnerMessage,
                        malformedResponse = initialContent,
                        cefrLevel = cefrLevel
                    ),
                    temperature = REPAIR_TEMPERATURE
                )

            val repairedPayload =
                decodePayload(
                    responseContent = repairedContent
                )

            validatePayload(
                payload = repairedPayload,
                learnerMessage = learnerMessage
            ) ?: throw AiGatewayException(
                message =
                    "Qwen did not return a usable reply " +
                            "after one retry."
            )
        } catch (
            cancellationException: CancellationException
        ) {
            throw cancellationException
        } catch (
            httpException: HttpException
        ) {
            throw AiGatewayException(
                message = createHttpErrorMessage(
                    statusCode = httpException.code()
                ),
                cause = httpException
            )
        } catch (
            ioException: IOException
        ) {
            throw AiGatewayException(
                message =
                    "DeutschCraft could not reach Qwen. " +
                            "Check your internet connection.",
                cause = ioException
            )
        } catch (
            gatewayException: AiGatewayException
        ) {
            throw gatewayException
        } catch (throwable: Throwable) {
            throw AiGatewayException(
                message =
                    throwable.message
                        ?: "DeutschCraft could not " +
                        "generate an AI reply.",
                cause = throwable
            )
        }
    }

    private suspend fun requestCompletion(
        messages: List<QwenMessage>,
        temperature: Double =
            RESPONSE_TEMPERATURE
    ): String {
        val request =
            QwenChatCompletionRequest(
                model = BuildConfig.QWEN_MODEL,
                messages = messages,
                enableThinking = false,
                temperature = temperature,
                maxTokens = MAXIMUM_RESPONSE_TOKENS,
                responseFormat = QwenResponseFormat(
                    type = JSON_OBJECT_TYPE
                )
            )

        val response =
            qwenApi.createChatCompletion(
                request = request
            )

        return response
            .choices
            .firstOrNull()
            ?.message
            ?.content
            ?.trim()
            .orEmpty()
            .takeIf { content ->
                content.isNotBlank()
            }
            ?: throw AiGatewayException(
                message =
                    "Qwen returned an empty response."
            )
    }

    private fun buildPromptMessages(
        learnerMessage: String,
        conversationHistory: List<ChatMessage>,
        cefrLevel: CefrLevel
    ): List<QwenMessage> {
        val recentHistory =
            conversationHistory
                .takeLast(MAXIMUM_HISTORY_MESSAGES)
                .filter { message ->
                    message.content.isNotBlank()
                }
                .map { message ->
                    QwenMessage(
                        role =
                            message.sender.toQwenRole(),
                        content =
                            message.content.trim()
                    )
                }

        val latestMessageAlreadyIncluded =
            recentHistory.lastOrNull()?.let {
                    latestMessage ->

                latestMessage.role == USER_ROLE &&
                        normalize(latestMessage.content) ==
                        normalize(learnerMessage)
            } ?: false

        return buildList {
            add(
                QwenMessage(
                    role = SYSTEM_ROLE,
                    content = createSystemPrompt(
                        cefrLevel = cefrLevel
                    )
                )
            )

            addAll(recentHistory)

            if (!latestMessageAlreadyIncluded) {
                add(
                    QwenMessage(
                        role = USER_ROLE,
                        content = learnerMessage.trim()
                    )
                )
            }
        }
    }

    private fun buildRepairMessages(
        learnerMessage: String,
        malformedResponse: String,
        cefrLevel: CefrLevel
    ): List<QwenMessage> {
        return listOf(
            QwenMessage(
                role = SYSTEM_ROLE,
                content =
                    """
                    You repair malformed JSON responses for
                    DeutschCraft.

                    Return exactly one JSON object containing:

                    {
                      "reply": "A new German assistant reply",
                      "suggestions": [
                        "First learner response",
                        "Second learner response",
                        "Third learner response"
                      ]
                    }

                    Requirements:
                    - "reply" must answer the learner.
                    - "reply" must not repeat the learner's
                      sentence.
                    - Suggestions must respond to "reply".
                    - Suggestions must be strings, not objects.
                    - Use CEFR level ${cefrLevel.name}.
                    - Return JSON only.
                    """.trimIndent()
            ),
            QwenMessage(
                role = USER_ROLE,
                content =
                    """
                    Learner message:
                    "$learnerMessage"

                    Malformed response:
                    $malformedResponse

                    Repair it now as one valid JSON object.
                    """.trimIndent()
            )
        )
    }

    private fun createSystemPrompt(
        cefrLevel: CefrLevel
    ): String {
        return """
            You are DeutschCraft, a friendly German-language
            conversation partner.

            The learner's CEFR level is ${cefrLevel.name}.

            Respond naturally to the learner's latest message.

            If the learner uses incorrect German or inserts
            English words:
            - Understand their intended meaning.
            - Reply naturally to that meaning.
            - Model correct German in your response.
            - Do not simply repeat their incorrect sentence.

            Return exactly one JSON object:

            {
              "reply": "Your German assistant response",
              "suggestions": [
                "Short learner response one",
                "Short learner response two",
                "Short learner response three"
              ]
            }

            Strict rules:
            - "reply" must be a string.
            - "suggestions" must be an array of exactly
              three strings.
            - Never return an array at the top level.
            - Never return suggestions as objects.
            - Never return keys named "text".
            - The assistant reply must not equal the
              learner's latest message.
            - Suggestions are possible learner responses
              to the assistant's generated reply.
            - Return valid JSON only.
        """.trimIndent()
    }

    private fun decodePayload(
        responseContent: String
    ): QwenReplyPayload {
        val cleanedContent =
            removeMarkdownCodeFence(
                content = responseContent
            )

        val root =
            try {
                json.parseToJsonElement(
                    string = cleanedContent
                )
            } catch (
                exception: SerializationException
            ) {
                throw AiGatewayException(
                    message =
                        "Qwen returned invalid JSON.",
                    cause = exception
                )
            }

        return when (root) {
            is JsonObject -> {
                decodeObjectPayload(
                    root = root
                )
            }

            is JsonArray -> {
                /*
                 * A top-level suggestion array has no
                 * assistant reply. Mark it invalid so the
                 * gateway retries instead of displaying it.
                 */
                Log.w(
                    QWEN_GATEWAY_TAG,
                    "Qwen returned a top-level array " +
                            "instead of a reply object."
                )

                QwenReplyPayload()
            }

            else -> {
                QwenReplyPayload()
            }
        }
    }

    private fun decodeObjectPayload(
        root: JsonObject
    ): QwenReplyPayload {
        val reply =
            root.stringValue(
                key = REPLY_KEY
            )
                ?: root.stringValue(
                    key = CONTENT_KEY
                )
                ?: root.stringValue(
                    key = MESSAGE_KEY
                )
                ?: ""

        val suggestions =
            readSuggestions(
                element =
                    root[SUGGESTIONS_KEY]
                        ?: root[REPLIES_KEY]
            )

        return QwenReplyPayload(
            reply = reply,
            suggestions = suggestions
        )
    }

    private fun readSuggestions(
        element: JsonElement?
    ): List<String> {
        val array =
            element as? JsonArray
                ?: return emptyList()

        return array
            .mapNotNull { item ->
                when (item) {
                    is JsonPrimitive -> {
                        item.contentOrNull
                    }

                    is JsonObject -> {
                        item.stringValue(
                            key = TEXT_KEY
                        )
                    }

                    else -> {
                        null
                    }
                }
            }
            .map { suggestion ->
                suggestion.trim()
            }
            .filter { suggestion ->
                suggestion.isNotBlank()
            }
            .distinct()
            .take(REQUIRED_SUGGESTION_COUNT)
    }

    private fun validatePayload(
        payload: QwenReplyPayload,
        learnerMessage: String
    ): AiReply? {
        val reply =
            payload.reply.trim()

        if (
            reply.isBlank() ||
            looksLikeJson(reply) ||
            repliesAreEquivalent(
                reply = reply,
                learnerMessage = learnerMessage
            )
        ) {
            return null
        }

        return AiReply(
            content = reply,
            suggestions =
                payload.suggestions
                    .map { suggestion ->
                        suggestion
                            .trim()
                            .replace(
                                MULTIPLE_WHITESPACE_REGEX,
                                " "
                            )
                    }
                    .filter { suggestion ->
                        suggestion.isNotBlank() &&
                                !looksLikeJson(suggestion)
                    }
                    .distinct()
                    .take(REQUIRED_SUGGESTION_COUNT)
        )
    }

    private fun repliesAreEquivalent(
        reply: String,
        learnerMessage: String
    ): Boolean {
        val normalizedReply =
            normalize(reply)

        val normalizedLearnerMessage =
            normalize(learnerMessage)

        return normalizedReply ==
                normalizedLearnerMessage
    }

    private fun normalize(
        value: String
    ): String {
        return value
            .trim()
            .lowercase()
            .replace(
                NON_LETTER_OR_DIGIT_REGEX,
                " "
            )
            .replace(
                MULTIPLE_WHITESPACE_REGEX,
                " "
            )
            .trim()
    }

    private fun looksLikeJson(
        value: String
    ): Boolean {
        val trimmedValue =
            value.trim()

        return (
                trimmedValue.startsWith("[") &&
                        trimmedValue.endsWith("]")
                ) || (
                trimmedValue.startsWith("{") &&
                        trimmedValue.endsWith("}")
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

    private fun JsonObject.stringValue(
        key: String
    ): String? {
        return (
                this[key] as? JsonPrimitive
                )
            ?.contentOrNull
            ?.trim()
            ?.takeIf { value ->
                value.isNotBlank()
            }
    }

    private fun MessageSender.toQwenRole():
            String {
        return when (this) {
            MessageSender.USER ->
                USER_ROLE

            MessageSender.ASSISTANT ->
                ASSISTANT_ROLE
        }
    }

    private fun createHttpErrorMessage(
        statusCode: Int
    ): String {
        return when (statusCode) {
            401 -> {
                "The Qwen API key is invalid or " +
                        "belongs to another region."
            }

            403 -> {
                "The Qwen request is not authorized."
            }

            429 -> {
                "Qwen is temporarily rate-limiting " +
                        "DeutschCraft."
            }

            in 500..599 -> {
                "Qwen is temporarily unavailable."
            }

            else -> {
                "The Qwen request failed with status " +
                        "$statusCode."
            }
        }
    }

    private companion object {
        const val QWEN_GATEWAY_TAG =
            "QwenAiGateway"

        const val SYSTEM_ROLE =
            "system"

        const val USER_ROLE =
            "user"

        const val ASSISTANT_ROLE =
            "assistant"

        const val JSON_OBJECT_TYPE =
            "json_object"

        const val REPLY_KEY =
            "reply"

        const val CONTENT_KEY =
            "content"

        const val MESSAGE_KEY =
            "message"

        const val SUGGESTIONS_KEY =
            "suggestions"

        const val REPLIES_KEY =
            "replies"

        const val TEXT_KEY =
            "text"

        const val MAXIMUM_HISTORY_MESSAGES =
            12

        const val MAXIMUM_RESPONSE_TOKENS =
            500

        const val REQUIRED_SUGGESTION_COUNT =
            3

        const val RESPONSE_TEMPERATURE =
            0.55

        const val REPAIR_TEMPERATURE =
            0.15

        val MULTIPLE_WHITESPACE_REGEX =
            Regex("\\s+")

        val NON_LETTER_OR_DIGIT_REGEX =
            Regex("[^\\p{L}\\p{N}]+")
    }
}