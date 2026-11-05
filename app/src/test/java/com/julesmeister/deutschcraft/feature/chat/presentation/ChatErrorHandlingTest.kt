package com.julesmeister.deutschcraft.feature.chat.presentation

import com.julesmeister.deutschcraft.feature.chat.data.ai.AiGatewayException
import java.io.IOException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class ChatErrorHandlingTest {
    @Test
    fun wrappedHttpFailuresKeepTheirStatusAndCategory() {
        for (code in listOf(400, 401, 403, 404, 429, 500, 502, 503)) {
            val httpError = HttpException(Response.error<Unit>(code, "{}".toResponseBody()))
            val failure = AiGatewayException("Wrapped HTTP failure", httpError)
            val error = failure.toChatError()

            assertTrue(error.displayMessage.contains(code.toString()))
            if (code == 401 || code == 403) {
                assertTrue(error is ChatErrorType.AuthError)
            } else {
                assertTrue(error is ChatErrorType.NetworkError)
            }
            assertEquals(error, httpError.toChatError())
        }
    }

    @Test
    fun wrappedConnectionFailureShowsActionableMessageInsteadOfInternalDetails() {
        val error = AiGatewayException(
            "Wrapped connection failure",
            IOException("Internal host details")
        ).toChatError()

        assertTrue(error is ChatErrorType.NetworkError)
        assertTrue(error.displayMessage.contains("connection"))
        assertFalse(error.displayMessage.contains("Internal host"))
    }

    @Test
    fun gatewayConfigurationErrorsKeepTheirExplanation() {
        val failure = AiGatewayException("The AI proxy URL is missing.")
        assertEquals(ChatErrorType.GenericError(failure.message!!), failure.toChatError())
    }

    @Test
    fun failureSetsPersistentBarWithoutAlsoStartingAnAutoDismissedSnackbar() {
        val error = ChatErrorType.AuthError("Session expired (401). Please sign in again.")
        val state = ChatUiState(
            isSending = true,
            isAssistantTyping = true,
            isAssessing = true,
            isCheckingMissionOpportunity = true,
            errorMessage = "An older snackbar"
        ).sendingFailed(error)

        assertEquals(error, state.error)
        assertNull(state.errorMessage)
        assertFalse(state.isSending)
        assertFalse(state.isAssistantTyping)
        assertFalse(state.isAssessing)
        assertFalse(state.isCheckingMissionOpportunity)
        assertNull(state.errorDismissed().error)
    }
}
