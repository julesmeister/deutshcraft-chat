package com.julesmeister.deutschcraft.feature.chat.domain.usecase

import android.util.Log
import com.julesmeister.deutschcraft.feature.chat.data.correction.MessageCorrectionGateway
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.MessageCorrection
import com.julesmeister.deutschcraft.feature.chat.domain.MessageSender
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class CorrectLearnerMessageUseCase @Inject constructor(
    private val correctionGateway:
    MessageCorrectionGateway
) {

    suspend operator fun invoke(
        message: ChatMessage
    ): MessageCorrection? {
        if (
            message.sender != MessageSender.USER ||
            message.content.isBlank()
        ) {
            return null
        }

        return try {
            val correctedSentence =
                correctionGateway.correctSentence(
                    sentence = message.content
                ) ?: return null

            MessageCorrection(
                messageId = message.id,
                correctedSentence =
                    correctedSentence
            )
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            /*
             * Correction is supplementary.
             *
             * A failed correction request must not prevent
             * the message or assistant response from being
             * delivered.
             */
            Log.e(
                CORRECTION_TAG,
                "Learner-message correction failed.",
                throwable
            )

            null
        }
    }

    private companion object {
        const val CORRECTION_TAG =
            "CorrectLearnerMessage"
    }
}