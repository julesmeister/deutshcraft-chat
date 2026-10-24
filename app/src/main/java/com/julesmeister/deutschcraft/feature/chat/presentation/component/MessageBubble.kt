package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschSurfaceHigh
import com.julesmeister.deutschcraft.ui.theme.DeutschTextOnLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextPrimary
import com.julesmeister.deutschcraft.ui.theme.DeutschTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val isUserMessage =
        message.sender == MessageSender.USER

    val bubbleColor =
        if (isUserMessage) {
            DeutschLime
        } else {
            DeutschSurfaceHigh
        }

    val contentColor =
        if (isUserMessage) {
            DeutschTextOnLime
        } else {
            DeutschTextPrimary
        }

    val timeColor =
        if (isUserMessage) {
            DeutschTextOnLime.copy(alpha = 0.65f)
        } else {
            DeutschTextSecondary
        }

    val bubbleShape =
        if (isUserMessage) {
            RoundedCornerShape(
                topStart = 24.dp,
                topEnd = 24.dp,
                bottomStart = 24.dp,
                bottomEnd = 7.dp
            )
        } else {
            RoundedCornerShape(
                topStart = 24.dp,
                topEnd = 24.dp,
                bottomStart = 7.dp,
                bottomEnd = 24.dp
            )
        }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        if (isUserMessage) {
            Spacer(modifier = Modifier.weight(0.18f))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(bubbleShape)
                .background(bubbleColor)
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                )
        ) {
            Column {
                Text(
                    text = message.content,
                    color = contentColor,
                    style =
                        MaterialTheme.typography.bodyLarge
                )

                Text(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 5.dp),
                    text = formatMessageTime(
                        message.timestamp
                    ),
                    color = timeColor,
                    style =
                        MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (!isUserMessage) {
            Spacer(modifier = Modifier.weight(0.18f))
        }
    }
}

private fun formatMessageTime(
    timestamp: Long
): String {
    return SimpleDateFormat(
        "HH:mm",
        Locale.getDefault()
    ).format(Date(timestamp))
}