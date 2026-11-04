package com.julesmeister.deutschcraft.feature.auth.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.julesmeister.deutschcraft.feature.auth.presentation.theme.authAccent
import com.julesmeister.deutschcraft.feature.auth.presentation.theme.AuthDesignTokens as Tokens

/*
 * One hidden text field drawn as six cells so paste and
 * SMS autofill keep working.
 */
@Composable
internal fun AuthCodeField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    errorText: String? = null,
    onDone: () -> Unit = {}
) {
    val colorScheme =
        MaterialTheme.colorScheme

    val accent =
        authAccent

    var isFocused by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BasicTextField(
            value = value,
            onValueChange = { input ->
                onValueChange(
                    input
                        .filter(Char::isDigit)
                        .take(Tokens.CodeLength)
                )
            },
            enabled = enabled,
            singleLine = true,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done
                ),
            keyboardActions =
                KeyboardActions(
                    onDone = { onDone() }
                ),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .onFocusChanged { state ->
                    isFocused = state.isFocused
                },
            decorationBox = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Tokens.CodeCellSpacing)
                ) {
                    repeat(Tokens.CodeLength) { index ->
                        val digit =
                            value.getOrNull(index)

                        val isActive =
                            isFocused &&
                                (index == value.length ||
                                    (index == Tokens.CodeLength - 1 && value.length == Tokens.CodeLength))

                        val borderColor =
                            when {
                                errorText != null -> colorScheme.error
                                isActive -> accent
                                else -> colorScheme.outlineVariant.copy(alpha = Tokens.CodeCellRestingBorderAlpha)
                            }

                        Box(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .height(Tokens.CodeCellSize)
                                    .background(
                                        color = colorScheme.surfaceContainerHigh,
                                        shape = Tokens.CodeCellShape
                                    )
                                    .border(
                                        width =
                                            if (isActive) {
                                                Tokens.CodeCellBorderFocused
                                            } else {
                                                Tokens.CodeCellBorderResting
                                            },
                                        color = borderColor,
                                        shape = Tokens.CodeCellShape
                                    ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = digit?.toString().orEmpty(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        )

        if (errorText != null) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.error,
                modifier = Modifier.padding(top = Tokens.FieldSupportingTopPadding)
            )
        }
    }
}
