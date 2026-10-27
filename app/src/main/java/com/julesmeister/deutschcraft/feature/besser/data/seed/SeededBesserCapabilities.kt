package com.julesmeister.deutschcraft.feature.besser.data.seed


import com.julesmeister.deutschcraft.feature.besser.domain.Capability
import com.julesmeister.deutschcraft.feature.besser.domain.CapabilityCategory

object SeededBesserCapabilities {

    fun create(
        timestamp: Long
    ): List<Capability> {
        return listOf(
            Capability(
                id =
                    "a1-add-supporting-detail",
                name =
                    "Add supporting detail",
                description =
                    "Expand a simple statement with a concrete supporting detail.",
                category =
                    CapabilityCategory.COHERENCE,
                cefrLevel =
                    "A1",
                sortOrder =
                    100,
                isActive =
                    true,
                createdAt =
                    timestamp,
                updatedAt =
                    timestamp
            ),

            Capability(
                id =
                    "a1-ask-simple-follow-up",
                name =
                    "Ask a simple follow-up",
                description =
                    "Continue the interaction by asking a short relevant question.",
                category =
                    CapabilityCategory.INTERACTION,
                cefrLevel =
                    "A1",
                sortOrder =
                    110,
                isActive =
                    true,
                createdAt =
                    timestamp,
                updatedAt =
                    timestamp
            ),

            Capability(
                id =
                    "a1-use-time-expression",
                name =
                    "Use a time expression",
                description =
                    "Add a basic time reference such as today, tomorrow, or at eight o'clock.",
                category =
                    CapabilityCategory.VOCABULARY,
                cefrLevel =
                    "A1",
                sortOrder =
                    120,
                isActive =
                    true,
                createdAt =
                    timestamp,
                updatedAt =
                    timestamp
            ),

            Capability(
                id =
                    "a2-explain-with-weil",
                name =
                    "Explain with weil",
                description =
                    "Support an opinion or decision with a reason using weil.",
                category =
                    CapabilityCategory.GRAMMAR,
                cefrLevel =
                    "A2",
                sortOrder =
                    200,
                isActive =
                    true,
                createdAt =
                    timestamp,
                updatedAt =
                    timestamp
            ),

            Capability(
                id =
                    "a2-add-concrete-example",
                name =
                    "Add a concrete example",
                description =
                    "Strengthen an answer by giving one relevant real-life example.",
                category =
                    CapabilityCategory.COHERENCE,
                cefrLevel =
                    "A2",
                sortOrder =
                    210,
                isActive =
                    true,
                createdAt =
                    timestamp,
                updatedAt =
                    timestamp
            ),

            Capability(
                id =
                    "a2-request-clarification",
                name =
                    "Request clarification",
                description =
                    "Ask the other speaker to repeat or explain unclear information.",
                category =
                    CapabilityCategory.INTERACTION,
                cefrLevel =
                    "A2",
                sortOrder =
                    220,
                isActive =
                    true,
                createdAt =
                    timestamp,
                updatedAt =
                    timestamp
            ),

            Capability(
                id =
                    "a2-link-ideas",
                name =
                    "Link related ideas",
                description =
                    "Connect short ideas using words such as und, aber, dann, or deshalb.",
                category =
                    CapabilityCategory.COHERENCE,
                cefrLevel =
                    "A2",
                sortOrder =
                    230,
                isActive =
                    true,
                createdAt =
                    timestamp,
                updatedAt =
                    timestamp
            ),

            Capability(
                id =
                    "b1-develop-opinion",
                name =
                    "Develop an opinion",
                description =
                    "State an opinion, explain it, and support it with a relevant example.",
                category =
                    CapabilityCategory.COHERENCE,
                cefrLevel =
                    "B1",
                sortOrder =
                    300,
                isActive =
                    true,
                createdAt =
                    timestamp,
                updatedAt =
                    timestamp
            ),

            Capability(
                id =
                    "b1-compare-alternatives",
                name =
                    "Compare alternatives",
                description =
                    "Compare two options and explain the advantages or disadvantages of each.",
                category =
                    CapabilityCategory.PRAGMATICS,
                cefrLevel =
                    "B1",
                sortOrder =
                    310,
                isActive =
                    true,
                createdAt =
                    timestamp,
                updatedAt =
                    timestamp
            ),

            Capability(
                id =
                    "b1-maintain-conversation",
                name =
                    "Maintain the conversation",
                description =
                    "Respond to the previous turn and actively move the conversation forward.",
                category =
                    CapabilityCategory.INTERACTION,
                cefrLevel =
                    "B1",
                sortOrder =
                    320,
                isActive =
                    true,
                createdAt =
                    timestamp,
                updatedAt =
                    timestamp
            ),

            Capability(
                id =
                    "b1-express-cause-and-result",
                name =
                    "Express cause and result",
                description =
                    "Connect a cause with its consequence using appropriate linking language.",
                category =
                    CapabilityCategory.GRAMMAR,
                cefrLevel =
                    "B1",
                sortOrder =
                    330,
                isActive =
                    true,
                createdAt =
                    timestamp,
                updatedAt =
                    timestamp
            ),

            Capability(
                id =
                    "b1-handle-misunderstanding",
                name =
                    "Handle a misunderstanding",
                description =
                    "Recognize confusion, clarify the intended meaning, and confirm understanding.",
                category =
                    CapabilityCategory.INTERACTION,
                cefrLevel =
                    "B1",
                sortOrder =
                    340,
                isActive =
                    true,
                createdAt =
                    timestamp,
                updatedAt =
                    timestamp
            ),

            Capability(
                id =
                    "b2-qualify-opinion",
                name =
                    "Qualify an opinion",
                description =
                    "Present a nuanced position rather than an absolute statement.",
                category =
                    CapabilityCategory.PRAGMATICS,
                cefrLevel =
                    "B2",
                sortOrder =
                    400,
                isActive =
                    true,
                createdAt =
                    timestamp,
                updatedAt =
                    timestamp
            ),

            Capability(
                id =
                    "b2-structure-argument",
                name =
                    "Structure an argument",
                description =
                    "Organize a position into a claim, justification, example, and conclusion.",
                category =
                    CapabilityCategory.COHERENCE,
                cefrLevel =
                    "B2",
                sortOrder =
                    410,
                isActive =
                    true,
                createdAt =
                    timestamp,
                updatedAt =
                    timestamp
            ),

            Capability(
                id =
                    "b2-respond-to-counterpoint",
                name =
                    "Respond to a counterpoint",
                description =
                    "Acknowledge another perspective and respond with a relevant counterargument.",
                category =
                    CapabilityCategory.INTERACTION,
                cefrLevel =
                    "B2",
                sortOrder =
                    420,
                isActive =
                    true,
                createdAt =
                    timestamp,
                updatedAt =
                    timestamp
            )
        )
    }
}