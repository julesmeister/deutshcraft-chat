package com.julesmeister.deutschcraft.feature.progress.presentation

import com.julesmeister.deutschcraft.feature.progress.domain.model.ProgressSummary

data class ProgressUiState(
    val progress: ProgressSummary? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)