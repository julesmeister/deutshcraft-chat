package com.julesmeister.deutschcraft.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.julesmeister.deutschcraft.core.database.converter.DatabaseTypeConverters
import com.julesmeister.deutschcraft.core.database.dao.AssessmentDao
import com.julesmeister.deutschcraft.core.database.dao.ConversationDao
import com.julesmeister.deutschcraft.core.database.dao.LearnerProfileDao
import com.julesmeister.deutschcraft.core.database.dao.MessageDao
import com.julesmeister.deutschcraft.core.database.entity.AssessmentEvidenceEntity
import com.julesmeister.deutschcraft.core.database.entity.ConversationEntity
import com.julesmeister.deutschcraft.core.database.entity.DetectedMistakeEntity
import com.julesmeister.deutschcraft.core.database.entity.EvidenceGrammarFeatureEntity
import com.julesmeister.deutschcraft.core.database.entity.LearnerProfileEntity
import com.julesmeister.deutschcraft.core.database.entity.MessageEntity
import com.julesmeister.deutschcraft.core.database.entity.SkillScoreEntity

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        LearnerProfileEntity::class,
        SkillScoreEntity::class,
        AssessmentEvidenceEntity::class,
        EvidenceGrammarFeatureEntity::class,
        DetectedMistakeEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(DatabaseTypeConverters::class)
abstract class DeutschCraftDatabase : RoomDatabase() {

    abstract fun conversationDao(): ConversationDao

    abstract fun messageDao(): MessageDao

    abstract fun learnerProfileDao(): LearnerProfileDao

    abstract fun assessmentDao(): AssessmentDao
}