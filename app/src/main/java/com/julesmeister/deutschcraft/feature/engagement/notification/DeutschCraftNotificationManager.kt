package com.julesmeister.deutschcraft.feature.engagement.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.julesmeister.deutschcraft.R
import com.julesmeister.deutschcraft.feature.engagement.domain.ConversationStarter

class DeutschCraftNotificationManager(
    private val context: Context
) {

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel =
            NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description =
                    CHANNEL_DESCRIPTION

                enableVibration(true)

                setShowBadge(true)
            }

        val notificationManager =
            context.getSystemService(
                NotificationManager::class.java
            )

        notificationManager.createNotificationChannel(
            channel
        )
    }

    fun canPostNotifications(): Boolean {
        val permissionGranted =
            Build.VERSION.SDK_INT <
                    Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED

        return permissionGranted &&
                NotificationManagerCompat
                    .from(context)
                    .areNotificationsEnabled()
    }

    fun showConversationStarter(
        starter: ConversationStarter
    ): Boolean {
        if (!canPostNotifications()) {
            return false
        }

        createNotificationChannel()

        val launchIntent =
            context.packageManager
                .getLaunchIntentForPackage(
                    context.packageName
                )
                ?.apply {
                    flags =
                        android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                                android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP

                    putExtra(
                        EXTRA_OPEN_CHAT,
                        true
                    )
                }

        val pendingIntent =
            launchIntent?.let { intent ->
                PendingIntent.getActivity(
                    context,
                    NOTIFICATION_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or
                            PendingIntent.FLAG_IMMUTABLE
                )
            }

        val notification =
            NotificationCompat.Builder(
                context,
                CHANNEL_ID
            )
                .setSmallIcon(
                    R.drawable.ic_launcher_foreground
                )
                .setContentTitle(
                    starter.title
                )
                .setContentText(
                    starter.message
                )
                .setStyle(
                    NotificationCompat
                        .BigTextStyle()
                        .bigText(
                            starter.message
                        )
                )
                .setPriority(
                    NotificationCompat.PRIORITY_DEFAULT
                )
                .setCategory(
                    NotificationCompat.CATEGORY_MESSAGE
                )
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(
                    pendingIntent
                )
                .build()

        NotificationManagerCompat
            .from(context)
            .notify(
                IDLE_PROMPT_NOTIFICATION_ID,
                notification
            )

        return true
    }

    companion object {
        const val CHANNEL_ID =
            "emil_conversation_starters"

        const val EXTRA_OPEN_CHAT =
            "open_chat_from_idle_notification"

        private const val CHANNEL_NAME =
            "Emil conversation starters"

        private const val CHANNEL_DESCRIPTION =
            "Occasional German conversation topics from Emil"

        private const val IDLE_PROMPT_NOTIFICATION_ID =
            3_001

        private const val NOTIFICATION_REQUEST_CODE =
            3_001
    }
}