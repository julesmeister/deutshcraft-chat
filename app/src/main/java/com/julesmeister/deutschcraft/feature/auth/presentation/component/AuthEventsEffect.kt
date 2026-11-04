package com.julesmeister.deutschcraft.feature.auth.presentation.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.julesmeister.deutschcraft.feature.auth.presentation.AuthFlowSupport
import com.julesmeister.deutschcraft.feature.auth.presentation.AuthUiEvent
import kotlinx.coroutines.flow.Flow

/*
 * Turns one-shot auth events into navigation / Custom Tab
 * launches. Collected only while the screen is started so a
 * pop never happens behind another destination.
 */
@Composable
internal fun AuthEventsEffect(
    events: Flow<AuthUiEvent>,
    flowSupport: AuthFlowSupport?,
    onAuthenticated: () -> Unit,
    onOpenVerifyEmail: (String) -> Unit = {},
    onReturnToSignIn: (email: String, notice: String) -> Unit = { _, _ -> }
) {
    val context =
        LocalContext.current

    val lifecycleOwner =
        LocalLifecycleOwner.current

    val toolbarColor =
        MaterialTheme.colorScheme.background.toArgb()

    val currentOnAuthenticated by rememberUpdatedState(onAuthenticated)
    val currentOnOpenVerifyEmail by rememberUpdatedState(onOpenVerifyEmail)
    val currentOnReturnToSignIn by rememberUpdatedState(onReturnToSignIn)
    val currentToolbarColor by rememberUpdatedState(toolbarColor)

    LaunchedEffect(events, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            events.collect { event ->
                when (event) {
                    AuthUiEvent.Authenticated ->
                        currentOnAuthenticated()

                    is AuthUiEvent.LaunchGoogle ->
                        flowSupport?.launchGoogleSignIn(
                            context = context,
                            uri = event.uri,
                            toolbarColor = currentToolbarColor
                        )

                    is AuthUiEvent.OpenVerifyEmail ->
                        currentOnOpenVerifyEmail(event.email)

                    is AuthUiEvent.ReturnToSignIn ->
                        currentOnReturnToSignIn(event.email, event.notice)
                }
            }
        }
    }
}
