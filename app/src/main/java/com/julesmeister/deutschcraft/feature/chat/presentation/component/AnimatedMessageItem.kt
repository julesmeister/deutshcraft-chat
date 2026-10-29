package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun AnimatedMessageItem(
    message: ChatMessage,
    correction: String?,
    isLatestUserMessage: Boolean,
    isLatestAssistantMessage: Boolean,
    hasAlreadyBeenSeen: Boolean,

    /*
     * True when any content is visually attached beneath the
     * source message.
     */
    hasAttachedPhrase: Boolean,

    onAnimationStarted: () -> Unit,
    onClick: () -> Unit,
    onMessageCopied: () -> Unit,

    /*
     * Reports the exact rendered source-bubble width in pixels.
     */
    onBubbleWidthChanged: (Int) -> Unit,

    modifier: Modifier = Modifier
) {
    var isVisible by
    remember(
        message.id
    ) {
        mutableStateOf(
            hasAlreadyBeenSeen
        )
    }

    LaunchedEffect(
        message.id,
        hasAlreadyBeenSeen
    ) {
        if (hasAlreadyBeenSeen) {
            isVisible =
                true

            return@LaunchedEffect
        }

        delay(
            MESSAGE_ENTRANCE_START_DELAY_MILLIS
                .milliseconds
        )

        onAnimationStarted()

        isVisible =
            true
    }

    AnimatedVisibility(
        visible =
            isVisible,
        modifier =
            modifier.fillMaxWidth(),
        enter =
            messageEnterTransition(),
        exit =
            messageExitTransition()
    ) {
        MessageBubble(
            message =
                message,
            correction =
                correction,
            isLatestUserMessage =
                isLatestUserMessage,
            isLatestAssistantMessage =
                isLatestAssistantMessage,
            hasAttachedPhrase =
                hasAttachedPhrase,
            onClick =
                onClick,
            onMessageCopied =
                onMessageCopied,
            onBubbleWidthChanged =
                onBubbleWidthChanged
        )
    }
}

private fun messageEnterTransition() =
    fadeIn(
        animationSpec =
            tween(
                durationMillis =
                    MESSAGE_ENTER_DURATION_MILLIS,
                easing =
                    LinearOutSlowInEasing
            )
    ) +
            slideInVertically(
                initialOffsetY = { fullHeight ->
                    (
                            fullHeight *
                                    MESSAGE_VERTICAL_OFFSET_FRACTION
                            ).toInt()
                },
                animationSpec =
                    tween(
                        durationMillis =
                            MESSAGE_ENTER_DURATION_MILLIS,
                        easing =
                            FastOutSlowInEasing
                    )
            ) +
            scaleIn(
                initialScale =
                    MESSAGE_ENTER_SCALE,
                animationSpec =
                    tween(
                        durationMillis =
                            MESSAGE_ENTER_DURATION_MILLIS,
                        easing =
                            FastOutSlowInEasing
                    )
            )

private fun messageExitTransition() =
    fadeOut(
        animationSpec =
            tween(
                durationMillis =
                    MESSAGE_EXIT_DURATION_MILLIS
            )
    ) +
            slideOutVertically(
                targetOffsetY = { fullHeight ->
                    (
                            fullHeight *
                                    MESSAGE_VERTICAL_OFFSET_FRACTION
                            ).toInt()
                },
                animationSpec =
                    tween(
                        durationMillis =
                            MESSAGE_EXIT_DURATION_MILLIS,
                        easing =
                            FastOutSlowInEasing
                    )
            ) +
            scaleOut(
                targetScale =
                    MESSAGE_EXIT_SCALE,
                animationSpec =
                    tween(
                        durationMillis =
                            MESSAGE_EXIT_DURATION_MILLIS
                    )
            )