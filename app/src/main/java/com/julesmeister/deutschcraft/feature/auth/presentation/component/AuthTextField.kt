package com.julesmeister.deutschcraft.feature.auth.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.layout.fillMaxWidth
import com.julesmeister.deutschcraft.feature.auth.presentation.theme.authAccent
import com.julesmeister.deutschcraft.feature.auth.presentation.theme.AuthDesignTokens as Tokens

@Composable
internal fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    isPassword: Boolean = false,
    errorText: String? = null,
    supportingText: String? = null,
    enabled: Boolean = true,
    onImeAction: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme

    val accent = authAccent

    var isPasswordVisible by
    rememberSaveable {
        mutableStateOf(false)
    }

    val helperText =
        errorText ?: supportingText

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = true,
        shape = Tokens.FieldShape,
        label = {
            Text(text = label)
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(Tokens.FieldIconSize)
            )
        },
        trailingIcon =
            if (isPassword) {
                {
                    IconButton(
                        onClick = {
                            isPasswordVisible = !isPasswordVisible
                        }
                    ) {
                        Icon(
                            imageVector =
                                if (isPasswordVisible) {
                                    Icons.Outlined.VisibilityOff
                                } else {
                                    Icons.Outlined.Visibility
                                },
                            contentDescription =
                                if (isPasswordVisible) {
                                    "Hide password"
                                } else {
                                    "Show password"
                                },
                            modifier = Modifier.size(Tokens.FieldIconSize)
                        )
                    }
                }
            } else {
                null
            },
        isError = errorText != null,
        supportingText =
            helperText?.let { text ->
                {
                    Text(
                        text = text,
                        modifier = Modifier.padding(top = Tokens.FieldSupportingTopPadding)
                    )
                }
            },
        visualTransformation =
            if (isPassword && !isPasswordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction,
            capitalization = capitalization,
            autoCorrectEnabled = false
        ),
        keyboardActions = KeyboardActions(
            onAny = {
                onImeAction()
            }
        ),
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = colorScheme.surfaceContainerHigh,
            disabledContainerColor = colorScheme.surfaceContainerHigh,
            errorContainerColor = colorScheme.surfaceContainerHigh,
            focusedBorderColor = accent,
            unfocusedBorderColor = colorScheme.outlineVariant.copy(
                alpha = Tokens.FieldUnfocusedBorderAlpha
            ),
            focusedLabelColor = accent,
            focusedLeadingIconColor = accent,
            unfocusedLeadingIconColor = colorScheme.onSurfaceVariant,
            cursorColor = accent
        )
    )
}
