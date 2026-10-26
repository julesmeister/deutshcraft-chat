package com.julesmeister.deutschcraft.app.navigation

import androidx.compose.animation.EnterTransition
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.julesmeister.deutschcraft.feature.progress.presentation.ProgressRoute

internal fun NavGraphBuilder.progressDestination(
    navController: NavHostController
) {
    composable<ProgressDestination>(
        enterTransition = {
            progressEnterTransition()
        },
        exitTransition = {
            progressExitTransition()
        },
        popEnterTransition = {
            EnterTransition.None
        },
        popExitTransition = {
            progressExitTransition()
        }
    ) {
        ProgressRoute(
            onBackClick = {
                navController.popBackStack()
            }
        )
    }
}