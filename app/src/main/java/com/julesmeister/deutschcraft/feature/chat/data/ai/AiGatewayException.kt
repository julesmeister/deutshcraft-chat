package com.julesmeister.deutschcraft.feature.chat.data.ai

class AiGatewayException(
    message: String,
    cause: Throwable? = null
) : Exception(
    message,
    cause
)