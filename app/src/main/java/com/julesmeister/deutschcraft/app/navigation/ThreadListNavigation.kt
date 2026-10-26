package com.julesmeister.deutschcraft.app.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.julesmeister.deutschcraft.feature.threads.presentation.ThreadListRoute

internal fun NavGraphBuilder.threadListDestination(
    navController: NavHostController
) {
    composable<ThreadListDestination>(
        enterTransition = {
            backEnterTransition()
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
        ThreadListRoute(
            onThreadClick = { conversationId ->
                navController.navigate(
                    ChatDestination(
                        conversationId =
                            conversationId
                    )
                )
            },
            onArchivedThreadsClick = {
                navController.navigate(
                    ArchivedThreadsDestination
                )
            },
            onProgressClick = {
                navController.navigate(
                    ProgressDestination
                )
            },
            onSettingsClick = {
                /*
                 * Settings navigation has not been added to the
                 * navigation graph yet.
                 *
                 * Keep this callback empty until
                 * SettingsDestination is implemented.
                 */
            }
        )
    }
}