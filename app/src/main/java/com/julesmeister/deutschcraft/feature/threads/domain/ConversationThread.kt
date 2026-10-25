package com.julesmeister.deutschcraft.feature.threads.domain

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel

/**
 * UI-friendly representation of one saved conversation.
 *
 * This model does not depend on Room classes, allowing
 * the presentation layer to remain database-agnostic.
 */
data class ConversationThread(
    val id: String,
    val title: String,
    val previewText: String?,
    val cefrLevel: CefrLevel,
    val localDate: String,
    val epochDay: Long,
    val messageCount: Int,
    val updatedAt: Long,
    val isClosed: Boolean
) {
    val displayPreview: String
        get() {
            return previewText
                ?.trim()
                ?.takeIf(String::isNotEmpty)
                ?: DEFAULT_PREVIEW
        }

    val displayTitle: String
        get() {
            return title
                .trim()
                .takeIf(String::isNotEmpty)
                ?: DEFAULT_TITLE
        }

    private companion object {
        const val DEFAULT_TITLE =
            "Neue Unterhaltung"

        const val DEFAULT_PREVIEW =
            "Noch keine Nachrichten"
    }
}