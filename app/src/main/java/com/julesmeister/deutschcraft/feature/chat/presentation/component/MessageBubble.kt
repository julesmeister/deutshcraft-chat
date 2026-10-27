package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender

@Composable
fun MessageBubble(
    message: ChatMessage,
    correction: String?,
    isLatestUserMessage: Boolean,
    isLatestAssistantMessage: Boolean,

    /*
     * Controls only the connecting corner of the source
     * bubble.
     *
     * User:
     *     flatten bottom-left
     *
     * Assistant:
     *     flatten bottom-right
     */
    hasAttachedPhrase: Boolean,

    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUserMessage =
        message.sender ==
                MessageSender.USER

    val colors =
        rememberMessageBubbleColors(
            isUserMessage =
                isUserMessage,
            isLatestUserMessage =
                isLatestUserMessage,
            isLatestAssistantMessage =
                isLatestAssistantMessage
        )

    val maxBubbleWidth =
        (
                LocalConfiguration.current.screenWidthDp.dp *
                        MAXIMUM_BUBBLE_WIDTH_FRACTION
                )
            .coerceAtMost(
                MAXIMUM_BUBBLE_WIDTH_CAP
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
                messageBubbleShape(
                    isUserMessage =
                        isUserMessage,
                    hasAttachedPhrase =
                        hasAttachedPhrase
                ),
            backgroundColor =
                colors.backgroundColor,
            borderColor =
                colors.borderColor,
            maxWidth =
                maxBubbleWidth,
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

private const val MAXIMUM_BUBBLE_WIDTH_FRACTION =
    0.78f

private val MAXIMUM_BUBBLE_WIDTH_CAP =
    420.dp