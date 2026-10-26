package com.julesmeister.deutschcraft.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.julesmeister.deutschcraft.ui.theme.DeutschBlack

@Composable
fun DeutschCraftNavHost() {
    val navController =
        rememberNavController()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                DeutschBlack
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
}