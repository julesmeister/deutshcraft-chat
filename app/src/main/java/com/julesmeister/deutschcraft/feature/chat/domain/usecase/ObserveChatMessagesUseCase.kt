package com.julesmeister.deutschcraft.feature.chat.domain.usecase

import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.ChatRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveChatMessagesUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {

    operator fun invoke(
        conversationId: String
    ): Flow<List<ChatMessage>> {
        return chatRepository.observeMessages(
            conversationId = conversationId
        )
    }
}