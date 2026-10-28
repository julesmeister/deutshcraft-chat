package com.julesmeister.deutschcraft.feature.settings.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.julesmeister.deutschcraft.feature.settings.presentation.theme.SettingsDesignTokens as Tokens

@Composable
internal fun SettingsAboutCard() {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Tokens.RowPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(Tokens.AboutBadgeSize)
                .clip(Tokens.AboutBadgeShape)
                .background(colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = Tokens.AboutMonogram,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.width(Tokens.AboutBadgeSpacing))

        Column {
            Text(
                text = "DeutschCraft",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface
            )

            Text(
                text = "Conversation-first German learning",
                modifier = Modifier.padding(top = Tokens.DescriptionTopPadding),
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}