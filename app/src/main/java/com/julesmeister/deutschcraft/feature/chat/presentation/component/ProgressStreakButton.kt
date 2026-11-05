package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatDesignTokens
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
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
        remember {
            RoundedCornerShape(
                percent =
                    50
            )
        }

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
            /*
             * The pill's width animates only through AnimatedContent's
             * SizeTransform. Stacking animateContentSize here made the pill
             * chase an already-animating child and clip it top-start, which
             * cut off the right edge mid-transition.
             */
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
    val countStart =
        if (streakDays >= 1) {
            1f
        } else {
            0f
        }

    val fireScale =
        remember {
            Animatable(
                ChatDesignTokens.ProgressStreak.FireInitialScale
            )
        }

    val fireRotation =
        remember {
            Animatable(
                ChatDesignTokens.ProgressStreak.FireInitialRotationDegrees
            )
        }

    val count =
        remember {
            Animatable(
                countStart
            )
        }

    val countScale =
        remember {
            Animatable(
                1f
            )
        }

    /*
     * AnimatedContent recreates this content each time the
     * streak is shown, so the entrance replays every cycle.
     */
    LaunchedEffect(streakDays) {
        launch {
            fireScale.animateTo(
                targetValue =
                    1f,
                animationSpec =
                    spring(
                        dampingRatio =
                            Spring.DampingRatioMediumBouncy,
                        stiffness =
                            Spring.StiffnessLow
                    )
            )
        }

        launch {
            fireRotation.animateTo(
                targetValue =
                    0f,
                animationSpec =
                    spring(
                        dampingRatio =
                            Spring.DampingRatioLowBouncy,
                        stiffness =
                            Spring.StiffnessMediumLow
                    )
            )
        }

        launch {
            count.snapTo(
                countStart
            )

            delay(
                ChatDesignTokens.ProgressStreak.CountStartDelayMillis.milliseconds
            )

            count.animateTo(
                targetValue =
                    streakDays.toFloat(),
                animationSpec =
                    tween(
                        durationMillis =
                            ChatDesignTokens.ProgressStreak.CountDurationMillis,
                        easing =
                            FastOutSlowInEasing
                    )
            )

            countScale.animateTo(
                targetValue =
                    ChatDesignTokens.ProgressStreak.CountPulseScale,
                animationSpec =
                    tween(
                        durationMillis =
                            ChatDesignTokens.ProgressStreak.CountPulseUpDurationMillis
                    )
            )

            countScale.animateTo(
                targetValue =
                    1f,
                animationSpec =
                    spring(
                        dampingRatio =
                            Spring.DampingRatioMediumBouncy,
                        stiffness =
                            Spring.StiffnessMedium
                    )
            )
        }
    }

    // Recompose only when the shown integer changes, not every frame.
    val displayedCount by
    remember {
        derivedStateOf {
            count.value.roundToInt()
        }
    }

    val countTextStyle =
        MaterialTheme
            .typography
            .labelLarge

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
            modifier =
                Modifier.graphicsLayer {
                    scaleX =
                        fireScale.value
                    scaleY =
                        fireScale.value
                    rotationZ =
                        fireRotation.value
                    transformOrigin =
                        TransformOrigin(
                            pivotFractionX =
                                ChatDesignTokens.ProgressStreak.FirePivotX,
                            pivotFractionY =
                                ChatDesignTokens.ProgressStreak.FirePivotY
                        )
                },
            style =
                MaterialTheme
                    .typography
                    .titleMedium
        )

        Box(
            modifier =
                Modifier
                    .padding(
                        start =
                            ChatDesignTokens.ProgressStreak.StreakTextSpacing
                    )
                    .graphicsLayer {
                        scaleX =
                            countScale.value
                        scaleY =
                            countScale.value
                    },
            contentAlignment =
                Alignment.Center
        ) {
            // Reserves the final width so the pill doesn't jitter while counting.
            Text(
                text =
                    formatStreak(
                        streakDays =
                            streakDays
                    ),
                modifier =
                    Modifier.alpha(
                        0f
                    ),
                style =
                    countTextStyle,
                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text =
                    formatStreak(
                        streakDays =
                            displayedCount
                    ),
                style =
                    countTextStyle,
                fontWeight =
                    FontWeight.Bold,
                color =
                    streakColor(
                        streakDays =
                            displayedCount
                    )
            )
        }
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
