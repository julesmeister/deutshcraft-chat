package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatDesignTokens
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.rememberChatGlassColors
import com.kyant.backdrop.Backdrop

@Composable
fun ChatBottomBar(
    backdrop: Backdrop,
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isSending: Boolean = false,
    suggestionContent: (@Composable () -> Unit)? = null,
    refinementInstruction: String? = null,
    refinementFeedback: String? = null,
    isEvaluatingRefinement: Boolean = false,
    onCancelRefinement: () -> Unit = {},
    onInputFocused: () -> Unit = {}
) {
    val glassColors =
        rememberChatGlassColors()

    val background =
        MaterialTheme.colorScheme.background

    val isRefinementMode =
        !refinementInstruction.isNullOrBlank()

    val textState =
        rememberChatBottomBarTextState(
            text =
                text,
            onTextChange =
                onTextChange
        )

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    brush =
                        Brush.verticalGradient(
                            colorStops =
                                arrayOf(
                                    0f to
                                            Color.Transparent,

                                    ChatDesignTokens.BottomBarFadeMidStop to
                                            background.copy(
                                                alpha =
                                                    ChatDesignTokens
                                                        .BottomBarFadeMidAlpha
                                            ),

                                    1f to
                                            background.copy(
                                                alpha =
                                                    ChatDesignTokens
                                                        .BottomBarFadeEndAlpha
                                            )
                                )
                        )
                )
                .navigationBarsPadding()
                .imePadding()
                .padding(
                    start =
                        ChatDesignTokens
                            .BottomBarHorizontalPadding,
                    end =
                        ChatDesignTokens
                            .BottomBarHorizontalPadding,
                    top =
                        ChatDesignTokens
                            .BottomBarTopPadding,
                    bottom =
                        ChatDesignTokens
                            .BottomBarBottomPadding
                )
                .animateContentSize(
                    animationSpec =
                        tween(
                            durationMillis =
                                ChatDesignTokens
                                    .LayoutAnimationMillis,
                            easing =
                                FastOutSlowInEasing
                        )
                ),
        verticalArrangement =
            Arrangement.spacedBy(
                ChatDesignTokens
                    .SuggestionComposerSpacing
            )
    ) {
        ChatBottomBarSuggestionSlot(
            visible =
                !isRefinementMode,
            content =
                suggestionContent
        )

        ChatBottomBarRefinementSlot(
            instruction =
                refinementInstruction,
            feedback =
                refinementFeedback,
            isEvaluating =
                isEvaluatingRefinement,
            enabled =
                enabled &&
                        !isSending,
            glassColors =
                glassColors,
            onCancel =
                onCancelRefinement
        )

        ChatGlassInputField(
            backdrop =
                backdrop,
            value =
                textState.value,
            enabled =
                enabled,
            isSending =
                isSending,
            isRefinementMode =
                isRefinementMode,
            glassColors =
                glassColors,
            onValueChange =
                textState.onValueChange,
            onSendClick =
                onSend,
            onInputFocused =
                onInputFocused,
            modifier =
                Modifier.fillMaxWidth()
        )
    }
}