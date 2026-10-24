package com.julesmeister.deutschcraft.core.cefr.model

data class SkillScore(
    val skill: CefrSkill,
    val score: Int,
    val evidenceCount: Int,
    val confidence: Double
) {
    init {
        require(score in 0..100)
        require(evidenceCount >= 0)
        require(confidence in 0.0..1.0)
    }
}