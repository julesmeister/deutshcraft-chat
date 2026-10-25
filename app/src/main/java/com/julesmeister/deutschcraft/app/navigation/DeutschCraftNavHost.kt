package com.julesmeister.deutschcraft.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.julesmeister.deutschcraft.app.navigati.StartupRoute
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
            StartupDestination
    ) {
        composable<StartupDestination> {
            StartupRoute(
                onOpenChat = {
                        conversationId ->

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
                onThreadClick = {
                        conversationId: String ->

                    navController.navigate(
                        ChatDestination(
                            conversationId =
                                conversationId
                        )
                    )
                },
                onNewThreadClick = {
                    /*
                     * Temporary behavior.
                     *
                     * The next step will create a proper
                     * daily conversation before navigating.
                     */
                    navController.navigate(
                        ChatDestination(
                            conversationId =
                                PRIMARY_CONVERSATION_ID
                        )
                    )
                }
            )
        }

        composable<ChatDestination> {
            ChatRoute(
                onBackClick = {
                    /*
                     * If Chat was opened directly during
                     * startup, Thread List might not be
                     * present in the back stack.
                     */
                    val returned =
                        navController.popBackStack()

                    if (!returned) {
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
    }
}

private const val PRIMARY_CONVERSATION_ID =
    "primary-conversation"