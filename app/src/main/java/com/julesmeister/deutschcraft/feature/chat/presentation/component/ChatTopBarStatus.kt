package com.julesmeister.deutschcraft.feature.chat.presentation.component

internal sealed interface ChatTopBarStatus {

    data object Normal :
        ChatTopBarStatus

    data object AssistantTyping :
        ChatTopBarStatus

    data object Assessing :
        ChatTopBarStatus

    data class MissionProgress(
        val title: String,
        val percentage: Int
    ) : ChatTopBarStatus

    data class MissionObjective(
        val objective: String
    ) : ChatTopBarStatus
}