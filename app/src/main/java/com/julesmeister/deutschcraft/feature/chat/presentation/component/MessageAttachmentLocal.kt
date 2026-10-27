package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.runtime.compositionLocalOf

/*
 * True when the message currently being drawn has marked-phrase
 * notes attached underneath it.
 *
 * ChatMessageList provides it, MessageBubble reads it, so the
 * bubble can flatten its bottom edge and merge with the notes.
 */
val LocalHasAttachedNotes =
    compositionLocalOf { false }