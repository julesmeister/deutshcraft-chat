package com.julesmeister.deutschcraft.feature.chat.presentation.progress

import androidx.compose.runtime.Immutable

@Immutable
data class ChatLevelNeedUiModel(
    val key: String,
    val title: String,
    val reason: String,
    val action: String,
    val statusLabel: String,
    val score: Int,
    val icon: String
) {

    val safeScore: Int
        get() =
            score.coerceIn(
                minimumValue = 0,
                maximumValue = 100
            )
}
