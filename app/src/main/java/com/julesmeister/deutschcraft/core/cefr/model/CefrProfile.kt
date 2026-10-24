package com.julesmeister.deutschcraft.core.cefr.model

data class CefrProfile(
    val currentLevel: CefrLevel,
    val skillScores: Map<CefrSkill, SkillScore>,
    val overallScore: Int,
    val readinessPercentage: Int,
    val assessedMessageCount: Int,
    val activeDayCount: Int,
    val recentCriticalErrorRate: Double,
    val consecutiveQualifiedAssessments: Int,
    val progressionState: ProgressionState,
    val suggestedNextLevel: CefrLevel?,
    val lastAssessedAt: Long
)

enum class ProgressionState {
    NOT_READY,
    BUILDING_EVIDENCE,
    READY_FOR_REVIEW,
    OFFERED_TO_USER,
    ACCEPTED,
    POSTPONED,
    MAXIMUM_LEVEL
}