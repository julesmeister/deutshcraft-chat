package com.julesmeister.deutschcraft.feature.threads.presentation.component

import androidx.compose.runtime.Composable
import com.julesmeister.deutschcraft.feature.threads.presentation.state.ThreadListDialogsState

@Composable
fun ThreadDialogs(
    dialogsState: ThreadListDialogsState,
    onArchiveThread: (String) -> Unit,
    onDeleteThread: (String) -> Unit
) {
    dialogsState.selectedThreadForActions
        ?.let { thread ->
            ThreadActionDialog(
                thread = thread,
                onArchiveClick = {
                    onArchiveThread(thread.id)
                    dialogsState.dismissActions()
                },
                onDeleteClick = {
                    dialogsState.requestDeletion(
                        thread = thread
                    )
                },
                onDismiss = {
                    dialogsState.dismissActions()
                }
            )
        }

    dialogsState.threadPendingDeletion
        ?.let { thread ->
            DeleteThreadDialog(
                title = thread.displayTitle,
                onConfirm = {
                    onDeleteThread(thread.id)
                    dialogsState.dismissDeletion()
                },
                onDismiss = {
                    dialogsState.dismissDeletion()
                }
            )
        }
}