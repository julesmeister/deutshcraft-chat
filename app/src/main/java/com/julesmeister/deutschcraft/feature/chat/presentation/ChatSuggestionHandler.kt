package com.julesmeister.deutschcraft.feature.chat.presentation

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.julesmeister.deutschcraft.feature.chat.domain.ReplySuggestion
import javax.inject.Inject

class ChatSuggestionHandler @Inject constructor() {

    fun createInputFromSuggestion(
        suggestions: List<ReplySuggestion>,
        suggestionId: String,
        isEnabled: Boolean
    ): TextFieldValue? {
        if (!isEnabled) {
            return null
        }

        val suggestion =
            suggestions.firstOrNull { item ->
                item.id ==
                        suggestionId
            }
                ?: return null

        val suggestedText =
            suggestion
                .text
                .capitalizeFirstCharacter()

        return TextFieldValue(
            text =
                suggestedText,
            selection =
                TextRange(
                    index =
                        suggestedText.length
                )
        )
    }
}