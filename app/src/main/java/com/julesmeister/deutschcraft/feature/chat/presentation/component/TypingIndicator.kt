package com.julesmeister.deutschcraft.feature.chat.presentation.component

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.ui.theme.DeutschSurfaceHigh
import com.julesmeister.deutschcraft.ui.theme.DeutschTextSecondary

@Composable
fun TypingIndicator(
    modifier: Modifier = Modifier
) {
    val transition =
        rememberInfiniteTransition(
            label = "typing-transition"
        )

    val firstDotAlpha by
    transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    keyframes {
                        durationMillis = 900

                        0.25f at 0
                        1f at 200
                        0.25f at 400
                    },
                repeatMode =
                    RepeatMode.Restart
            ),
        label = "first-dot-alpha"
    )

    val secondDotAlpha by
    transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    keyframes {
                        durationMillis = 900

                        0.25f at 200
                        1f at 400
                        0.25f at 600
                    },
                repeatMode =
                    RepeatMode.Restart
            ),
        label = "second-dot-alpha"
    )

    val thirdDotAlpha by
    transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    keyframes {
                        durationMillis = 900

                        0.25f at 400
                        1f at 600
                        0.25f at 800
                    },
                repeatMode =
                    RepeatMode.Restart
            ),
        label = "third-dot-alpha"
    )

    Row(
        modifier = modifier
            .background(
                color = DeutschSurfaceHigh,
                shape = RoundedCornerShape(
                    topStart = 22.dp,
                    topEnd = 22.dp,
                    bottomStart = 7.dp,
                    bottomEnd = 22.dp
                )
            )
            .padding(
                horizontal = 16.dp,
                vertical = 14.dp
            ),
        horizontalArrangement =
            Arrangement.spacedBy(5.dp)
    ) {
        TypingDot(
            alpha = firstDotAlpha
        )

        TypingDot(
            alpha = secondDotAlpha
        )

        TypingDot(
            alpha = thirdDotAlpha
        )
    }
}

@Composable
private fun TypingDot(
    alpha: Float
) {
    Box(
        modifier = Modifier
            .size(7.dp)
            .alpha(alpha)
            .background(
                color = DeutschTextSecondary,
                shape = CircleShape
            )
    )
}