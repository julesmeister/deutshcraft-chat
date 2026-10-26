package com.julesmeister.deutschcraft.feature.threads.presentation.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
internal fun ThreadTopBarIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isPressed by
    remember {
        mutableStateOf(false)
    }

    val animatedScale by
    animateFloatAsState(
        targetValue =
            if (isPressed) {
                PRESSED_ICON_SCALE
            } else {
                NORMAL_ICON_SCALE
            },
        animationSpec =
            tween(
                durationMillis =
                    ICON_PRESS_ANIMATION_DURATION_MILLIS,
                easing =
                    FastOutSlowInEasing
            ),
        label =
            "ThreadTopBarIconPressScale"
    )

    Box(
        modifier = modifier
            .size(
                TOP_BAR_ICON_BUTTON_SIZE
            )
            .graphicsLayer {
                scaleX =
                    animatedScale

                scaleY =
                    animatedScale
            }
            .clip(
                CircleShape
            )
            .background(
                color =
                    if (isPressed) {
                        MaterialTheme
                            .colorScheme
                            .surfaceContainerHigh
                    } else {
                        MaterialTheme
                            .colorScheme
                            .surfaceContainer
                            .copy(
                                alpha =
                                    NORMAL_BUTTON_ALPHA
                            )
                    }
            )
            .pointerInput(
                onClick
            ) {
                detectTapGestures(
                    onPress = {
                        isPressed =
                            true

                        try {
                            tryAwaitRelease()
                        } finally {
                            isPressed =
                                false
                        }
                    },
                    onTap = {
                        onClick()
                    }
                )
            },
        contentAlignment =
            Alignment.Center
    ) {
        content()
    }
}

private val TOP_BAR_ICON_BUTTON_SIZE =
    40.dp

private const val NORMAL_ICON_SCALE =
    1f

private const val PRESSED_ICON_SCALE =
    0.9f

private const val NORMAL_BUTTON_ALPHA =
    0.68f

private const val ICON_PRESS_ANIMATION_DURATION_MILLIS =
    130