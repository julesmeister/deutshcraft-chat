package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.ui.theme.DeutschAssistantBubble
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschSky
import com.julesmeister.deutschcraft.ui.theme.DeutschTextOnLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextOnSky
import com.julesmeister.deutschcraft.ui.theme.DeutschTextPrimary
import com.julesmeister.deutschcraft.ui.theme.DeutschUserBubble

internal data class MessageBubbleColors(
    val backgroundColor: Color,
    val contentColor: Color,
    val borderColor: Color
)

/*
 * Chat color language:
 *
 * Emil latest     → solid DeutschSky, navy text
 * Emil older      → deep sky tint, light text
 * Learner latest  → solid DeutschLime, dark text
 * Learner older   → deep lime tint, light text
 *
 * When a newer message arrives, the previous "latest" bubble
 * fades back to its tint instead of jumping.
 */
@Composable
internal fun rememberMessageBubbleColors(
    isUserMessage: Boolean,
    isLatestUserMessage: Boolean,
    isLatestAssistantMessage: Boolean
): MessageBubbleColors {
    val targetBackground =
        when {
            isUserMessage && isLatestUserMessage -> DeutschLime
            isUserMessage -> DeutschUserBubble
            isLatestAssistantMessage -> DeutschSky
            else -> DeutschAssistantBubble
        }

    val targetContent =
        when {
            isUserMessage && isLatestUserMessage -> DeutschTextOnLime
            !isUserMessage && isLatestAssistantMessage -> DeutschTextOnSky
            else -> DeutschTextPrimary
        }

    val backgroundColor by
    animateColorAsState(
        targetValue = targetBackground,
        animationSpec = tween(COLOR_TRANSITION_MILLIS),
        label = "BubbleBackground"
    )

    val contentColor by
    animateColorAsState(
        targetValue = targetContent,
        animationSpec = tween(COLOR_TRANSITION_MILLIS),
        label = "BubbleContent"
    )

    return MessageBubbleColors(
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        /*
         * Both bubble types are filled with color now,
         * so no hairline border is needed.
         */
        borderColor = Color.Transparent
    )
}
internal fun messageBubbleShape(
    isUserMessage: Boolean,
    hasAttachedPhrase: Boolean = false
): RoundedCornerShape {
    return if (isUserMessage) {
        /*
         * USER
         *
         * Normal:
         *
         * ╭────────────╮
         * │            │
         * ╰────────────╯
         *
         * With attachment:
         *
         * ╭────────────╮
         * │            │
         * ┌────────────╯
         *
         * Only bottom-left becomes flat.
         * The normal bottom-right tail remains.
         */
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
        /*
         * ASSISTANT
         *
         * Normal:
         *
         * ╭────────────╮
         * │            │
         * ╰────────────╯
         *
         * With attachment:
         *
         * ╭────────────╮
         * │            │
         * ╰────────────┐
         *
         * Only bottom-right becomes flat.
         * The normal bottom-left tail remains.
         */
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


private const val COLOR_TRANSITION_MILLIS = 450