package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatDesignTokens
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.rememberChatGlassColors
import com.kyant.backdrop.Backdrop

/*
 * Composer entry point.
 *
 * Responsibilities are split across:
 *
 * - BesserRefinementHeader: refinement instruction + cancel
 * - ChatGlassInputField:    liquid-glass text field + send button
 * - ChatGlassColors:        light / dark glass palette
 *
 * The backdrop is the recorded conversation layer from
 * ChatScreen. The glass input field refracts it.
 */
@Composable
fun ChatComposer(
    backdrop: Backdrop,
    value: TextFieldValue,
    enabled: Boolean,
    isSending: Boolean,
    onValueChange: (TextFieldValue) -> Unit,
    onSendClick: () -> Unit,
    onInputFocused: () -> Unit = {},
    refinementInstruction: String? = null,
    onCancelRefinement: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val glassColors =
        rememberChatGlassColors()

    val isRefinementMode =
        !refinementInstruction.isNullOrBlank()

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec =
                        tween(
                            durationMillis =
                                ChatDesignTokens
                                    .RefinementAnimationMillis,
                            easing =
                                FastOutSlowInEasing
                        )
                )
    ) {
        AnimatedVisibility(
            visible =
                isRefinementMode,
            enter =
                fadeIn(
                    animationSpec =
                        tween(
                            durationMillis =
                                ChatDesignTokens
                                    .RefinementAnimationMillis
                        )
                ) +
                        slideInVertically(
                            initialOffsetY = { height ->
                                height /
                                        ChatDesignTokens
                                            .RefinementSlideOffsetDivisor
                            },
                            animationSpec =
                                tween(
                                    durationMillis =
                                        ChatDesignTokens
                                            .RefinementAnimationMillis,
                                    easing =
                                        FastOutSlowInEasing
                                )
                        ),
            exit =
                fadeOut(
                    animationSpec =
                        tween(
                            durationMillis =
                                ChatDesignTokens
                                    .RefinementAnimationMillis
                        )
                ) +
                        slideOutVertically(
                            targetOffsetY = { height ->
                                height /
                                        ChatDesignTokens
                                            .RefinementSlideOffsetDivisor
                            },
                            animationSpec =
                                tween(
                                    durationMillis =
                                        ChatDesignTokens
                                            .RefinementAnimationMillis,
                                    easing =
                                        FastOutSlowInEasing
                                )
                        )
        ) {
            BesserRefinementHeader(
                instruction =
                    refinementInstruction.orEmpty(),
                enabled =
                    enabled &&
                            !isSending,
                glassColors =
                    glassColors,
                onCancel =
                    onCancelRefinement,
                modifier =
                    Modifier.padding(
                        start =
                            ChatDesignTokens
                                .RefinementHorizontalMargin,
                        end =
                            ChatDesignTokens
                                .RefinementHorizontalMargin,
                        bottom =
                            ChatDesignTokens
                                .RefinementComposerSpacing
                    )
            )
        }

        ChatGlassInputField(
            backdrop =
                backdrop,
            value =
                value,
            enabled =
                enabled,
            isSending =
                isSending,
            isRefinementMode =
                isRefinementMode,
            glassColors =
                glassColors,
            onValueChange =
                onValueChange,
            onSendClick =
                onSendClick,
            onInputFocused =
                onInputFocused,
            modifier =
                Modifier.fillMaxWidth()
        )
    }
}