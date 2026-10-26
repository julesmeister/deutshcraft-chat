package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun MessageBubbleContent(
    message: ChatMessage,
    correction: String?,
    isUserMessage: Boolean,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    val contentAlignment =
        if (isUserMessage) {
            Alignment.End
        } else {
            Alignment.Start
        }

    Column(
        modifier =
            modifier.wrapContentWidth(),
        horizontalAlignment =
            contentAlignment
    ) {
        Text(
            text =
                message.content,
            style =
                MaterialTheme
                    .typography
                    .bodyLarge,
            color =
                contentColor
        )

        MessageTimestamp(
            timestamp =
                message.timestamp,
            contentColor =
                contentColor,
            modifier =
                Modifier.align(
                    contentAlignment
                )
        )

        if (
            isUserMessage &&
            !correction.isNullOrBlank()
        ) {
            MessageCorrectionContent(
                correction =
                    correction,
                contentColor =
                    contentColor,
                modifier =
                    Modifier.align(
                        Alignment.End
                    )
            )
        }
    }
}

@Composable
private fun MessageTimestamp(
    timestamp: Long,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Text(
        text =
            formatMessageTime(
                timestamp =
                    timestamp
            ),
        modifier =
            modifier.padding(
                top = 4.dp
            ),
        style =
            MaterialTheme
                .typography
                .labelSmall,
        color =
            contentColor.copy(
                alpha = 0.65f
            )
    )
}

private fun formatMessageTime(
    timestamp: Long
): String {
    val formatter =
        DateTimeFormatter.ofPattern(
            TIME_FORMAT_PATTERN,
            Locale.getDefault()
        )

    return Instant
        .ofEpochMilli(
            timestamp
        )
        .atZone(
            ZoneId.systemDefault()
        )
        .format(
            formatter
        )
}

private const val TIME_FORMAT_PATTERN =
    "HH:mm"