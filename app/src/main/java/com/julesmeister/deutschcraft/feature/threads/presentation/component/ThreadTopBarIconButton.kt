package com.julesmeister.deutschcraft.feature.threads.presentation.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
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
import com.julesmeister.deutschcraft.feature.threads.presentation.theme.ThreadDesignTokens as Tokens

@Composable
internal fun ThreadTopBarIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = rememberThreadColors()
    val colorScheme = MaterialTheme.colorScheme

    var isPressed by remember { mutableStateOf(false) }

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) {
            Tokens.TopBarIconPressedScale
        } else {
            Tokens.TopBarIconNormalScale
        },
        animationSpec = tween(
            durationMillis = Tokens.TopBarIconPressAnimationMillis,
            easing = FastOutSlowInEasing
        ),
        label = "ThreadTopBarIconPressScale"
    )

    val restingBackground =
        if (colors.isDark) {
            colorScheme.surfaceContainer.copy(alpha = Tokens.TopBarIconDarkRestingAlpha)
        } else {
            colorScheme.surfaceContainerHigh
        }

    val pressedBackground =
        if (colors.isDark) {
            colorScheme.surfaceContainerHigh
        } else {
            colorScheme.surfaceContainerHighest
        }

    Box(
        modifier = modifier
            .size(Tokens.TopBarIconButtonSize)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .clip(Tokens.CircleShapeToken)
            .background(if (isPressed) pressedBackground else restingBackground)
            .pointerInput(onClick) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        try {
                            tryAwaitRelease()
                        } finally {
                            isPressed = false
                        }
                    },
                    onTap = { onClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}