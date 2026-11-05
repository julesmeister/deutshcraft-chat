package com.julesmeister.deutschcraft.feature.chat.data.factory

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.chat.data.ai.DeutschCraftAiGateway
import com.julesmeister.deutschcraft.feature.chat.data.local.ChatLocalDataSource
import com.julesmeister.deutschcraft.feature.chat.data.local.toDomain
import com.julesmeister.deutschcraft.feature.chat.domain.AssistantReply
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import com.julesmeister.deutschcraft.feature.chat.domain.ReplySuggestion
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import com.julesmeister.deutschcraft.feature.mission.domain.MissionConversationContextMapper
import java.time.Clock
import java.util.UUID
import javax.inject.Inject

class AssistantReplyFactory @Inject constructor(
    private val localDataSource:
    ChatLocalDataSource,
    private val aiGateway:
    DeutschCraftAiGateway,
    private val replySuggestionFactory:
    ReplySuggestionFactory,
    private val missionContextMapper:
    MissionConversationContextMapper,
    private val clock:
    Clock
) {

    suspend fun create(
        conversationId: String,
        learnerMessage: String,
        activeMission: ConversationMission? =
            null
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

        val missionContext =
            missionContextMapper.map(
                mission =
                    activeMission
            )

        val aiReply =
            aiGateway.generateReply(
                learnerMessage =
                    learnerMessage,
                conversationHistory =
                    conversationHistory,
                cefrLevel =
                    learnerLevel,
                activeMissionContext =
                    missionContext
            )

        val nextSequenceNumber =
            localDataSource.getNextSequenceNumber(
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
                .filter(
                    String::isNotBlank
                )
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

    private companion object {
        const val REQUIRED_SUGGESTION_COUNT =
            3

        val MULTIPLE_WHITESPACE_REGEX =
            Regex("\\s+")
    }
}