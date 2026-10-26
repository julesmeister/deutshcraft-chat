package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
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
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschOutline
import com.julesmeister.deutschcraft.ui.theme.DeutschSurface
import com.julesmeister.deutschcraft.ui.theme.DeutschTextDisabled
import com.julesmeister.deutschcraft.ui.theme.DeutschTextPrimary

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
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val isFocused by
    interactionSource
        .collectIsFocusedAsState()

    val canSend =
        value.text.isNotBlank() &&
                enabled &&
                !isSending

    val composerShape =
        RoundedCornerShape(
            COMPOSER_CORNER_RADIUS
        )

    LaunchedEffect(isFocused) {
        if (isFocused) {
            onInputFocused()
        }
    }

    Row(
        modifier = modifier
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
                color =
                    DeutschSurface
            )
            .border(
                width =
                    COMPOSER_BORDER_WIDTH,
                color =
                    if (isFocused) {
                        DeutschLime.copy(
                            alpha =
                                FOCUSED_BORDER_ALPHA
                        )
                    } else {
                        DeutschOutline
                    },
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
                start = 18.dp,
                top = 7.dp,
                end = 7.dp,
                bottom = 7.dp
            ),
        verticalAlignment =
            Alignment.Bottom
    ) {
        BasicTextField(
            value =
                value,
            onValueChange =
                onValueChange,
            modifier = Modifier
                .weight(1f)
                .sizeIn(
                    minHeight =
                        TEXT_FIELD_MINIMUM_HEIGHT,
                    maxHeight =
                        TEXT_FIELD_MAXIMUM_HEIGHT
                )
                .padding(
                    vertical = 8.dp
                ),
            enabled =
                enabled && !isSending,
            textStyle =
                MaterialTheme
                    .typography
                    .bodyLarge
                    .copy(
                        color =
                            if (enabled) {
                                DeutschTextPrimary
                            } else {
                                DeutschTextDisabled
                            }
                    ),
            cursorBrush =
                SolidColor(
                    DeutschLime
                ),
            interactionSource =
                interactionSource,
            keyboardOptions =
                KeyboardOptions(
                    capitalization =
                        KeyboardCapitalization.Sentences,
                    autoCorrectEnabled =
                        true,
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
                5,
            decorationBox = { innerTextField ->
                Box(
                    modifier =
                        Modifier,
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
                                DeutschTextDisabled
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
            onClick =
                onSendClick
        )
    }
}

private val COMPOSER_CORNER_RADIUS =
    28.dp

private val COMPOSER_ELEVATION =
    12.dp

private val COMPOSER_BORDER_WIDTH =
    1.dp

private val MINIMUM_COMPOSER_HEIGHT =
    56.dp

private val TEXT_FIELD_MINIMUM_HEIGHT =
    40.dp

private val TEXT_FIELD_MAXIMUM_HEIGHT =
    128.dp

private const val FOCUSED_BORDER_ALPHA =
    0.46f

private const val COMPOSER_SIZE_ANIMATION_MILLIS =
    240