package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.ui.theme.DeutschSurfaceHigh
import com.julesmeister.deutschcraft.ui.theme.DeutschTextSecondary

@Composable
fun TypingIndicator(
    modifier: Modifier = Modifier
) {
    val transition =
        rememberInfiniteTransition(
            label = "TypingIndicatorTransition"
        )

    Row(
        modifier = modifier
            .background(
                color =
                    DeutschSurfaceHigh,
                shape =
                    typingIndicatorShape()
            )
            .padding(
                horizontal =
                    INDICATOR_HORIZONTAL_PADDING,
                vertical =
                    INDICATOR_VERTICAL_PADDING
            ),
        horizontalArrangement =
            Arrangement.spacedBy(
                DOT_SPACING
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        repeat(
            times = DOT_COUNT
        ) { dotIndex ->
            val animationDelay =
                dotIndex *
                        DOT_ANIMATION_DELAY_MILLIS

            val dotOffset by
            transition.animateFloat(
                initialValue =
                    RESTING_OFFSET,
                targetValue =
                    RESTING_OFFSET,
                animationSpec =
                    infiniteRepeatable(
                        animation =
                            dotOffsetKeyframes(
                                delayMillis =
                                    animationDelay
                            ),
                        repeatMode =
                            RepeatMode.Restart
                    ),
                label =
                    "TypingDotOffset$dotIndex"
            )

            val dotAlpha by
            transition.animateFloat(
                initialValue =
                    RESTING_ALPHA,
                targetValue =
                    RESTING_ALPHA,
                animationSpec =
                    infiniteRepeatable(
                        animation =
                            dotAlphaKeyframes(
                                delayMillis =
                                    animationDelay
                            ),
                        repeatMode =
                            RepeatMode.Restart
                    ),
                label =
                    "TypingDotAlpha$dotIndex"
            )

            val dotScale by
            transition.animateFloat(
                initialValue =
                    RESTING_SCALE,
                targetValue =
                    RESTING_SCALE,
                animationSpec =
                    infiniteRepeatable(
                        animation =
                            dotScaleKeyframes(
                                delayMillis =
                                    animationDelay
                            ),
                        repeatMode =
                            RepeatMode.Restart
                    ),
                label =
                    "TypingDotScale$dotIndex"
            )

            TypingDot(
                offsetY =
                    dotOffset,
                alpha =
                    dotAlpha,
                scale =
                    dotScale
            )
        }
    }
}

@Composable
private fun TypingDot(
    offsetY: Float,
    alpha: Float,
    scale: Float
) {
    Box(
        modifier = Modifier
            .size(
                DOT_SIZE
            )
            .graphicsLayer {
                translationY =
                    offsetY

                scaleX =
                    scale

                scaleY =
                    scale
            }
            .alpha(
                alpha
            )
            .background(
                color =
                    DeutschTextSecondary,
                shape =
                    CircleShape
            )
    )
}

private fun dotOffsetKeyframes(
    delayMillis: Int
) = keyframes<Float> {
    durationMillis =
        DOT_ANIMATION_CYCLE_MILLIS

    RESTING_OFFSET at 0
    RESTING_OFFSET at delayMillis

    BOUNCE_OFFSET at
            (
                    delayMillis +
                            DOT_RISE_DURATION_MILLIS
                    ) using
            FastOutSlowInEasing

    RESTING_OFFSET at
            (
                    delayMillis +
                            DOT_BOUNCE_DURATION_MILLIS
                    ) using
            FastOutSlowInEasing

    RESTING_OFFSET at
            DOT_ANIMATION_CYCLE_MILLIS
}

private fun dotAlphaKeyframes(
    delayMillis: Int
) = keyframes<Float> {
    durationMillis =
        DOT_ANIMATION_CYCLE_MILLIS

    RESTING_ALPHA at 0
    RESTING_ALPHA at delayMillis

    ACTIVE_ALPHA at
            (
                    delayMillis +
                            DOT_RISE_DURATION_MILLIS
                    ) using
            FastOutSlowInEasing

    RESTING_ALPHA at
            (
                    delayMillis +
                            DOT_BOUNCE_DURATION_MILLIS
                    ) using
            FastOutSlowInEasing

    RESTING_ALPHA at
            DOT_ANIMATION_CYCLE_MILLIS
}

private fun dotScaleKeyframes(
    delayMillis: Int
) = keyframes<Float> {
    durationMillis =
        DOT_ANIMATION_CYCLE_MILLIS

    RESTING_SCALE at 0
    RESTING_SCALE at delayMillis

    ACTIVE_SCALE at
            (
                    delayMillis +
                            DOT_RISE_DURATION_MILLIS
                    ) using
            FastOutSlowInEasing

    RESTING_SCALE at
            (
                    delayMillis +
                            DOT_BOUNCE_DURATION_MILLIS
                    ) using
            FastOutSlowInEasing

    RESTING_SCALE at
            DOT_ANIMATION_CYCLE_MILLIS
}

private fun typingIndicatorShape():
        RoundedCornerShape {
    return RoundedCornerShape(
        topStart =
            22.dp,
        topEnd =
            22.dp,
        bottomStart =
            7.dp,
        bottomEnd =
            22.dp
    )
}

private val DOT_SIZE =
    7.dp

private val DOT_SPACING =
    5.dp

private val INDICATOR_HORIZONTAL_PADDING =
    16.dp

private val INDICATOR_VERTICAL_PADDING =
    14.dp

private const val DOT_COUNT =
    3

/*
 * Total time before the dot sequence repeats.
 *
 * The third dot finishes before reaching this value,
 * leaving a small pause between animation cycles.
 */
private const val DOT_ANIMATION_CYCLE_MILLIS =
    1_200

/*
 * Each following dot starts 180 milliseconds after the
 * previous dot.
 */
private const val DOT_ANIMATION_DELAY_MILLIS =
    180

private const val DOT_RISE_DURATION_MILLIS =
    170

private const val DOT_BOUNCE_DURATION_MILLIS =
    390

private const val RESTING_OFFSET =
    0f

/*
 * Negative Y moves the dot upward.
 */
private const val BOUNCE_OFFSET =
    -7f

private const val RESTING_ALPHA =
    0.38f

private const val ACTIVE_ALPHA =
    1f

private const val RESTING_SCALE =
    0.88f

private const val ACTIVE_SCALE =
    1.08f