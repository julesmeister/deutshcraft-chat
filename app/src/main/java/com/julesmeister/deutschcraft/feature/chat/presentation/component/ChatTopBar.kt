package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    isAssistantTyping: Boolean,
    isAssessing: Boolean,
    onBackClick: () -> Unit = {},
    onProgressClick: () -> Unit,
    onNewConversationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onClearConversationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        navigationIcon = {
            CircularBackButton(
                onClick = onBackClick
            )
        },
        title = {
            Column(
                modifier = Modifier.padding(
                    start = 8.dp
                ),
                verticalArrangement =
                    Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = "DeutschCraft",
                    color = DeutschTextPrimary,
                    style =
                        MaterialTheme.typography
                            .titleMedium,
                    fontWeight =
                        FontWeight.SemiBold,
                    maxLines = 1
                )

                AssistantStatus(
                    isAssistantTyping =
                        isAssistantTyping,
                    isAssessing =
                        isAssessing
                )
            }
        },
        actions = {
            IconButton(
                modifier = Modifier
                    .padding(end = 6.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        DeutschSurfaceHigh
                    ),
                onClick = onProgressClick
            ) {
                Icon(
                    imageVector =
                        Icons.Outlined.Insights,
                    contentDescription =
                        "View German progress",
                    tint = DeutschLime
                )
            }

            ChatOverflowMenu(
                onNewConversationClick =
                    onNewConversationClick,
                onSettingsClick =
                    onSettingsClick,
                onClearConversationClick =
                    onClearConversationClick
            )
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor =
                    MaterialTheme.colorScheme
                        .background,
                titleContentColor =
                    DeutschTextPrimary,
                navigationIconContentColor =
                    DeutschTextPrimary,
                actionIconContentColor =
                    DeutschTextPrimary
            )
    )
}

@Composable
private fun DeutschCraftAvatar() {
    Box(
        modifier = Modifier
            .padding(start = 12.dp)
            .size(44.dp)
            .clip(CircleShape)
            .background(DeutschLime),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "D",
            color = DeutschTextOnLime,
            style =
                MaterialTheme.typography
                    .titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CircularBackButton(
    onClick: () -> Unit
) {
    IconButton(
        modifier = Modifier
            .padding(start = 12.dp)
            .size(42.dp)
            .clip(CircleShape)
            .background(
                color = DeutschSurfaceHigh
            ),
        onClick = onClick
    ) {
        Icon(
            imageVector =
                Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription =
                "Back to conversations",
            tint = DeutschTextPrimary
        )
    }
}