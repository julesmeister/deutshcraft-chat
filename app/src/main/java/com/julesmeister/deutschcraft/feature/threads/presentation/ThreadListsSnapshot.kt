package com.julesmeister.deutschcraft.feature.threads.presentation

import com.julesmeister.deutschcraft.feature.threads.domain.ConversationThread

data class ThreadListsSnapshot(
    val activeThreads:
    List<ConversationThread>,
    val archivedThreads:
    List<ConversationThread>
)