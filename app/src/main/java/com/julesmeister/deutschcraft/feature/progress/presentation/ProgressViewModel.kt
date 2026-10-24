package com.julesmeister.deutschcraft.feature.progress.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julesmeister.deutschcraft.feature.progress.domain.ProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val repository: ProgressRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            ProgressUiState()
        )

    val uiState =
        _uiState.asStateFlow()

    init {
        observeProgress()
    }

    private fun observeProgress() {
        viewModelScope.launch {
            repository.observeProgress()
                .collect { progress ->

                    _uiState.update {
                        it.copy(
                            progress = progress,
                            isLoading = false
                        )
                    }
                }
        }
    }
}