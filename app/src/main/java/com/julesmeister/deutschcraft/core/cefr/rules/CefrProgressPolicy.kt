package com.julesmeister.deutschcraft.core.cefr.rules

import com.julesmeister.deutschcraft.core.cefr.model.CefrProfile
import com.julesmeister.deutschcraft.core.cefr.model.ProgressionDecision

interface CefrProgressionPolicy {

    fun evaluate(
        profile: CefrProfile
    ): ProgressionDecision
}