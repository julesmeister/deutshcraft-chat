package com.julesmeister.deutschcraft.core.di

import android.content.Context
import androidx.room.Room
import com.julesmeister.deutschcraft.core.database.DeutschCraftDatabase
import com.julesmeister.deutschcraft.core.database.dao.ActivityDayDao
import com.julesmeister.deutschcraft.core.database.dao.AssessmentDao
import com.julesmeister.deutschcraft.core.database.dao.ConversationDao
import com.julesmeister.deutschcraft.core.database.dao.LearnerProfileDao
import com.julesmeister.deutschcraft.core.database.dao.MessageDao
import com.julesmeister.deutschcraft.core.database.dao.MissionDao
import com.julesmeister.deutschcraft.core.database.migration.DatabaseMigrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDeutschCraftDatabase(
        @ApplicationContext
        context: Context
    ): DeutschCraftDatabase {
        return Room.databaseBuilder(
            context,
            DeutschCraftDatabase::class.java,
            DATABASE_NAME
        )
            .addMigrations(
                DatabaseMigrations.MIGRATION_5_6,
                DatabaseMigrations.MIGRATION_6_7
            )
            .build()
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

    @Provides
    fun provideMissionDao(
        database: DeutschCraftDatabase
    ): MissionDao {
        return database.missionDao()
    }

    private const val DATABASE_NAME =
        "deutschcraft.db"
}