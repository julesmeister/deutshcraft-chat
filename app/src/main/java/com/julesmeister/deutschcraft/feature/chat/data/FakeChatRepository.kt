package com.julesmeister.deutschcraft.feature.chat.data

import kotlinx.coroutines.delay
import java.util.UUID
import com.julesmeister.deutschcraft.feature.chat.domain.AssistantReply
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.ChatRepository
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import com.julesmeister.deutschcraft.feature.chat.domain.ReplySuggestion

class FakeChatRepository : ChatRepository {

    override suspend fun sendMessage(
        message: String
    ): AssistantReply {
        delay(1_200)

        return AssistantReply(
            message = ChatMessage(
                id = UUID.randomUUID().toString(),
                content = "Das klingt interessant! Wie fühlst du dich dabei?",
                sender = MessageSender.ASSISTANT,
                timestamp = System.currentTimeMillis()
            ),
            suggestions = listOf(
                ReplySuggestion(
                    id = UUID.randomUUID().toString(),
                    text = "Ich fühle mich sehr gut."
                ),
                ReplySuggestion(
                    id = UUID.randomUUID().toString(),
                    text = "Ich bin ein bisschen nervös."
                ),
                ReplySuggestion(
                    id = UUID.randomUUID().toString(),
                    text = "Ich weiß es noch nicht."
                )
            )
        )
    }
}