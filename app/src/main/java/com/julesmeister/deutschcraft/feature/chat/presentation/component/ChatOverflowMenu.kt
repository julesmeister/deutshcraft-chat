package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.julesmeister.deutschcraft.ui.theme.DeutschError
import com.julesmeister.deutschcraft.ui.theme.DeutschTextPrimary

@Composable
fun ChatOverflowMenu(
    onNewConversationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onClearConversationClick: () -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    IconButton(
        onClick = {
            expanded = true
        }
    ) {
        Icon(
            imageVector =
                Icons.Outlined.MoreHoriz,
            contentDescription = "Chat menu",
            tint = DeutschTextPrimary
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = {
            expanded = false
        },
        containerColor =
            MaterialTheme.colorScheme
                .surfaceContainerHigh
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    text = "Neue Unterhaltung"
                )
            },
            leadingIcon = {
                Icon(
                    imageVector =
                        Icons.Outlined.Refresh,
                    contentDescription = null
                )
            },
            onClick = {
                expanded = false
                onNewConversationClick()
            }
        )

        DropdownMenuItem(
            text = {
                Text(
                    text = "Einstellungen"
                )
            },
            leadingIcon = {
                Icon(
                    imageVector =
                        Icons.Outlined.Settings,
                    contentDescription = null
                )
            },
            onClick = {
                expanded = false
                onSettingsClick()
            }
        )

        DropdownMenuItem(
            text = {
                Text(
                    text = "Chat löschen",
                    color = DeutschError
                )
            },
            leadingIcon = {
                Icon(
                    imageVector =
                        Icons.Outlined.DeleteSweep,
                    contentDescription = null,
                    tint = DeutschError
                )
            },
            onClick = {
                expanded = false
                onClearConversationClick()
            }
        )
    }
}