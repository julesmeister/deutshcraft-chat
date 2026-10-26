package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp

@Composable
internal fun MessageBubbleContainer(
    shape: Shape,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val isPressed by
    interactionSource
        .collectIsPressedAsState()

    val animatedScale by
    animateFloatAsState(
        targetValue =
            if (isPressed) {
                PRESSED_BUBBLE_SCALE
            } else {
                NORMAL_BUBBLE_SCALE
            },
        animationSpec =
            spring(
                dampingRatio =
                    PRESS_ANIMATION_DAMPING_RATIO,
                stiffness =
                    PRESS_ANIMATION_STIFFNESS
            ),
        label =
            "MessageBubblePressScale"
    )

    Box(
        modifier = modifier
            .wrapContentWidth()
            .widthIn(
                min =
                    MINIMUM_BUBBLE_WIDTH,
                max =
                    MAXIMUM_BUBBLE_WIDTH
            )
            .graphicsLayer {
                scaleX =
                    animatedScale

                scaleY =
                    animatedScale
            }
            .animateContentSize(
                animationSpec =
                    spring(
                        dampingRatio =
                            CONTENT_ANIMATION_DAMPING_RATIO,
                        stiffness =
                            CONTENT_ANIMATION_STIFFNESS
                    )
            )
            .clip(
                shape
            )
            .background(
                color =
                    backgroundColor
            )
            .clickable(
                interactionSource =
                    interactionSource,
                indication =
                    null,
                onClick =
                    onClick
            )
            .padding(
                horizontal =
                    14.dp,
                vertical =
                    10.dp
            ),
        content =
            content
    )
}

private val MINIMUM_BUBBLE_WIDTH =
    72.dp

private val MAXIMUM_BUBBLE_WIDTH =
    320.dp

private const val NORMAL_BUBBLE_SCALE =
    1f

private const val PRESSED_BUBBLE_SCALE =
    0.965f

private const val PRESS_ANIMATION_DAMPING_RATIO =
    0.72f

private const val PRESS_ANIMATION_STIFFNESS =
    650f

private const val CONTENT_ANIMATION_DAMPING_RATIO =
    0.82f

private const val CONTENT_ANIMATION_STIFFNESS =
    420f