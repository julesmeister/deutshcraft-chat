package com.julesmeister.deutschcraft.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.julesmeister.deutschcraft.feature.chat.presentation.ChatRoute
import com.julesmeister.deutschcraft.feature.progress.presentation.ProgressRoute
import com.julesmeister.deutschcraft.feature.threads.presentation.ThreadListRoute

@Composable
fun DeutschCraftNavHost() {
    val navController =
        rememberNavController()

    NavHost(
        navController = navController,
        startDestination =
            ThreadListDestination
    ) {
        composable<ThreadListDestination> {
            ThreadListRoute(
                onThreadClick = {
                        conversationId: String ->

                    navController.navigate(
                        ChatDestination(
                            conversationId =
                                conversationId
                        )
                    )
                }
            )
        }

        composable<ChatDestination> {
            ChatRoute(
                onBackClick = {
                    navController.popBackStack()
                },
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