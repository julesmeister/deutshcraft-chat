package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.ui.theme.DeutschSky
import com.julesmeister.deutschcraft.ui.theme.DeutschTextOnLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextOnSky

internal data class MessageBubbleColors(
    val backgroundColor: Color,
    val contentColor: Color,
    val borderColor: Color
)

@Composable
internal fun rememberMessageBubbleColors(
    isUserMessage: Boolean,
    isLatestUserMessage: Boolean,
    isLatestAssistantMessage: Boolean
): MessageBubbleColors {
    val colorScheme =
        MaterialTheme.colorScheme

    /*
     * The newest messages remain vibrant.
     *
     * Older messages use semantic containers so they adapt to
     * both DeutschCraft themes.
     */
    val targetBackground =
        when {
            isUserMessage &&
                    isLatestUserMessage -> {
                colorScheme.primary
            }

            isUserMessage -> {
                colorScheme.primaryContainer
            }

            isLatestAssistantMessage -> {
                DeutschSky
            }

            else -> {
                colorScheme.secondaryContainer
            }
        }

    val targetContent =
        when {
            isUserMessage &&
                    isLatestUserMessage -> {
                DeutschTextOnLime
            }

            isUserMessage -> {
                colorScheme.onPrimaryContainer
            }

            isLatestAssistantMessage -> {
                DeutschTextOnSky
            }

            else -> {
                colorScheme.onSecondaryContainer
            }
        }

    val backgroundColor by
    animateColorAsState(
        targetValue =
            targetBackground,
        animationSpec =
            tween(
                durationMillis =
                    COLOR_TRANSITION_MILLIS
            ),
        label =
            "BubbleBackground"
    )

    val contentColor by
    animateColorAsState(
        targetValue =
            targetContent,
        animationSpec =
            tween(
                durationMillis =
                    COLOR_TRANSITION_MILLIS
            ),
        label =
            "BubbleContent"
    )

    return MessageBubbleColors(
        backgroundColor =
            backgroundColor,
        contentColor =
            contentColor,
        borderColor =
            Color.Transparent
    )
}

internal fun messageBubbleShape(
    isUserMessage: Boolean,
    hasAttachedPhrase: Boolean = false
): RoundedCornerShape {
    return if (isUserMessage) {
        RoundedCornerShape(
            topStart =
                SOURCE_BUBBLE_CORNER,
            topEnd =
                SOURCE_BUBBLE_CORNER,
            bottomStart =
                if (hasAttachedPhrase) {
                    ATTACHMENT_CORNER
                } else {
                    SOURCE_BUBBLE_CORNER
                },
            bottomEnd =
                SOURCE_BUBBLE_TAIL_CORNER
        )
    } else {
        RoundedCornerShape(
            topStart =
                SOURCE_BUBBLE_CORNER,
            topEnd =
                SOURCE_BUBBLE_CORNER,
            bottomStart =
                SOURCE_BUBBLE_TAIL_CORNER,
            bottomEnd =
                if (hasAttachedPhrase) {
                    ATTACHMENT_CORNER
                } else {
                    SOURCE_BUBBLE_CORNER
                }
        )
    }
}

private val SOURCE_BUBBLE_CORNER =
    20.dp

private val SOURCE_BUBBLE_TAIL_CORNER =
    6.dp

private val ATTACHMENT_CORNER =
    0.dp

private const val COLOR_TRANSITION_MILLIS =
    450