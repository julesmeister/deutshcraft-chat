package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun ProgressStreakButton(
    streakDays: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val safeStreak =
        streakDays.coerceAtLeast(0)

    var displayMode by
    remember {
        mutableStateOf(
            ProgressButtonDisplay.PROGRESS
        )
    }

    /*
     * Always alternate between the progress icon and streak
     * display.
     *
     * Previously, this coroutine stopped when safeStreak
     * was zero. The thread screen initially supplies zero
     * while its real streak value is loading, which caused
     * the animation to disappear permanently.
     */
    LaunchedEffect(Unit) {
        while (true) {
            displayMode =
                ProgressButtonDisplay.PROGRESS

            delay(
                PROGRESS_DISPLAY_DURATION_MILLIS
            )

            displayMode =
                ProgressButtonDisplay.STREAK

            delay(
                STREAK_DISPLAY_DURATION_MILLIS
            )
        }
    }

    val containerShape =
        RoundedCornerShape(
            percent =
                50
        )

    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    Box(
        modifier = modifier
            .defaultMinSize(
                minWidth =
                    MINIMUM_BUTTON_SIZE,
                minHeight =
                    MINIMUM_BUTTON_SIZE
            )
            .animateContentSize(
                animationSpec =
                    tween(
                        durationMillis =
                            CONTAINER_ANIMATION_DURATION_MILLIS,
                        easing =
                            FastOutSlowInEasing
                    )
            )
            .clip(
                containerShape
            )
            .background(
                color =
                    MaterialTheme
                        .colorScheme
                        .surfaceContainerHigh
            )
            .clickable(
                interactionSource =
                    interactionSource,
                indication =
                    null,
                role =
                    Role.Button,
                onClick =
                    onClick
            )
            .padding(
                horizontal =
                    HORIZONTAL_PADDING,
                vertical =
                    VERTICAL_PADDING
            ),
        contentAlignment =
            Alignment.Center
    ) {
        AnimatedContent(
            targetState =
                displayMode,
            transitionSpec = {
                val enterTransition =
                    fadeIn(
                        animationSpec =
                            tween(
                                durationMillis =
                                    CONTENT_ENTER_DURATION_MILLIS,
                                easing =
                                    LinearOutSlowInEasing
                            )
                    ) +
                            scaleIn(
                                initialScale =
                                    CONTENT_ENTER_SCALE,
                                animationSpec =
                                    tween(
                                        durationMillis =
                                            CONTENT_ENTER_DURATION_MILLIS,
                                        easing =
                                            FastOutSlowInEasing
                                    )
                            )

                val exitTransition =
                    fadeOut(
                        animationSpec =
                            tween(
                                durationMillis =
                                    CONTENT_EXIT_DURATION_MILLIS
                            )
                    ) +
                            scaleOut(
                                targetScale =
                                    CONTENT_EXIT_SCALE,
                                animationSpec =
                                    tween(
                                        durationMillis =
                                            CONTENT_EXIT_DURATION_MILLIS
                                    )
                            )

                enterTransition
                    .togetherWith(
                        exitTransition
                    )
                    .using(
                        SizeTransform(
                            clip =
                                false,
                            sizeAnimationSpec = {
                                    _,
                                    _ ->

                                tween(
                                    durationMillis =
                                        CONTAINER_ANIMATION_DURATION_MILLIS,
                                    easing =
                                        FastOutSlowInEasing
                                )
                            }
                        )
                    )
            },
            contentAlignment =
                Alignment.Center,
            label =
                "ProgressStreakTransition"
        ) { mode ->
            when (mode) {
                ProgressButtonDisplay.PROGRESS -> {
                    ProgressIconContent()
                }

                ProgressButtonDisplay.STREAK -> {
                    StreakContent(
                        streakDays =
                            safeStreak
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
            Modifier.size(
                PROGRESS_ICON_SIZE
            ),
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
        modifier =
            Modifier.padding(
                horizontal =
                    STREAK_HORIZONTAL_PADDING
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Text(
            text =
                streakEmoji(
                    streakDays =
                        streakDays
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
                    start =
                        STREAK_TEXT_SPACING
                ),
            style =
                MaterialTheme
                    .typography
                    .labelLarge,
            fontWeight =
                FontWeight.Bold,
            color =
                streakColor(
                    streakDays =
                        streakDays
                )
        )
    }
}

@Composable
private fun streakColor(
    streakDays: Int
): Color {
    return when {
        streakDays >= 365 -> {
            Color(
                color =
                    0xFF7C3AED
            )
        }

        streakDays >= 100 -> {
            Color(
                color =
                    0xFFC58A00
            )
        }

        streakDays >= 30 -> {
            Color(
                color =
                    0xFFE85D04
            )
        }

        streakDays >= 7 -> {
            Color(
                color =
                    0xFFF57C00
            )
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
            String.format(
                Locale.getDefault(),
                "%.1fM",
                streakDays /
                        1_000_000f
            )
        }

        streakDays >= 10_000 -> {
            String.format(
                Locale.getDefault(),
                "%.1fK",
                streakDays /
                        1_000f
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

private val PROGRESS_ICON_SIZE =
    22.dp

private val HORIZONTAL_PADDING =
    10.dp

private val VERTICAL_PADDING =
    8.dp

private val STREAK_HORIZONTAL_PADDING =
    1.dp

private val STREAK_TEXT_SPACING =
    4.dp

private const val PROGRESS_DISPLAY_DURATION_MILLIS =
    4_000L

private const val STREAK_DISPLAY_DURATION_MILLIS =
    3_000L

private const val CONTAINER_ANIMATION_DURATION_MILLIS =
    380

private const val CONTENT_ENTER_DURATION_MILLIS =
    320

private const val CONTENT_EXIT_DURATION_MILLIS =
    220

private const val CONTENT_ENTER_SCALE =
    0.88f

private const val CONTENT_EXIT_SCALE =
    0.92f