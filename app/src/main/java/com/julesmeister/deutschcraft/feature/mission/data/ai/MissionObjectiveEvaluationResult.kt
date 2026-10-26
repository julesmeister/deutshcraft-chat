package com.julesmeister.deutschcraft.feature.mission.data.ai

/*
 * Structurally validated result returned by the AI gateway.
 *
 * EvaluateMissionMessageUseCase performs the final domain
 * validation before any objective is updated in Room.
 */
data class MissionObjectiveEvaluationResult(
    val completedObjectives:
    List<EvaluatedMissionObjective>
)

/*
 * objectiveId contains the exact ID of an unfinished
 * MissionObjective supplied to Qwen.
 *
 * It must not contain the objective title or description.
 */
data class EvaluatedMissionObjective(
    val objectiveId: String,
    val confidence: Float,
    val reason: String
)