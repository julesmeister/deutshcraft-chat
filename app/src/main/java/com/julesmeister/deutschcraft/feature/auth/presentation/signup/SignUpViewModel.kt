package com.julesmeister.deutschcraft.feature.auth.presentation.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julesmeister.deutschcraft.feature.auth.domain.AuthRepository
import com.julesmeister.deutschcraft.feature.auth.domain.AuthResult
import com.julesmeister.deutschcraft.feature.auth.domain.GoogleSignInState
import com.julesmeister.deutschcraft.feature.auth.presentation.AuthFlowSupport
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

data class SignUpUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
    val isGoogleLoading: Boolean = false,
    val isConfigured: Boolean = true
) {

    val isBusy: Boolean
        get() = isSubmitting || isGoogleLoading
}

@HiltViewModel
class SignUpViewModel
@Inject constructor(
    private val authRepository: AuthRepository,
    private val support: AuthFlowSupport
) : ViewModel() {

    private val mutableUiState =
        MutableStateFlow(
            SignUpUiState(
                isConfigured = support.isConfigured
            )
        )

    val uiState: StateFlow<SignUpUiState> =
        mutableUiState.asStateFlow()

    private val eventChannel =
        Channel<AuthUiEvent>(Channel.BUFFERED)

    val events: Flow<AuthUiEvent> =
        eventChannel.receiveAsFlow()

    val flowSupport: AuthFlowSupport
        get() = support

    init {
        support.clearGoogleError()

        viewModelScope.launch {
            support.awaitSignedIn()
            eventChannel.send(AuthUiEvent.Authenticated)
        }

        viewModelScope.launch {
            support.googleSignInState.collect { state ->
                mutableUiState.update { current ->
                    current.copy(
                        isGoogleLoading = state is GoogleSignInState.Exchanging,
                        errorMessage =
                            (state as? GoogleSignInState.Failed)
                                ?.error
                                ?.toMessage()
                                ?: current.errorMessage
                    )
                }
            }
        }
    }

    fun onNameChange(
        value: String
    ) {
        mutableUiState.update { current ->
            current.copy(
                name = value,
                errorMessage = null
            )
        }
    }

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

    fun onPasswordChange(
        value: String
    ) {
        mutableUiState.update { current ->
            current.copy(
                password = value,
                passwordError = null,
                errorMessage = null
            )
        }
    }

    fun onSubmit() {
        val state =
            mutableUiState.value

        if (state.isBusy) {
            return
        }

        val emailError =
            AuthValidation.emailError(state.email)

        val passwordError =
            AuthValidation.passwordError(state.password)

        if (emailError != null || passwordError != null) {
            mutableUiState.update { current ->
                current.copy(
                    emailError = emailError,
                    passwordError = passwordError
                )
            }

            return
        }

        support.clearGoogleError()

        mutableUiState.update { current ->
            current.copy(
                isSubmitting = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val email =
                state.email.trim()

            val result =
                authRepository.signUp(
                    email = email,
                    password = state.password,
                    name = state.name.trim().ifEmpty { null }
                )

            mutableUiState.update { current ->
                current.copy(
                    isSubmitting = false,
                    errorMessage = (result as? AuthResult.Failure)?.error?.toMessage()
                )
            }

            if (result is AuthResult.Success && result.value.needsConfirmation) {
                eventChannel.send(AuthUiEvent.OpenVerifyEmail(email))
            }

            // An auto-confirmed user is signed in already; awaitSignedIn closes the flow.
        }
    }

    fun onGoogleClick() {
        if (mutableUiState.value.isBusy) {
            return
        }

        support.clearGoogleError()

        mutableUiState.update { current ->
            current.copy(errorMessage = null)
        }

        viewModelScope.launch {
            when (val result = support.prepareGoogleSignIn()) {
                is AuthResult.Success ->
                    eventChannel.send(AuthUiEvent.LaunchGoogle(result.value))

                is AuthResult.Failure ->
                    mutableUiState.update { current ->
                        current.copy(errorMessage = result.error.toMessage())
                    }
            }
        }
    }
}
