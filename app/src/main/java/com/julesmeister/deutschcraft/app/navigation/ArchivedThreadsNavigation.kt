package com.julesmeister.deutschcraft.app.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.julesmeister.deutschcraft.feature.archivedthreads.presentation.ArchivedThreadsRoute

internal fun NavGraphBuilder.archivedThreadsDestination(
    navController: NavHostController
) {
    composable<ArchivedThreadsDestination>(
        enterTransition = {
            forwardEnterTransition()
        },
        exitTransition = {
            forwardExitTransition()
        },
        popEnterTransition = {
            backEnterTransition()
        },
        popExitTransition = {
            backExitTransition()
        }
    ) {
        ArchivedThreadsRoute(
            onBackClick = {
                navController.popBackStack()
            },
            onThreadClick = { conversationId ->
                navController.navigate(
                    ChatDestination(
                        conversationId =
                            conversationId
                    )
                ) {
                    launchSingleTop = true
                }
            }
        )
    }
}