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
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatDesignTokens
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
                    ChatDesignTokens.TypingIndicator.IndicatorHorizontalPadding,
                vertical =
                    ChatDesignTokens.TypingIndicator.IndicatorVerticalPadding
            ),
        horizontalArrangement =
            Arrangement.spacedBy(
                ChatDesignTokens.TypingIndicator.DotSpacing
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        repeat(
            times = ChatDesignTokens.TypingIndicator.DotCount
        ) { dotIndex ->
            val animationDelay =
                dotIndex *
                        ChatDesignTokens.TypingIndicator.DotAnimationDelayMillis

            val dotOffset by
            transition.animateFloat(
                initialValue =
                    ChatDesignTokens.TypingIndicator.RestingOffset,
                targetValue =
                    ChatDesignTokens.TypingIndicator.RestingOffset,
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
                    ChatDesignTokens.TypingIndicator.RestingAlpha,
                targetValue =
                    ChatDesignTokens.TypingIndicator.RestingAlpha,
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
                    ChatDesignTokens.TypingIndicator.RestingScale,
                targetValue =
                    ChatDesignTokens.TypingIndicator.RestingScale,
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
                ChatDesignTokens.TypingIndicator.DotSize
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
        ChatDesignTokens.TypingIndicator.DotAnimationCycleMillis

    ChatDesignTokens.TypingIndicator.RestingOffset at 0
    ChatDesignTokens.TypingIndicator.RestingOffset at delayMillis

    ChatDesignTokens.TypingIndicator.BounceOffset at
            (
                    delayMillis +
                            ChatDesignTokens.TypingIndicator.DotRiseDurationMillis
                    ) using
            FastOutSlowInEasing

    ChatDesignTokens.TypingIndicator.RestingOffset at
            (
                    delayMillis +
                            ChatDesignTokens.TypingIndicator.DotBounceDurationMillis
                    ) using
            FastOutSlowInEasing

    ChatDesignTokens.TypingIndicator.RestingOffset at
            ChatDesignTokens.TypingIndicator.DotAnimationCycleMillis
}

private fun dotAlphaKeyframes(
    delayMillis: Int
) = keyframes<Float> {
    durationMillis =
        ChatDesignTokens.TypingIndicator.DotAnimationCycleMillis

    ChatDesignTokens.TypingIndicator.RestingAlpha at 0
    ChatDesignTokens.TypingIndicator.RestingAlpha at delayMillis

    ChatDesignTokens.TypingIndicator.ActiveAlpha at
            (
                    delayMillis +
                            ChatDesignTokens.TypingIndicator.DotRiseDurationMillis
                    ) using
            FastOutSlowInEasing

    ChatDesignTokens.TypingIndicator.RestingAlpha at
            (
                    delayMillis +
                            ChatDesignTokens.TypingIndicator.DotBounceDurationMillis
                    ) using
            FastOutSlowInEasing

    ChatDesignTokens.TypingIndicator.RestingAlpha at
            ChatDesignTokens.TypingIndicator.DotAnimationCycleMillis
}

private fun dotScaleKeyframes(
    delayMillis: Int
) = keyframes<Float> {
    durationMillis =
        ChatDesignTokens.TypingIndicator.DotAnimationCycleMillis

    ChatDesignTokens.TypingIndicator.RestingScale at 0
    ChatDesignTokens.TypingIndicator.RestingScale at delayMillis

    ChatDesignTokens.TypingIndicator.ActiveScale at
            (
                    delayMillis +
                            ChatDesignTokens.TypingIndicator.DotRiseDurationMillis
                    ) using
            FastOutSlowInEasing

    ChatDesignTokens.TypingIndicator.RestingScale at
            (
                    delayMillis +
                            ChatDesignTokens.TypingIndicator.DotBounceDurationMillis
                    ) using
            FastOutSlowInEasing

    ChatDesignTokens.TypingIndicator.RestingScale at
            ChatDesignTokens.TypingIndicator.DotAnimationCycleMillis
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
