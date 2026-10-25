package com.julesmeister.deutschcraft.core.di

import com.julesmeister.deutschcraft.feature.chat.data.LocalFirstChatRepository
import com.julesmeister.deutschcraft.feature.chat.domain.ChatRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ChatModule {

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        implementation: LocalFirstChatRepository
    ): ChatRepository

}