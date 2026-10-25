package com.julesmeister.deutschcraft.feature.chat.data.correction

class MessageCorrectionException(
    message: String,
    cause: Throwable? = null
) : Exception(
    message,
    cause
)