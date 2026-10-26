package com.julesmeister.deutschcraft.feature.chat.presentation

import com.julesmeister.deutschcraft.feature.chat.data.cache.ReplySuggestionCache
import com.julesmeister.deutschcraft.feature.chat.domain.ReplySuggestion
import com.julesmeister.deutschcraft.feature.chat.domain.usecase.SendChatMessageUseCase
import javax.inject.Inject

class ChatMessageSender @Inject constructor(
    private val sendChatMessage:
    SendChatMessageUseCase,
    private val replySuggestionCache:
    ReplySuggestionCache
) {

    suspend fun send(
        conversationId: String,
        messageText: String
    ): ChatMessageSendResult {
        val result =
            sendChatMessage(
                conversationId =
                    conversationId,
                messageText =
                    messageText
            )

        val suggestions =
            result
                .assistantReply
                .suggestions

        /*
         * Replace cached suggestions only after the
         * assistant reply succeeds.
         */
        replySuggestionCache.replaceSuggestions(
            conversationId =
                conversationId,
            suggestions =
                suggestions
        )

        return ChatMessageSendResult(
            suggestions =
                suggestions,
            warning =
                result.assessmentWarning
        )
    }

    fun clearSuggestions(
        conversationId: String
    ) {
        replySuggestionCache.clearSuggestions(
            conversationId =
                conversationId
        )
    }
}

data class ChatMessageSendResult(
    val suggestions:
    List<ReplySuggestion>,
    val warning: String?
)