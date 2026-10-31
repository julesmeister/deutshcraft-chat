// app/src/main/java/com/julesmeister/deutschcraft/feature/chat/presentation/progress/ChatLevelProgressUiState.kt

package com.julesmeister.deutschcraft.feature.chat.presentation.progress

import androidx.compose.runtime.Immutable
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel

@Immutable
data class ChatLevelProgressUiState(
    val isLoading: Boolean =
        true,

    val currentLevel: CefrLevel =
        CefrLevel.A1,

    val readinessPercentage: Int =
        0,

    val suggestedNextLevel: CefrLevel? =
        null,

    val assessedMessageCount: Int =
        0,

    val activeDayCount: Int =
        0,

    val immediateNeed:
    ChatImmediateLevelNeedUiModel? =
        null,

    val reduceAnimations: Boolean =
        false
) {

    val safeReadinessPercentage: Int
        get() =
            readinessPercentage.coerceIn(
                minimumValue = 0,
                maximumValue = 100
            )

    val levelLabel: String
        get() =
            currentLevel.name

    val nextLevel: CefrLevel?
        get() =
            suggestedNextLevel
                ?: currentLevel.nextOrNull()

    val nextLevelLabel: String
        get() =
            nextLevel?.name
                ?: currentLevel.name

    val isHighestLevel: Boolean
        get() =
            currentLevel ==
                    CefrLevel.C2

    val progressTitle: String
        get() =
            if (isHighestLevel) {
                "Maintaining C2"
            } else {
                "$levelLabel toward $nextLevelLabel"
            }

    val readinessLabel: String
        get() =
            if (isHighestLevel) {
                "Advanced maintenance"
            } else {
                "$safeReadinessPercentage% ready"
            }

    val accessibilityDescription: String
        get() =
            if (isHighestLevel) {
                "$levelLabel, highest CEFR level reached"
            } else {
                "$levelLabel, $safeReadinessPercentage percent ready for $nextLevelLabel"
            }

    fun withAnimationPreference(
        reduceAnimations: Boolean
    ): ChatLevelProgressUiState {
        return copy(
            reduceAnimations =
                reduceAnimations
        )
    }

    private fun CefrLevel.nextOrNull():
            CefrLevel? {
        return when (this) {
            CefrLevel.A1 ->
                CefrLevel.A2

            CefrLevel.A2 ->
                CefrLevel.B1

            CefrLevel.B1 ->
                CefrLevel.B2

            CefrLevel.B2 ->
                CefrLevel.C1

            CefrLevel.C1 ->
                CefrLevel.C2

            CefrLevel.C2 ->
                null
        }
    }
}