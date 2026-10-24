package com.julesmeister.deutschcraft.core.di

import android.content.Context
import androidx.room.Room
import com.julesmeister.deutschcraft.core.database.DeutschCraftDatabase
import com.julesmeister.deutschcraft.core.database.dao.AssessmentDao
import com.julesmeister.deutschcraft.core.database.dao.ConversationDao
import com.julesmeister.deutschcraft.core.database.dao.LearnerProfileDao
import com.julesmeister.deutschcraft.core.database.dao.MessageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.julesmeister.deutschcraft.core.database.dao.ActivityDayDao

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME =
        "deutschcraft.db"

    @Provides
    @Singleton
    fun provideDeutschCraftDatabase(
        @ApplicationContext context: Context
    ): DeutschCraftDatabase {
        return Room.databaseBuilder(
            context = context,
            klass = DeutschCraftDatabase::class.java,
            name = DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideConversationDao(
        database: DeutschCraftDatabase
    ): ConversationDao {
        return database.conversationDao()
    }

    @Provides
    fun provideMessageDao(
        database: DeutschCraftDatabase
    ): MessageDao {
        return database.messageDao()
    }

    @Provides
    fun provideLearnerProfileDao(
        database: DeutschCraftDatabase
    ): LearnerProfileDao {
        return database.learnerProfileDao()
    }

    @Provides
    fun provideAssessmentDao(
        database: DeutschCraftDatabase
    ): AssessmentDao {
        return database.assessmentDao()
    }

    @Provides
    fun provideActivityDayDao(
        database: DeutschCraftDatabase
    ): ActivityDayDao {
        return database.activityDayDao()
    }
}