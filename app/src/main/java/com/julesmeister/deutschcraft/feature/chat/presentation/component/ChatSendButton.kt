package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextDisabled
import com.julesmeister.deutschcraft.ui.theme.DeutschTextOnLime

/*
 * Three states:
 *
 * Empty input → transparent circle, dim tilted icon
 * Ready       → lime circle, icon rotates and scales in
 * Sending     → lime circle with a dark spinner
 */
@Composable
internal fun ChatSendButton(
    enabled: Boolean,
    isSending: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource =
        remember { MutableInteractionSource() }

    val isPressed by
    interactionSource.collectIsPressedAsState()

    /*
     * Stay lime while sending so the button doesn't look
     * disabled mid-send.
     */
    val isActive =
        enabled || isSending

    val buttonColor by
    animateColorAsState(
        targetValue = if (isActive) DeutschLime else Color.Transparent,
        animationSpec = colorAnimation(),
        label = "SendButtonColor"
    )

    val iconColor by
    animateColorAsState(
        targetValue = if (isActive) DeutschTextOnLime else DeutschTextDisabled,
        animationSpec = colorAnimation(),
        label = "SendIconColor"
    )

    val buttonScale by
    animateFloatAsState(
        targetValue = if (isPressed && enabled) PRESSED_BUTTON_SCALE else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 650f),
        label = "SendButtonScale"
    )

    val iconRotation by
    animateFloatAsState(
        targetValue = if (isActive) 0f else INACTIVE_ICON_ROTATION,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "SendIconRotation"
    )

    val iconScale by
    animateFloatAsState(
        targetValue = if (isActive) 1f else INACTIVE_ICON_SCALE,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "SendIconScale"
    )

    Box(
        modifier =
            modifier
                .size(SEND_BUTTON_SIZE)
                .graphicsLayer {
                    scaleX = buttonScale
                    scaleY = buttonScale
                }
                .background(color = buttonColor, shape = CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled && !isSending,
                    onClick = onClick
                ),
        contentAlignment = Alignment.Center
    ) {
        if (isSending) {
            CircularProgressIndicator(
                modifier = Modifier.size(SEND_PROGRESS_SIZE),
                color = DeutschTextOnLime,
                strokeWidth = SEND_PROGRESS_STROKE
            )
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Nachricht senden",
                modifier =
                    Modifier
                        .size(SEND_ICON_SIZE)
                        .graphicsLayer {
                            rotationZ = iconRotation
                            scaleX = iconScale
                            scaleY = iconScale
                        },
                tint = iconColor
            )
        }
    }
}

private fun <T> colorAnimation() =
    tween<T>(
        durationMillis = COLOR_ANIMATION_DURATION_MILLIS,
        easing = FastOutSlowInEasing
    )

private val SEND_BUTTON_SIZE = 42.dp
private val SEND_ICON_SIZE = 20.dp
private val SEND_PROGRESS_SIZE = 18.dp
private val SEND_PROGRESS_STROKE = 2.dp

private const val PRESSED_BUTTON_SCALE = 0.88f
private const val INACTIVE_ICON_ROTATION = -35f
private const val INACTIVE_ICON_SCALE = 0.85f
private const val COLOR_ANIMATION_DURATION_MILLIS = 220