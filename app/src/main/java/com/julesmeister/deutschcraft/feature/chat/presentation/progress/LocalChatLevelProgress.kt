// app/src/main/java/com/julesmeister/deutschcraft/feature/chat/presentation/progress/LocalChatLevelProgress.kt

package com.julesmeister.deutschcraft.feature.chat.presentation.progress

import androidx.compose.runtime.staticCompositionLocalOf

val LocalChatLevelProgress =
    staticCompositionLocalOf {
        ChatLevelProgressUiState()
    }