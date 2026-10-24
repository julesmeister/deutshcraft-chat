package com.julesmeister.deutschcraft.core.cefr.rules

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevelDefinition

interface CefrRuleProvider {

    fun getDefinition(
        level: CefrLevel
    ): CefrLevelDefinition

    fun getAllDefinitions():
            List<CefrLevelDefinition>
}