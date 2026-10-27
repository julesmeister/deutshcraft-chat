package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun ChatComposer(
    value: TextFieldValue,
    enabled: Boolean,
    isSending: Boolean,
    onValueChange: (TextFieldValue) -> Unit,
    onSendClick: () -> Unit,
    onInputFocused: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme =
        MaterialTheme.colorScheme

    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val isFocused by
    interactionSource.collectIsFocusedAsState()

    val hasText =
        value.text.isNotBlank()

    val canSend =
        hasText &&
                enabled &&
                !isSending

    val composerShape =
        RoundedCornerShape(
            COMPOSER_CORNER_RADIUS
        )

    /*
     * Border states:
     *
     * Not focused:
     * Neutral theme outline.
     *
     * Focused and empty:
     * Soft theme-aware primary.
     *
     * Focused with text:
     * Full theme-aware primary.
     */
    val targetBorderColor =
        when {
            isFocused &&
                    hasText -> {
                colorScheme.primary
            }

            isFocused -> {
                colorScheme
                    .primary
                    .copy(
                        alpha =
                            FOCUSED_EMPTY_BORDER_ALPHA
                    )
            }

            else -> {
                colorScheme.outline
            }
        }

    val borderColor by
    animateColorAsState(
        targetValue =
            targetBorderColor,
        animationSpec =
            focusAnimation(),
        label =
            "ComposerBorderColor"
    )

    val borderWidth by
    animateDpAsState(
        targetValue =
            if (isFocused) {
                FOCUSED_BORDER_WIDTH
            } else {
                UNFOCUSED_BORDER_WIDTH
            },
        animationSpec =
            focusAnimation(),
        label =
            "ComposerBorderWidth"
    )

    /*
     * The composer uses a slightly stronger surface while
     * focused.
     */
    val containerColor by
    animateColorAsState(
        targetValue =
            if (isFocused) {
                colorScheme.surfaceContainerHigh
            } else {
                colorScheme.surfaceContainer
            },
        animationSpec =
            focusAnimation(),
        label =
            "ComposerContainer"
    )

    val textColor =
        if (
            enabled &&
            !isSending
        ) {
            colorScheme.onSurface
        } else {
            colorScheme
                .onSurface
                .copy(
                    alpha =
                        DISABLED_CONTENT_ALPHA
                )
        }

    val placeholderColor =
        colorScheme
            .onSurfaceVariant
            .copy(
                alpha =
                    PLACEHOLDER_ALPHA
            )

    LaunchedEffect(
        isFocused
    ) {
        if (isFocused) {
            onInputFocused()
        }
    }

    Row(
        modifier =
            modifier
                .shadow(
                    elevation =
                        COMPOSER_ELEVATION,
                    shape =
                        composerShape,
                    clip =
                        false
                )
                .clip(
                    composerShape
                )
                .background(
                    containerColor
                )
                .border(
                    width =
                        borderWidth,
                    color =
                        borderColor,
                    shape =
                        composerShape
                )
                .animateContentSize(
                    animationSpec =
                        tween(
                            durationMillis =
                                COMPOSER_SIZE_ANIMATION_MILLIS,
                            easing =
                                FastOutSlowInEasing
                        )
                )
                .defaultMinSize(
                    minHeight =
                        MINIMUM_COMPOSER_HEIGHT
                )
                .padding(
                    start =
                        COMPOSER_START_PADDING,
                    top =
                        COMPOSER_VERTICAL_PADDING,
                    end =
                        COMPOSER_END_PADDING,
                    bottom =
                        COMPOSER_VERTICAL_PADDING
                ),
        verticalAlignment =
            Alignment.Bottom
    ) {
        BasicTextField(
            value =
                value,
            onValueChange =
                onValueChange,
            modifier =
                Modifier
                    .weight(1f)
                    .sizeIn(
                        minHeight =
                            TEXT_FIELD_MINIMUM_HEIGHT,
                        maxHeight =
                            TEXT_FIELD_MAXIMUM_HEIGHT
                    )
                    .padding(
                        vertical =
                            TEXT_FIELD_VERTICAL_PADDING
                    ),
            enabled =
                enabled &&
                        !isSending,
            textStyle =
                MaterialTheme
                    .typography
                    .bodyLarge
                    .copy(
                        color =
                            textColor
                    ),
            cursorBrush =
                SolidColor(
                    colorScheme.primary
                ),
            interactionSource =
                interactionSource,
            keyboardOptions =
                KeyboardOptions(
                    capitalization =
                        KeyboardCapitalization.Sentences,

                    /*
                     * The keyboard must not correct the
                     * learner's German before Besser evaluates
                     * the original sentence.
                     */
                    autoCorrectEnabled =
                        false,

                    imeAction =
                        ImeAction.Send
                ),
            keyboardActions =
                KeyboardActions(
                    onSend = {
                        if (canSend) {
                            onSendClick()
                        }
                    }
                ),
            minLines =
                1,
            maxLines =
                MAXIMUM_INPUT_LINES,
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment =
                        Alignment.CenterStart
                ) {
                    if (value.text.isEmpty()) {
                        Text(
                            text =
                                "Nachricht auf Deutsch",
                            style =
                                MaterialTheme
                                    .typography
                                    .bodyLarge,
                            color =
                                placeholderColor
                        )
                    }

                    innerTextField()
                }
            }
        )

        ChatSendButton(
            enabled =
                canSend,
            isSending =
                isSending,
            onClick = {
                if (canSend) {
                    onSendClick()
                }
            }
        )
    }
}

private fun <T> focusAnimation() =
    tween<T>(
        durationMillis =
            FOCUS_ANIMATION_MILLIS,
        easing =
            FastOutSlowInEasing
    )

private val COMPOSER_CORNER_RADIUS =
    28.dp

private val COMPOSER_ELEVATION =
    8.dp

private val UNFOCUSED_BORDER_WIDTH =
    1.dp

private val FOCUSED_BORDER_WIDTH =
    2.dp

private val MINIMUM_COMPOSER_HEIGHT =
    56.dp

private val TEXT_FIELD_MINIMUM_HEIGHT =
    40.dp

private val TEXT_FIELD_MAXIMUM_HEIGHT =
    128.dp

private val COMPOSER_START_PADDING =
    18.dp

private val COMPOSER_END_PADDING =
    7.dp

private val COMPOSER_VERTICAL_PADDING =
    7.dp

private val TEXT_FIELD_VERTICAL_PADDING =
    8.dp

private const val MAXIMUM_INPUT_LINES =
    5

private const val FOCUSED_EMPTY_BORDER_ALPHA =
    0.50f

private const val DISABLED_CONTENT_ALPHA =
    0.38f

private const val PLACEHOLDER_ALPHA =
    0.72f

private const val FOCUS_ANIMATION_MILLIS =
    220

private const val COMPOSER_SIZE_ANIMATION_MILLIS =
    240