package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.chat.presentation.ChatUiAction
import com.julesmeister.deutschcraft.feature.chat.presentation.ChatUiState

/*
 * onAddClick remains available for future learning-tool or
 * attachment support even though no add button is displayed
 * in the current composer.
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun ChatBottomBar(
    uiState: ChatUiState,
    onAction: (ChatUiAction) -> Unit,
    onAddClick: () -> Unit,
    onInputFocused: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(
                start =
                    BOTTOM_BAR_HORIZONTAL_PADDING,
                end =
                    BOTTOM_BAR_HORIZONTAL_PADDING,
                top =
                    BOTTOM_BAR_TOP_PADDING,
                bottom =
                    BOTTOM_BAR_BOTTOM_PADDING
            )
    ) {
        AnimatedVisibility(
            visible =
                uiState.missionOffer != null,
            enter =
                fadeIn(
                    animationSpec =
                        tween(
                            durationMillis =
                                OFFER_ENTER_DURATION_MILLIS
                        )
                ) +
                        slideInVertically(
                            initialOffsetY = {
                                it / 3
                            },
                            animationSpec =
                                tween(
                                    durationMillis =
                                        OFFER_ENTER_DURATION_MILLIS,
                                    easing =
                                        FastOutSlowInEasing
                                )
                        ) +
                        scaleIn(
                            initialScale =
                                OFFER_ENTER_SCALE
                        ),
            exit =
                fadeOut(
                    animationSpec =
                        tween(
                            durationMillis =
                                OFFER_EXIT_DURATION_MILLIS
                        )
                ) +
                        slideOutVertically(
                            targetOffsetY = {
                                it / 4
                            },
                            animationSpec =
                                tween(
                                    durationMillis =
                                        OFFER_EXIT_DURATION_MILLIS
                                )
                        ) +
                        scaleOut(
                            targetScale =
                                OFFER_EXIT_SCALE
                        )
        ) {
            uiState.missionOffer?.let { offer ->
                MissionOfferCard(
                    opportunity =
                        offer,
                    isStarting =
                        uiState.isStartingMission,
                    onStartClick = {
                        onAction(
                            ChatUiAction
                                .StartOfferedMission
                        )
                    },
                    onDismissClick = {
                        onAction(
                            ChatUiAction
                                .DismissMissionOffer
                        )
                    }
                )
            }
        }

        if (uiState.missionOffer != null) {
            Spacer(
                modifier =
                    Modifier.height(
                        OFFER_CONTENT_SPACING
                    )
            )
        }

        FloatingReplySuggestions(
            suggestions =
                uiState.suggestions,
            enabled =
                !uiState.isInitializing &&
                        !uiState.isSending &&
                        !uiState.isStartingMission,
            onSuggestionClick = { suggestionId ->
                onAction(
                    ChatUiAction
                        .SuggestionSelected(
                            suggestionId =
                                suggestionId
                        )
                )

                onInputFocused()
            }
        )

        if (uiState.suggestions.isNotEmpty()) {
            Spacer(
                modifier =
                    Modifier.height(
                        SUGGESTION_COMPOSER_SPACING
                    )
            )
        }

        ChatComposer(
            value =
                uiState.messageInput,
            enabled =
                !uiState.isInitializing &&
                        !uiState.isStartingMission,
            isSending =
                uiState.isSending,
            onValueChange = { value ->
                onAction(
                    ChatUiAction
                        .MessageInputChanged(
                            value =
                                value
                        )
                )
            },
            onSendClick = {
                onAction(
                    ChatUiAction.SendMessage
                )
            },
            onInputFocused =
                onInputFocused,
            modifier =
                Modifier.fillMaxWidth()
        )
    }
}

private val BOTTOM_BAR_HORIZONTAL_PADDING =
    12.dp

private val BOTTOM_BAR_TOP_PADDING =
    4.dp

private val BOTTOM_BAR_BOTTOM_PADDING =
    10.dp

private val OFFER_CONTENT_SPACING =
    10.dp

private val SUGGESTION_COMPOSER_SPACING =
    8.dp

private const val OFFER_ENTER_DURATION_MILLIS =
    340

private const val OFFER_EXIT_DURATION_MILLIS =
    220

private const val OFFER_ENTER_SCALE =
    0.96f

private const val OFFER_EXIT_SCALE =
    0.98f