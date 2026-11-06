// app/src/main/java/com/julesmeister/deutschcraft/feature/sync/data/SyncPreferences.kt

package com.julesmeister.deutschcraft.feature.sync.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.julesmeister.deutschcraft.core.di.SyncDataStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class SyncPreferences
@Inject constructor(
    @SyncDataStore
    private val dataStore:
    DataStore<Preferences>
) {

    fun nextCursor(
        userId: String
    ) =
        dataStore.data.map { preferences ->
            preferences[
                cursorKey(
                    userId = userId
                )
            ] ?: 0L
        }

    fun lastSyncTimestamp(
        userId: String
    ) =
        dataStore.data.map { preferences ->
            preferences[
                lastSyncKey(
                    userId = userId
                )
            ] ?: 0L
        }

    suspend fun getNextCursor(
        userId: String
    ): Long {
        return nextCursor(
            userId = userId
        ).first()
    }

    suspend fun getLastSyncTimestamp(
        userId: String
    ): Long {
        return lastSyncTimestamp(
            userId = userId
        ).first()
    }

    suspend fun updateNextCursor(
        userId: String,
        cursor: Long
    ) {
        require(
            userId.isNotBlank()
        ) {
            "Sync user ID cannot be blank."
        }

        dataStore.edit { preferences ->
            preferences[
                cursorKey(
                    userId = userId
                )
            ] =
                cursor.coerceAtLeast(
                    0L
                )
        }
    }

    suspend fun updateLastSyncTimestamp(
        userId: String,
        timestamp: Long
    ) {
        require(
            userId.isNotBlank()
        ) {
            "Sync user ID cannot be blank."
        }

        dataStore.edit { preferences ->
            preferences[
                lastSyncKey(
                    userId = userId
                )
            ] =
                timestamp.coerceAtLeast(
                    0L
                )
        }
    }

    suspend fun clearSyncState(
        userId: String
    ) {
        if (userId.isBlank()) {
            return
        }

        dataStore.edit { preferences ->
            preferences.remove(
                cursorKey(
                    userId = userId
                )
            )

            preferences.remove(
                lastSyncKey(
                    userId = userId
                )
            )
        }
    }

    private fun cursorKey(
        userId: String
    ): Preferences.Key<Long> {
        return longPreferencesKey(
            CURSOR_KEY_PREFIX +
                    userId.toPreferenceKeyPart()
        )
    }

    private fun lastSyncKey(
        userId: String
    ): Preferences.Key<Long> {
        return longPreferencesKey(
            LAST_SYNC_KEY_PREFIX +
                    userId.toPreferenceKeyPart()
        )
    }

    private fun String.toPreferenceKeyPart():
            String {
        return trim()
            .replace(
                NON_KEY_CHARACTER_REGEX,
                "_"
            )
    }

    private companion object {

        const val CURSOR_KEY_PREFIX =
            "sync_cursor_"

        const val LAST_SYNC_KEY_PREFIX =
            "sync_last_sync_"

        val NON_KEY_CHARACTER_REGEX =
            Regex(
                "[^A-Za-z0-9._-]"
            )
    }
}