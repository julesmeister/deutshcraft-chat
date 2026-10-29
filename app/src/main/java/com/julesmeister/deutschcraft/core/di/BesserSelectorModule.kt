package com.julesmeister.deutschcraft.feature.besser.di

import com.julesmeister.deutschcraft.feature.besser.data.selector.BesserSelectionTextGenerator
import com.julesmeister.deutschcraft.feature.besser.data.selector.QwenBesserChallengeSelector
import com.julesmeister.deutschcraft.feature.besser.data.selector.QwenBesserSelectionTextGenerator
import com.julesmeister.deutschcraft.feature.besser.domain.selector.BesserChallengeSelector
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(
    SingletonComponent::class
)
abstract class BesserSelectorModule {

    @Binds
    @Singleton
    abstract fun bindBesserChallengeSelector(
        implementation:
        QwenBesserChallengeSelector
    ): BesserChallengeSelector

    @Binds
    @Singleton
    abstract fun bindBesserSelectionTextGenerator(
        implementation:
        QwenBesserSelectionTextGenerator
    ): BesserSelectionTextGenerator
}