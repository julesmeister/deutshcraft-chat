package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.chat.domain.ReplySuggestion
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatDesignTokens
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschOutlineSoft
import com.julesmeister.deutschcraft.ui.theme.DeutschSurfaceHighest
import com.julesmeister.deutschcraft.ui.theme.DeutschTextPrimary
import kotlinx.coroutines.delay

@Composable
fun FloatingReplySuggestions(
    suggestions: List<ReplySuggestion>,
    enabled: Boolean,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val visibleSuggestions =
        remember {
            mutableStateMapOf<String, Boolean>()
        }

    LaunchedEffect(
        suggestions.map { suggestion ->
            suggestion.id
        }
    ) {
        val activeIds =
            suggestions
                .map { suggestion ->
                    suggestion.id
                }
                .toSet()

        visibleSuggestions
            .keys
            .toList()
            .filterNot { suggestionId ->
                suggestionId in activeIds
            }
            .forEach { suggestionId ->
                visibleSuggestions.remove(
                    suggestionId
                )
            }

        suggestions.forEachIndexed {
                index,
                suggestion ->

            if (
                visibleSuggestions[
                    suggestion.id
                ] != true
            ) {
                delay(
                    index *
                            ChatDesignTokens.ReplySuggestions.SuggestionStaggerMillis
                )

                visibleSuggestions[
                    suggestion.id
                ] = true
            }
        }
    }

    AnimatedVisibility(
        visible =
            suggestions.isNotEmpty(),
        modifier =
            modifier,
        enter =
            fadeIn(
                animationSpec =
                    tween(
                        durationMillis =
                            ChatDesignTokens.ReplySuggestions.ContainerEnterMillis,
                        easing =
                            LinearOutSlowInEasing
                    )
            ) +
                    slideInVertically(
                        initialOffsetY = {
                            it / 3
                        },
                        animationSpec =
                            tween(
                                durationMillis =
                                    ChatDesignTokens.ReplySuggestions.ContainerEnterMillis,
                                easing =
                                    FastOutSlowInEasing
                            )
                    ),
        exit =
            fadeOut(
                animationSpec =
                    tween(
                        durationMillis =
                            ChatDesignTokens.ReplySuggestions.ContainerExitMillis
                    )
            ) +
                    slideOutVertically(
                        targetOffsetY = {
                            it / 4
                        },
                        animationSpec =
                            tween(
                                durationMillis =
                                    ChatDesignTokens.ReplySuggestions.ContainerExitMillis
                            )
                    )
    ) {
        LazyRow(
            contentPadding =
                PaddingValues(
                    horizontal = 2.dp
                )
        ) {
            itemsIndexed(
                items =
                    suggestions,
                key = {
                        _,
                        suggestion ->

                    suggestion.id
                }
            ) {
                    _,
                    suggestion ->

                AnimatedVisibility(
                    visible =
                        visibleSuggestions[
                            suggestion.id
                        ] == true,
                    enter =
                        fadeIn(
                            animationSpec =
                                tween(
                                    durationMillis =
                                        ChatDesignTokens.ReplySuggestions.ChipEnterMillis
                                )
                        ) +
                                scaleIn(
                                    initialScale =
                                        ChatDesignTokens.ReplySuggestions.ChipEnterScale,
                                    animationSpec =
                                        tween(
                                            durationMillis =
                                                ChatDesignTokens.ReplySuggestions.ChipEnterMillis,
                                            easing =
                                                FastOutSlowInEasing
                                        )
                                ) +
                                slideInVertically(
                                    initialOffsetY = {
                                        it / 2
                                    },
                                    animationSpec =
                                        tween(
                                            durationMillis =
                                                ChatDesignTokens.ReplySuggestions.ChipEnterMillis,
                                            easing =
                                                FastOutSlowInEasing
                                        )
                                ),
                    exit =
                        fadeOut(
                            animationSpec =
                                tween(
                                    durationMillis =
                                        ChatDesignTokens.ReplySuggestions.ChipExitMillis
                                )
                        ) +
                                scaleOut(
                                    targetScale =
                                        ChatDesignTokens.ReplySuggestions.ChipExitScale
                                )
                ) {
                    FloatingSuggestionChip(
                        text =
                            suggestion.text,
                        enabled =
                            enabled,
                        onClick = {
                            onSuggestionClick(
                                suggestion.id
                            )
                        },
                        modifier =
                            Modifier.padding(
                                end = 8.dp
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingSuggestionChip(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val isPressed by
    interactionSource
        .collectIsPressedAsState()

    val scale by
    animateFloatAsState(
        targetValue =
            if (isPressed && enabled) {
                ChatDesignTokens.ReplySuggestions.PressedChipScale
            } else {
                ChatDesignTokens.ReplySuggestions.NormalChipScale
            },
        animationSpec =
            spring(
                dampingRatio =
                    ChatDesignTokens.ReplySuggestions.ChipPressDampingRatio,
                stiffness =
                    ChatDesignTokens.ReplySuggestions.ChipPressStiffness
            ),
        label =
            "SuggestionChipScale"
    )

    Text(
        text =
            text,
        modifier = modifier
            .graphicsLayer {
                scaleX =
                    scale

                scaleY =
                    scale
            }
            .sizeIn(
                minHeight =
                    ChatDesignTokens.ReplySuggestions.MinimumChipHeight,
                maxWidth =
                    ChatDesignTokens.ReplySuggestions.MaximumChipWidth
            )
            .clip(
                RoundedCornerShape(
                    percent = 50
                )
            )
            .background(
                color =
                    if (isPressed && enabled) {
                        DeutschLime
                    } else {
                        DeutschSurfaceHighest
                    }
            )
            .border(
                width =
                    1.dp,
                color =
                    if (isPressed && enabled) {
                        DeutschLime
                    } else {
                        DeutschOutlineSoft
                    },
                shape =
                    RoundedCornerShape(
                        percent = 50
                    )
            )
            .clickable(
                interactionSource =
                    interactionSource,
                indication =
                    null,
                enabled =
                    enabled,
                onClick =
                    onClick
            )
            .padding(
                horizontal = 14.dp,
                vertical = 9.dp
            ),
        color =
            if (isPressed && enabled) {
                com.julesmeister.deutschcraft
                    .ui.theme
                    .DeutschTextOnLime
            } else {
                DeutschTextPrimary
            },
        maxLines =
            1,
        overflow =
            TextOverflow.Ellipsis
    )
}
