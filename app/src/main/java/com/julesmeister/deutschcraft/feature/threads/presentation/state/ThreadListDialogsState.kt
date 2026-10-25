package com.julesmeister.deutschcraft.feature.threads.presentation.state

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.julesmeister.deutschcraft.feature.threads.domain.ConversationThread

@Stable
class ThreadListDialogsState {

    var selectedThreadForActions:
            ConversationThread? by mutableStateOf(null)
        private set

    var threadPendingDeletion:
            ConversationThread? by mutableStateOf(null)
        private set

    fun showActions(
        thread: ConversationThread
    ) {
        selectedThreadForActions = thread
    }

    fun dismissActions() {
        selectedThreadForActions = null
    }

    fun requestDeletion(
        thread: ConversationThread
    ) {
        selectedThreadForActions = null
        threadPendingDeletion = thread
    }

    fun dismissDeletion() {
        threadPendingDeletion = null
    }
}