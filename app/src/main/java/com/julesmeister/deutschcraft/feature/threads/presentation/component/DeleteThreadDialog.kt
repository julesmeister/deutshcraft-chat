package com.julesmeister.deutschcraft.feature.threads.presentation.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.julesmeister.deutschcraft.feature.threads.presentation.theme.ThreadDesignTokens as Tokens

@Composable
fun DeleteThreadDialog(
    title: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = rememberThreadColors()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.dialogSurface,
        titleContentColor = colors.textPrimary,
        textContentColor = colors.textSecondary,
        shape = Tokens.DeleteDialogShape,
        title = {
            Text(
                text = "Unterhaltung löschen?"
            )
        },
        text = {
            Text(
                text = "\"$title\" und die zugehörigen " +
                        "Nachrichten werden dauerhaft gelöscht."
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(
                    text = "Löschen",
                    color = colors.error
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Abbrechen",
                    color = colors.textSecondary
                )
            }
        }
    )
}