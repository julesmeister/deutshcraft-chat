package com.julesmeister.deutschcraft.feature.engagement.data

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EngagementPreferences @Inject constructor(
    @ApplicationContext
    context: Context
) {

    private val preferences =
        context.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

    var remindersEnabled: Boolean
        get() {
            return preferences.getBoolean(
                KEY_REMINDERS_ENABLED,
                false
            )
        }
        set(value) {
            preferences.edit {
                putBoolean(
                    KEY_REMINDERS_ENABLED,
                    value
                )
            }
        }

    var lastUserActivityAt: Long
        get() {
            return preferences.getLong(
                KEY_LAST_USER_ACTIVITY_AT,
                NO_TIMESTAMP
            )
        }
        set(value) {
            preferences.edit {
                putLong(
                    KEY_LAST_USER_ACTIVITY_AT,
                    value
                )
            }
        }

    var lastNotificationEpochDay: Long
        get() {
            return preferences.getLong(
                KEY_LAST_NOTIFICATION_EPOCH_DAY,
                NO_EPOCH_DAY
            )
        }
        set(value) {
            preferences.edit {
                putLong(
                    KEY_LAST_NOTIFICATION_EPOCH_DAY,
                    value
                )
            }
        }

    var lastStarterId: String?
        get() {
            return preferences.getString(
                KEY_LAST_STARTER_ID,
                null
            )
        }
        set(value) {
            preferences.edit {
                if (value == null) {
                    remove(KEY_LAST_STARTER_ID)
                } else {
                    putString(
                        KEY_LAST_STARTER_ID,
                        value
                    )
                }
            }
        }

    fun recordUserActivity(
        timestamp: Long
    ) {
        lastUserActivityAt = timestamp
    }

    fun recordNotification(
        epochDay: Long,
        starterId: String
    ) {
        lastNotificationEpochDay = epochDay
        lastStarterId = starterId
    }

    private companion object {
        const val PREFERENCES_NAME =
            "deutschcraft_engagement"

        const val KEY_REMINDERS_ENABLED =
            "reminders_enabled"

        const val KEY_LAST_USER_ACTIVITY_AT =
            "last_user_activity_at"

        const val KEY_LAST_NOTIFICATION_EPOCH_DAY =
            "last_notification_epoch_day"

        const val KEY_LAST_STARTER_ID =
            "last_starter_id"

        const val NO_TIMESTAMP =
            0L

        const val NO_EPOCH_DAY =
            Long.MIN_VALUE
    }
}