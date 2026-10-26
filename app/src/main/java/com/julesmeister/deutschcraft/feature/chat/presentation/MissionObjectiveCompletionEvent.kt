package com.julesmeister.deutschcraft.feature.chat.presentation

data class MissionObjectiveCompletionEvent(
    val eventId: String,
    val objectiveId: String,
    val objectiveTitle: String
)