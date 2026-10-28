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

/*
 * Mission offer / reply suggestions.
 *
 * Hidden while the learner is refining a message.
 */
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
 * Remembers the last instruction so the header keeps its
 * text while it animates out after refinement ends.
 */
@Composable
internal fun ChatBottomBarRefinementSlot(
    instruction: String?,
    enabled: Boolean,
    glassColors: ChatGlassColors,
    onCancel: () -> Unit
) {
    val lastInstruction =
        remember {
            LastInstructionHolder()
        }

    if (!instruction.isNullOrBlank()) {
        lastInstruction.value =
            instruction
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
                lastInstruction.value,
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

/*
 * Plain holder, not snapshot state: the value only needs to
 * survive the exit animation, not trigger recomposition.
 */
private class LastInstructionHolder {
    var value: String =
        ""
}