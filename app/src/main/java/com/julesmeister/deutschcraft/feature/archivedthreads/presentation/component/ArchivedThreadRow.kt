package com.julesmeister.deutschcraft.feature.archivedthreads.presentation.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.archivedthreads.domain.ArchivedThread
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ArchivedThreadRow(
    thread: ArchivedThread,
    isProcessing: Boolean,
    onClick: () -> Unit,
    onSwipeActions: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dragOffset by remember(thread.id) {
        mutableFloatStateOf(0f)
    }

    val animatedOffset by animateFloatAsState(
        targetValue = dragOffset,
        label = "ArchivedThreadSwipeOffset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(
                thread.id,
                isProcessing
            ) {
                if (!isProcessing) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()

                            dragOffset = (
                                    dragOffset + dragAmount
                                    ).coerceIn(
                                    minimumValue = -MAX_DRAG_DISTANCE,
                                    maximumValue = MAX_DRAG_DISTANCE
                                )
                        },
                        onDragEnd = {
                            val swipedFarEnough =
                                kotlin.math.abs(dragOffset) >=
                                        SWIPE_TRIGGER_DISTANCE

                            dragOffset = 0f

                            if (swipedFarEnough) {
                                onSwipeActions()
                            }
                        },
                        onDragCancel = {
                            dragOffset = 0f
                        }
                    )
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset {
                    IntOffset(
                        x = animatedOffset.roundToInt(),
                        y = 0
                    )
                }
                .background(
                    color = MaterialTheme.colorScheme.background
                )
                .clickable(
                    enabled = !isProcessing,
                    onClick = onClick
                )
                .padding(
                    horizontal = 20.dp,
                    vertical = 14.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NiveauIndicator(
                levelName = thread.cefrLevel.name
            )

            Spacer(
                modifier = Modifier.width(14.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Text(
                        text = thread.title,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style =
                            MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color =
                            MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(
                        modifier = Modifier.width(10.dp)
                    )

                    Text(
                        text = formatThreadDate(
                            timestamp = thread.updatedAt
                        ),
                        style =
                            MaterialTheme.typography.labelSmall,
                        color = MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                    )
                }

                Text(
                    text = thread.previewText
                        ?.takeIf { preview ->
                            preview.isNotBlank()
                        }
                        ?: "No messages yet",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isProcessing) {
                Spacer(
                    modifier = Modifier.width(12.dp)
                )

                CircularProgressIndicator(
                    modifier = Modifier.size(21.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun NiveauIndicator(
    levelName: String,
    modifier: Modifier = Modifier
) {
    val normalizedLevel = levelName
        .trim()
        .uppercase()

    val levelLetter = normalizedLevel
        .firstOrNull()
        ?.toString()
        ?: "N"

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                DeutschLime.copy(
                    alpha = 0.14f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = normalizedLevel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = DeutschLime
            )
        }
    }
}

@Composable
private fun niveauContainerColor(
    levelLetter: String
) = when (levelLetter) {
    "A" -> MaterialTheme.colorScheme.primaryContainer

    "B" -> MaterialTheme.colorScheme.secondaryContainer

    "C" -> MaterialTheme.colorScheme.tertiaryContainer

    else -> MaterialTheme.colorScheme.surfaceContainerHigh
}

@Composable
private fun niveauContentColor(
    levelLetter: String
) = when (levelLetter) {
    "A" -> MaterialTheme.colorScheme.onPrimaryContainer

    "B" -> MaterialTheme.colorScheme.onSecondaryContainer

    "C" -> MaterialTheme.colorScheme.onTertiaryContainer

    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

private fun formatThreadDate(
    timestamp: Long
): String {
    val threadDate = Instant
        .ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    val today = LocalDate.now()
    val yesterday = today.minusDays(1)

    return when (threadDate) {
        today -> "Today"

        yesterday -> "Yesterday"

        else -> {
            val formatter = DateTimeFormatter
                .ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(Locale.getDefault())

            threadDate.format(formatter)
        }
    }
}

private const val MAX_DRAG_DISTANCE = 180f
private const val SWIPE_TRIGGER_DISTANCE = 90f