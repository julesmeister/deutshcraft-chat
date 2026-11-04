package com.julesmeister.deutschcraft.feature.settings.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import com.julesmeister.deutschcraft.feature.auth.domain.AuthProvider
import com.julesmeister.deutschcraft.feature.auth.domain.AuthSession
import com.julesmeister.deutschcraft.feature.auth.domain.GuestMessagePolicy
import com.julesmeister.deutschcraft.feature.settings.presentation.theme.SettingsDesignTokens as Tokens

@Composable
internal fun SettingsAccountSection(
    session: AuthSession,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    SettingsSection(title = "Account") {
        when (session) {
            is AuthSession.SignedIn -> {
                SettingsRowLabel(
                    icon = Icons.Outlined.AccountCircle,
                    title = session.displayName?.takeIf { it.isNotBlank() } ?: session.email,
                    description =
                        if (session.displayName.isNullOrBlank()) {
                            session.provider.label()
                        } else {
                            "${session.email} · ${session.provider.label()}"
                        },
                    isActive = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Tokens.RowPadding)
                )

                SettingsDivider()

                SettingsActionRow(
                    icon = Icons.AutoMirrored.Outlined.Logout,
                    title = "Sign out",
                    description = "Your conversations stay on this device",
                    onClick = onSignOutClick
                )
            }

            AuthSession.Guest,
            AuthSession.Loading -> {
                SettingsActionRow(
                    icon = Icons.AutoMirrored.Outlined.Login,
                    title = "Sign in",
                    description =
                        "Guests get ${GuestMessagePolicy.MessageLimitPerConversation} " +
                            "messages per conversation",
                    onClick = onSignInClick,
                    enabled = session is AuthSession.Guest
                )
            }
        }
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .padding(Tokens.RowPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsRowLabel(
            icon = icon,
            title = title,
            description = description,
            isActive = false,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(Tokens.SwitchSpacing))

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun AuthProvider.label(): String =
    when (this) {
        AuthProvider.EMAIL -> "Signed in with email"
        AuthProvider.GOOGLE -> "Signed in with Google"
    }
