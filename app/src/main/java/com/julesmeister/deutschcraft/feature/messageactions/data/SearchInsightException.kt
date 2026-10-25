package com.julesmeister.deutschcraft.feature.messageactions.data

class SentenceInsightException(
    message: String,
    cause: Throwable? = null
) : Exception(
    message,
    cause
)