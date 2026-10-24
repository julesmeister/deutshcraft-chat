package com.julesmeister.deutschcraft.core.cefr.model

sealed interface ProgressionDecision {

    data class NotEnoughEvidence(
        val currentMessages: Int,
        val requiredMessages: Int
    ) : ProgressionDecision

    data class SkillsNeedImprovement(
        val weakSkills: List<CefrSkill>
    ) : ProgressionDecision

    data class MoreConsistencyRequired(
        val currentQualifiedAssessments: Int,
        val requiredQualifiedAssessments: Int
    ) : ProgressionDecision

    data class Eligible(
        val currentLevel: CefrLevel,
        val proposedLevel: CefrLevel
    ) : ProgressionDecision

    data object MaximumLevelReached :
        ProgressionDecision
}