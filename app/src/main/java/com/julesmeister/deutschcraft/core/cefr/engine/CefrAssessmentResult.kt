package com.julesmeister.deutschcraft.core.cefr.engine

import com.julesmeister.deutschcraft.core.cefr.model.CefrProfile
import com.julesmeister.deutschcraft.core.cefr.model.ProgressionDecision
import com.julesmeister.deutschcraft.core.cefr.model.SkillScore

data class CefrAssessmentResult(
    val updatedProfile: CefrProfile,
    val updatedSkillScores: List<SkillScore>,
    val progressionDecision: ProgressionDecision
)