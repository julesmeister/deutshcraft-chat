package com.julesmeister.deutschcraft.feature.chat.presentation

import com.julesmeister.deutschcraft.feature.chat.data.cache.ReplySuggestionCache
import com.julesmeister.deutschcraft.feature.chat.domain.ReplySuggestion
import com.julesmeister.deutschcraft.feature.chat.domain.usecase.InitializeChatUseCase
import com.julesmeister.deutschcraft.feature.mission.domain.usecase.SeedMissionCatalogUseCase
import javax.inject.Inject

class ChatSessionInitializer @Inject constructor(
    private val initializeChat:
    InitializeChatUseCase,
    private val seedMissionCatalog:
    SeedMissionCatalogUseCase,
    private val replySuggestionCache:
    ReplySuggestionCache
) {

    suspend fun initialize(
        requestedConversationId: String
    ): ChatSessionInitializationResult {
        val conversation =
            initializeChat(
                conversationId =
                    requestedConversationId
            )

        /*
         * Seed predefined missions before active-mission
         * observation begins.
         *
         * The seeder checks Room and does not create
         * duplicate templates.
         */
        seedMissionCatalog()

        val cachedSuggestions =
            replySuggestionCache
                .getSuggestions(
                    conversationId =
                        conversation.id
                )

        return ChatSessionInitializationResult(
            conversationId =
                conversation.id,
            cachedSuggestions =
                cachedSuggestions
        )
    }
}

data class ChatSessionInitializationResult(
    val conversationId: String,
    val cachedSuggestions:
    List<ReplySuggestion>
)