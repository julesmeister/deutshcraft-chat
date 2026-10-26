package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschSurfaceHighest
import com.julesmeister.deutschcraft.ui.theme.DeutschTextDisabled
import com.julesmeister.deutschcraft.ui.theme.DeutschTextOnLime

@Composable
internal fun ChatSendButton(
    enabled: Boolean,
    isSending: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val isPressed by
    interactionSource
        .collectIsPressedAsState()

    val buttonColor by
    animateColorAsState(
        targetValue =
            if (enabled) {
                DeutschLime
            } else {
                DeutschSurfaceHighest
            },
        animationSpec =
            tween(
                durationMillis =
                    COLOR_ANIMATION_DURATION_MILLIS,
                easing =
                    FastOutSlowInEasing
            ),
        label =
            "ChatSendButtonColor"
    )

    val iconColor by
    animateColorAsState(
        targetValue =
            if (enabled) {
                DeutschTextOnLime
            } else {
                DeutschTextDisabled
            },
        animationSpec =
            tween(
                durationMillis =
                    COLOR_ANIMATION_DURATION_MILLIS,
                easing =
                    FastOutSlowInEasing
            ),
        label =
            "ChatSendIconColor"
    )

    val scale by
    animateFloatAsState(
        targetValue =
            if (isPressed && enabled) {
                PRESSED_BUTTON_SCALE
            } else {
                NORMAL_BUTTON_SCALE
            },
        animationSpec =
            spring(
                dampingRatio =
                    PRESS_DAMPING_RATIO,
                stiffness =
                    PRESS_STIFFNESS
            ),
        label =
            "ChatSendButtonScale"
    )

    Box(
        modifier = modifier
            .size(
                SEND_BUTTON_SIZE
            )
            .graphicsLayer {
                scaleX =
                    scale

                scaleY =
                    scale
            }
            .background(
                color =
                    buttonColor,
                shape =
                    CircleShape
            )
            .clickable(
                interactionSource =
                    interactionSource,
                indication =
                    null,
                enabled =
                    enabled,
                onClick =
                    onClick
            ),
        contentAlignment =
            Alignment.Center
    ) {
        if (isSending) {
            CircularProgressIndicator(
                modifier =
                    Modifier.size(
                        SEND_PROGRESS_SIZE
                    ),
                color =
                    DeutschTextDisabled,
                strokeWidth =
                    SEND_PROGRESS_STROKE
            )
        } else {
            Icon(
                imageVector =
                    Icons.AutoMirrored
                        .Filled
                        .Send,
                contentDescription =
                    "Send message",
                modifier =
                    Modifier.size(
                        SEND_ICON_SIZE
                    ),
                tint =
                    iconColor
            )
        }
    }
}

private val SEND_BUTTON_SIZE =
    42.dp

private val SEND_ICON_SIZE =
    20.dp

private val SEND_PROGRESS_SIZE =
    19.dp

private val SEND_PROGRESS_STROKE =
    2.dp

private const val NORMAL_BUTTON_SCALE =
    1f

private const val PRESSED_BUTTON_SCALE =
    0.88f

private const val PRESS_DAMPING_RATIO =
    0.7f

private const val PRESS_STIFFNESS =
    650f

private const val COLOR_ANIMATION_DURATION_MILLIS =
    220