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
import androidx.compose.material3.CircularProgressIndicator
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
import com.julesmeister.deutschcraft.feature.archivedthreads.domain.ArchivedThread
import com.julesmeister.deutschcraft.feature.archivedthreads.presentation.theme.ArchivedThreadDesignTokens as Tokens
import com.julesmeister.deutschcraft.feature.threads.presentation.component.ThreadColors
import com.julesmeister.deutschcraft.feature.threads.presentation.component.rememberThreadColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun ArchivedThreadRow(
    thread: ArchivedThread,
    isProcessing: Boolean,
    onClick: () -> Unit,
    onSwipeActions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberThreadColors()

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
            .pointerInput(thread.id, isProcessing) {
                if (!isProcessing) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()

                            dragOffset = (dragOffset + dragAmount).coerceIn(
                                minimumValue = -Tokens.MaxDragDistance,
                                maximumValue = Tokens.MaxDragDistance
                            )
                        },
                        onDragEnd = {
                            val swipedFarEnough =
                                abs(dragOffset) >= Tokens.SwipeTriggerDistance

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
                .background(MaterialTheme.colorScheme.background)
                .clickable(
                    enabled = !isProcessing,
                    onClick = onClick
                )
                .padding(
                    horizontal = Tokens.RowHorizontalPadding,
                    vertical = Tokens.RowVerticalPadding
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NiveauIndicator(
                levelName = thread.cefrLevel.name,
                colors = colors
            )

            Spacer(modifier = Modifier.width(Tokens.BadgeTextSpacing))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Tokens.TextLineSpacing)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = thread.title,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )

                    Spacer(modifier = Modifier.width(Tokens.TitleDateSpacing))

                    Text(
                        text = formatThreadDate(timestamp = thread.updatedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                }

                Text(
                    text = thread.previewText
                        ?.takeIf { preview -> preview.isNotBlank() }
                        ?: "No messages yet",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }

            if (isProcessing) {
                Spacer(modifier = Modifier.width(Tokens.ProgressSpacing))

                CircularProgressIndicator(
                    modifier = Modifier.size(Tokens.ProgressSize),
                    strokeWidth = Tokens.ProgressStroke,
                    color = colors.accent
                )
            }
        }
    }
}

@Composable
private fun NiveauIndicator(
    levelName: String,
    colors: ThreadColors,
    modifier: Modifier = Modifier
) {
    val normalizedLevel =
        levelName
            .trim()
            .uppercase()
            .ifEmpty { Tokens.BadgeFallbackLevel }

    Box(
        modifier = modifier
            .size(Tokens.BadgeSize)
            .clip(Tokens.BadgeShape)
            .background(colors.accentContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = normalizedLevel,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = colors.accent
        )
    }
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