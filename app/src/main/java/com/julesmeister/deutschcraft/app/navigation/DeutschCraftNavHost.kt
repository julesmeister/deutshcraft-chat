package com.julesmeister.deutschcraft.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

@Composable
fun DeutschCraftNavHost() {
    val navController =
        rememberNavController()

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
    }
}
