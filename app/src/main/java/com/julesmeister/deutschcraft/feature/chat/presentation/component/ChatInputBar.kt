package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.ui.theme.DeutschLime

@Composable
fun ChatInputBar(
    value: TextFieldValue,
    isSending: Boolean,
    enabled: Boolean,
    onValueChange: (TextFieldValue) -> Unit,
    onSendClick: () -> Unit,
    onAddClick: () -> Unit,
    onInputFocused: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val isInputFocused by
    interactionSource
        .collectIsFocusedAsState()

    val canSend =
        value.text.isNotBlank() &&
                enabled &&
                !isSending

    LaunchedEffect(isInputFocused) {
        if (isInputFocused) {
            onInputFocused()
        }
    }

    Row(
        modifier = modifier.padding(
            start = 12.dp,
            top = 8.dp,
            end = 12.dp,
            bottom = 8.dp
        ),
        verticalAlignment =
            Alignment.Bottom
    ) {
        IconButton(
            onClick =
                onAddClick,
            enabled =
                enabled && !isSending,
            modifier =
                Modifier.size(46.dp),
            shape =
                CircleShape,
            colors =
                IconButtonDefaults
                    .iconButtonColors(
                        containerColor =
                            MaterialTheme
                                .colorScheme
                                .surfaceContainer,
                        contentColor =
                            MaterialTheme
                                .colorScheme
                                .onSurface,
                        disabledContainerColor =
                            MaterialTheme
                                .colorScheme
                                .surfaceContainer
                                .copy(alpha = 0.55f),
                        disabledContentColor =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                                .copy(alpha = 0.5f)
                    )
        ) {
            Icon(
                imageVector =
                    Icons.Outlined.Add,
                contentDescription =
                    "Add attachment"
            )
        }

        OutlinedTextField(
            value =
                value,
            onValueChange =
                onValueChange,
            modifier = Modifier
                .weight(1f)
                .padding(
                    horizontal = 8.dp
                ),
            enabled =
                enabled,
            interactionSource =
                interactionSource,
            placeholder = {
                Text(
                    text =
                        "Nachricht auf Deutsch"
                )
            },
            minLines =
                1,
            maxLines =
                5,
            shape =
                RoundedCornerShape(24.dp),
            keyboardOptions =
                KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    autoCorrectEnabled = true,
                    imeAction =
                        ImeAction.Send
                ),
            keyboardActions =
                KeyboardActions(
                    onSend = {
                        if (canSend) {
                            onInputFocused()
                            onSendClick()
                        }
                    }
                ),
            colors =
                OutlinedTextFieldDefaults
                    .colors(
                        focusedBorderColor =
                            DeutschLime,
                        unfocusedBorderColor =
                            MaterialTheme
                                .colorScheme
                                .outlineVariant,
                        focusedContainerColor =
                            MaterialTheme
                                .colorScheme
                                .surfaceContainer,
                        unfocusedContainerColor =
                            MaterialTheme
                                .colorScheme
                                .surfaceContainer,
                        disabledContainerColor =
                            MaterialTheme
                                .colorScheme
                                .surfaceContainer
                                .copy(alpha = 0.6f)
                    )
        )

        IconButton(
            onClick = {
                onInputFocused()
                onSendClick()
            },
            enabled =
                canSend,
            modifier =
                Modifier.size(46.dp),
            shape =
                CircleShape,
            colors =
                IconButtonDefaults
                    .iconButtonColors(
                        containerColor =
                            DeutschLime,
                        contentColor =
                            MaterialTheme
                                .colorScheme
                                .onPrimary,
                        disabledContainerColor =
                            MaterialTheme
                                .colorScheme
                                .surfaceContainer,
                        disabledContentColor =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                                .copy(alpha = 0.45f)
                    )
        ) {
            if (isSending) {
                CircularProgressIndicator(
                    modifier =
                        Modifier.size(20.dp),
                    strokeWidth =
                        2.dp,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            } else {
                Icon(
                    imageVector =
                        Icons.AutoMirrored
                            .Filled
                            .Send,
                    contentDescription =
                        "Send message",
                    modifier =
                        Modifier.size(20.dp)
                )
            }
        }
    }
}