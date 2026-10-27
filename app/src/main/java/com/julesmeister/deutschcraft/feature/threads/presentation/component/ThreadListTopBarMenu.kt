package com.julesmeister.deutschcraft.feature.threads.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddComment
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun ThreadListTopBarMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onNewConversationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded =
            expanded,
        onDismissRequest =
            onDismissRequest,
        modifier =
            modifier.width(
                MENU_WIDTH
            ),
        shape =
            MENU_SHAPE,
        containerColor =
            MaterialTheme
                .colorScheme
                .surfaceContainerHigh,
        tonalElevation =
            MENU_TONAL_ELEVATION,
        shadowElevation =
            MENU_SHADOW_ELEVATION
    ) {
        Column(
            modifier =
                Modifier.padding(
                    vertical =
                        MENU_VERTICAL_PADDING
                )
        ) {
            ThreadListTopBarMenuItem(
                title =
                    "New conversation",
                description =
                    "Start a fresh German chat",
                icon = {
                    Icon(
                        imageVector =
                            Icons
                                .Outlined
                                .AddComment,
                        contentDescription =
                            null
                    )
                },
                onClick =
                    onNewConversationClick
            )

            Spacer(
                modifier =
                    Modifier.size(
                        MENU_ITEM_SPACING
                    )
            )

            ThreadListTopBarMenuItem(
                title =
                    "Settings",
                description =
                    "Customize DeutschCraft",
                icon = {
                    Icon(
                        imageVector =
                            Icons
                                .Outlined
                                .Settings,
                        contentDescription =
                            null
                    )
                },
                onClick =
                    onSettingsClick
            )
        }
    }
}

@Composable
private fun ThreadListTopBarMenuItem(
    title: String,
    description: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenuItem(
        modifier =
            modifier
                .padding(
                    horizontal =
                        MENU_ITEM_HORIZONTAL_MARGIN
                )
                .clip(
                    MENU_ITEM_SHAPE
                ),
        text = {
            Column {
                Text(
                    text =
                        title,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurface,
                    style =
                        MaterialTheme
                            .typography
                            .bodyLarge,
                    fontWeight =
                        FontWeight.SemiBold
                )

                Text(
                    text =
                        description,
                    modifier =
                        Modifier.padding(
                            top =
                                MENU_DESCRIPTION_TOP_PADDING
                        ),
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                    style =
                        MaterialTheme
                            .typography
                            .bodySmall
                )
            }
        },
        leadingIcon = {
            ThreadMenuIconContainer(
                content =
                    icon
            )
        },
        onClick =
            onClick,
        contentPadding =
            MenuDefaults
                .DropdownMenuItemContentPadding
    )
}

@Composable
private fun ThreadMenuIconContainer(
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor =
        MaterialTheme
            .colorScheme
            .primary

    Box(
        modifier =
            modifier
                .size(
                    MENU_ICON_CONTAINER_SIZE
                )
                .clip(
                    CircleShape
                )
                .background(
                    color =
                        accentColor.copy(
                            alpha =
                                MENU_ICON_BACKGROUND_ALPHA
                        )
                ),
        contentAlignment =
            Alignment.Center
    ) {
        CompositionLocalProvider(
            LocalContentColor provides
                    accentColor
        ) {
            content()
        }
    }
}

private val MENU_SHAPE =
    RoundedCornerShape(
        size = 24.dp
    )

private val MENU_ITEM_SHAPE =
    RoundedCornerShape(
        size = 18.dp
    )

private val MENU_WIDTH =
    260.dp

private val MENU_VERTICAL_PADDING =
    8.dp

private val MENU_ITEM_HORIZONTAL_MARGIN =
    6.dp

private val MENU_ITEM_SPACING =
    2.dp

private val MENU_DESCRIPTION_TOP_PADDING =
    2.dp

private val MENU_ICON_CONTAINER_SIZE =
    38.dp

private val MENU_TONAL_ELEVATION =
    6.dp

private val MENU_SHADOW_ELEVATION =
    14.dp

private const val MENU_ICON_BACKGROUND_ALPHA =
    0.14f