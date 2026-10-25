package com.julesmeister.deutschcraft.feature.chat.domain.usecase

import com.julesmeister.deutschcraft.feature.chat.domain.ChatRepository
import com.julesmeister.deutschcraft.feature.chat.domain.Conversation
import com.julesmeister.deutschcraft.feature.progress.domain.ProgressRepository
import javax.inject.Inject

class InitializeChatUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val progressRepository: ProgressRepository
) {

    suspend operator fun invoke(
        conversationId: String
    ): Conversation {
        /*
         * The CEFR learner profile must exist before the
         * learner sends a message or opens Progress.
         */
        progressRepository.ensureProfileExists()

        return chatRepository.getOrCreateConversation(
            conversationId = conversationId
        )
    }
}