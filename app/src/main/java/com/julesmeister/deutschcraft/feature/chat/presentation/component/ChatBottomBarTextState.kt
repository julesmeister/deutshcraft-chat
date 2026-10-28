package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/*
 * Bridges the String-based ChatBottomBar API to the
 * TextFieldValue-based ChatGlassInputField.
 *
 * Keeps cursor position and selection locally while typing.
 * When the text changes from outside (send clears it, a
 * suggestion fills it), the cursor moves to the end.
 */
internal class ChatBottomBarTextState(
    val value: TextFieldValue,
    val onValueChange: (TextFieldValue) -> Unit
)

@Composable
internal fun rememberChatBottomBarTextState(
    text: String,
    onTextChange: (String) -> Unit
): ChatBottomBarTextState {
    val currentOnTextChange by
    rememberUpdatedState(
        newValue =
            onTextChange
    )

    var localValue by
    remember {
        mutableStateOf(
            TextFieldValue(
                text =
                    text,
                selection =
                    TextRange(
                        text.length
                    )
            )
        )
    }

    val displayedValue =
        if (localValue.text == text) {
            localValue
        } else {
            TextFieldValue(
                text =
                    text,
                selection =
                    TextRange(
                        text.length
                    )
            )
        }

    return ChatBottomBarTextState(
        value =
            displayedValue,
        onValueChange = { updatedValue ->
            localValue =
                updatedValue

            if (updatedValue.text != text) {
                currentOnTextChange(
                    updatedValue.text
                )
            }
        }
    )
}