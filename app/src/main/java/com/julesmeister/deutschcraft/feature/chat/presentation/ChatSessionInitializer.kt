package com.julesmeister.deutschcraft.feature.chat.presentation

import com.julesmeister.deutschcraft.feature.chat.data.cache.ReplySuggestionCache
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.ChatRepository
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import com.julesmeister.deutschcraft.feature.chat.domain.ReplySuggestion
import com.julesmeister.deutschcraft.feature.chat.domain.usecase.InitializeChatUseCase
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatPresentationTokens
import com.julesmeister.deutschcraft.feature.mission.domain.usecase.SeedMissionCatalogUseCase
import java.time.Clock
import javax.inject.Inject

class ChatSessionInitializer
@Inject constructor(
    private val initializeChat:
    InitializeChatUseCase,

    private val seedMissionCatalog:
    SeedMissionCatalogUseCase,

    private val replySuggestionCache:
    ReplySuggestionCache,

    private val chatRepository:
    ChatRepository,

    private val clock:
    Clock
) {

    suspend fun initialize(
        requestedConversationId: String
    ): ChatSessionInitializationResult {
        require(
            requestedConversationId.isNotBlank()
        ) {
            "Conversation ID cannot be blank."
        }

        /*
         * Check persistence before initialization.
         *
         * A missing conversation ID represents a provisional
         * ordinary conversation.
         */
        val persistedConversation =
            chatRepository.getConversation(
                conversationId =
                    requestedConversationId
            )

        val isProvisional =
            persistedConversation ==
                    null

        /*
         * InitializeChatUseCase returns either:
         *
         * - the normal persisted conversation
         * - an in-memory provisional conversation
         *
         * InitializeChatUseCase must not persist a missing
         * ordinary conversation.
         */
        val conversation =
            initializeChat(
                conversationId =
                    requestedConversationId
            )

        /*
         * Seed predefined missions before active-mission
         * observation begins.
         */
        seedMissionCatalog()

        val cachedSuggestions =
            if (isProvisional) {
                emptyList()
            } else {
                replySuggestionCache
                    .getSuggestions(
                        conversationId =
                            conversation.id
                    )
            }

        val provisionalMessages =
            if (isProvisional) {
                listOf(
                    createProvisionalGreeting(
                        conversationId =
                            conversation.id
                    )
                )
            } else {
                emptyList()
            }

        return ChatSessionInitializationResult(
            conversationId =
                conversation.id,

            cachedSuggestions =
                cachedSuggestions,

            provisionalMessages =
                provisionalMessages,

            isProvisional =
                isProvisional
        )
    }

    private fun createProvisionalGreeting(
        conversationId: String
    ): ChatMessage {
        return ChatMessage(
            id =
                provisionalGreetingId(
                    conversationId =
                        conversationId
                ),

            conversationId =
                conversationId,

            content =
                ChatPresentationTokens.SessionInitializer.InitialGreeting,

            sender =
                MessageSender.ASSISTANT,

            sequenceNumber =
                ChatPresentationTokens.SessionInitializer.InitialMessageSequenceNumber,

            timestamp =
                clock.millis(),

            correction =
                null
        )
    }

    private fun provisionalGreetingId(
        conversationId: String
    ): String {
        return conversationId +
                ChatPresentationTokens.SessionInitializer.ProvisionalGreetingIdSuffix
    }

}

data class ChatSessionInitializationResult(
    val conversationId: String,

    val cachedSuggestions:
    List<ReplySuggestion>,

    val provisionalMessages:
    List<ChatMessage>,

    val isProvisional: Boolean
)
