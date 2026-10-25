package com.julesmeister.deutschcraft.feature.threads.presentation.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.ui.theme.DeutschError
import com.julesmeister.deutschcraft.ui.theme.DeutschSurfaceHigh
import com.julesmeister.deutschcraft.ui.theme.DeutschTextPrimary
import com.julesmeister.deutschcraft.ui.theme.DeutschTextSecondary

@Composable
fun DeleteThreadDialog(
    title: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeutschSurfaceHigh,
        titleContentColor = DeutschTextPrimary,
        textContentColor = DeutschTextSecondary,
        shape = RoundedCornerShape(26.dp),
        title = {
            Text(
                text = "Unterhaltung löschen?"
            )
        },
        text = {
            Text(
                text =
                    "\"$title\" und die zugehörigen " +
                            "Nachrichten werden dauerhaft " +
                            "gelöscht."
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(
                    text = "Löschen",
                    color = DeutschError
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Abbrechen",
                    color = DeutschTextSecondary
                )
            }
        }
    )
}