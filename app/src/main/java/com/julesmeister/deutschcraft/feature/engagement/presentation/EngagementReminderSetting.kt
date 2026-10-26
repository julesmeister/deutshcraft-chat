package com.julesmeister.deutschcraft.feature.engagement.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.engagement.notification.NotificationPermissionHandler

@Composable
fun EngagementReminderSetting(
    state: EngagementSettingsState,
    onEnabledChanged: (Boolean) -> Unit,
    onPermissionResult: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    NotificationPermissionHandler(
        shouldRequestPermission =
            state.shouldRequestPermission,
        onPermissionResult =
            onPermissionResult
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Column(
            modifier =
                Modifier.weight(1f)
        ) {
            Text(
                text =
                    "Nachrichten von Emil",
                style =
                    MaterialTheme
                        .typography
                        .titleMedium
            )

            Text(
                text =
                    "Emil schickt dir nach längerer " +
                            "Inaktivität ein neues Gesprächsthema.",
                modifier =
                    Modifier.padding(
                        top = 4.dp
                    ),
                style =
                    MaterialTheme
                        .typography
                        .bodySmall,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }

        Switch(
            checked =
                state.remindersEnabled,
            onCheckedChange =
                onEnabledChanged
        )
    }
}