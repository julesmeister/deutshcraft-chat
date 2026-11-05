package com.julesmeister.deutschcraft.feature.auth.data

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/*
 * Opens Cognito's /logout endpoint so the hosted-UI browser
 * session cookie is cleared. Without this, the next
 * "Continue with Google" can silently reuse the old session.
 *
 * Cognito then redirects to deutschcraft://auth/signout, which
 * brings MainActivity back to the front.
 */
@Singleton
class CognitoLogoutLauncher
@Inject constructor(
    @ApplicationContext
    private val context: Context,

    private val config: CognitoConfig
) {

    fun buildLogoutUri(): Uri =
        Uri.parse(config.hostedUiBaseUrl)
            .buildUpon()
            .appendPath("logout")
            .appendQueryParameter("client_id", config.clientId)
            .appendQueryParameter("logout_uri", CognitoConfig.SignOutUri)
            .build()

    fun launch() {
        if (!config.isHostedUiConfigured) {
            return
        }

        val uri =
            buildLogoutUri()

        val customTabsIntent =
            CustomTabsIntent.Builder()
                .setShowTitle(false)
                .build()

        // Launched from the application context, so a new task is required.
        customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            customTabsIntent.launchUrl(context, uri)
        } catch (exception: ActivityNotFoundException) {
            Log.w(LOGOUT_TAG, "No browser available for Cognito logout.", exception)
        }
    }

    private companion object {
        const val LOGOUT_TAG =
            "CognitoLogout"
    }
}
