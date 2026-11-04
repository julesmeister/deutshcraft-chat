package com.julesmeister.deutschcraft.feature.auth.presentation.signin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julesmeister.deutschcraft.feature.auth.domain.AuthError
import com.julesmeister.deutschcraft.feature.auth.domain.AuthRepository
import com.julesmeister.deutschcraft.feature.auth.domain.AuthResult
import com.julesmeister.deutschcraft.feature.auth.domain.GoogleSignInState
import com.julesmeister.deutschcraft.feature.auth.presentation.AuthFlowSupport
import com.julesmeister.deutschcraft.feature.auth.presentation.AuthResultKeys
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

data class SignInUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val errorMessage: String? = null,
    val noticeMessage: String? = null,
    val isSubmitting: Boolean = false,
    val isGoogleLoading: Boolean = false,
    val isConfigured: Boolean = true
) {

    val isBusy: Boolean
        get() = isSubmitting || isGoogleLoading
}

@HiltViewModel
class SignInViewModel
@Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val support: AuthFlowSupport
) : ViewModel() {

    private val mutableUiState =
        MutableStateFlow(
            SignInUiState(
                isConfigured = support.isConfigured
            )
        )

    val uiState: StateFlow<SignInUiState> =
        mutableUiState.asStateFlow()

    private val eventChannel =
        Channel<AuthUiEvent>(Channel.BUFFERED)

    val events: Flow<AuthUiEvent> =
        eventChannel.receiveAsFlow()

    val flowSupport: AuthFlowSupport
        get() = support

    init {
        // Drop a stale Google error from an earlier attempt.
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

        viewModelScope.launch {
            savedStateHandle
                .getStateFlow<String?>(AuthResultKeys.Notice, null)
                .collect { notice ->
                    if (notice == null) {
                        return@collect
                    }

                    val email =
                        savedStateHandle.get<String>(AuthResultKeys.Email)

                    mutableUiState.update { current ->
                        current.copy(
                            noticeMessage = notice,
                            errorMessage = null,
                            email = email ?: current.email,
                            password = ""
                        )
                    }

                    savedStateHandle[AuthResultKeys.Notice] = null
                }
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
            if (state.password.isEmpty()) "Enter your password." else null

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
                errorMessage = null,
                noticeMessage = null
            )
        }

        viewModelScope.launch {
            val result =
                authRepository.signIn(
                    email = state.email,
                    password = state.password
                )

            if (result is AuthResult.Failure && result.error == AuthError.UserNotConfirmed) {
                // Make sure a fresh code is on its way, then verify.
                authRepository.resendConfirmationCode(state.email)

                mutableUiState.update { current ->
                    current.copy(isSubmitting = false)
                }

                eventChannel.send(
                    AuthUiEvent.OpenVerifyEmail(
                        email = state.email.trim()
                    )
                )

                return@launch
            }

            mutableUiState.update { current ->
                current.copy(
                    isSubmitting = false,
                    errorMessage = (result as? AuthResult.Failure)?.error?.toMessage()
                )
            }
        }
    }

    fun onGoogleClick() {
        if (mutableUiState.value.isBusy) {
            return
        }

        support.clearGoogleError()

        mutableUiState.update { current ->
            current.copy(
                errorMessage = null,
                noticeMessage = null
            )
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
