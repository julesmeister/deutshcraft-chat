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
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.threads.domain.ConversationThread
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextOnLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextPrimary
import com.julesmeister.deutschcraft.ui.theme.DeutschTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
                        SWIPE_THRESHOLD_FRACTION
            },
            confirmValueChange = { newValue ->
                if (
                    newValue ==
                    SwipeToDismissBoxValue.EndToStart
                ) {
                    onActionsRequested()
                }

                /*
                 * Returning false prevents the row
                 * from remaining dismissed. The swipe
                 * only opens the custom action dialog.
                 */
                false
            }
        )

    SwipeToDismissBox(
        modifier = modifier.fillMaxWidth(),
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            SwipeActionBackground(
                progress =
                    dismissState.progress
            )
        }
    ) {
        ThreadRowContent(
            thread = thread,
            onClick = onClick
        )
    }
}

@Composable
private fun SwipeActionBackground(
    progress: Float
) {
    val animatedScale by animateFloatAsState(
        targetValue =
            0.75f +
                    progress.coerceIn(0f, 1f) *
                    0.25f,
        animationSpec = spring(),
        label = "thread-swipe-icon-scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                DeutschLime.copy(
                    alpha =
                        0.14f +
                                progress
                                    .coerceIn(0f, 1f) *
                                0.18f
                )
            )
            .padding(end = 22.dp),
        contentAlignment =
            Alignment.CenterEnd
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                }
                .clip(CircleShape)
                .background(DeutschLime),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(22.dp),
                imageVector =
                    Icons.Outlined.MoreHoriz,
                contentDescription = null,
                tint = DeutschTextOnLime
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
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.background
            )
            .clickable(onClick = onClick)
            .padding(
                start = 16.dp,
                top = 12.dp,
                end = 16.dp,
                bottom = 12.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        ThreadLevelAvatar(
            thread = thread
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
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
                    text = formatThreadTime(
                        timestamp =
                            thread.updatedAt
                    ),
                    color = DeutschTextSecondary,
                    style =
                        MaterialTheme.typography
                            .labelMedium
                )
            }

            Text(
                modifier = Modifier.padding(
                    top = 4.dp
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
    }
}

@Composable
private fun ThreadLevelAvatar(
    thread: ConversationThread
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

private const val SWIPE_THRESHOLD_FRACTION =
    0.35f