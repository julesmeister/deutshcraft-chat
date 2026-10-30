// app/src/main/java/com/julesmeister/deutschcraft/feature/chat/data/cache/ReplySuggestionCache.kt

package com.julesmeister.deutschcraft.feature.chat.data.cache

import com.julesmeister.deutschcraft.feature.chat.domain.ReplySuggestion
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReplySuggestionCache
@Inject constructor() {

    private val suggestionsByConversationId =
        mutableMapOf<
                String,
                List<ReplySuggestion>
                >()

    @Synchronized
    fun getSuggestions(
        conversationId: String
    ): List<ReplySuggestion> {
        return suggestionsByConversationId[
            conversationId
        ]
            ?.toList()
            ?: emptyList()
    }

    @Synchronized
    fun replaceSuggestions(
        conversationId: String,
        suggestions: List<ReplySuggestion>
    ) {
        val normalizedSuggestions =
            suggestions
                .distinctBy { suggestion ->
                    suggestion.text
                        .trim()
                        .lowercase()
                }
                .take(
                    MAXIMUM_SUGGESTION_COUNT
                )

        if (normalizedSuggestions.isEmpty()) {
            suggestionsByConversationId.remove(
                conversationId
            )

            return
        }

        suggestionsByConversationId[
            conversationId
        ] =
            normalizedSuggestions.toList()
    }

    @Synchronized
    fun appendSuggestion(
        conversationId: String,
        suggestion: ReplySuggestion
    ): List<ReplySuggestion> {
        val cleanText =
            suggestion.text.trim()

        if (cleanText.isBlank()) {
            return getSuggestions(
                conversationId =
                    conversationId
            )
        }

        val currentSuggestions =
            suggestionsByConversationId[
                conversationId
            ].orEmpty()

        val updatedSuggestions =
            (
                    currentSuggestions +
                            suggestion.copy(
                                text =
                                    cleanText
                            )
                    )
                .distinctBy { item ->
                    item.text
                        .trim()
                        .lowercase()
                }
                .take(
                    MAXIMUM_SUGGESTION_COUNT
                )

        suggestionsByConversationId[
            conversationId
        ] =
            updatedSuggestions

        return updatedSuggestions.toList()
    }

    @Synchronized
    fun clearSuggestions(
        conversationId: String
    ) {
        suggestionsByConversationId.remove(
            conversationId
        )
    }

    @Synchronized
    fun clearAll() {
        suggestionsByConversationId.clear()
    }

    private companion object {

        const val MAXIMUM_SUGGESTION_COUNT =
            3
    }
}