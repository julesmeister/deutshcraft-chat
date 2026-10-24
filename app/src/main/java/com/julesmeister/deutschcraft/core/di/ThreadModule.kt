package com.julesmeister.deutschcraft.core.di

import com.julesmeister.deutschcraft.feature.threads.data.LocalConversationTitleGenerator
import com.julesmeister.deutschcraft.feature.threads.domain.ConversationTitleGenerator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ThreadModule {

    @Binds
    @Singleton
    abstract fun bindConversationTitleGenerator(
        implementation:
        LocalConversationTitleGenerator
    ): ConversationTitleGenerator
}