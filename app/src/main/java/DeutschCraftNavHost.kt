package com.julesmeister.deutschcraft.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

@Composable
fun DeutschCraftNavHost() {
    val navController =
        rememberNavController()

    /*
     * Use the active Material theme instead of DeutschBlack.
     *
     * Dark mode:
     *     DeutschCraftDarkColorScheme.background
     *
     * Light mode:
     *     DeutschCraftLightColorScheme.background
     */
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme
                        .colorScheme
                        .background
                )
    ) {
        NavHost(
            navController =
                navController,
            startDestination =
                StartupDestination
        ) {
            startupDestination(
                navController =
                    navController
            )

            threadListDestination(
                navController =
                    navController
            )

            settingsDestination(
                navController =
                    navController
            )

            chatDestination(
                navController =
                    navController
            )

            progressDestination(
                navController =
                    navController
            )

            archivedThreadsDestination(
                navController =
                    navController
            )

            authDestinations(
                navController =
                    navController
            )
        }
    }
}