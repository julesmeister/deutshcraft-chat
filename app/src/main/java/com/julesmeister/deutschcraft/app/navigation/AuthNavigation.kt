package com.julesmeister.deutschcraft.app.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.julesmeister.deutschcraft.feature.auth.presentation.AuthResultKeys
import com.julesmeister.deutschcraft.feature.auth.presentation.forgot.ForgotPasswordRoute
import com.julesmeister.deutschcraft.feature.auth.presentation.signin.SignInRoute
import com.julesmeister.deutschcraft.feature.auth.presentation.signup.SignUpRoute
import com.julesmeister.deutschcraft.feature.auth.presentation.verify.VerifyEmailRoute
import kotlinx.serialization.Serializable

@Serializable
data object SignInDestination

@Serializable
data object SignUpDestination

/*
 * Property names are read by the ViewModels through
 * SavedStateHandle ("email"); keep them in sync.
 */
@Serializable
data class VerifyEmailDestination(
    val email: String
)

@Serializable
data class ForgotPasswordDestination(
    val email: String = ""
)

internal fun NavController.navigateToSignIn() {
    navigate(SignInDestination) {
        launchSingleTop =
            true
    }
}

internal fun NavGraphBuilder.authDestinations(
    navController: NavHostController
) {
    composable<SignInDestination>(
        enterTransition = { forwardEnterTransition() },
        exitTransition = { secondaryScreenExitTransition() },
        popEnterTransition = { secondaryScreenEnterTransition() },
        popExitTransition = { backExitTransition() }
    ) {
        SignInRoute(
            onBackClick = {
                navController.popBackStack()
            },
            onAuthenticated = {
                closeAuthFlow(navController)
            },
            onCreateAccountClick = {
                navController.navigate(SignUpDestination) {
                    launchSingleTop =
                        true
                }
            },
            onForgotPasswordClick = { email ->
                navController.navigate(ForgotPasswordDestination(email))
            },
            onVerifyEmail = { email ->
                navController.navigate(VerifyEmailDestination(email))
            }
        )
    }

    composable<SignUpDestination>(
        enterTransition = { forwardEnterTransition() },
        exitTransition = { secondaryScreenExitTransition() },
        popEnterTransition = { secondaryScreenEnterTransition() },
        popExitTransition = { backExitTransition() }
    ) {
        SignUpRoute(
            onBackClick = {
                navController.popBackStack()
            },
            onAuthenticated = {
                closeAuthFlow(navController)
            },
            onSignInClick = {
                // Sign-up is normally opened from sign-in; go back instead of stacking.
                val returned =
                    navController.popBackStack<SignInDestination>(
                        inclusive = false
                    )

                if (!returned) {
                    navController.navigate(SignInDestination) {
                        popUpTo<SignUpDestination> {
                            inclusive =
                                true
                        }
                    }
                }
            },
            onVerifyEmail = { email ->
                navController.navigate(VerifyEmailDestination(email))
            }
        )
    }

    composable<VerifyEmailDestination>(
        enterTransition = { forwardEnterTransition() },
        exitTransition = { secondaryScreenExitTransition() },
        popEnterTransition = { secondaryScreenEnterTransition() },
        popExitTransition = { backExitTransition() }
    ) {
        VerifyEmailRoute(
            onBackClick = {
                navController.popBackStack()
            },
            onAuthenticated = {
                closeAuthFlow(navController)
            },
            onReturnToSignIn = { email, notice ->
                returnToSignIn(
                    navController = navController,
                    email = email,
                    notice = notice
                )
            }
        )
    }

    composable<ForgotPasswordDestination>(
        enterTransition = { forwardEnterTransition() },
        exitTransition = { secondaryScreenExitTransition() },
        popEnterTransition = { secondaryScreenEnterTransition() },
        popExitTransition = { backExitTransition() }
    ) {
        ForgotPasswordRoute(
            onBackClick = {
                navController.popBackStack()
            },
            onReturnToSignIn = { email, notice ->
                returnToSignIn(
                    navController = navController,
                    email = email,
                    notice = notice
                )
            }
        )
    }
}

/*
 * Removes every auth screen and lands the user back where
 * they asked to sign in (chat, settings, ...).
 */
private fun closeAuthFlow(
    navController: NavHostController
) {
    val currentRoute =
        navController.currentBackStackEntry ?: return

    val isAuthScreen =
        currentRoute.destination.route?.let { route ->
            AuthRoutePrefixes.any { prefix -> route.startsWith(prefix) }
        } ?: false

    if (!isAuthScreen) {
        return
    }

    val poppedSignIn =
        navController.popBackStack<SignInDestination>(
            inclusive = true
        )

    if (!poppedSignIn) {
        val poppedSignUp =
            navController.popBackStack<SignUpDestination>(
                inclusive = true
            )

        if (!poppedSignUp) {
            navController.popBackStack()
        }
    }
}

private fun returnToSignIn(
    navController: NavHostController,
    email: String,
    notice: String
) {
    val signInEntry =
        runCatching {
            navController.getBackStackEntry<SignInDestination>()
        }.getOrNull()

    if (signInEntry != null) {
        signInEntry.savedStateHandle[AuthResultKeys.Email] = email
        signInEntry.savedStateHandle[AuthResultKeys.Notice] = notice

        navController.popBackStack<SignInDestination>(
            inclusive = false
        )

        return
    }

    navController.navigate(SignInDestination) {
        popUpTo<SignUpDestination> {
            inclusive =
                true
        }

        launchSingleTop =
            true
    }

    navController.currentBackStackEntry?.savedStateHandle?.let { handle ->
        handle[AuthResultKeys.Email] = email
        handle[AuthResultKeys.Notice] = notice
    }
}

private val AuthRoutePrefixes =
    listOf(
        SignInDestination::class.qualifiedName.orEmpty(),
        SignUpDestination::class.qualifiedName.orEmpty(),
        VerifyEmailDestination::class.qualifiedName.orEmpty(),
        ForgotPasswordDestination::class.qualifiedName.orEmpty()
    )
