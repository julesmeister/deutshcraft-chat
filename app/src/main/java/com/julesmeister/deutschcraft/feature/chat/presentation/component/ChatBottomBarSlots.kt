package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatDesignTokens
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatGlassColors

@Composable
internal fun ChatBottomBarSuggestionSlot(
    visible: Boolean,
    content: (@Composable () -> Unit)?
) {
    AnimatedVisibility(
        visible =
            visible &&
                    content != null,
        enter =
            ChatBottomBarTransitions
                .SuggestionEnter,
        exit =
            ChatBottomBarTransitions
                .SuggestionExit
    ) {
        Column(
            verticalArrangement =
                Arrangement.spacedBy(
                    ChatDesignTokens
                        .OfferContentSpacing
                )
        ) {
            content?.invoke()
        }
    }
}

/*
 * Besser refinement header.
 *
 * Remembers the last instruction and feedback so the header
 * keeps its text while it animates out.
 */
@Composable
internal fun ChatBottomBarRefinementSlot(
    instruction: String?,
    feedback: String?,
    isEvaluating: Boolean,
    enabled: Boolean,
    glassColors: ChatGlassColors,
    onCancel: () -> Unit
) {
    val lastContent =
        remember {
            LastRefinementHolder()
        }

    if (!instruction.isNullOrBlank()) {
        lastContent.instruction =
            instruction

        lastContent.feedback =
            feedback
    }

    AnimatedVisibility(
        visible =
            !instruction.isNullOrBlank(),
        enter =
            ChatBottomBarTransitions
                .RefinementEnter,
        exit =
            ChatBottomBarTransitions
                .RefinementExit
    ) {
        BesserRefinementHeader(
            instruction =
                lastContent.instruction,
            feedback =
                lastContent.feedback,
            isEvaluating =
                isEvaluating,
            enabled =
                enabled,
            glassColors =
                glassColors,
            onCancel =
                onCancel,
            modifier =
                Modifier.padding(
                    horizontal =
                        ChatDesignTokens
                            .RefinementHorizontalMargin
                )
        )
    }
}

private class LastRefinementHolder {

    var instruction: String =
        ""

    var feedback: String? =
        null
}