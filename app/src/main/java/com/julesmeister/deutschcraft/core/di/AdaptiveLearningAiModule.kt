package com.julesmeister.deutschcraft.core.di

import com.julesmeister.deutschcraft.feature.adaptivelearning.data.ai.CommunicationEvidenceAiGateway
import com.julesmeister.deutschcraft.feature.adaptivelearning.data.ai.QwenCommunicationEvidenceAiGateway
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(
    SingletonComponent::class
)
abstract class AdaptiveLearningAiModule {

    @Binds
    @Singleton
    abstract fun bindCommunicationEvidenceAiGateway(
        implementation:
        QwenCommunicationEvidenceAiGateway
    ): CommunicationEvidenceAiGateway
}