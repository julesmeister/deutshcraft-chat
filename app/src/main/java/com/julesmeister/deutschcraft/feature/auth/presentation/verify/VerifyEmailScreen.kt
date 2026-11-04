package com.julesmeister.deutschcraft.feature.auth.presentation.verify

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthBanner
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthBannerTone
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthCodeField
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthEventsEffect
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthFormCard
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthPrimaryButton
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthScaffold
import com.julesmeister.deutschcraft.feature.auth.presentation.component.AuthTextAction

@Composable
fun VerifyEmailRoute(
    onBackClick: () -> Unit,
    onAuthenticated: () -> Unit,
    onReturnToSignIn: (email: String, notice: String) -> Unit,
    viewModel: VerifyEmailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AuthEventsEffect(
        events = viewModel.events,
        flowSupport = null,
        onAuthenticated = onAuthenticated,
        onReturnToSignIn = onReturnToSignIn
    )

    VerifyEmailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onCodeChange = viewModel::onCodeChange,
        onSubmit = viewModel::onSubmit,
        onResend = viewModel::onResend
    )
}

@Composable
internal fun VerifyEmailScreen(
    uiState: VerifyEmailUiState,
    onBackClick: () -> Unit,
    onCodeChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onResend: () -> Unit
) {
    AuthScaffold(
        title = "Check your email",
        subtitle = "Enter the 6-digit code we sent to ${uiState.email.ifBlank { "your email" }}.",
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
            AuthCodeField(
                value = uiState.code,
                onValueChange = onCodeChange,
                enabled = !uiState.isSubmitting,
                errorText = uiState.codeError,
                onDone = onSubmit
            )

            AuthPrimaryButton(
                text = "Verify",
                onClick = onSubmit,
                enabled = !uiState.isBusy,
                isLoading = uiState.isSubmitting
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.resendSecondsLeft > 0) {
                Text(
                    text = "Resend code in ${uiState.resendSecondsLeft}s",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            } else {
                AuthTextAction(
                    text = "Resend code",
                    onClick = onResend,
                    enabled = uiState.canResend
                )
            }
        }
    }
}
