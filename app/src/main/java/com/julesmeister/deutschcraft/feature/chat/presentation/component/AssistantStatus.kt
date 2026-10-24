package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextSecondary

@Composable
fun AssistantStatus(
    isAssistantTyping: Boolean,
    isAssessing: Boolean,
    modifier: Modifier = Modifier
) {
    val statusText = when {
        isAssistantTyping -> {
            "schreibt gerade"
        }

        isAssessing -> {
            "analysiert"
        }

        else -> {
            "online"
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(DeutschLime)
        )

        Text(
            text = statusText,
            color = DeutschTextSecondary,
            style =
                MaterialTheme.typography
                    .labelMedium
                    .copy(
                        fontSize = 11.sp,
                        lineHeight = 13.sp
                    ),
            fontWeight = FontWeight.Normal,
            maxLines = 1
        )
    }
}