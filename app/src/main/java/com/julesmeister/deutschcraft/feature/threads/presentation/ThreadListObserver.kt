package com.julesmeister.deutschcraft.feature.threads.presentation

import com.julesmeister.deutschcraft.feature.threads.domain.ThreadRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ThreadListObserver @Inject constructor(
    private val threadRepository:
    ThreadRepository
) {

    fun observe():
            Flow<ThreadListsSnapshot> {
        return combine(
            threadRepository.observeThreads(),
            threadRepository.observeArchivedThreads()
        ) {
                activeThreads,
                archivedThreads ->

            ThreadListsSnapshot(
                activeThreads =
                    activeThreads,
                archivedThreads =
                    archivedThreads
            )
        }
    }
}