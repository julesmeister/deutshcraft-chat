package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender

@Composable
fun MessageBubble(
    message: ChatMessage,
    correction: String?,
    isLatestUserMessage: Boolean,
    isLatestAssistantMessage: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUserMessage =
        message.sender == MessageSender.USER

    val colors =
        rememberMessageBubbleColors(
            isUserMessage =
                isUserMessage,
            isLatestUserMessage =
                isLatestUserMessage,
            isLatestAssistantMessage =
                isLatestAssistantMessage
        )

    val shape =
        messageBubbleShape(
            isUserMessage =
                isUserMessage
        )

    Row(
        modifier =
            modifier.fillMaxWidth(),
        horizontalArrangement =
            if (isUserMessage) {
                Arrangement.End
            } else {
                Arrangement.Start
            }
    ) {
        MessageBubbleContainer(
            shape =
                shape,
            backgroundColor =
                colors.backgroundColor,
            onClick =
                onClick
        ) {
            MessageBubbleContent(
                message =
                    message,
                correction =
                    correction,
                isUserMessage =
                    isUserMessage,
                contentColor =
                    colors.contentColor
            )
        }
    }
}