package com.julesmeister.deutschcraft.feature.chat.domain

data class MessageCorrection(
    val messageId: String,
    val correctedSentence: String
)