package com.julesmeister.deutschcraft.feature.chat.data.cache

import com.julesmeister.deutschcraft.feature.chat.domain.ReplySuggestion
import javax.inject.Inject
import javax.inject.Singleton

/*
 * Temporary in-memory storage for generated reply
 * suggestions.
 *
 * Suggestions are grouped by conversation ID so every
 * thread can preserve its own latest suggestions while
 * the application process remains alive.
 *
 * This cache intentionally does not use Room:
 *
 * - Suggestions survive normal navigation.
 * - Suggestions disappear after process death.
 * - Suggestions remain temporary helper content.
 */
@Singleton
class ReplySuggestionCache @Inject constructor() {

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
        if (suggestions.isEmpty()) {
            suggestionsByConversationId.remove(
                conversationId
            )

            return
        }

        suggestionsByConversationId[
            conversationId
        ] = suggestions.toList()
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
}