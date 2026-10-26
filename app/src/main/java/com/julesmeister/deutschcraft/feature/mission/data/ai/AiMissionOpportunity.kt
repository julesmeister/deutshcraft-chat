package com.julesmeister.deutschcraft.feature.mission.data.ai

/*
 * Raw, untrusted mission-opportunity response produced by
 * the AI gateway.
 *
 * These models are public because they are part of the
 * public MissionOpportunityAiGateway contract.
 *
 * They remain data-layer models and must still pass through
 * DetectMissionOpportunityUseCase validation before being
 * displayed or persisted.
 */
data class AiMissionOpportunity(
    val shouldOfferMission: Boolean,
    val confidence: Float,
    val reason: String,
    val mission: AiGeneratedMission?
)

data class AiGeneratedMission(
    val title: String,
    val description: String,
    val scenario: String,
    val learnerRole: String,
    val assistantRole: String,
    val difficulty: String,
    val icon: String,
    val objectives:
    List<AiGeneratedMissionObjective>
)

data class AiGeneratedMissionObjective(
    val title: String,
    val description: String,
    val completionHint: String,
    val required: Boolean
)