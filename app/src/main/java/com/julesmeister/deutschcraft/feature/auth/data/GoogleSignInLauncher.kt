package com.julesmeister.deutschcraft.feature.auth.data

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import com.julesmeister.deutschcraft.feature.auth.data.crypto.PkcePair
import javax.inject.Inject
import javax.inject.Singleton

/*
 * Opens Cognito's /oauth2/authorize with identity_provider=Google
 * in a Chrome Custom Tab. Cognito redirects straight to Google's
 * account picker, so the user never sees Cognito's own UI.
 */
@Singleton
class GoogleSignInLauncher
@Inject constructor(
    private val config: CognitoConfig
) {

    internal fun buildAuthorizeUri(
        pkce: PkcePair,
        state: String
    ): Uri =
        Uri.parse(config.hostedUiBaseUrl)
            .buildUpon()
            .appendEncodedPath("oauth2/authorize")
            .appendQueryParameter("identity_provider", CognitoConfig.GoogleIdentityProvider)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("client_id", config.clientId)
            .appendQueryParameter("redirect_uri", CognitoConfig.RedirectUri)
            .appendQueryParameter("scope", CognitoConfig.OAuthScopes)
            .appendQueryParameter("state", state)
            .appendQueryParameter("code_challenge_method", "S256")
            .appendQueryParameter("code_challenge", pkce.challenge)
            // Always show the picker so users can switch accounts.
            .appendQueryParameter("prompt", "select_account")
            .build()

    fun launch(
        context: Context,
        uri: Uri,
        toolbarColor: Int
    ) {
        val customTabsIntent =
            CustomTabsIntent
                .Builder()
                .setShowTitle(true)
                .setDefaultColorSchemeParams(
                    CustomTabColorSchemeParams
                        .Builder()
                        .setToolbarColor(toolbarColor)
                        .build()
                )
                .build()

        try {
            customTabsIntent.launchUrl(context, uri)
        } catch (_: ActivityNotFoundException) {
            // No Custom Tabs browser: fall back to any browser.
            context.startActivity(
                Intent(Intent.ACTION_VIEW, uri)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
