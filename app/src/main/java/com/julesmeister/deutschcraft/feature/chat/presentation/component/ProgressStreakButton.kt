package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun ProgressStreakButton(
    streakDays: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var displayMode by remember {
        mutableStateOf(
            ProgressButtonDisplay.PROGRESS
        )
    }

    /*
     * Alternate between the existing progress icon and
     * the learner's current streak.
     *
     * For a zero streak, keep the progress icon visible
     * instead of displaying "🔥 0".
     */
    LaunchedEffect(streakDays) {
        if (streakDays <= 0) {
            displayMode =
                ProgressButtonDisplay.PROGRESS

            return@LaunchedEffect
        }

        while (true) {
            delay(PROGRESS_DISPLAY_DURATION_MILLIS)

            displayMode =
                ProgressButtonDisplay.STREAK

            delay(STREAK_DISPLAY_DURATION_MILLIS)

            displayMode =
                ProgressButtonDisplay.PROGRESS
        }
    }

    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val containerShape =
        RoundedCornerShape(
            percent = 50
        )

    Box(
        modifier = modifier
            .defaultMinSize(
                minWidth = MINIMUM_BUTTON_SIZE,
                minHeight = MINIMUM_BUTTON_SIZE
            )
            .animateContentSize(
                animationSpec =
                    tween(
                        durationMillis =
                            SIZE_ANIMATION_DURATION_MILLIS,
                        easing =
                            FastOutSlowInEasing
                    )
            )
            .clip(containerShape)
            .background(
                color =
                    MaterialTheme
                        .colorScheme
                        .surfaceContainerHigh
            )
            .clickable(
                interactionSource =
                    interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .padding(
                horizontal =
                    HORIZONTAL_CONTENT_PADDING,
                vertical =
                    VERTICAL_CONTENT_PADDING
            ),
        contentAlignment =
            Alignment.Center
    ) {
        AnimatedContent(
            targetState =
                displayMode,
            transitionSpec = {
                fadeIn(
                    animationSpec =
                        tween(
                            durationMillis =
                                CONTENT_FADE_DURATION_MILLIS
                        )
                ) togetherWith
                        fadeOut(
                            animationSpec =
                                tween(
                                    durationMillis =
                                        CONTENT_FADE_DURATION_MILLIS
                                )
                        ) using
                        SizeTransform(
                            clip = false
                        )
            },
            label =
                "ProgressStreakContent"
        ) { mode ->
            when (mode) {
                ProgressButtonDisplay.PROGRESS -> {
                    ProgressIconContent()
                }

                ProgressButtonDisplay.STREAK -> {
                    StreakContent(
                        streakDays =
                            streakDays
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressIconContent() {
    Icon(
        imageVector =
            Icons.Outlined.Insights,
        contentDescription =
            "Open progress",
        modifier =
            Modifier.size(22.dp),
        tint =
            MaterialTheme
                .colorScheme
                .onSurfaceVariant
    )
}

@Composable
private fun StreakContent(
    streakDays: Int
) {
    Row(
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Text(
            text = streakEmoji(
                streakDays = streakDays
            ),
            style =
                MaterialTheme
                    .typography
                    .titleMedium
        )

        Text(
            text =
                formatStreak(
                    streakDays =
                        streakDays
                ),
            modifier =
                Modifier.padding(
                    start = 4.dp
                ),
            style =
                MaterialTheme
                    .typography
                    .labelLarge,
            fontWeight =
                FontWeight.Bold,
            color =
                streakTextColor(
                    streakDays =
                        streakDays
                )
        )
    }
}

@Composable
private fun streakTextColor(
    streakDays: Int
): Color {
    return when {
        streakDays >= 365 -> {
            Color(0xFF8B5CF6)
        }

        streakDays >= 100 -> {
            Color(0xFFD69E00)
        }

        streakDays >= 30 -> {
            Color(0xFFE85D04)
        }

        streakDays >= 7 -> {
            Color(0xFFF57C00)
        }

        else -> {
            MaterialTheme
                .colorScheme
                .onSurfaceVariant
        }
    }
}

private fun streakEmoji(
    streakDays: Int
): String {
    return when {
        streakDays >= 365 -> {
            "💎🔥"
        }

        else -> {
            "🔥"
        }
    }
}

private fun formatStreak(
    streakDays: Int
): String {
    return when {
        streakDays >= 1_000_000 -> {
            val millions =
                streakDays / 1_000_000f

            String.format(
                "%.1fM",
                millions
            )
        }

        streakDays >= 10_000 -> {
            val thousands =
                streakDays / 1_000f

            String.format(
                "%.1fK",
                thousands
            )
        }

        else -> {
            streakDays.toString()
        }
    }
}

private enum class ProgressButtonDisplay {
    PROGRESS,
    STREAK
}

private val MINIMUM_BUTTON_SIZE =
    40.dp

private val HORIZONTAL_CONTENT_PADDING =
    10.dp

private val VERTICAL_CONTENT_PADDING =
    8.dp

private const val PROGRESS_DISPLAY_DURATION_MILLIS =
    4_000L

private const val STREAK_DISPLAY_DURATION_MILLIS =
    3_000L

private const val SIZE_ANIMATION_DURATION_MILLIS =
    350

private const val CONTENT_FADE_DURATION_MILLIS =
    280