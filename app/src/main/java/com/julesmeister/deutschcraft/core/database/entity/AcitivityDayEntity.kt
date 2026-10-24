package com.julesmeister.deutschcraft.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "activity_days",
    primaryKeys = [
        "profileId",
        "epochDay"
    ],
    foreignKeys = [
        ForeignKey(
            entity = LearnerProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["profileId"]),
        Index(value = ["epochDay"]),
        Index(value = ["lastActivityAt"])
    ]
)
data class ActivityDayEntity(
    val profileId: String,

    /**
     * Number returned by LocalDate.toEpochDay().
     *
     * One value represents one local calendar date.
     */
    val epochDay: Long,

    /**
     * Useful for displaying the date without
     * recalculating it from epochDay.
     *
     * Example: "2026-10-24"
     */
    val localDate: String,

    /**
     * Device time zone used when this activity
     * date was calculated.
     *
     * Example: "Asia/Manila"
     */
    val zoneId: String,

    val messageCount: Int,

    val assessmentCount: Int,

    val firstActivityAt: Long,

    val lastActivityAt: Long,

    val syncStatus: SyncStatusEntity,

    val remoteVersion: Long?,

    val lastSyncedAt: Long?
)