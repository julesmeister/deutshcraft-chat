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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.ui.theme.DeutschTextOnLime

/*
 * Three visual states:
 *
 * Empty input:
 * Transparent circle with a dim, tilted icon.
 *
 * Ready:
 * Theme-aware primary circle with dark icon.
 *
 * Sending:
 * Theme-aware primary circle with dark progress indicator.
 */
@Composable
internal fun ChatSendButton(
    enabled: Boolean,
    isSending: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme =
        MaterialTheme.colorScheme

    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val isPressed by
    interactionSource.collectIsPressedAsState()

    /*
     * The button stays active-looking while sending.
     */
    val isActive =
        enabled ||
                isSending

    val buttonColor by
    animateColorAsState(
        targetValue =
            if (isActive) {
                colorScheme.primary
            } else {
                Color.Transparent
            },
        animationSpec =
            colorAnimation(),
        label =
            "SendButtonColor"
    )

    val iconColor by
    animateColorAsState(
        targetValue =
            if (isActive) {
                /*
                 * Intentional fixed dark color because the
                 * icon is displayed on a lime surface.
                 */
                DeutschTextOnLime
            } else {
                colorScheme
                    .onSurface
                    .copy(
                        alpha =
                            DISABLED_ICON_ALPHA
                    )
            },
        animationSpec =
            colorAnimation(),
        label =
            "SendIconColor"
    )

    val buttonScale by
    animateFloatAsState(
        targetValue =
            if (
                isPressed &&
                enabled &&
                !isSending
            ) {
                PRESSED_BUTTON_SCALE
            } else {
                NORMAL_BUTTON_SCALE
            },
        animationSpec =
            spring(
                dampingRatio =
                    BUTTON_SPRING_DAMPING_RATIO,
                stiffness =
                    BUTTON_SPRING_STIFFNESS
            ),
        label =
            "SendButtonScale"
    )

    val iconRotation by
    animateFloatAsState(
        targetValue =
            if (isActive) {
                ACTIVE_ICON_ROTATION
            } else {
                INACTIVE_ICON_ROTATION
            },
        animationSpec =
            spring(
                dampingRatio =
                    ICON_SPRING_DAMPING_RATIO,
                stiffness =
                    ICON_SPRING_STIFFNESS
            ),
        label =
            "SendIconRotation"
    )

    val iconScale by
    animateFloatAsState(
        targetValue =
            if (isActive) {
                ACTIVE_ICON_SCALE
            } else {
                INACTIVE_ICON_SCALE
            },
        animationSpec =
            spring(
                dampingRatio =
                    ICON_SPRING_DAMPING_RATIO,
                stiffness =
                    ICON_SPRING_STIFFNESS
            ),
        label =
            "SendIconScale"
    )

    Box(
        modifier =
            modifier
                .size(
                    SEND_BUTTON_SIZE
                )
                .graphicsLayer {
                    scaleX =
                        buttonScale

                    scaleY =
                        buttonScale
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
                        enabled &&
                                !isSending,
                    role =
                        Role.Button,
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
                    DeutschTextOnLime,
                strokeWidth =
                    SEND_PROGRESS_STROKE
            )
        } else {
            Icon(
                imageVector =
                    Icons
                        .AutoMirrored
                        .Filled
                        .Send,
                contentDescription =
                    "Nachricht senden",
                modifier =
                    Modifier
                        .size(
                            SEND_ICON_SIZE
                        )
                        .graphicsLayer {
                            rotationZ =
                                iconRotation

                            scaleX =
                                iconScale

                            scaleY =
                                iconScale
                        },
                tint =
                    iconColor
            )
        }
    }
}

private fun <T> colorAnimation() =
    tween<T>(
        durationMillis =
            COLOR_ANIMATION_DURATION_MILLIS,
        easing =
            FastOutSlowInEasing
    )

private val SEND_BUTTON_SIZE =
    42.dp

private val SEND_ICON_SIZE =
    20.dp

private val SEND_PROGRESS_SIZE =
    18.dp

private val SEND_PROGRESS_STROKE =
    2.dp

private const val NORMAL_BUTTON_SCALE =
    1f

private const val PRESSED_BUTTON_SCALE =
    0.88f

private const val ACTIVE_ICON_ROTATION =
    0f

private const val INACTIVE_ICON_ROTATION =
    -35f

private const val ACTIVE_ICON_SCALE =
    1f

private const val INACTIVE_ICON_SCALE =
    0.85f

private const val DISABLED_ICON_ALPHA =
    0.38f

private const val BUTTON_SPRING_DAMPING_RATIO =
    0.7f

private const val BUTTON_SPRING_STIFFNESS =
    650f

private const val ICON_SPRING_DAMPING_RATIO =
    0.6f

private const val ICON_SPRING_STIFFNESS =
    400f

private const val COLOR_ANIMATION_DURATION_MILLIS =
    220