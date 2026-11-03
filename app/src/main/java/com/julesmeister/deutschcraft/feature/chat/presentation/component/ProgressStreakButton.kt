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
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatDesignTokens
import java.util.Locale
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

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
                ChatDesignTokens.ProgressStreak.ProgressDisplayDurationMillis.milliseconds
            )

            displayMode =
                ProgressButtonDisplay.STREAK

            delay(
                ChatDesignTokens.ProgressStreak.StreakDisplayDurationMillis.milliseconds
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
                    ChatDesignTokens.ProgressStreak.MinimumButtonSize,
                minHeight =
                    ChatDesignTokens.ProgressStreak.MinimumButtonSize
            )
            .animateContentSize(
                animationSpec =
                    tween(
                        durationMillis =
                            ChatDesignTokens.ProgressStreak.ContainerAnimationDurationMillis,
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
                    ChatDesignTokens.ProgressStreak.HorizontalPadding,
                vertical =
                    ChatDesignTokens.ProgressStreak.VerticalPadding
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
                                    ChatDesignTokens.ProgressStreak.ContentEnterDurationMillis,
                                easing =
                                    LinearOutSlowInEasing
                            )
                    ) +
                            scaleIn(
                                initialScale =
                                    ChatDesignTokens.ProgressStreak.ContentEnterScale,
                                animationSpec =
                                    tween(
                                        durationMillis =
                                            ChatDesignTokens.ProgressStreak.ContentEnterDurationMillis,
                                        easing =
                                            FastOutSlowInEasing
                                    )
                            )

                val exitTransition =
                    fadeOut(
                        animationSpec =
                            tween(
                                durationMillis =
                                    ChatDesignTokens.ProgressStreak.ContentExitDurationMillis
                            )
                    ) +
                            scaleOut(
                                targetScale =
                                    ChatDesignTokens.ProgressStreak.ContentExitScale,
                                animationSpec =
                                    tween(
                                        durationMillis =
                                            ChatDesignTokens.ProgressStreak.ContentExitDurationMillis
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
                                        ChatDesignTokens.ProgressStreak.ContainerAnimationDurationMillis,
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
                ChatDesignTokens.ProgressStreak.ProgressIconSize
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
                    ChatDesignTokens.ProgressStreak.StreakHorizontalPadding
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
                        ChatDesignTokens.ProgressStreak.StreakTextSpacing
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
