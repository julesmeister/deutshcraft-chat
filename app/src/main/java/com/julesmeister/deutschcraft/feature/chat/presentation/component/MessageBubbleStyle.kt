package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatDesignTokens
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
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
     * =======================================================
     * Bubble background
     * =======================================================
     *
     * Latest user:
     *     DeutschCraft lime
     *
     * Older user:
     *     semantic primary container
     *
     * Latest assistant:
     *     DeutschCraft sky
     *
     * Older assistant:
     *     semantic secondary container
     *
     * This keeps the newest conversation turn visually vivid
     * while older messages settle into the current theme.
     */
    val targetBackground =
        when {
            isUserMessage &&
                    isLatestUserMessage -> {
                DeutschLime
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

    /*
     * =======================================================
     * Bubble content
     * =======================================================
     */

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

    /*
     * Smoothly transition a bubble from "latest" styling into
     * its older semantic-container styling after a newer
     * message becomes the current conversation turn.
     */
    val backgroundColor by
    animateColorAsState(
        targetValue =
            targetBackground,
        animationSpec =
            tween(
                durationMillis =
                    ChatDesignTokens.BubbleStyle.ColorTransitionMillis
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
                    ChatDesignTokens.BubbleStyle.ColorTransitionMillis
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

/*
 * ===========================================================
 * Bubble geometry
 * ===========================================================
 *
 * The attachment corner is always the tail-side corner.
 *
 * User:
 *
 *     normal:
 *         bottom-right = small tail
 *
 *     attached phrase:
 *         bottom-right = completely flat
 *
 * Assistant:
 *
 *     normal:
 *         bottom-left = small tail
 *
 *     attached phrase:
 *         bottom-left = completely flat
 */
internal fun messageBubbleShape(
    isUserMessage: Boolean,
    hasAttachedPhrase: Boolean = false
): RoundedCornerShape {
    val tailSideCorner =
        if (hasAttachedPhrase) {
            ChatDesignTokens.BubbleStyle.AttachmentCorner
        } else {
            ChatDesignTokens.BubbleStyle.SourceBubbleTailCorner
        }

    return if (isUserMessage) {
        RoundedCornerShape(
            topStart =
                ChatDesignTokens.BubbleStyle.SourceBubbleCorner,
            topEnd =
                ChatDesignTokens.BubbleStyle.SourceBubbleCorner,
            bottomStart =
                ChatDesignTokens.BubbleStyle.SourceBubbleCorner,
            bottomEnd =
                tailSideCorner
        )
    } else {
        RoundedCornerShape(
            topStart =
                ChatDesignTokens.BubbleStyle.SourceBubbleCorner,
            topEnd =
                ChatDesignTokens.BubbleStyle.SourceBubbleCorner,
            bottomStart =
                tailSideCorner,
            bottomEnd =
                ChatDesignTokens.BubbleStyle.SourceBubbleCorner
        )
    }
}
