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
                            SUGGESTION_STAGGER_MILLIS
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
                            CONTAINER_ENTER_MILLIS,
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
                                    CONTAINER_ENTER_MILLIS,
                                easing =
                                    FastOutSlowInEasing
                            )
                    ),
        exit =
            fadeOut(
                animationSpec =
                    tween(
                        durationMillis =
                            CONTAINER_EXIT_MILLIS
                    )
            ) +
                    slideOutVertically(
                        targetOffsetY = {
                            it / 4
                        },
                        animationSpec =
                            tween(
                                durationMillis =
                                    CONTAINER_EXIT_MILLIS
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
                                        CHIP_ENTER_MILLIS
                                )
                        ) +
                                scaleIn(
                                    initialScale =
                                        CHIP_ENTER_SCALE,
                                    animationSpec =
                                        tween(
                                            durationMillis =
                                                CHIP_ENTER_MILLIS,
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
                                                CHIP_ENTER_MILLIS,
                                            easing =
                                                FastOutSlowInEasing
                                        )
                                ),
                    exit =
                        fadeOut(
                            animationSpec =
                                tween(
                                    durationMillis =
                                        CHIP_EXIT_MILLIS
                                )
                        ) +
                                scaleOut(
                                    targetScale =
                                        CHIP_EXIT_SCALE
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
                PRESSED_CHIP_SCALE
            } else {
                NORMAL_CHIP_SCALE
            },
        animationSpec =
            spring(
                dampingRatio =
                    CHIP_PRESS_DAMPING_RATIO,
                stiffness =
                    CHIP_PRESS_STIFFNESS
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
                    MINIMUM_CHIP_HEIGHT,
                maxWidth =
                    MAXIMUM_CHIP_WIDTH
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

private val MINIMUM_CHIP_HEIGHT =
    38.dp

private val MAXIMUM_CHIP_WIDTH =
    280.dp

private const val NORMAL_CHIP_SCALE =
    1f

private const val PRESSED_CHIP_SCALE =
    0.96f

private const val CHIP_ENTER_SCALE =
    0.92f

private const val CHIP_EXIT_SCALE =
    0.96f

private const val CHIP_PRESS_DAMPING_RATIO =
    0.72f

private const val CHIP_PRESS_STIFFNESS =
    620f

private const val SUGGESTION_STAGGER_MILLIS =
    90L

private const val CONTAINER_ENTER_MILLIS =
    280

private const val CONTAINER_EXIT_MILLIS =
    180

private const val CHIP_ENTER_MILLIS =
    320

private const val CHIP_EXIT_MILLIS =
    160