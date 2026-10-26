package com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model

/*
 * Learner communication behaviors that DeutschCraft can
 * observe and reinforce through normal conversation.
 *
 * These are not traditional grammar exercises. Emil creates
 * conversational opportunities requiring the learner to
 * demonstrate these behaviors naturally.
 */
enum class CommunicationSkill(
    val displayName: String,
    val learnerGuidance: String
) {

    GREETING_AND_SOCIAL_OPENING(
        displayName =
            "Greeting and social opening",
        learnerGuidance =
            "Open the conversation naturally and acknowledge " +
                    "the other person."
    ),

    ASKING_QUESTIONS(
        displayName =
            "Asking questions",
        learnerGuidance =
            "Ask a relevant question to keep the conversation " +
                    "moving."
    ),

    GIVING_REASONS(
        displayName =
            "Giving reasons",
        learnerGuidance =
            "Explain why you think, prefer, or chose something."
    ),

    EXPRESSING_OPINIONS(
        displayName =
            "Expressing opinions",
        learnerGuidance =
            "State your view and add supporting information."
    ),

    CONNECTING_IDEAS(
        displayName =
            "Connecting ideas",
        learnerGuidance =
            "Connect related thoughts instead of using only " +
                    "separate short sentences."
    ),

    DESCRIBING_EXPERIENCES(
        displayName =
            "Describing experiences",
        learnerGuidance =
            "Describe what happened and include relevant details."
    ),

    STORYTELLING(
        displayName =
            "Storytelling",
        learnerGuidance =
            "Present events in a clear and understandable order."
    ),

    COMPARING_OPTIONS(
        displayName =
            "Comparing options",
        learnerGuidance =
            "Explain similarities, differences, advantages, or " +
                    "disadvantages."
    ),

    CLARIFYING_MEANING(
        displayName =
            "Clarifying meaning",
        learnerGuidance =
            "Rephrase or add an example when an idea may be " +
                    "unclear."
    ),

    DEFENDING_AN_ARGUMENT(
        displayName =
            "Defending an argument",
        learnerGuidance =
            "Support your position with reasons and respond to " +
                    "another viewpoint."
    ),

    DEVELOPING_A_RESPONSE(
        displayName =
            "Developing a response",
        learnerGuidance =
            "Add useful details instead of stopping after a " +
                    "minimal answer."
    ),

    USING_CONVERSATION_CONNECTORS(
        displayName =
            "Using conversation connectors",
        learnerGuidance =
            "Use linking expressions to organize your response."
    ),

    POLITE_INTERACTION(
        displayName =
            "Polite interaction",
        learnerGuidance =
            "Use language appropriate to the social situation."
    ),

    NATURAL_CONVERSATION_CLOSING(
        displayName =
            "Natural conversation closing",
        learnerGuidance =
            "Conclude the interaction naturally and appropriately."
    )
}