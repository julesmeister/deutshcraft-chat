package com.julesmeister.deutschcraft.core.database.converter

import androidx.room.TypeConverter
import com.julesmeister.deutschcraft.core.database.entity.MessageSenderEntity
import com.julesmeister.deutschcraft.core.database.entity.SyncStatusEntity

class DatabaseTypeConverters {

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