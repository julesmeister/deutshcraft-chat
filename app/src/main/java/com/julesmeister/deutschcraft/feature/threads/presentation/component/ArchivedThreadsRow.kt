package com.julesmeister.deutschcraft.feature.threads.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.julesmeister.deutschcraft.feature.threads.presentation.theme.ThreadDesignTokens as Tokens
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextOnLime

@Composable
fun ArchivedThreadsRow(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberThreadColors()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = Tokens.ArchivedRowHorizontalPadding,
                vertical = Tokens.ArchivedRowVerticalPadding
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(Tokens.ArchivedIconCircleSize)
                .clip(Tokens.CircleShapeToken)
                .background(DeutschLime),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(Tokens.ArchivedIconSize),
                imageVector = Icons.Outlined.Archive,
                contentDescription = "Archivierte Chats",
                tint = DeutschTextOnLime
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = Tokens.ArchivedTextStartPadding)
        ) {
            Text(
                text = "Archivierte Chats",
                color = colors.textPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                modifier = Modifier.padding(top = Tokens.ArchivedCountTopPadding),
                text = archivedCountText(count = count),
                color = colors.textSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun archivedCountText(
    count: Int
): String {
    return if (count == 1) {
        "1 Unterhaltung"
    } else {
        "$count Unterhaltungen"
    }
}