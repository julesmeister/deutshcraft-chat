package com.julesmeister.deutschcraft.feature.auth.domain

/*
 * Guests may send a few messages per conversation before they
 * have to sign in to keep that conversation going.
 */
object GuestMessagePolicy {

    const val MessageLimitPerConversation = 5

    fun remainingMessages(
        session: AuthSession,
        sentUserMessages: Int
    ): Int? {
        if (session !is AuthSession.Guest) {
            return null
        }

        return (MessageLimitPerConversation - sentUserMessages)
            .coerceAtLeast(0)
    }

    fun isLimitReached(
        session: AuthSession,
        sentUserMessages: Int
    ): Boolean =
        remainingMessages(
            session = session,
            sentUserMessages = sentUserMessages
        ) == 0
}
