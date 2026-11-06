package com.julesmeister.deutschcraft.feature.threads.presentation.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.julesmeister.deutschcraft.feature.threads.domain.ConversationThread
import com.julesmeister.deutschcraft.feature.threads.presentation.theme.ThreadDesignTokens
import com.julesmeister.deutschcraft.ui.theme.DeutschTextOnLime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadRow(
    thread: ConversationThread,
    onClick: () -> Unit,
    onActionsRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState =
        rememberSwipeToDismissBoxState(
            positionalThreshold = { totalDistance ->
                totalDistance *
                        ThreadDesignTokens.ThreadRowSwipeThresholdFraction
            },
            confirmValueChange = { newValue ->
                if (
                    newValue ==
                    SwipeToDismissBoxValue.EndToStart
                ) {
                    onActionsRequested()
                }

                /*
                 * The swipe only opens the action UI.
                 *
                 * Returning false prevents the conversation
                 * row from remaining dismissed.
                 */
                false
            }
        )

    SwipeToDismissBox(
        modifier =
            modifier.fillMaxWidth(),
        state =
            dismissState,
        enableDismissFromStartToEnd =
            false,
        enableDismissFromEndToStart =
            true,
        backgroundContent = {
            SwipeActionBackground(
                progress =
                    dismissState.progress
            )
        }
    ) {
        ThreadRowContent(
            thread =
                thread,
            onClick =
                onClick
        )
    }
}

@Composable
private fun SwipeActionBackground(
    progress: Float
) {
    val accentColor =
        MaterialTheme
            .colorScheme
            .primary

    val animatedScale by
    animateFloatAsState(
        targetValue =
            0.75f +
                    progress
                        .coerceIn(
                            0f,
                            1f
                        ) *
                    0.25f,
        animationSpec =
            spring(),
        label =
            "thread-swipe-icon-scale"
    )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    accentColor.copy(
                        alpha =
                            ThreadDesignTokens.ThreadRowSwipeBackgroundBaseAlpha +
                                    progress
                                        .coerceIn(
                                            0f,
                                            1f
                                        ) *
                                    ThreadDesignTokens.ThreadRowSwipeBackgroundProgressAlpha
                    )
                )
                .padding(
                    end =
                        ThreadDesignTokens.ThreadRowSwipeActionEndPadding
                ),
        contentAlignment =
            Alignment.CenterEnd
    ) {
        Box(
            modifier =
                Modifier
                    .size(
                        ThreadDesignTokens.ThreadRowSwipeActionSize
                    )
                    .graphicsLayer {
                        scaleX =
                            animatedScale

                        scaleY =
                            animatedScale
                    }
                    .clip(
                        CircleShape
                    )
                    .background(
                        accentColor
                    ),
            contentAlignment =
                Alignment.Center
        ) {
            Icon(
                modifier =
                    Modifier.size(
                        ThreadDesignTokens.ThreadRowSwipeActionIconSize
                    ),
                imageVector =
                    Icons
                        .Outlined
                        .MoreHoriz,
                contentDescription =
                    null,

                /*
                 * Keep this fixed because it is content
                 * intentionally displayed over the lime
                 * branding surface.
                 */
                tint =
                    DeutschTextOnLime
            )
        }
    }
}

@Composable
private fun ThreadRowContent(
    thread: ConversationThread,
    onClick: () -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme
                        .colorScheme
                        .background
                )
                .clickable(
                    onClick =
                        onClick
                )
                .padding(
                    start =
                        ThreadDesignTokens.ThreadRowHorizontalPadding,
                    top =
                        ThreadDesignTokens.ThreadRowVerticalPadding,
                    end =
                        ThreadDesignTokens.ThreadRowHorizontalPadding,
                    bottom =
                        ThreadDesignTokens.ThreadRowVerticalPadding
                ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        ThreadLevelAvatar(
            thread =
                thread
        )

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(
                        start =
                            ThreadDesignTokens.ThreadRowContentStartPadding
                    )
        ) {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    modifier =
                        Modifier.weight(1f),
                    text =
                        thread.displayTitle,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onBackground,
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium.copy(
                            lineHeight = ThreadDesignTokens.ThreadRowTextLineHeight
                        ),
                    fontWeight =
                        FontWeight.SemiBold,
                    maxLines =
                        1,
                    overflow =
                        TextOverflow.Ellipsis
                )

                Text(
                    text =
                        remember(
                            thread.updatedAt
                        ) {
                            formatThreadTime(
                                timestamp =
                                    thread.updatedAt
                            )
                        },
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                    style =
                        MaterialTheme
                            .typography
                            .labelMedium
                )
            }

            Text(
                modifier =
                    Modifier.padding(
                        top =
                            ThreadDesignTokens.ThreadRowPreviewTopPadding
                    ),
                text =
                    thread.displayPreview,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
                style =
                    MaterialTheme
                        .typography
                        .titleMedium.copy(
                            lineHeight = ThreadDesignTokens.ThreadRowTextLineHeight
                        ),
                maxLines =
                    1,
                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ThreadLevelAvatar(
    thread: ConversationThread
) {
    val accentColor =
        MaterialTheme
            .colorScheme
            .primary

    Box(
        modifier =
            Modifier
                .size(
                    ThreadDesignTokens.ThreadRowAvatarSize
                )
                .clip(
                    CircleShape
                )
                .background(
                    accentColor.copy(
                        alpha =
                            ThreadDesignTokens.ThreadRowAvatarBackgroundAlpha
                    )
                ),
        contentAlignment =
            Alignment.Center
    ) {
        Text(
            text =
                thread.cefrLevel.name,
            color =
                accentColor,
            style =
                MaterialTheme
                    .typography
                    .labelLarge,
            fontWeight =
                FontWeight.Bold
        )
    }
}

private fun formatThreadTime(
    timestamp: Long
): String {
    return SimpleDateFormat(
        "HH:mm",
        Locale.GERMAN
    ).format(
        Date(timestamp)
    )
}
