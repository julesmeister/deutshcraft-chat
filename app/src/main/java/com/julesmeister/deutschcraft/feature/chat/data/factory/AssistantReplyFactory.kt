package com.julesmeister.deutschcraft.feature.chat.data.factory

import android.util.Log
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.chat.data.ai.AiReply
import com.julesmeister.deutschcraft.feature.chat.data.ai.DeutschCraftAiGateway
import com.julesmeister.deutschcraft.feature.chat.data.local.ChatLocalDataSource
import com.julesmeister.deutschcraft.feature.chat.data.local.toDomain
import com.julesmeister.deutschcraft.feature.chat.domain.AssistantReply
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import com.julesmeister.deutschcraft.feature.chat.domain.ReplySuggestion
import java.time.Clock
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class AssistantReplyFactory @Inject constructor(
    private val localDataSource:
    ChatLocalDataSource,
    private val aiGateway:
    DeutschCraftAiGateway,
    private val replySuggestionFactory:
    ReplySuggestionFactory,
    private val clock:
    Clock
) {

    suspend fun create(
        conversationId: String,
        learnerMessage: String
    ): AssistantReply {
        val learnerLevel =
            getLearnerLevel(
                conversationId =
                    conversationId
            )

        val conversationHistory =
            localDataSource
                .getMessages(
                    conversationId =
                        conversationId
                )
                .map { entity ->
                    entity.toDomain()
                }

        val aiReply =
            generateAiReply(
                learnerMessage =
                    learnerMessage,
                conversationHistory =
                    conversationHistory,
                learnerLevel =
                    learnerLevel
            )

        val nextSequenceNumber =
            localDataSource
                .getNextSequenceNumber(
                    conversationId =
                        conversationId
                )

        val assistantMessage =
            ChatMessage(
                id =
                    UUID.randomUUID().toString(),
                conversationId =
                    conversationId,
                sequenceNumber =
                    nextSequenceNumber,
                sender =
                    MessageSender.ASSISTANT,
                content =
                    aiReply.content,
                timestamp =
                    clock.millis()
            )

        return AssistantReply(
            message =
                assistantMessage,
            suggestions =
                createSuggestions(
                    aiSuggestions =
                        aiReply.suggestions,
                    learnerLevel =
                        learnerLevel
                )
        )
    }

    private suspend fun getLearnerLevel(
        conversationId: String
    ): CefrLevel {
        return localDataSource
            .getConversation(
                conversationId =
                    conversationId
            )
            ?.cefrLevel
            ?: CefrLevel.A1
    }

    private suspend fun generateAiReply(
        learnerMessage: String,
        conversationHistory: List<ChatMessage>,
        learnerLevel: CefrLevel
    ): AiReply {
        return try {
            aiGateway.generateReply(
                learnerMessage =
                    learnerMessage,
                conversationHistory =
                    conversationHistory,
                cefrLevel =
                    learnerLevel
            )
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Log.e(
                ASSISTANT_REPLY_TAG,
                "Qwen request failed. Using the " +
                        "offline assistant response.",
                throwable
            )

            createOfflineFallback(
                learnerLevel =
                    learnerLevel
            )
        }
    }

    private fun createSuggestions(
        aiSuggestions: List<String>,
        learnerLevel: CefrLevel
    ): List<ReplySuggestion> {
        val cleanedAiSuggestions =
            aiSuggestions
                .map { suggestion ->
                    suggestion
                        .trim()
                        .replace(
                            MULTIPLE_WHITESPACE_REGEX,
                            " "
                        )
                }
                .filter { suggestion ->
                    suggestion.isNotBlank()
                }
                .distinct()
                .take(
                    REQUIRED_SUGGESTION_COUNT
                )

        val fallbackSuggestions =
            replySuggestionFactory
                .create(
                    cefrLevel =
                        learnerLevel
                )
                .map { suggestion ->
                    suggestion.text
                }

        return (
                cleanedAiSuggestions +
                        fallbackSuggestions
                )
            .distinct()
            .take(
                REQUIRED_SUGGESTION_COUNT
            )
            .map { suggestionText ->
                ReplySuggestion(
                    id =
                        UUID.randomUUID().toString(),
                    text =
                        suggestionText
                )
            }
    }

    private fun createOfflineFallback(
        learnerLevel: CefrLevel
    ): AiReply {
        val suggestions =
            replySuggestionFactory
                .create(
                    cefrLevel =
                        learnerLevel
                )
                .map { suggestion ->
                    suggestion.text
                }

        return AiReply(
            content =
                offlineReplyContent(
                    learnerLevel =
                        learnerLevel
                ),
            suggestions =
                suggestions
        )
    }

    private fun offlineReplyContent(
        learnerLevel: CefrLevel
    ): String {
        return when (learnerLevel) {
            CefrLevel.A1 -> {
                "Das ist interessant. " +
                        "Kannst du mehr erzählen?"
            }

            CefrLevel.A2 -> {
                "Ich verstehe. Warum denkst du das?"
            }

            CefrLevel.B1 -> {
                "Das ist ein interessanter Gedanke. " +
                        "Welche Erfahrungen hast du damit gemacht?"
            }

            CefrLevel.B2,
            CefrLevel.C1,
            CefrLevel.C2 -> {
                "Das ist eine interessante Perspektive. " +
                        "Welche Argumente sprechen dafür?"
            }
        }
    }

    private companion object {
        const val ASSISTANT_REPLY_TAG =
            "AssistantReplyFactory"

        const val REQUIRED_SUGGESTION_COUNT =
            3

        val MULTIPLE_WHITESPACE_REGEX =
            Regex("\\s+")
    }
}