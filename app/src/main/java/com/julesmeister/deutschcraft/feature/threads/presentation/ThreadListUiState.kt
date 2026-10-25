package com.julesmeister.deutschcraft.feature.threads.presentation

import com.julesmeister.deutschcraft.feature.threads.domain.ThreadSection

data class ThreadListUiState(
    val sections: List<ThreadSection> =
        emptyList(),

    val isLoading: Boolean = true,

    val isCreatingThread: Boolean = false,

    val errorMessage: String? = null
) {
    val isEmpty: Boolean
        get() {
            return !isLoading &&
                    sections.isEmpty()
        }

    val totalThreadCount: Int
        get() {
            return sections.sumOf { section ->
                section.threads.size
            }
        }
}