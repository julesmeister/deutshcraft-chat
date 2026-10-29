package com.julesmeister.deutschcraft.core.database.entity.besser

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "besser_capabilities",
    indices = [
        Index(
            value = [
                "cefr_level"
            ]
        ),
        Index(
            value = [
                "category"
            ]
        ),
        Index(
            value = [
                "is_active"
            ]
        )
    ]
)
data class CapabilityEntity(
    @PrimaryKey
    @ColumnInfo(
        name = "id"
    )
    val id: String,

    @ColumnInfo(
        name = "name"
    )
    val name: String,

    @ColumnInfo(
        name = "description"
    )
    val description: String,

    @ColumnInfo(
        name = "category"
    )
    val category: String,

    @ColumnInfo(
        name = "cefr_level"
    )
    val cefrLevel: String,

    @ColumnInfo(
        name = "sort_order"
    )
    val sortOrder: Int = 0,

    @ColumnInfo(
        name = "is_active"
    )
    val isActive: Boolean = true,

    @ColumnInfo(
        name = "created_at"
    )
    val createdAt: Long,

    @ColumnInfo(
        name = "updated_at"
    )
    val updatedAt: Long
)