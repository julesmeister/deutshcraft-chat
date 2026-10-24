package com.julesmeister.deutschcraft.core.cefr.engine

import com.julesmeister.deutschcraft.core.cefr.model.AssessmentEvidence
import com.julesmeister.deutschcraft.core.cefr.model.CefrProfile

interface CefrAssessmentEngine {

    fun assess(
        currentProfile: CefrProfile,
        newEvidence: AssessmentEvidence,
        recentEvidence: List<AssessmentEvidence>,
        activeDayCount: Int
    ): CefrAssessmentResult
}