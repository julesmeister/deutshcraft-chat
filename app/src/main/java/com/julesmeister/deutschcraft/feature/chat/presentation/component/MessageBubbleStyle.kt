package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.ui.theme.DeutschLime

internal data class MessageBubbleColors(
    val backgroundColor: Color,
    val contentColor: Color
)

@Composable
internal fun rememberMessageBubbleColors(
    isUserMessage: Boolean,
    isLatestUserMessage: Boolean,
    isLatestAssistantMessage: Boolean
): MessageBubbleColors {
    val regularUserColor =
        MaterialTheme
            .colorScheme
            .primaryContainer

    val regularUserContentColor =
        MaterialTheme
            .colorScheme
            .onPrimaryContainer

    val latestUserColor =
        DeutschLime

    /*
     * Deep forest green remains readable over the
     * bright DeutschLime background.
     */
    val latestUserContentColor =
        Color(0xFF17331A)

    val assistantStartColor =
        MaterialTheme
            .colorScheme
            .surfaceContainer

    val assistantPulseColor =
        MaterialTheme
            .colorScheme
            .surfaceContainerHigh

    val assistantContentColor =
        MaterialTheme
            .colorScheme
            .onSurface

    /*
     * The transition can remain active for all composed
     * bubbles, but its value is applied only to the latest
     * assistant bubble.
     */
    val infiniteTransition =
        rememberInfiniteTransition(
            label =
                "AssistantBubblePulse"
        )

    val pulseFraction by
    infiniteTransition.animateFloat(
        initialValue =
            0f,
        targetValue =
            1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis =
                            PULSE_DURATION_MILLIS,
                        easing =
                            FastOutSlowInEasing
                    ),
                repeatMode =
                    RepeatMode.Reverse
            ),
        label =
            "AssistantBubblePulseFraction"
    )

    val backgroundColor =
        when {
            isUserMessage &&
                    isLatestUserMessage -> {
                latestUserColor
            }

            isUserMessage -> {
                regularUserColor
            }

            isLatestAssistantMessage -> {
                lerp(
                    start =
                        assistantStartColor,
                    stop =
                        assistantPulseColor,
                    fraction =
                        pulseFraction *
                                PULSE_COLOR_STRENGTH
                )
            }

            else -> {
                assistantStartColor
            }
        }

    val contentColor =
        when {
            isUserMessage &&
                    isLatestUserMessage -> {
                latestUserContentColor
            }

            isUserMessage -> {
                regularUserContentColor
            }

            else -> {
                assistantContentColor
            }
        }

    return MessageBubbleColors(
        backgroundColor =
            backgroundColor,
        contentColor =
            contentColor
    )
}

internal fun messageBubbleShape(
    isUserMessage: Boolean
): RoundedCornerShape {
    return if (isUserMessage) {
        RoundedCornerShape(
            topStart = 20.dp,
            topEnd = 20.dp,
            bottomStart = 20.dp,
            bottomEnd = 6.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 20.dp,
            topEnd = 20.dp,
            bottomStart = 6.dp,
            bottomEnd = 20.dp
        )
    }
}

private const val PULSE_DURATION_MILLIS =
    2_200

private const val PULSE_COLOR_STRENGTH =
    0.45f