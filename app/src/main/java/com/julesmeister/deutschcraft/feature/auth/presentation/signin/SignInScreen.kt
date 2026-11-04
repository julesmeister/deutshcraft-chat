package com.julesmeister.deutschcraft.feature.auth.presentation.signin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthBanner
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthBannerTone
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthEventsEffect
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthFooterLink
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthFormCard
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthOrDivider
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthPrimaryButton
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthScaffold
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthTextAction
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthTextField
import com.julesmeister.deutschcraft.feature.auth.presentation.component.GoogleSignInButton

@Composable
fun SignInRoute(
    onBackClick: () -> Unit,
    onAuthenticated: () -> Unit,
    onCreateAccountClick: () -> Unit,
    onForgotPasswordClick: (email: String) -> Unit,
    onVerifyEmail: (email: String) -> Unit,
    viewModel: SignInViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AuthEventsEffect(
        events = viewModel.events,
        flowSupport = viewModel.flowSupport,
        onAuthenticated = onAuthenticated,
        onOpenVerifyEmail = onVerifyEmail
    )

    SignInScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSubmit = viewModel::onSubmit,
        onGoogleClick = viewModel::onGoogleClick,
        onCreateAccountClick = onCreateAccountClick,
        onForgotPasswordClick = {
            onForgotPasswordClick(uiState.email.trim())
        }
    )
}

@Composable
internal fun SignInScreen(
    uiState: SignInUiState,
    onBackClick: () -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onGoogleClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    AuthScaffold(
        title = "Welcome back",
        subtitle = "Sign in to keep every conversation going.",
        onBackClick = onBackClick
    ) {
        AuthBanner(
            message =
                if (uiState.isConfigured) {
                    null
                } else {
                    NotConfiguredMessage
                },
            tone = AuthBannerTone.Info
        )

        AuthBanner(
            message = uiState.noticeMessage,
            tone = AuthBannerTone.Info
        )

        AuthBanner(
            message = uiState.errorMessage,
            tone = AuthBannerTone.Error
        )

        AuthFormCard {
            AuthTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = "Email",
                leadingIcon = Icons.Outlined.Email,
                keyboardType = KeyboardType.Email,
                errorText = uiState.emailError,
                enabled = !uiState.isBusy
            )

            AuthTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = "Password",
                leadingIcon = Icons.Outlined.Lock,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                isPassword = true,
                errorText = uiState.passwordError,
                enabled = !uiState.isBusy,
                onImeAction = onSubmit
            )

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                AuthTextAction(
                    text = "Forgot password?",
                    onClick = onForgotPasswordClick,
                    enabled = !uiState.isBusy
                )
            }

            AuthPrimaryButton(
                text = "Sign in",
                onClick = onSubmit,
                enabled = !uiState.isBusy,
                isLoading = uiState.isSubmitting
            )
        }

        AuthOrDivider()

        GoogleSignInButton(
            onClick = onGoogleClick,
            enabled = !uiState.isBusy,
            isLoading = uiState.isGoogleLoading
        )

        AuthFooterLink(
            prompt = "New to DeutschCraft?",
            action = "Create account",
            onClick = onCreateAccountClick,
            enabled = !uiState.isBusy
        )
    }
}

internal const val NotConfiguredMessage =
    "Sign-in isn't configured on this build. Add your Cognito settings to local.properties."
