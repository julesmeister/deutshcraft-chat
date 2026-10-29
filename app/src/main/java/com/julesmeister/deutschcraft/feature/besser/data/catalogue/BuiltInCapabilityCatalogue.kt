package com.julesmeister.deutschcraft.feature.besser.data.catalogue

import com.julesmeister.deutschcraft.core.database.entity.besser.CapabilityEntity

object BuiltInCapabilityCatalogue {

    fun create(
        now: Long = System.currentTimeMillis()
    ): List<CapabilityEntity> {
        return buildList {
            /*
             * =================================================
             * A1
             * =================================================
             */

            add(
                capability(
                    id = "a1-introduce-yourself",
                    name = "Introduce yourself",
                    description =
                        "Give basic information about yourself, " +
                                "such as your name, origin, work, " +
                                "studies, or where you live.",
                    category = "communicative_function",
                    cefrLevel = "A1",
                    sortOrder = 10,
                    now = now
                )
            )

            add(
                capability(
                    id = "a1-state-preference",
                    name = "State a preference",
                    description =
                        "Say what you like, dislike, or prefer " +
                                "in a simple and clear way.",
                    category = "communicative_function",
                    cefrLevel = "A1",
                    sortOrder = 20,
                    now = now
                )
            )

            add(
                capability(
                    id = "a1-describe-simply",
                    name = "Describe something simply",
                    description =
                        "Describe a person, place, activity, or " +
                                "object using basic relevant details.",
                    category = "description",
                    cefrLevel = "A1",
                    sortOrder = 30,
                    now = now
                )
            )

            add(
                capability(
                    id = "a1-ask-basic-information",
                    name = "Ask for basic information",
                    description =
                        "Ask a clear question to obtain simple " +
                                "information about a person, place, " +
                                "object, time, or activity.",
                    category = "interaction",
                    cefrLevel = "A1",
                    sortOrder = 40,
                    now = now
                )
            )

            add(
                capability(
                    id = "a1-express-basic-need",
                    name = "Express a basic need",
                    description =
                        "Say what you need or want in an " +
                                "everyday situation.",
                    category = "communicative_function",
                    cefrLevel = "A1",
                    sortOrder = 50,
                    now = now
                )
            )

            add(
                capability(
                    id = "a1-talk-about-routine",
                    name = "Talk about a routine",
                    description =
                        "Describe common everyday actions, " +
                                "activities, or habits.",
                    category = "narration",
                    cefrLevel = "A1",
                    sortOrder = 60,
                    now = now
                )
            )

            add(
                capability(
                    id = "a1-state-location",
                    name = "State where something is",
                    description =
                        "Explain where a person, place, or object " +
                                "is using simple location language.",
                    category = "description",
                    cefrLevel = "A1",
                    sortOrder = 70,
                    now = now
                )
            )

            add(
                capability(
                    id = "a1-state-possession",
                    name = "Express possession",
                    description =
                        "Explain simply who something belongs to " +
                                "or what someone has.",
                    category = "grammar",
                    cefrLevel = "A1",
                    sortOrder = 80,
                    now = now
                )
            )

            add(
                capability(
                    id = "a1-add-basic-detail",
                    name = "Add a basic detail",
                    description =
                        "Develop a short statement by adding one " +
                                "useful piece of information.",
                    category = "discourse",
                    cefrLevel = "A1",
                    sortOrder = 90,
                    now = now
                )
            )

            add(
                capability(
                    id = "a1-connect-simple-ideas",
                    name = "Connect simple ideas",
                    description =
                        "Combine related simple thoughts instead " +
                                "of expressing every idea separately.",
                    category = "discourse",
                    cefrLevel = "A1",
                    sortOrder = 100,
                    now = now
                )
            )

            /*
             * =================================================
             * A2
             * =================================================
             */

            add(
                capability(
                    id = "a2-give-simple-reason",
                    name = "Give a simple reason",
                    description =
                        "Expand a statement, preference, or " +
                                "opinion by explaining why.",
                    category = "communicative_function",
                    cefrLevel = "A2",
                    sortOrder = 10,
                    now = now
                )
            )

            add(
                capability(
                    id = "a2-add-supporting-detail",
                    name = "Add supporting detail",
                    description =
                        "Develop an idea with an additional " +
                                "relevant fact, description, or detail.",
                    category = "discourse",
                    cefrLevel = "A2",
                    sortOrder = 20,
                    now = now
                )
            )

            add(
                capability(
                    id = "a2-explain-preference",
                    name = "Explain a preference",
                    description =
                        "State what you prefer and provide a " +
                                "simple explanation for that preference.",
                    category = "communicative_function",
                    cefrLevel = "A2",
                    sortOrder = 30,
                    now = now
                )
            )

            add(
                capability(
                    id = "a2-make-comparison",
                    name = "Make a simple comparison",
                    description =
                        "Compare two people, things, places, " +
                                "activities, or situations.",
                    category = "discourse",
                    cefrLevel = "A2",
                    sortOrder = 40,
                    now = now
                )
            )

            add(
                capability(
                    id = "a2-describe-past-experience",
                    name = "Describe a past experience",
                    description =
                        "Explain what happened during a simple " +
                                "past experience or event.",
                    category = "narration",
                    cefrLevel = "A2",
                    sortOrder = 50,
                    now = now
                )
            )

            add(
                capability(
                    id = "a2-describe-future-plan",
                    name = "Describe a future plan",
                    description =
                        "Explain what you intend or expect to do " +
                                "in the future.",
                    category = "narration",
                    cefrLevel = "A2",
                    sortOrder = 60,
                    now = now
                )
            )

            add(
                capability(
                    id = "a2-sequence-events",
                    name = "Sequence events",
                    description =
                        "Connect events or actions in a clear " +
                                "time order.",
                    category = "discourse",
                    cefrLevel = "A2",
                    sortOrder = 70,
                    now = now
                )
            )

            add(
                capability(
                    id = "a2-give-simple-advice",
                    name = "Give simple advice",
                    description =
                        "Suggest a practical course of action in " +
                                "an everyday situation.",
                    category = "interaction",
                    cefrLevel = "A2",
                    sortOrder = 80,
                    now = now
                )
            )

            add(
                capability(
                    id = "a2-make-polite-request",
                    name = "Make a polite request",
                    description =
                        "Ask another person to do something using " +
                                "appropriate everyday politeness.",
                    category = "pragmatics",
                    cefrLevel = "A2",
                    sortOrder = 90,
                    now = now
                )
            )

            add(
                capability(
                    id = "a2-explain-consequence",
                    name = "Express a simple consequence",
                    description =
                        "Explain a straightforward result or " +
                                "consequence of an action or situation.",
                    category = "communicative_function",
                    cefrLevel = "A2",
                    sortOrder = 100,
                    now = now
                )
            )

            /*
             * =================================================
             * B1
             * =================================================
             */

            add(
                capability(
                    id = "b1-develop-opinion",
                    name = "Develop an opinion",
                    description =
                        "State an opinion and develop it beyond a " +
                                "single unsupported statement.",
                    category = "discourse",
                    cefrLevel = "B1",
                    sortOrder = 10,
                    now = now
                )
            )

            add(
                capability(
                    id = "b1-support-opinion-example",
                    name = "Support an opinion with an example",
                    description =
                        "Strengthen an opinion or reason with a " +
                                "relevant concrete example.",
                    category = "discourse",
                    cefrLevel = "B1",
                    sortOrder = 20,
                    now = now
                )
            )

            add(
                capability(
                    id = "b1-explain-cause-consequence",
                    name = "Explain cause and consequence",
                    description =
                        "Explain both why something happens and " +
                                "what results from it.",
                    category = "communicative_function",
                    cefrLevel = "B1",
                    sortOrder = 30,
                    now = now
                )
            )

            add(
                capability(
                    id = "b1-narrate-connected-events",
                    name = "Narrate connected events",
                    description =
                        "Tell a sequence of events as a connected " +
                                "story rather than isolated statements.",
                    category = "narration",
                    cefrLevel = "B1",
                    sortOrder = 40,
                    now = now
                )
            )

            add(
                capability(
                    id = "b1-explain-problem",
                    name = "Explain a problem",
                    description =
                        "Describe a problem clearly enough for " +
                                "another person to understand its cause " +
                                "or significance.",
                    category = "communicative_function",
                    cefrLevel = "B1",
                    sortOrder = 50,
                    now = now
                )
            )

            add(
                capability(
                    id = "b1-propose-solution",
                    name = "Propose a solution",
                    description =
                        "Suggest a solution to a problem and " +
                                "explain why the solution may help.",
                    category = "interaction",
                    cefrLevel = "B1",
                    sortOrder = 60,
                    now = now
                )
            )

            add(
                capability(
                    id = "b1-justify-decision",
                    name = "Justify a decision",
                    description =
                        "Explain the reasoning behind a choice " +
                                "or decision.",
                    category = "communicative_function",
                    cefrLevel = "B1",
                    sortOrder = 70,
                    now = now
                )
            )

            add(
                capability(
                    id = "b1-express-uncertainty",
                    name = "Express uncertainty",
                    description =
                        "Indicate that something is possible, " +
                                "probable, uncertain, or not fully known.",
                    category = "pragmatics",
                    cefrLevel = "B1",
                    sortOrder = 80,
                    now = now
                )
            )

            add(
                capability(
                    id = "b1-discuss-advantages-disadvantages",
                    name = "Discuss advantages and disadvantages",
                    description =
                        "Present useful positive and negative " +
                                "aspects of an option or situation.",
                    category = "discourse",
                    cefrLevel = "B1",
                    sortOrder = 90,
                    now = now
                )
            )

            add(
                capability(
                    id = "b1-handle-disagreement",
                    name = "Handle disagreement",
                    description =
                        "Disagree while acknowledging and " +
                                "responding to another person's view.",
                    category = "pragmatics",
                    cefrLevel = "B1",
                    sortOrder = 100,
                    now = now
                )
            )

            /*
             * =================================================
             * B2
             * =================================================
             */

            add(
                capability(
                    id = "b2-qualify-opinion",
                    name = "Qualify an opinion",
                    description =
                        "Make an opinion more nuanced by adding " +
                                "conditions, limitations, or exceptions.",
                    category = "pragmatics",
                    cefrLevel = "B2",
                    sortOrder = 10,
                    now = now
                )
            )

            add(
                capability(
                    id = "b2-acknowledge-counterargument",
                    name = "Acknowledge a counterargument",
                    description =
                        "Recognize a reasonable opposing point " +
                                "before developing your own position.",
                    category = "discourse",
                    cefrLevel = "B2",
                    sortOrder = 20,
                    now = now
                )
            )

            add(
                capability(
                    id = "b2-contrast-perspectives",
                    name = "Contrast competing perspectives",
                    description =
                        "Compare different perspectives and show " +
                                "how they differ or conflict.",
                    category = "discourse",
                    cefrLevel = "B2",
                    sortOrder = 30,
                    now = now
                )
            )

            add(
                capability(
                    id = "b2-support-claim-evidence",
                    name = "Support a claim with evidence",
                    description =
                        "Strengthen a claim using a relevant " +
                                "example, fact, observation, or explanation.",
                    category = "discourse",
                    cefrLevel = "B2",
                    sortOrder = 40,
                    now = now
                )
            )

            add(
                capability(
                    id = "b2-evaluate-alternatives",
                    name = "Evaluate alternatives",
                    description =
                        "Compare multiple options using meaningful " +
                                "criteria before reaching a conclusion.",
                    category = "discourse",
                    cefrLevel = "B2",
                    sortOrder = 50,
                    now = now
                )
            )

            add(
                capability(
                    id = "b2-hedge-claim",
                    name = "Hedge a strong claim",
                    description =
                        "Reduce excessive certainty and express an " +
                                "appropriate degree of caution or probability.",
                    category = "pragmatics",
                    cefrLevel = "B2",
                    sortOrder = 60,
                    now = now
                )
            )

            add(
                capability(
                    id = "b2-explain-exception",
                    name = "Explain an exception",
                    description =
                        "Identify when a general statement does not " +
                                "apply and explain why.",
                    category = "discourse",
                    cefrLevel = "B2",
                    sortOrder = 70,
                    now = now
                )
            )

            add(
                capability(
                    id = "b2-multi-step-argument",
                    name = "Develop a multi-step argument",
                    description =
                        "Build an argument through several connected " +
                                "reasons instead of relying on one point.",
                    category = "discourse",
                    cefrLevel = "B2",
                    sortOrder = 80,
                    now = now
                )
            )

            add(
                capability(
                    id = "b2-negotiate-disagreement",
                    name = "Negotiate disagreement",
                    description =
                        "Express disagreement while seeking common " +
                                "ground or a workable compromise.",
                    category = "pragmatics",
                    cefrLevel = "B2",
                    sortOrder = 90,
                    now = now
                )
            )

            add(
                capability(
                    id = "b2-reformulate-precisely",
                    name = "Reformulate an idea precisely",
                    description =
                        "Restate an idea more accurately when the " +
                                "first formulation is too vague or general.",
                    category = "discourse",
                    cefrLevel = "B2",
                    sortOrder = 100,
                    now = now
                )
            )

            /*
             * =================================================
             * C1
             * =================================================
             */

            add(
                capability(
                    id = "c1-develop-nuanced-argument",
                    name = "Develop a nuanced argument",
                    description =
                        "Develop a complex position while accounting " +
                                "for qualifications, limitations, and " +
                                "competing considerations.",
                    category = "discourse",
                    cefrLevel = "C1",
                    sortOrder = 10,
                    now = now
                )
            )

            add(
                capability(
                    id = "c1-synthesize-perspectives",
                    name = "Synthesize multiple perspectives",
                    description =
                        "Combine important elements from several " +
                                "positions into a coherent interpretation.",
                    category = "discourse",
                    cefrLevel = "C1",
                    sortOrder = 20,
                    now = now
                )
            )

            add(
                capability(
                    id = "c1-express-reservation",
                    name = "Express a reservation diplomatically",
                    description =
                        "Raise a concern or limitation without making " +
                                "the response unnecessarily confrontational.",
                    category = "pragmatics",
                    cefrLevel = "C1",
                    sortOrder = 30,
                    now = now
                )
            )

            add(
                capability(
                    id = "c1-reframe-argument",
                    name = "Reframe another argument",
                    description =
                        "Restate another person's position from a " +
                                "different but accurate perspective.",
                    category = "discourse",
                    cefrLevel = "C1",
                    sortOrder = 40,
                    now = now
                )
            )

            add(
                capability(
                    id = "c1-fine-grained-evaluation",
                    name = "Make a fine-grained evaluation",
                    description =
                        "Evaluate differences in quality, importance, " +
                                "impact, or appropriateness precisely.",
                    category = "communicative_function",
                    cefrLevel = "C1",
                    sortOrder = 50,
                    now = now
                )
            )

            add(
                capability(
                    id = "c1-adapt-register",
                    name = "Adapt register to context",
                    description =
                        "Adjust wording, formality, and interpersonal " +
                                "distance to suit the situation.",
                    category = "pragmatics",
                    cefrLevel = "C1",
                    sortOrder = 60,
                    now = now
                )
            )

            add(
                capability(
                    id = "c1-clarify-implicit-meaning",
                    name = "Clarify implicit meaning",
                    description =
                        "Make an implied assumption, relationship, or " +
                                "consequence explicit when useful.",
                    category = "pragmatics",
                    cefrLevel = "C1",
                    sortOrder = 70,
                    now = now
                )
            )

            add(
                capability(
                    id = "c1-rhetorical-emphasis",
                    name = "Use rhetorical emphasis",
                    description =
                        "Structure an important point so its relevance " +
                                "or contrast is communicated deliberately.",
                    category = "discourse",
                    cefrLevel = "C1",
                    sortOrder = 80,
                    now = now
                )
            )

            add(
                capability(
                    id = "c1-handle-abstract-topic",
                    name = "Develop an abstract topic precisely",
                    description =
                        "Discuss an abstract issue with clear conceptual " +
                                "relationships and precise distinctions.",
                    category = "discourse",
                    cefrLevel = "C1",
                    sortOrder = 90,
                    now = now
                )
            )

            add(
                capability(
                    id = "c1-subtle-agreement-disagreement",
                    name = "Express subtle agreement or disagreement",
                    description =
                        "Communicate partial agreement, qualified " +
                                "disagreement, or conditional acceptance.",
                    category = "pragmatics",
                    cefrLevel = "C1",
                    sortOrder = 100,
                    now = now
                )
            )

            /*
             * =================================================
             * C2
             * =================================================
             */

            add(
                capability(
                    id = "c2-fine-shades-meaning",
                    name = "Express fine shades of meaning",
                    description =
                        "Choose language that communicates subtle " +
                                "differences in attitude, certainty, " +
                                "intensity, or implication.",
                    category = "pragmatics",
                    cefrLevel = "C2",
                    sortOrder = 10,
                    now = now
                )
            )

            add(
                capability(
                    id = "c2-reformulate-seamlessly",
                    name = "Reformulate seamlessly",
                    description =
                        "Rephrase an idea naturally and precisely " +
                                "without disrupting conversational flow.",
                    category = "discourse",
                    cefrLevel = "C2",
                    sortOrder = 20,
                    now = now
                )
            )

            add(
                capability(
                    id = "c2-handle-ambiguity",
                    name = "Handle ambiguity strategically",
                    description =
                        "Recognize and manage ambiguity according to " +
                                "the speaker's communicative intention.",
                    category = "pragmatics",
                    cefrLevel = "C2",
                    sortOrder = 30,
                    now = now
                )
            )

            add(
                capability(
                    id = "c2-idiomatic-nuance",
                    name = "Use idiomatic nuance",
                    description =
                        "Express an idea naturally using idiomatic " +
                                "language appropriate to context and register.",
                    category = "vocabulary",
                    cefrLevel = "C2",
                    sortOrder = 40,
                    now = now
                )
            )

            add(
                capability(
                    id = "c2-sophisticated-disagreement",
                    name = "Manage sophisticated disagreement",
                    description =
                        "Challenge another position precisely while " +
                                "maintaining appropriate rhetorical and " +
                                "interpersonal control.",
                    category = "pragmatics",
                    cefrLevel = "C2",
                    sortOrder = 50,
                    now = now
                )
            )

            add(
                capability(
                    id = "c2-adapt-rhetorical-strategy",
                    name = "Adapt rhetorical strategy",
                    description =
                        "Change how an argument is framed according " +
                                "to audience, purpose, and conversational context.",
                    category = "discourse",
                    cefrLevel = "C2",
                    sortOrder = 60,
                    now = now
                )
            )

            add(
                capability(
                    id = "c2-summarize-complex-position",
                    name = "Summarize a complex position precisely",
                    description =
                        "Condense a complex argument while retaining " +
                                "its essential distinctions and qualifications.",
                    category = "discourse",
                    cefrLevel = "C2",
                    sortOrder = 70,
                    now = now
                )
            )

            add(
                capability(
                    id = "c2-convey-implied-meaning",
                    name = "Convey implied meaning",
                    description =
                        "Communicate meaning indirectly when implication " +
                                "is more appropriate than explicit statement.",
                    category = "pragmatics",
                    cefrLevel = "C2",
                    sortOrder = 80,
                    now = now
                )
            )

            add(
                capability(
                    id = "c2-control-style",
                    name = "Control style deliberately",
                    description =
                        "Choose and maintain a deliberate stylistic " +
                                "effect while preserving clarity and naturalness.",
                    category = "pragmatics",
                    cefrLevel = "C2",
                    sortOrder = 90,
                    now = now
                )
            )

            add(
                capability(
                    id = "c2-consolidate-discourse-control",
                    name = "Consolidate advanced discourse control",
                    description =
                        "Organize sophisticated spoken interaction " +
                                "smoothly, precisely, and flexibly.",
                    category = "discourse",
                    cefrLevel = "C2",
                    sortOrder = 100,
                    now = now
                )
            )
        }
    }

    private fun capability(
        id: String,
        name: String,
        description: String,
        category: String,
        cefrLevel: String,
        sortOrder: Int,
        now: Long
    ): CapabilityEntity {
        return CapabilityEntity(
            id =
                id,
            name =
                name,
            description =
                description,
            category =
                category,
            cefrLevel =
                cefrLevel,
            sortOrder =
                sortOrder,
            isActive =
                true,
            createdAt =
                now,
            updatedAt =
                now
        )
    }
}