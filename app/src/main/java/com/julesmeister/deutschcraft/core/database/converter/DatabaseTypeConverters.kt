package com.julesmeister.deutschcraft.core.database.converter

import androidx.room.TypeConverter
import com.julesmeister.deutschcraft.core.cefr.model.AssessmentSource
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.core.cefr.model.CefrSkill
import com.julesmeister.deutschcraft.core.cefr.model.GrammarFeature
import com.julesmeister.deutschcraft.core.cefr.model.MistakeCategory
import com.julesmeister.deutschcraft.core.cefr.model.MistakeSeverity
import com.julesmeister.deutschcraft.core.cefr.model.ProgressionState
import com.julesmeister.deutschcraft.core.database.entity.MessageSenderEntity
import com.julesmeister.deutschcraft.core.database.entity.SyncStatusEntity

class DatabaseTypeConverters {

    @TypeConverter
    fun cefrLevelToString(
        level: CefrLevel?
    ): String? {
        return level?.name
    }

    @TypeConverter
    fun stringToCefrLevel(
        value: String?
    ): CefrLevel? {
        return value?.let(CefrLevel::valueOf)
    }

    @TypeConverter
    fun cefrSkillToString(
        skill: CefrSkill
    ): String {
        return skill.name
    }

    @TypeConverter
    fun stringToCefrSkill(
        value: String
    ): CefrSkill {
        return CefrSkill.valueOf(value)
    }

    @TypeConverter
    fun grammarFeatureToString(
        feature: GrammarFeature?
    ): String? {
        return feature?.name
    }

    @TypeConverter
    fun stringToGrammarFeature(
        value: String?
    ): GrammarFeature? {
        return value?.let(GrammarFeature::valueOf)
    }

    @TypeConverter
    fun progressionStateToString(
        state: ProgressionState
    ): String {
        return state.name
    }

    @TypeConverter
    fun stringToProgressionState(
        value: String
    ): ProgressionState {
        return ProgressionState.valueOf(value)
    }

    @TypeConverter
    fun mistakeCategoryToString(
        category: MistakeCategory
    ): String {
        return category.name
    }

    @TypeConverter
    fun stringToMistakeCategory(
        value: String
    ): MistakeCategory {
        return MistakeCategory.valueOf(value)
    }

    @TypeConverter
    fun mistakeSeverityToString(
        severity: MistakeSeverity
    ): String {
        return severity.name
    }

    @TypeConverter
    fun stringToMistakeSeverity(
        value: String
    ): MistakeSeverity {
        return MistakeSeverity.valueOf(value)
    }

    @TypeConverter
    fun assessmentSourceToString(
        source: AssessmentSource
    ): String {
        return source.name
    }

    @TypeConverter
    fun stringToAssessmentSource(
        value: String
    ): AssessmentSource {
        return AssessmentSource.valueOf(value)
    }

    @TypeConverter
    fun messageSenderToString(
        sender: MessageSenderEntity
    ): String {
        return sender.name
    }

    @TypeConverter
    fun stringToMessageSender(
        value: String
    ): MessageSenderEntity {
        return MessageSenderEntity.valueOf(value)
    }

    @TypeConverter
    fun syncStatusToString(
        status: SyncStatusEntity
    ): String {
        return status.name
    }

    @TypeConverter
    fun stringToSyncStatus(
        value: String
    ): SyncStatusEntity {
        return SyncStatusEntity.valueOf(value)
    }
}