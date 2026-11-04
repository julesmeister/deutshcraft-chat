package com.julesmeister.deutschcraft.feature.chat.presentation

/*
 * Guest allowance for the current conversation. remaining is
 * null when the user is signed in (no limit).
 */
data class ChatGuestGate(
    val remaining: Int? = null
) {

    val isLocked: Boolean
        get() = remaining != null && remaining <= 0
}
