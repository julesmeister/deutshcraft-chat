package com.julesmeister.deutschcraft.feature.chat.data.correction

interface MessageCorrectionGateway {

    suspend fun correctSentence(
        sentence: String
    ): String?
}