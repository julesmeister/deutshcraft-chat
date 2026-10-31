// app/src/main/java/com/julesmeister/deutschcraft/feature/chat/presentation/progress/ChatImmediateLevelNeedUiModel.kt

package com.julesmeister.deutschcraft.feature.chat.presentation.progress

import androidx.compose.runtime.Immutable

@Immutable
data class ChatImmediateLevelNeedUiModel(
    val key: String,
    val capabilityId: String?,
    val title: String,
    val explanation: String,
    val whyNow: String,
    val grammarTargets: List<String>,
    val vocabularyTargets: List<String>,
    val examplePhrase: String,
    val masteryPercentage: Int?,
    val statusLabel: String
) {

    val safeMasteryPercentage: Int?
        get() =
            masteryPercentage?.coerceIn(
                minimumValue = 0,
                maximumValue = 100
            )

    val hasGrammarTargets: Boolean
        get() =
            grammarTargets.isNotEmpty()

    val hasVocabularyTargets: Boolean
        get() =
            vocabularyTargets.isNotEmpty()

    val hasExamplePhrase: Boolean
        get() =
            examplePhrase.isNotBlank()

    val hasTeachingSupport: Boolean
        get() =
            hasGrammarTargets ||
                    hasVocabularyTargets ||
                    hasExamplePhrase
}