// app/src/main/java/com/julesmeister/deutschcraft/feature/chat/presentation/progress/CapabilityTeachingSupport.kt

package com.julesmeister.deutschcraft.feature.chat.presentation.progress

import androidx.compose.runtime.Immutable

@Immutable
data class CapabilityTeachingSupport(
    val capabilityId: String,
    val learnerTitle: String,
    val explanation: String,
    val grammarTargets: List<String>,
    val vocabularyTargets: List<String>,
    val examplePhrase: String
)
