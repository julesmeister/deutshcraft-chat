package com.julesmeister.deutschcraft.feature.auth.presentation.forgot

import androidx.activity.compose.BackHandler
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
import com.julesmeister.deutschcraft.feature.auth.presentation.AuthValidation
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthBanner
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthBannerTone
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthCodeField
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthEventsEffect
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthFormCard
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthPrimaryButton
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthScaffold
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthTextAction
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthTextField

@Composable
fun ForgotPasswordRoute(
    onBackClick: () -> Unit,
    onReturnToSignIn: (email: String, notice: String) -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AuthEventsEffect(
        events = viewModel.events,
        flowSupport = null,
        onAuthenticated = {},
        onReturnToSignIn = onReturnToSignIn
    )

    val handleBack = {
        if (!viewModel.onBack()) {
            onBackClick()
        }
    }

    BackHandler(
        enabled = uiState.step == ForgotPasswordStep.ResetPassword,
        onBack = handleBack
    )

    ForgotPasswordScreen(
        uiState = uiState,
        onBackClick = handleBack,
        onEmailChange = viewModel::onEmailChange,
        onCodeChange = viewModel::onCodeChange,
        onNewPasswordChange = viewModel::onNewPasswordChange,
        onRequestCode = viewModel::onRequestCode,
        onResetPassword = viewModel::onResetPassword
    )
}

@Composable
internal fun ForgotPasswordScreen(
    uiState: ForgotPasswordUiState,
    onBackClick: () -> Unit,
    onEmailChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onRequestCode: () -> Unit,
    onResetPassword: () -> Unit
) {
    val isRequestStep =
        uiState.step == ForgotPasswordStep.RequestCode

    AuthScaffold(
        title =
            if (isRequestStep) {
                "Reset your password"
            } else {
                "Choose a new password"
            },
        subtitle =
            if (isRequestStep) {
                "We'll email you a code to reset it."
            } else {
                "Enter the code from your email and a new password."
            },
        onBackClick = onBackClick
    ) {
        AuthBanner(
            message = uiState.infoMessage,
            tone = AuthBannerTone.Info
        )

        AuthBanner(
            message = uiState.errorMessage,
            tone = AuthBannerTone.Error
        )

        AuthFormCard {
            if (isRequestStep) {
                AuthTextField(
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    label = "Email",
                    leadingIcon = Icons.Outlined.Email,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done,
                    errorText = uiState.emailError,
                    enabled = !uiState.isSubmitting,
                    onImeAction = onRequestCode
                )

                AuthPrimaryButton(
                    text = "Send code",
                    onClick = onRequestCode,
                    enabled = !uiState.isSubmitting,
                    isLoading = uiState.isSubmitting
                )
            } else {
                AuthCodeField(
                    value = uiState.code,
                    onValueChange = onCodeChange,
                    enabled = !uiState.isSubmitting,
                    errorText = uiState.codeError
                )

                AuthTextField(
                    value = uiState.newPassword,
                    onValueChange = onNewPasswordChange,
                    label = "New password",
                    leadingIcon = Icons.Outlined.Lock,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    isPassword = true,
                    errorText = uiState.passwordError,
                    supportingText = AuthValidation.PasswordHint,
                    enabled = !uiState.isSubmitting,
                    onImeAction = onResetPassword
                )

                AuthPrimaryButton(
                    text = "Update password",
                    onClick = onResetPassword,
                    enabled = !uiState.isSubmitting,
                    isLoading = uiState.isSubmitting
                )
            }
        }

        if (!isRequestStep) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AuthTextAction(
                    text = "Use a different email",
                    onClick = onBackClick,
                    enabled = !uiState.isSubmitting
                )
            }
        }
    }
}
