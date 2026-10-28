package com.julesmeister.deutschcraft.feature.settings.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import com.julesmeister.deutschcraft.feature.settings.presentation.theme.SettingsDesignTokens as Tokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsTopBar(
    onBackClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorScheme.background,
            scrolledContainerColor = colorScheme.background,
            navigationIconContentColor = colorScheme.onBackground
        ),
        title = {},
        navigationIcon = {
            Box(
                modifier = Modifier
                    .padding(start = Tokens.TopBarStartPadding)
                    .size(Tokens.TopBarButtonSize)
                    .clip(CircleShape)
                    .background(colorScheme.surfaceContainer)
                    .clickable(
                        role = Role.Button,
                        onClick = onBackClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
    )
}

/*
 * Large in-list title, so it scrolls away with the content.
 */
@Composable
internal fun SettingsHeader() {
    Column(
        modifier = Modifier.padding(
            start = Tokens.SectionTitleStartPadding,
            bottom = Tokens.HeaderBottomPadding
        )
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Make DeutschCraft feel like yours",
            modifier = Modifier.padding(top = Tokens.HeaderSubtitleTopPadding),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}