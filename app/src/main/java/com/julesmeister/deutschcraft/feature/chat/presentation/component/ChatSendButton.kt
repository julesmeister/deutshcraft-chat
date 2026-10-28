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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatDesignTokens
import com.julesmeister.deutschcraft.ui.theme.DeutschInk
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextOnLime

/*
 * Theme-aware send button colors.
 *
 * Dark mode:  lime circle, ink arrow
 * Light mode: ink circle, lime arrow
 */
@Immutable
private data class SendButtonColors(
    val activeContainer: Color,
    val activeContent: Color,
    val inactiveContent: Color
)

@Composable
private fun rememberSendButtonColors(): SendButtonColors {
    val colorScheme =
        MaterialTheme.colorScheme

    return remember(colorScheme) {
        val isDark =
            colorScheme.background.luminance() <
                    ChatDesignTokens.DarkLuminanceThreshold

        if (isDark) {
            SendButtonColors(
                activeContainer =
                    colorScheme.primary,
                activeContent =
                    DeutschTextOnLime,
                inactiveContent =
                    colorScheme.onSurface.copy(
                        alpha =
                            ChatDesignTokens.SendDisabledIconAlpha
                    )
            )
        } else {
            SendButtonColors(
                activeContainer =
                    DeutschInk,
                activeContent =
                    DeutschLime,
                inactiveContent =
                    colorScheme.onSurface.copy(
                        alpha =
                            ChatDesignTokens.SendLightDisabledIconAlpha
                    )
            )
        }
    }
}

@Composable
internal fun ChatSendButton(
    enabled: Boolean,
    isSending: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors =
        rememberSendButtonColors()

    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val isPressed by
    interactionSource.collectIsPressedAsState()

    val isActive =
        enabled ||
                isSending

    val buttonColor by
    animateColorAsState(
        targetValue =
            if (isActive) {
                colors.activeContainer
            } else {
                Color.Transparent
            },
        animationSpec =
            sendColorAnimation(),
        label =
            "SendButtonColor"
    )

    val iconColor by
    animateColorAsState(
        targetValue =
            if (isActive) {
                colors.activeContent
            } else {
                colors.inactiveContent
            },
        animationSpec =
            sendColorAnimation(),
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
                ChatDesignTokens.SendPressedScale
            } else {
                ChatDesignTokens.SendNormalScale
            },
        animationSpec =
            spring(
                dampingRatio =
                    ChatDesignTokens.SendButtonSpringDampingRatio,
                stiffness =
                    ChatDesignTokens.SendButtonSpringStiffness
            ),
        label =
            "SendButtonScale"
    )

    val iconRotation by
    animateFloatAsState(
        targetValue =
            if (isActive) {
                ChatDesignTokens.SendActiveIconRotation
            } else {
                ChatDesignTokens.SendInactiveIconRotation
            },
        animationSpec =
            spring(
                dampingRatio =
                    ChatDesignTokens.SendIconSpringDampingRatio,
                stiffness =
                    ChatDesignTokens.SendIconSpringStiffness
            ),
        label =
            "SendIconRotation"
    )

    val iconScale by
    animateFloatAsState(
        targetValue =
            if (isActive) {
                ChatDesignTokens.SendActiveIconScale
            } else {
                ChatDesignTokens.SendInactiveIconScale
            },
        animationSpec =
            spring(
                dampingRatio =
                    ChatDesignTokens.SendIconSpringDampingRatio,
                stiffness =
                    ChatDesignTokens.SendIconSpringStiffness
            ),
        label =
            "SendIconScale"
    )

    Box(
        modifier =
            modifier
                .size(
                    ChatDesignTokens.SendButtonSize
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
                        ChatDesignTokens.SendProgressSize
                    ),
                color =
                    colors.activeContent,
                strokeWidth =
                    ChatDesignTokens.SendProgressStroke
            )
        } else {
            Icon(
                imageVector =
                    Icons.AutoMirrored.Filled.Send,
                contentDescription =
                    "Nachricht senden",
                modifier =
                    Modifier
                        .size(
                            ChatDesignTokens.SendIconSize
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

private fun <T> sendColorAnimation() =
    tween<T>(
        durationMillis =
            ChatDesignTokens.SendColorAnimationMillis,
        easing =
            FastOutSlowInEasing
    )