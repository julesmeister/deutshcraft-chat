package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MessageBubble(
    message: ChatMessage,
    correction: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUserMessage =
        message.sender == MessageSender.USER

    val bubbleShape =
        if (isUserMessage) {
            RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = 20.dp,
                bottomEnd = 6.dp
            )
        } else {
            RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = 6.dp,
                bottomEnd = 20.dp
            )
        }

    val bubbleColor =
        if (isUserMessage) {
            MaterialTheme
                .colorScheme
                .primaryContainer
        } else {
            MaterialTheme
                .colorScheme
                .surfaceContainer
        }

    val contentColor =
        if (isUserMessage) {
            MaterialTheme
                .colorScheme
                .onPrimaryContainer
        } else {
            MaterialTheme
                .colorScheme
                .onSurface
        }

    Row(
        modifier =
            modifier.fillMaxWidth(),
        horizontalArrangement =
            if (isUserMessage) {
                androidx.compose.foundation.layout
                    .Arrangement
                    .End
            } else {
                androidx.compose.foundation.layout
                    .Arrangement
                    .Start
            }
    ) {
        Box(
            modifier = Modifier
                .wrapContentWidth()
                .widthIn(
                    min = 72.dp,
                    max = 320.dp
                )
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = 0.82f,
                        stiffness = 420f
                    )
                )
                .clip(bubbleShape)
                .background(bubbleColor)
                .clickable(
                    interactionSource =
                        remember {
                            MutableInteractionSource()
                        },
                    indication = null,
                    onClick = onClick
                )
                .padding(
                    horizontal = 14.dp,
                    vertical = 10.dp
                )
        ) {
            Column(
                modifier = Modifier.wrapContentWidth(),
                horizontalAlignment =
                    if (isUserMessage) {
                        Alignment.End
                    } else {
                        Alignment.Start
                    }
            ) {
                Text(
                    text = message.content,
                    style =
                        MaterialTheme.typography.bodyLarge,
                    color = contentColor
                )

                Text(
                    text = formatMessageTime(
                        timestamp =
                            message.timestamp
                    ),
                    modifier = Modifier.padding(
                        top = 4.dp
                    ),
                    style =
                        MaterialTheme.typography.labelSmall,
                    color =
                        contentColor.copy(
                            alpha = 0.65f
                        ),
                    textAlign =
                        if (isUserMessage) {
                            TextAlign.End
                        } else {
                            TextAlign.Start
                        }
                )

                if (
                    isUserMessage &&
                    !correction.isNullOrBlank()
                ) {
                    MessageCorrectionContent(
                        correction = correction,
                        contentColor = contentColor
                    )
                }
            }
        }
    }
}

private fun formatMessageTime(
    timestamp: Long
): String {
    val formatter =
        DateTimeFormatter.ofPattern(
            "HH:mm",
            Locale.getDefault()
        )

    return Instant
        .ofEpochMilli(timestamp)
        .atZone(
            ZoneId.systemDefault()
        )
        .format(formatter)
}