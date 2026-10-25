package com.julesmeister.deutschcraft.feature.archivedthreads.presentation.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.julesmeister.deutschcraft.feature.archivedthreads.domain.ArchivedThread

@Composable
fun ArchivedThreadDeleteDialog(
    thread: ArchivedThread,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete conversation?",
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = buildString {
                    append("\"")
                    append(thread.title)
                    append("\" and all of its messages ")
                    append("will be permanently deleted.")
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmDelete
            ) {
                Text(
                    text = "Delete",
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Cancel"
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}