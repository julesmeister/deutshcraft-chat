package com.julesmeister.deutschcraft.feature.threads.domain

interface ConversationTitleGenerator {

    fun generateTitle(
        messages: List<String>
    ): String
}