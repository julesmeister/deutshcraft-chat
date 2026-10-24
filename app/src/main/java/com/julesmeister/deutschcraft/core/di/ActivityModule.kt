package com.julesmeister.deutschcraft.core.di

import com.julesmeister.deutschcraft.core.activity.ActivityTracker
import com.julesmeister.deutschcraft.core.activity.LocalActivityTracker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ActivityModule {

    @Binds
    @Singleton
    abstract fun bindActivityTracker(
        implementation: LocalActivityTracker
    ): ActivityTracker
}