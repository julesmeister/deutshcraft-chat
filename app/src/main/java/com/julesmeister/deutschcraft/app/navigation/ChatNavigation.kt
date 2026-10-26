package com.julesmeister.deutschcraft.app.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.julesmeister.deutschcraft.feature.chat.presentation.ChatRoute

internal fun NavGraphBuilder.chatDestination(
    navController: NavHostController
) {
    composable<ChatDestination>(
        enterTransition = {
            forwardEnterTransition()
        },
        exitTransition = {
            secondaryScreenExitTransition()
        },
        popEnterTransition = {
            secondaryScreenEnterTransition()
        },
        popExitTransition = {
            backExitTransition()
        }
    ) {
        ChatRoute(
            onBackClick = {
                returnFromChat(
                    navController =
                        navController
                )
            },
            onProgressClick = {
                navController.navigate(
                    ProgressDestination
                ) {
                    launchSingleTop = true
                }
            }
        )
    }
}

private fun returnFromChat(
    navController: NavHostController
) {
    val returnedToPreviousDestination =
        navController.popBackStack()

    if (returnedToPreviousDestination) {
        return
    }

    navController.navigate(
        ThreadListDestination
    ) {
        launchSingleTop = true
    }
}