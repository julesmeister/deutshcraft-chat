package com.julesmeister.deutschcraft.feature.auth.presentation.verify

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julesmeister.deutschcraft.feature.auth.domain.AuthRepository
import com.julesmeister.deutschcraft.feature.auth.domain.AuthResult
import com.julesmeister.deutschcraft.feature.auth.presentation.AuthUiEvent
import com.julesmeister.deutschcraft.feature.auth.presentation.AuthValidation
import com.julesmeister.deutschcraft.feature.auth.presentation.toMessage
import com.julesmeister.deutschcraft.feature.auth.presentation.theme.AuthDesignTokens
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VerifyEmailUiState(
    val email: String = "",
    val code: String = "",
    val codeError: String? = null,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val isSubmitting: Boolean = false,
    val isResending: Boolean = false,
    val resendSecondsLeft: Int = 0
) {

    val isBusy: Boolean
        get() = isSubmitting || isResending

    val canResend: Boolean
        get() = !isBusy && resendSecondsLeft == 0
}

@HiltViewModel
class VerifyEmailViewModel
@Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val mutableUiState =
        MutableStateFlow(
            VerifyEmailUiState(
                email = savedStateHandle.get<String>(EmailArg).orEmpty()
            )
        )

    val uiState: StateFlow<VerifyEmailUiState> =
        mutableUiState.asStateFlow()

    private val eventChannel =
        Channel<AuthUiEvent>(Channel.BUFFERED)

    val events: Flow<AuthUiEvent> =
        eventChannel.receiveAsFlow()

    private var cooldownJob: Job? = null

    init {
        // A code was just sent by sign-up or sign-in.
        startCooldown()
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

        if (value.length == AuthDesignTokens.CodeLength) {
            onSubmit()
        }
    }

    fun onSubmit() {
        val state =
            mutableUiState.value

        if (state.isBusy) {
            return
        }

        val codeError =
            AuthValidation.codeError(state.code)

        if (codeError != null) {
            mutableUiState.update { current ->
                current.copy(codeError = codeError)
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
            val result =
                authRepository.confirmSignUp(
                    email = state.email,
                    code = state.code
                )

            mutableUiState.update { current ->
                current.copy(
                    isSubmitting = false,
                    errorMessage = (result as? AuthResult.Failure)?.error?.toMessage()
                )
            }

            if (result is AuthResult.Success) {
                eventChannel.send(
                    if (result.value) {
                        AuthUiEvent.Authenticated
                    } else {
                        AuthUiEvent.ReturnToSignIn(
                            email = state.email,
                            notice = "Email verified. Sign in to continue."
                        )
                    }
                )
            }
        }
    }

    fun onResend() {
        val state =
            mutableUiState.value

        if (!state.canResend) {
            return
        }

        mutableUiState.update { current ->
            current.copy(
                isResending = true,
                errorMessage = null,
                infoMessage = null
            )
        }

        viewModelScope.launch {
            val result =
                authRepository.resendConfirmationCode(state.email)

            mutableUiState.update { current ->
                current.copy(
                    isResending = false,
                    errorMessage = (result as? AuthResult.Failure)?.error?.toMessage(),
                    infoMessage =
                        if (result is AuthResult.Success) {
                            "We sent a new code to ${state.email}."
                        } else {
                            null
                        }
                )
            }

            if (result is AuthResult.Success) {
                startCooldown()
            }
        }
    }

    private fun startCooldown() {
        cooldownJob?.cancel()

        cooldownJob =
            viewModelScope.launch {
                for (seconds in AuthDesignTokens.ResendCooldownSeconds downTo 0) {
                    mutableUiState.update { current ->
                        current.copy(resendSecondsLeft = seconds)
                    }

                    if (seconds > 0) {
                        delay(OneSecondMillis)
                    }
                }
            }
    }

    private companion object {

        // Matches the property name on VerifyEmailDestination.
        const val EmailArg = "email"

        const val OneSecondMillis = 1_000L
    }
}
