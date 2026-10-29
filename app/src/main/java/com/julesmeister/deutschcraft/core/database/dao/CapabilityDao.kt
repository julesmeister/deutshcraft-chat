package com.julesmeister.deutschcraft.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.julesmeister.deutschcraft.core.database.entity.besser.CapabilityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CapabilityDao {

    @Upsert
    suspend fun upsert(
        capability: CapabilityEntity
    )

    @Upsert
    suspend fun upsertAll(
        capabilities: List<CapabilityEntity>
    )

    @Query(
        """
        SELECT *
        FROM besser_capabilities
        WHERE id = :capabilityId
        LIMIT 1
        """
    )
    suspend fun getById(
        capabilityId: String
    ): CapabilityEntity?

    /*
     * Used when Besser needs to resolve a capability selected
     * from the active catalogue.
     */
    @Query(
        """
        SELECT *
        FROM besser_capabilities
        WHERE id = :capabilityId
            AND is_active = 1
        LIMIT 1
        """
    )
    suspend fun getActiveById(
        capabilityId: String
    ): CapabilityEntity?

    @Query(
        """
        SELECT *
        FROM besser_capabilities
        WHERE id = :capabilityId
        LIMIT 1
        """
    )
    fun observeById(
        capabilityId: String
    ): Flow<CapabilityEntity?>

    @Query(
        """
        SELECT *
        FROM besser_capabilities
        WHERE is_active = 1
        ORDER BY
            sort_order ASC,
            name COLLATE NOCASE ASC
        """
    )
    fun observeActive():
            Flow<List<CapabilityEntity>>

    @Query(
        """
        SELECT *
        FROM besser_capabilities
        WHERE is_active = 1
            AND cefr_level = :cefrLevel
        ORDER BY
            sort_order ASC,
            name COLLATE NOCASE ASC
        """
    )
    fun observeActiveByCefrLevel(
        cefrLevel: String
    ): Flow<List<CapabilityEntity>>

    @Query(
        """
        SELECT *
        FROM besser_capabilities
        WHERE is_active = 1
            AND category = :category
        ORDER BY
            sort_order ASC,
            name COLLATE NOCASE ASC
        """
    )
    fun observeActiveByCategory(
        category: String
    ): Flow<List<CapabilityEntity>>

    /*
     * Core shortlist query.
     *
     * For a learner currently at B1 and working toward B2,
     * this will normally receive:
     *
     * ["B1", "B2"]
     */
    @Query(
        """
        SELECT *
        FROM besser_capabilities
        WHERE is_active = 1
            AND cefr_level IN (:cefrLevels)
        ORDER BY
            sort_order ASC,
            name COLLATE NOCASE ASC
        """
    )
    suspend fun getActiveForShortlist(
        cefrLevels: List<String>
    ): List<CapabilityEntity>

    @Query(
        """
        UPDATE besser_capabilities
        SET
            is_active = :isActive,
            updated_at = :updatedAt
        WHERE id = :capabilityId
        """
    )
    suspend fun setActive(
        capabilityId: String,
        isActive: Boolean,
        updatedAt: Long
    ): Int

    @Query(
        """
        SELECT COUNT(*)
        FROM besser_capabilities
        """
    )
    suspend fun count():
            Int

    @Query(
        """
        DELETE FROM besser_capabilities
        WHERE id = :capabilityId
        """
    )
    suspend fun deleteById(
        capabilityId: String
    ): Int
}
