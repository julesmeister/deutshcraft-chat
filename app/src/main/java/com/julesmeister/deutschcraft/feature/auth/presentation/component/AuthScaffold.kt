package com.julesmeister.deutschcraft.feature.auth.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import com.julesmeister.deutschcraft.feature.auth.presentation.theme.AuthDesignTokens as Tokens

/*
 * Shared frame for every auth screen: Settings-style top bar,
 * large scrolling header and a keyboard-aware column.
 */
@Composable
internal fun AuthScaffold(
    title: String,
    subtitle: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AuthTopBar(
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .imePadding()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(
                    start = Tokens.ScreenHorizontalPadding,
                    top = Tokens.ScreenTopPadding,
                    end = Tokens.ScreenHorizontalPadding,
                    bottom = Tokens.ScreenBottomPadding
                ),
            verticalArrangement = Arrangement.spacedBy(Tokens.ContentSpacing)
        ) {
            AuthHeader(
                title = title,
                subtitle = subtitle
            )

            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthTopBar(
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

@Composable
private fun AuthHeader(
    title: String,
    subtitle: String
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.padding(
            start = Tokens.HeaderStartPadding,
            bottom = Tokens.HeaderBottomPadding
        )
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = Tokens.HeaderBadgeBottomPadding)
                .size(Tokens.HeaderBadgeSize)
                .clip(Tokens.HeaderBadgeShape)
                .background(colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = Tokens.HeaderMonogram,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = colorScheme.onPrimary
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onBackground
        )

        Text(
            text = subtitle,
            modifier = Modifier.padding(top = Tokens.HeaderSubtitleTopPadding),
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant
        )
    }
}

/*
 * Rounded surfaceContainer card, same as a Settings section.
 */
@Composable
internal fun AuthFormCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(Tokens.CardShape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(Tokens.CardPadding),
        verticalArrangement = Arrangement.spacedBy(Tokens.ContentSpacing),
        content = content
    )
}
