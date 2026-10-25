package com.julesmeister.deutschcraft.feature.threads.domain

/**
 * A date-based group shown on the Thread List screen.
 *
 * Examples:
 *
 * Heute
 *     Conversation A
 *     Conversation B
 *
 * Gestern
 *     Conversation C
 */
data class ThreadSection(
    val id: Long,
    val title: String,
    val threads: List<ConversationThread>
)