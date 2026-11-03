// app/src/main/java/com/julesmeister/deutschcraft/feature/chat/domain/ReplySuggestion.kt

package com.julesmeister.deutschcraft.feature.chat.domain

data class ReplySuggestion(
    val id: String,
    val text: String,
    val action: ReplySuggestionAction =
        ReplySuggestionAction.FillComposer
)

sealed interface ReplySuggestionAction {

    data object FillComposer :
        ReplySuggestionAction

    data object OpenMissionVariations :
        ReplySuggestionAction

    data object DismissMissionContinuation :
        ReplySuggestionAction
}
