package com.julesmeister.deutschcraft.feature.archivedthreads.presentation

import com.julesmeister.deutschcraft.feature.archivedthreads.domain.ArchivedThread

data class ArchivedThreadsUiState(
    val threads: List<ArchivedThread> = emptyList(),
    val isLoading: Boolean = true,
    val processingThreadId: String? = null,
    val errorMessage: String? = null
)