package com.julesmeister.deutschcraft.feature.mission.data.ai

data class MissionEvaluationTurn(
    val speaker: MissionEvaluationSpeaker,
    val content: String
)

enum class MissionEvaluationSpeaker {
    LEARNER,
    ASSISTANT
}