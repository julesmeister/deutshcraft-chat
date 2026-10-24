package com.julesmeister.deutschcraft.core.di

import com.julesmeister.deutschcraft.core.cefr.engine.CefrAssessmentEngine
import com.julesmeister.deutschcraft.core.cefr.engine.DefaultCefrAssessmentEngine
import com.julesmeister.deutschcraft.core.cefr.engine.DefaultLocalMessageEvaluator
import com.julesmeister.deutschcraft.core.cefr.engine.LocalMessageEvaluator
import com.julesmeister.deutschcraft.core.cefr.rules.CefrProgressionPolicy
import com.julesmeister.deutschcraft.core.cefr.rules.CefrRuleProvider
import com.julesmeister.deutschcraft.core.cefr.rules.DefaultCefrProgressionPolicy
import com.julesmeister.deutschcraft.core.cefr.rules.DefaultCefrRuleProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CefrModule {

    @Binds
    @Singleton
    abstract fun bindCefrRuleProvider(
        implementation: DefaultCefrRuleProvider
    ): CefrRuleProvider

    @Binds
    @Singleton
    abstract fun bindProgressionPolicy(
        implementation:
        DefaultCefrProgressionPolicy
    ): CefrProgressionPolicy

    @Binds
    @Singleton
    abstract fun bindAssessmentEngine(
        implementation:
        DefaultCefrAssessmentEngine
    ): CefrAssessmentEngine

    @Binds
    @Singleton
    abstract fun bindLocalMessageEvaluator(
        implementation:
        DefaultLocalMessageEvaluator
    ): LocalMessageEvaluator
}