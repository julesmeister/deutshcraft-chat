package com.julesmeister.deutschcraft.feature.auth.data

import com.julesmeister.deutschcraft.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CognitoConfig
@Inject constructor() {

    val region: String =
        BuildConfig.COGNITO_REGION

    val userPoolId: String =
        BuildConfig.COGNITO_USER_POOL_ID

    val clientId: String =
        BuildConfig.COGNITO_CLIENT_ID

    val domainPrefix: String =
        BuildConfig.COGNITO_DOMAIN_PREFIX

    val isPasswordAuthConfigured: Boolean
        get() =
            clientId.isNotBlank() &&
                    region.isNotBlank()

    val isHostedUiConfigured: Boolean
        get() =
            isPasswordAuthConfigured &&
                    domainPrefix.isNotBlank()

    val identityProviderBaseUrl: String
        get() =
            "https://cognito-idp.$region.amazonaws.com/"

    val hostedUiBaseUrl: String
        get() =
            if (domainPrefix.isBlank()) {
                PlaceholderUrl
            } else {
                "https://$domainPrefix.auth.$region.amazoncognito.com/"
            }

    companion object {

        const val RedirectUri =
            "deutschcraft://auth/callback"

        const val SignOutUri =
            "deutschcraft://auth/signout"

        const val RedirectScheme =
            "deutschcraft"

        const val RedirectHost =
            "auth"

        const val RedirectCallbackPath =
            "/callback"

        const val OAuthScopes =
            "openid email profile"

        const val GoogleIdentityProvider =
            "Google"

        const val PlaceholderUrl =
            "https://cognito.invalid/"
    }
}
