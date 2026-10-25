package com.julesmeister.deutschcraft.feature.threads.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.threads.domain.ConversationThread
import com.julesmeister.deutschcraft.ui.theme.DeutschError
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschSurface
import com.julesmeister.deutschcraft.ui.theme.DeutschSurfaceHigh
import com.julesmeister.deutschcraft.ui.theme.DeutschTextOnLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextPrimary
import com.julesmeister.deutschcraft.ui.theme.DeutschTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadActionDialog(
    thread: ConversationThread,
    onArchiveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(
                    RoundedCornerShape(28.dp)
                )
                .background(DeutschSurfaceHigh)
                .padding(20.dp)
        ) {
            DialogHeader(
                thread = thread,
                onDismiss = onDismiss
            )

            Text(
                modifier = Modifier.padding(
                    top = 20.dp,
                    bottom = 10.dp
                ),
                text = "Was möchtest du tun?",
                color = DeutschTextSecondary,
                style =
                    MaterialTheme.typography
                        .labelMedium
            )

            ThreadActionItem(
                title = "Archivieren",
                description =
                    "Aus der Hauptliste ausblenden",
                icon = Icons.Outlined.Archive,
                iconBackground = DeutschLime,
                iconColor = DeutschTextOnLime,
                titleColor = DeutschTextPrimary,
                onClick = onArchiveClick
            )

            ThreadActionItem(
                modifier = Modifier.padding(
                    top = 8.dp
                ),
                title = "Dauerhaft löschen",
                description =
                    "Unterhaltung und Nachrichten entfernen",
                icon =
                    Icons.Outlined.DeleteOutline,
                iconBackground =
                    DeutschError.copy(
                        alpha = 0.16f
                    ),
                iconColor = DeutschError,
                titleColor = DeutschError,
                onClick = onDeleteClick
            )
        }
    }
}

@Composable
private fun DialogHeader(
    thread: ConversationThread,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    DeutschLime.copy(
                        alpha = 0.14f
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = thread.cefrLevel.name,
                color = DeutschLime,
                style =
                    MaterialTheme.typography
                        .labelLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = thread.displayTitle,
                color = DeutschTextPrimary,
                style =
                    MaterialTheme.typography
                        .titleMedium,
                fontWeight =
                    FontWeight.SemiBold,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis
            )

            Text(
                modifier = Modifier.padding(
                    top = 3.dp
                ),
                text = thread.displayPreview,
                color = DeutschTextSecondary,
                style =
                    MaterialTheme.typography
                        .bodySmall,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis
            )
        }

        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(DeutschSurface)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(19.dp),
                imageVector =
                    Icons.Outlined.Close,
                contentDescription = "Schließen",
                tint = DeutschTextSecondary
            )
        }
    }
}

@Composable
private fun ThreadActionItem(
    title: String,
    description: String,
    icon: ImageVector,
    iconBackground:
    androidx.compose.ui.graphics.Color,
    iconColor:
    androidx.compose.ui.graphics.Color,
    titleColor:
    androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = 4.dp,
                vertical = 10.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(22.dp),
                imageVector = icon,
                contentDescription = null,
                tint = iconColor
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = titleColor,
                style =
                    MaterialTheme.typography
                        .titleMedium,
                fontWeight =
                    FontWeight.SemiBold
            )

            Text(
                modifier = Modifier.padding(
                    top = 2.dp
                ),
                text = description,
                color = DeutschTextSecondary,
                style =
                    MaterialTheme.typography
                        .bodySmall
            )
        }
    }
}