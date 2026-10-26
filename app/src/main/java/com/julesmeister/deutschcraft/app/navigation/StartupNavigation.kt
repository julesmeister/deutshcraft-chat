package com.julesmeister.deutschcraft.app.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.julesmeister.deutschcraft.app.navigati.StartupRoute

internal fun NavGraphBuilder.startupDestination(
    navController: NavHostController
) {
    composable<StartupDestination>(
        enterTransition = {
            startupEnterTransition()
        },
        exitTransition = {
            startupExitTransition()
        },
        popEnterTransition = {
            startupEnterTransition()
        },
        popExitTransition = {
            startupExitTransition()
        }
    ) {
        StartupRoute(
            onOpenChat = { conversationId ->
                navController.navigate(
                    ChatDestination(
                        conversationId =
                            conversationId
                    )
                ) {
                    popUpTo(
                        StartupDestination
                    ) {
                        inclusive = true
                    }

                    launchSingleTop = true
                }
            },
            onOpenThreadList = {
                navController.navigate(
                    ThreadListDestination
                ) {
                    popUpTo(
                        StartupDestination
                    ) {
                        inclusive = true
                    }

                    launchSingleTop = true
                }
            }
        )
    }
}