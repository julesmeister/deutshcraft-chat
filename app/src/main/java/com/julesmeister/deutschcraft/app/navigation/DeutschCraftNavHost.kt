package com.julesmeister.deutschcraft.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.julesmeister.deutschcraft.feature.chat.presentation.ChatRoute
import com.julesmeister.deutschcraft.feature.progress.presentation.ProgressRoute

@Composable
fun DeutschCraftNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ChatDestination
    ) {
        composable<ChatDestination> {
            ChatRoute(
                onProgressClick = {
                    navController.navigate(
                        ProgressDestination
                    )
                }
            )
        }

        composable<ProgressDestination> {
            ProgressRoute(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}