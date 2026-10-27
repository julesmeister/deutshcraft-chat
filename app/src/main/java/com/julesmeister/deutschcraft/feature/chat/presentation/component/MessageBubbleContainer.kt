package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun MessageBubbleContainer(
    shape: Shape,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color = Color.Transparent,
    maxWidth: Dp = DEFAULT_MAXIMUM_BUBBLE_WIDTH,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val isPressed by
    interactionSource.collectIsPressedAsState()

    val animatedScale by
    animateFloatAsState(
        targetValue =
            if (isPressed) PRESSED_BUBBLE_SCALE else 1f,
        animationSpec =
            spring(
                dampingRatio = 0.72f,
                stiffness = 650f
            ),
        label = "MessageBubblePressScale"
    )

    Box(
        modifier =
            modifier
                .wrapContentWidth()
                .widthIn(
                    min = MINIMUM_BUBBLE_WIDTH,
                    max = maxWidth
                )
                .graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                }
                .animateContentSize(
                    animationSpec =
                        spring(
                            dampingRatio = 0.82f,
                            stiffness = 420f
                        )
                )
                .clip(shape)
                .background(backgroundColor)
                .border(
                    width = BORDER_WIDTH,
                    color = borderColor,
                    shape = shape
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .padding(
                    start = 14.dp,
                    end = 12.dp,
                    top = 9.dp,
                    bottom = 8.dp
                ),
        content = content
    )
}

private val MINIMUM_BUBBLE_WIDTH = 64.dp
private val DEFAULT_MAXIMUM_BUBBLE_WIDTH = 320.dp
private val BORDER_WIDTH = 0.75.dp

private const val PRESSED_BUBBLE_SCALE = 0.965f