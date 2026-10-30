package com.julesmeister.deutschcraft.core.di

import com.julesmeister.deutschcraft.feature.besser.data.evaluation.QwenBesserRefinementEvaluator
import com.julesmeister.deutschcraft.feature.besser.domain.evaluation.BesserRefinementEvaluator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BesserEvaluationModule {

    @Binds
    @Singleton
    abstract fun bindBesserRefinementEvaluator(
        implementation:
        QwenBesserRefinementEvaluator
    ): BesserRefinementEvaluator
}