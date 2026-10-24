package com.julesmeister.deutschcraft.feature.threads.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschSurfaceHigh
import com.julesmeister.deutschcraft.ui.theme.DeutschTextOnLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextPrimary
import com.julesmeister.deutschcraft.ui.theme.DeutschTextSecondary

@Composable
fun ThreadListScreen(
    onThreadClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor =
            MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(
                    horizontal = 16.dp,
                    vertical = 18.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(18.dp)
        ) {
            ThreadListHeader(
                onNewThreadClick = {
                    onThreadClick(
                        PRIMARY_CONVERSATION_ID
                    )
                }
            )

            Text(
                text = "Heute",
                color = DeutschTextSecondary,
                style =
                    MaterialTheme.typography.labelLarge
            )

            ThreadCard(
                title = "Neue Unterhaltung",
                preview =
                    "Öffne deine aktuelle Deutsch-Unterhaltung",
                level = "A1",
                messageCount = 0,
                onClick = {
                    onThreadClick(
                        PRIMARY_CONVERSATION_ID
                    )
                }
            )
        }
    }
}

@Composable
private fun ThreadListHeader(
    onNewThreadClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Unterhaltungen",
                color = DeutschTextPrimary,
                style =
                    MaterialTheme.typography
                        .headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                modifier = Modifier.padding(top = 3.dp),
                text =
                    "Deine täglichen Deutsch-Gespräche",
                color = DeutschTextSecondary,
                style =
                    MaterialTheme.typography.bodySmall
            )
        }

        IconButton(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(DeutschLime),
            onClick = onNewThreadClick
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription =
                    "Start new conversation",
                tint = DeutschTextOnLime
            )
        }
    }
}

@Composable
private fun ThreadCard(
    title: String,
    preview: String,
    level: String,
    messageCount: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(22.dp)
            )
            .background(DeutschSurfaceHigh)
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    DeutschLime.copy(
                        alpha = 0.15f
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = level,
                color = DeutschLime,
                style =
                    MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = DeutschTextPrimary,
                style =
                    MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )

            Text(
                modifier = Modifier.padding(top = 5.dp),
                text = preview,
                color = DeutschTextSecondary,
                style =
                    MaterialTheme.typography.bodySmall,
                maxLines = 1
            )

            Text(
                modifier = Modifier.padding(top = 8.dp),
                text =
                    if (messageCount == 1) {
                        "1 Nachricht"
                    } else {
                        "$messageCount Nachrichten"
                    },
                color =
                    DeutschTextSecondary.copy(
                        alpha = 0.72f
                    ),
                style =
                    MaterialTheme.typography.labelMedium
            )
        }
    }
}

private const val PRIMARY_CONVERSATION_ID =
    "primary-conversation"