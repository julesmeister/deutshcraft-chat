package com.julesmeister.deutschcraft.feature.auth.presentation.forgot

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julesmeister.deutschcraft.feature.auth.domain.AuthRepository
import com.julesmeister.deutschcraft.feature.auth.domain.AuthResult
import com.julesmeister.deutschcraft.feature.auth.presentation.AuthUiEvent
import com.julesmeister.deutschcraft.feature.auth.presentation.AuthValidation
import com.julesmeister.deutschcraft.feature.auth.presentation.toMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ForgotPasswordStep {
    RequestCode,
    ResetPassword
}

data class ForgotPasswordUiState(
    val step: ForgotPasswordStep = ForgotPasswordStep.RequestCode,
    val email: String = "",
    val code: String = "",
    val newPassword: String = "",
    val emailError: String? = null,
    val codeError: String? = null,
    val passwordError: String? = null,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val isSubmitting: Boolean = false
)

@HiltViewModel
class ForgotPasswordViewModel
@Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val mutableUiState =
        MutableStateFlow(
            ForgotPasswordUiState(
                email = savedStateHandle.get<String>(EmailArg).orEmpty()
            )
        )

    val uiState: StateFlow<ForgotPasswordUiState> =
        mutableUiState.asStateFlow()

    private val eventChannel =
        Channel<AuthUiEvent>(Channel.BUFFERED)

    val events: Flow<AuthUiEvent> =
        eventChannel.receiveAsFlow()

    fun onEmailChange(
        value: String
    ) {
        mutableUiState.update { current ->
            current.copy(
                email = value,
                emailError = null,
                errorMessage = null
            )
        }
    }

    fun onCodeChange(
        value: String
    ) {
        mutableUiState.update { current ->
            current.copy(
                code = value,
                codeError = null,
                errorMessage = null
            )
        }
    }

    fun onNewPasswordChange(
        value: String
    ) {
        mutableUiState.update { current ->
            current.copy(
                newPassword = value,
                passwordError = null,
                errorMessage = null
            )
        }
    }

    fun onBack(): Boolean {
        if (mutableUiState.value.step == ForgotPasswordStep.ResetPassword &&
            !mutableUiState.value.isSubmitting
        ) {
            mutableUiState.update { current ->
                current.copy(
                    step = ForgotPasswordStep.RequestCode,
                    code = "",
                    newPassword = "",
                    codeError = null,
                    passwordError = null,
                    errorMessage = null,
                    infoMessage = null
                )
            }

            return true
        }

        return false
    }

    fun onRequestCode() {
        val state =
            mutableUiState.value

        if (state.isSubmitting) {
            return
        }

        val emailError =
            AuthValidation.emailError(state.email)

        if (emailError != null) {
            mutableUiState.update { current ->
                current.copy(emailError = emailError)
            }

            return
        }

        mutableUiState.update { current ->
            current.copy(
                isSubmitting = true,
                errorMessage = null,
                infoMessage = null
            )
        }

        viewModelScope.launch {
            val email =
                state.email.trim()

            when (val result = authRepository.requestPasswordReset(email)) {
                is AuthResult.Success ->
                    mutableUiState.update { current ->
                        current.copy(
                            isSubmitting = false,
                            email = email,
                            step = ForgotPasswordStep.ResetPassword,
                            infoMessage = "We sent a reset code to ${result.value ?: email}."
                        )
                    }

                is AuthResult.Failure ->
                    mutableUiState.update { current ->
                        current.copy(
                            isSubmitting = false,
                            errorMessage = result.error.toMessage()
                        )
                    }
            }
        }
    }

    fun onResetPassword() {
        val state =
            mutableUiState.value

        if (state.isSubmitting) {
            return
        }

        val codeError =
            AuthValidation.codeError(state.code)

        val passwordError =
            AuthValidation.passwordError(state.newPassword)

        if (codeError != null || passwordError != null) {
            mutableUiState.update { current ->
                current.copy(
                    codeError = codeError,
                    passwordError = passwordError
                )
            }

            return
        }

        mutableUiState.update { current ->
            current.copy(
                isSubmitting = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val result =
                authRepository.confirmPasswordReset(
                    email = state.email,
                    code = state.code,
                    newPassword = state.newPassword
                )

            mutableUiState.update { current ->
                current.copy(
                    isSubmitting = false,
                    errorMessage = (result as? AuthResult.Failure)?.error?.toMessage()
                )
            }

            if (result is AuthResult.Success) {
                eventChannel.send(
                    AuthUiEvent.ReturnToSignIn(
                        email = state.email,
                        notice = "Password updated. Sign in with your new password."
                    )
                )
            }
        }
    }

    private companion object {

        // Matches the property name on ForgotPasswordDestination.
        const val EmailArg = "email"
    }
}
