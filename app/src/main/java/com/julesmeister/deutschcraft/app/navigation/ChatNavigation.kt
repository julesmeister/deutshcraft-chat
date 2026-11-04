// app/src/main/java/com/julesmeister/deutschcraft/app/navigation/ChatNavigation.kt

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
            onSignInClick = {
                navController.navigateToSignIn()
            }
        )
    }
}

private fun returnFromChat(
    navController: NavHostController
) {
    val returnedToThreadList =
        navController.popBackStack(
            route =
                ThreadListDestination,

            inclusive =
                false
        )

    if (returnedToThreadList) {
        return
    }

    navController.navigate(
        ThreadListDestination
    ) {
        popUpTo<ChatDestination> {
            inclusive =
                true
        }

        launchSingleTop =
            true
    }
}