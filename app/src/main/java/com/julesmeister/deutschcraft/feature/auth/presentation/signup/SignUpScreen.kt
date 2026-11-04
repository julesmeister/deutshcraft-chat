package com.julesmeister.deutschcraft.feature.auth.presentation.signup

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.julesmeister.deutschcraft.feature.auth.presentation.AuthValidation
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthBanner
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthBannerTone
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthEventsEffect
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthFooterLink
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthFormCard
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthOrDivider
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthPrimaryButton
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthScaffold
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthTextField
import com.julesmeister.deutschcraft.feature.auth.presentation.component.GoogleSignInButton
import com.julesmeister.deutschcraft.feature.auth.presentation.signin.NotConfiguredMessage

@Composable
fun SignUpRoute(
    onBackClick: () -> Unit,
    onAuthenticated: () -> Unit,
    onSignInClick: () -> Unit,
    onVerifyEmail: (email: String) -> Unit,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AuthEventsEffect(
        events = viewModel.events,
        flowSupport = viewModel.flowSupport,
        onAuthenticated = onAuthenticated,
        onOpenVerifyEmail = onVerifyEmail
    )

    SignUpScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onNameChange = viewModel::onNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSubmit = viewModel::onSubmit,
        onGoogleClick = viewModel::onGoogleClick,
        onSignInClick = onSignInClick
    )
}

@Composable
internal fun SignUpScreen(
    uiState: SignUpUiState,
    onBackClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onGoogleClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    AuthScaffold(
        title = "Create your account",
        subtitle = "Save your progress and chat without limits.",
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
            message = uiState.errorMessage,
            tone = AuthBannerTone.Error
        )

        AuthFormCard {
            AuthTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                label = "Name (optional)",
                leadingIcon = Icons.Outlined.Person,
                capitalization = KeyboardCapitalization.Words,
                enabled = !uiState.isBusy
            )

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
                supportingText = AuthValidation.PasswordHint,
                enabled = !uiState.isBusy,
                onImeAction = onSubmit
            )

            AuthPrimaryButton(
                text = "Create account",
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
            prompt = "Already have an account?",
            action = "Sign in",
            onClick = onSignInClick,
            enabled = !uiState.isBusy
        )
    }
}
