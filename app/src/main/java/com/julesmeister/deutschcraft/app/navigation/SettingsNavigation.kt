package com.julesmeister.deutschcraft.app.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.julesmeister.deutschcraft.feature.settings.presentation.SettingsRoute
import kotlinx.serialization.Serializable

@Serializable
data object SettingsDestination

fun NavGraphBuilder.settingsDestination(
    navController: NavController
) {
    composable<SettingsDestination> {
        SettingsRoute(
            onBackClick = {
                navController.popBackStack()
            },
            onSignInClick = {
                navController.navigateToSignIn()
            }
        )
    }
}