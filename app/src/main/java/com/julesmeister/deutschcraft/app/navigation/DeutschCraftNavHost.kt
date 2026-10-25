package com.julesmeister.deutschcraft.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.julesmeister.deutschcraft.app.navigati.StartupRoute
import com.julesmeister.deutschcraft.feature.archivedthreads.presentation.ArchivedThreadsRoute
import com.julesmeister.deutschcraft.feature.chat.presentation.ChatRoute
import com.julesmeister.deutschcraft.feature.progress.presentation.ProgressRoute
import com.julesmeister.deutschcraft.feature.threads.presentation.ThreadListRoute

@Composable
fun DeutschCraftNavHost() {
    val navController =
        rememberNavController()

    NavHost(
        navController = navController,
        startDestination = StartupDestination
    ) {
        composable<StartupDestination> {
            StartupRoute(
                onOpenChat = { conversationId ->
                    navController.navigate(
                        ChatDestination(
                            conversationId = conversationId
                        )
                    ) {
                        popUpTo(
                            StartupDestination
                        ) {
                            inclusive = true
                        }
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
                    }
                }
            )
        }

        composable<ThreadListDestination> {
            ThreadListRoute(
                onThreadClick = { conversationId ->
                    navController.navigate(
                        ChatDestination(
                            conversationId = conversationId
                        )
                    )
                },
                onArchivedThreadsClick = {
                    navController.navigate(
                        ArchivedThreadsDestination
                    )
                }
            )
        }

        composable<ChatDestination> {
            ChatRoute(
                onBackClick = {
                    val returnedToPreviousDestination =
                        navController.popBackStack()

                    if (!returnedToPreviousDestination) {
                        navController.navigate(
                            ThreadListDestination
                        ) {
                            launchSingleTop = true
                        }
                    }
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

        composable<ArchivedThreadsDestination> {
            ArchivedThreadsRoute(
                onBackClick = {
                    navController.popBackStack()
                },
                onThreadClick = { conversationId ->
                    navController.navigate(
                        ChatDestination(
                            conversationId = conversationId
                        )
                    )
                }
            )
        }
    }
}