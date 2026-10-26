package com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel

/*
 * Defines the communication behaviors DeutschCraft should
 * encourage at each CEFR level.
 *
 * This profile will eventually be supplied to:
 *
 * - Besser generation
 * - Chat reply generation
 * - Mission recommendations
 * - Assessment prompts
 * - Adaptive conversation steering
 */
data class CefrCommunicationProfile(
    val level: CefrLevel,
    val expectedSkills: Set<CommunicationSkill>,
    val coachingInstruction: String,
    val besserInstruction: String
)

object CefrCommunicationProfiles {

    fun forLevel(
        level: CefrLevel
    ): CefrCommunicationProfile {
        return when (level) {
            CefrLevel.A1 -> a1Profile
            CefrLevel.A2 -> a2Profile
            CefrLevel.B1 -> b1Profile
            CefrLevel.B2 -> b2Profile
            CefrLevel.C1 -> c1Profile
            CefrLevel.C2 -> c2Profile
        }
    }

    private val a1Profile =
        CefrCommunicationProfile(
            level = CefrLevel.A1,
            expectedSkills = setOf(
                CommunicationSkill.GREETING_AND_SOCIAL_OPENING,
                CommunicationSkill.ASKING_QUESTIONS,
                CommunicationSkill.POLITE_INTERACTION,
                CommunicationSkill.NATURAL_CONVERSATION_CLOSING
            ),
            coachingInstruction =
                """
                Encourage short, understandable German
                responses about familiar personal topics.

                Help the learner:
                - Greet people appropriately.
                - Ask and answer basic questions.
                - State simple needs and preferences.
                - Use short complete sentences.
                """.trimIndent(),
            besserInstruction =
                """
                Preserve simple A1 language.

                Correct errors without introducing grammar or
                vocabulary that is substantially above A1.

                Prefer one clear sentence the learner can reuse
                immediately.
                """.trimIndent()
        )

    private val a2Profile =
        CefrCommunicationProfile(
            level = CefrLevel.A2,
            expectedSkills = setOf(
                CommunicationSkill.ASKING_QUESTIONS,
                CommunicationSkill.GIVING_REASONS,
                CommunicationSkill.DEVELOPING_A_RESPONSE,
                CommunicationSkill.DESCRIBING_EXPERIENCES,
                CommunicationSkill.POLITE_INTERACTION
            ),
            coachingInstruction =
                """
                Encourage the learner to add details and connect
                familiar ideas.

                Help the learner:
                - Add when, where, or with whom.
                - Give a simple reason.
                - Describe routine experiences.
                - Ask relevant follow-up questions.
                """.trimIndent(),
            besserInstruction =
                """
                Correct the learner's message at A2 level.

                When appropriate, demonstrate how to add one
                useful detail or one simple reason without making
                the sentence unnaturally advanced.
                """.trimIndent()
        )

    private val b1Profile =
        CefrCommunicationProfile(
            level = CefrLevel.B1,
            expectedSkills = setOf(
                CommunicationSkill.GIVING_REASONS,
                CommunicationSkill.EXPRESSING_OPINIONS,
                CommunicationSkill.CONNECTING_IDEAS,
                CommunicationSkill.DESCRIBING_EXPERIENCES,
                CommunicationSkill.STORYTELLING,
                CommunicationSkill.DEVELOPING_A_RESPONSE,
                CommunicationSkill.USING_CONVERSATION_CONNECTORS
            ),
            coachingInstruction =
                """
                Encourage connected B1 communication rather than
                isolated short answers.

                Naturally invite the learner to:
                - Explain why.
                - Express and support an opinion.
                - Connect ideas using appropriate linking words.
                - Describe experiences with relevant details.
                - Tell events in a clear sequence.
                - Develop answers beyond one short sentence.
                """.trimIndent(),
            besserInstruction =
                """
                First correct the learner's meaning and grammar.

                Then express the same thought as natural B1
                German.

                The B1 version should:
                - Preserve the learner's original meaning.
                - Add a reason or relevant detail when appropriate.
                - Connect related ideas naturally.
                - Demonstrate reusable B1 sentence structure.
                - Avoid making the learner sound like an academic
                  or native C1 speaker.

                Do not invent personal facts the learner did not
                communicate.
                """.trimIndent()
        )

    private val b2Profile =
        CefrCommunicationProfile(
            level = CefrLevel.B2,
            expectedSkills = setOf(
                CommunicationSkill.GIVING_REASONS,
                CommunicationSkill.EXPRESSING_OPINIONS,
                CommunicationSkill.CONNECTING_IDEAS,
                CommunicationSkill.COMPARING_OPTIONS,
                CommunicationSkill.CLARIFYING_MEANING,
                CommunicationSkill.DEFENDING_AN_ARGUMENT,
                CommunicationSkill.USING_CONVERSATION_CONNECTORS
            ),
            coachingInstruction =
                """
                Encourage clear, detailed, and appropriately
                nuanced B2 communication.

                Invite the learner to:
                - Compare viewpoints.
                - Explain advantages and disadvantages.
                - Support opinions with examples.
                - Clarify ambiguous statements.
                - Respond to another perspective.
                """.trimIndent(),
            besserInstruction =
                """
                Correct the message and provide a natural B2
                expression of the same thought.

                The B2 version may use:
                - More precise vocabulary.
                - Clear comparison.
                - Nuanced connectors.
                - Supporting explanation.

                Preserve the learner's intended meaning and do not
                invent new personal information.
                """.trimIndent()
        )

    private val c1Profile =
        CefrCommunicationProfile(
            level = CefrLevel.C1,
            expectedSkills = setOf(
                CommunicationSkill.EXPRESSING_OPINIONS,
                CommunicationSkill.CONNECTING_IDEAS,
                CommunicationSkill.COMPARING_OPTIONS,
                CommunicationSkill.CLARIFYING_MEANING,
                CommunicationSkill.DEFENDING_AN_ARGUMENT,
                CommunicationSkill.USING_CONVERSATION_CONNECTORS
            ),
            coachingInstruction =
                """
                Encourage precise, flexible, and well-structured
                communication.

                Invite nuance, qualification, clear argument
                structure, and adaptation to the social context.
                """.trimIndent(),
            besserInstruction =
                """
                Preserve the learner's voice while demonstrating
                precise and idiomatic C1 German.

                Improve cohesion, register, nuance, and lexical
                precision without changing the learner's position.
                """.trimIndent()
        )

    private val c2Profile =
        CefrCommunicationProfile(
            level = CefrLevel.C2,
            expectedSkills =
                CommunicationSkill.entries.toSet(),
            coachingInstruction =
                """
                Encourage highly flexible, precise, nuanced, and
                context-sensitive communication.

                Focus on subtle differences in meaning, register,
                tone, and rhetorical effect.
                """.trimIndent(),
            besserInstruction =
                """
                Refine the learner's message for precision,
                naturalness, register, and stylistic control.

                Preserve the learner's meaning and personal voice.
                Avoid rewriting merely for complexity.
                """.trimIndent()
        )
}