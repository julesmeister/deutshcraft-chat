// app/src/main/java/com/julesmeister/deutschcraft/feature/besser/domain/progression/CapabilityProgressionGraph.kt

package com.julesmeister.deutschcraft.feature.besser.domain.progression

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CapabilityProgressionGraph
@Inject constructor() {

    private val rulesByCapabilityId:
            Map<String, CapabilityProgressionRule> =
        buildRules()
            .associateBy { rule ->
                rule.capabilityId
            }

    fun ruleFor(
        capabilityId: String
    ): CapabilityProgressionRule? {
        return rulesByCapabilityId[
            capabilityId
        ]
    }

    fun prerequisitesFor(
        capabilityId: String
    ): Set<String> {
        return ruleFor(
            capabilityId =
                capabilityId
        )
            ?.prerequisiteCapabilityIds
            .orEmpty()
    }

    fun successorsFor(
        capabilityId: String
    ): Set<String> {
        return ruleFor(
            capabilityId =
                capabilityId
        )
            ?.successorCapabilityIds
            .orEmpty()
    }

    fun allRules():
            List<CapabilityProgressionRule> {

        return rulesByCapabilityId
            .values
            .sortedBy { rule ->
                rule.capabilityId
            }
    }

    private fun buildRules():
            List<CapabilityProgressionRule> {

        return listOf(
            rule(
                capabilityId =
                    "a1-state-preference",

                successors =
                    setOf(
                        "a2-explain-preference",
                        "a2-give-simple-reason"
                    )
            ),

            rule(
                capabilityId =
                    "a1-describe-simply",

                successors =
                    setOf(
                        "a2-add-supporting-detail",
                        "a2-make-comparison"
                    )
            ),

            rule(
                capabilityId =
                    "a1-add-basic-detail",

                successors =
                    setOf(
                        "a2-add-supporting-detail"
                    )
            ),

            rule(
                capabilityId =
                    "a1-connect-simple-ideas",

                successors =
                    setOf(
                        "a2-sequence-events",
                        "a2-give-simple-reason"
                    )
            ),

            rule(
                capabilityId =
                    "a1-talk-about-routine",

                successors =
                    setOf(
                        "a2-describe-past-experience",
                        "a2-describe-future-plan",
                        "a2-sequence-events"
                    )
            ),

            rule(
                capabilityId =
                    "a1-ask-basic-information",

                successors =
                    setOf(
                        "a2-make-polite-request"
                    )
            ),

            rule(
                capabilityId =
                    "a1-express-basic-need",

                successors =
                    setOf(
                        "a2-make-polite-request"
                    )
            ),

            rule(
                capabilityId =
                    "a2-give-simple-reason",

                prerequisites =
                    setOf(
                        "a1-connect-simple-ideas"
                    ),

                successors =
                    setOf(
                        "b1-develop-opinion",
                        "b1-explain-cause-consequence",
                        "b1-justify-decision"
                    )
            ),

            rule(
                capabilityId =
                    "a2-add-supporting-detail",

                prerequisites =
                    setOf(
                        "a1-add-basic-detail"
                    ),

                successors =
                    setOf(
                        "b1-develop-opinion",
                        "b1-support-opinion-example",
                        "b1-explain-problem"
                    )
            ),

            rule(
                capabilityId =
                    "a2-explain-preference",

                prerequisites =
                    setOf(
                        "a1-state-preference"
                    ),

                successors =
                    setOf(
                        "b1-develop-opinion",
                        "b1-justify-decision"
                    )
            ),

            rule(
                capabilityId =
                    "a2-make-comparison",

                prerequisites =
                    setOf(
                        "a1-describe-simply"
                    ),

                successors =
                    setOf(
                        "b1-discuss-advantages-disadvantages"
                    )
            ),

            rule(
                capabilityId =
                    "a2-describe-past-experience",

                prerequisites =
                    setOf(
                        "a1-talk-about-routine"
                    ),

                successors =
                    setOf(
                        "b1-narrate-connected-events"
                    )
            ),

            rule(
                capabilityId =
                    "a2-describe-future-plan",

                successors =
                    setOf(
                        "b1-justify-decision"
                    )
            ),

            rule(
                capabilityId =
                    "a2-sequence-events",

                prerequisites =
                    setOf(
                        "a1-connect-simple-ideas"
                    ),

                successors =
                    setOf(
                        "b1-narrate-connected-events"
                    )
            ),

            rule(
                capabilityId =
                    "a2-give-simple-advice",

                successors =
                    setOf(
                        "b1-propose-solution"
                    )
            ),

            rule(
                capabilityId =
                    "a2-make-polite-request",

                successors =
                    setOf(
                        "b1-handle-disagreement"
                    )
            ),

            rule(
                capabilityId =
                    "a2-explain-consequence",

                prerequisites =
                    setOf(
                        "a2-give-simple-reason"
                    ),

                successors =
                    setOf(
                        "b1-explain-cause-consequence"
                    )
            ),

            rule(
                capabilityId =
                    "b1-develop-opinion",

                prerequisites =
                    setOf(
                        "a2-give-simple-reason",
                        "a2-explain-preference"
                    ),

                successors =
                    setOf(
                        "b2-qualify-opinion",
                        "b2-support-claim-evidence",
                        "b2-multi-step-argument"
                    )
            ),

            rule(
                capabilityId =
                    "b1-support-opinion-example",

                prerequisites =
                    setOf(
                        "a2-add-supporting-detail"
                    ),

                successors =
                    setOf(
                        "b2-support-claim-evidence",
                        "b2-multi-step-argument"
                    )
            ),

            rule(
                capabilityId =
                    "b1-explain-cause-consequence",

                prerequisites =
                    setOf(
                        "a2-give-simple-reason",
                        "a2-explain-consequence"
                    ),

                successors =
                    setOf(
                        "b2-support-claim-evidence",
                        "b2-explain-exception",
                        "b2-multi-step-argument"
                    )
            ),

            rule(
                capabilityId =
                    "b1-narrate-connected-events",

                prerequisites =
                    setOf(
                        "a2-sequence-events"
                    ),

                successors =
                    setOf(
                        "b2-reformulate-precisely"
                    )
            ),

            rule(
                capabilityId =
                    "b1-explain-problem",

                prerequisites =
                    setOf(
                        "a2-add-supporting-detail"
                    ),

                successors =
                    setOf(
                        "b1-propose-solution",
                        "b2-evaluate-alternatives"
                    )
            ),

            rule(
                capabilityId =
                    "b1-propose-solution",

                prerequisites =
                    setOf(
                        "b1-explain-problem"
                    ),

                successors =
                    setOf(
                        "b2-evaluate-alternatives",
                        "b2-negotiate-disagreement"
                    )
            ),

            rule(
                capabilityId =
                    "b1-justify-decision",

                prerequisites =
                    setOf(
                        "a2-give-simple-reason"
                    ),

                successors =
                    setOf(
                        "b2-qualify-opinion",
                        "b2-evaluate-alternatives",
                        "b2-support-claim-evidence"
                    )
            ),

            rule(
                capabilityId =
                    "b1-express-uncertainty",

                successors =
                    setOf(
                        "b2-hedge-claim",
                        "b2-qualify-opinion"
                    )
            ),

            rule(
                capabilityId =
                    "b1-discuss-advantages-disadvantages",

                prerequisites =
                    setOf(
                        "a2-make-comparison"
                    ),

                successors =
                    setOf(
                        "b2-contrast-perspectives",
                        "b2-evaluate-alternatives",
                        "b2-acknowledge-counterargument"
                    )
            ),

            rule(
                capabilityId =
                    "b1-handle-disagreement",

                successors =
                    setOf(
                        "b2-acknowledge-counterargument",
                        "b2-negotiate-disagreement",
                        "b2-contrast-perspectives"
                    )
            ),

            rule(
                capabilityId =
                    "b2-qualify-opinion",

                prerequisites =
                    setOf(
                        "b1-develop-opinion",
                        "b1-express-uncertainty"
                    ),

                successors =
                    setOf(
                        "c1-develop-nuanced-argument",
                        "c1-subtle-agreement-disagreement"
                    )
            ),

            rule(
                capabilityId =
                    "b2-acknowledge-counterargument",

                prerequisites =
                    setOf(
                        "b1-handle-disagreement",
                        "b1-discuss-advantages-disadvantages"
                    ),

                successors =
                    setOf(
                        "c1-develop-nuanced-argument",
                        "c1-synthesize-perspectives",
                        "c1-subtle-agreement-disagreement"
                    )
            ),

            rule(
                capabilityId =
                    "b2-contrast-perspectives",

                prerequisites =
                    setOf(
                        "b1-discuss-advantages-disadvantages"
                    ),

                successors =
                    setOf(
                        "c1-synthesize-perspectives",
                        "c1-fine-grained-evaluation"
                    )
            ),

            rule(
                capabilityId =
                    "b2-support-claim-evidence",

                prerequisites =
                    setOf(
                        "b1-develop-opinion",
                        "b1-support-opinion-example"
                    ),

                successors =
                    setOf(
                        "c1-develop-nuanced-argument"
                    )
            ),

            rule(
                capabilityId =
                    "b2-evaluate-alternatives",

                prerequisites =
                    setOf(
                        "b1-discuss-advantages-disadvantages"
                    ),

                successors =
                    setOf(
                        "c1-fine-grained-evaluation",
                        "c1-synthesize-perspectives"
                    )
            ),

            rule(
                capabilityId =
                    "b2-hedge-claim",

                prerequisites =
                    setOf(
                        "b1-express-uncertainty"
                    ),

                successors =
                    setOf(
                        "c1-express-reservation",
                        "c1-subtle-agreement-disagreement"
                    )
            ),

            rule(
                capabilityId =
                    "b2-explain-exception",

                prerequisites =
                    setOf(
                        "b1-explain-cause-consequence"
                    ),

                successors =
                    setOf(
                        "c1-develop-nuanced-argument",
                        "c1-clarify-implicit-meaning"
                    )
            ),

            rule(
                capabilityId =
                    "b2-multi-step-argument",

                prerequisites =
                    setOf(
                        "b1-develop-opinion",
                        "b1-support-opinion-example"
                    ),

                successors =
                    setOf(
                        "c1-develop-nuanced-argument",
                        "c1-handle-abstract-topic",
                        "c1-rhetorical-emphasis"
                    )
            ),

            rule(
                capabilityId =
                    "b2-negotiate-disagreement",

                prerequisites =
                    setOf(
                        "b1-handle-disagreement"
                    ),

                successors =
                    setOf(
                        "c1-express-reservation",
                        "c1-subtle-agreement-disagreement"
                    )
            ),

            rule(
                capabilityId =
                    "b2-reformulate-precisely",

                successors =
                    setOf(
                        "c1-reframe-argument",
                        "c1-clarify-implicit-meaning"
                    )
            ),

            rule(
                capabilityId =
                    "c1-develop-nuanced-argument",

                prerequisites =
                    setOf(
                        "b2-qualify-opinion",
                        "b2-multi-step-argument"
                    ),

                successors =
                    setOf(
                        "c2-adapt-rhetorical-strategy",
                        "c2-summarize-complex-position"
                    )
            ),

            rule(
                capabilityId =
                    "c1-synthesize-perspectives",

                prerequisites =
                    setOf(
                        "b2-contrast-perspectives",
                        "b2-acknowledge-counterargument"
                    ),

                successors =
                    setOf(
                        "c2-summarize-complex-position",
                        "c2-adapt-rhetorical-strategy"
                    )
            ),

            rule(
                capabilityId =
                    "c1-express-reservation",

                prerequisites =
                    setOf(
                        "b2-hedge-claim"
                    ),

                successors =
                    setOf(
                        "c2-sophisticated-disagreement",
                        "c2-fine-shades-meaning"
                    )
            ),

            rule(
                capabilityId =
                    "c1-reframe-argument",

                prerequisites =
                    setOf(
                        "b2-reformulate-precisely"
                    ),

                successors =
                    setOf(
                        "c2-reformulate-seamlessly",
                        "c2-adapt-rhetorical-strategy"
                    )
            ),

            rule(
                capabilityId =
                    "c1-fine-grained-evaluation",

                prerequisites =
                    setOf(
                        "b2-evaluate-alternatives"
                    ),

                successors =
                    setOf(
                        "c2-fine-shades-meaning",
                        "c2-handle-ambiguity"
                    )
            ),

            rule(
                capabilityId =
                    "c1-adapt-register",

                successors =
                    setOf(
                        "c2-control-style",
                        "c2-idiomatic-nuance"
                    )
            ),

            rule(
                capabilityId =
                    "c1-clarify-implicit-meaning",

                prerequisites =
                    setOf(
                        "b2-explain-exception"
                    ),

                successors =
                    setOf(
                        "c2-handle-ambiguity",
                        "c2-convey-implied-meaning"
                    )
            ),

            rule(
                capabilityId =
                    "c1-rhetorical-emphasis",

                prerequisites =
                    setOf(
                        "b2-multi-step-argument"
                    ),

                successors =
                    setOf(
                        "c2-adapt-rhetorical-strategy",
                        "c2-control-style"
                    )
            ),

            rule(
                capabilityId =
                    "c1-handle-abstract-topic",

                prerequisites =
                    setOf(
                        "b2-multi-step-argument"
                    ),

                successors =
                    setOf(
                        "c2-summarize-complex-position",
                        "c2-consolidate-discourse-control"
                    )
            ),

            rule(
                capabilityId =
                    "c1-subtle-agreement-disagreement",

                prerequisites =
                    setOf(
                        "b2-qualify-opinion",
                        "b2-negotiate-disagreement"
                    ),

                successors =
                    setOf(
                        "c2-sophisticated-disagreement",
                        "c2-fine-shades-meaning"
                    )
            ),

            rule(
                capabilityId =
                    "c2-fine-shades-meaning",

                prerequisites =
                    setOf(
                        "c1-fine-grained-evaluation",
                        "c1-subtle-agreement-disagreement"
                    )
            ),

            rule(
                capabilityId =
                    "c2-reformulate-seamlessly",

                prerequisites =
                    setOf(
                        "c1-reframe-argument"
                    )
            ),

            rule(
                capabilityId =
                    "c2-handle-ambiguity",

                prerequisites =
                    setOf(
                        "c1-clarify-implicit-meaning"
                    )
            ),

            rule(
                capabilityId =
                    "c2-idiomatic-nuance",

                prerequisites =
                    setOf(
                        "c1-adapt-register"
                    )
            ),

            rule(
                capabilityId =
                    "c2-sophisticated-disagreement",

                prerequisites =
                    setOf(
                        "c1-express-reservation",
                        "c1-subtle-agreement-disagreement"
                    )
            ),

            rule(
                capabilityId =
                    "c2-adapt-rhetorical-strategy",

                prerequisites =
                    setOf(
                        "c1-develop-nuanced-argument",
                        "c1-rhetorical-emphasis"
                    )
            ),

            rule(
                capabilityId =
                    "c2-summarize-complex-position",

                prerequisites =
                    setOf(
                        "c1-develop-nuanced-argument",
                        "c1-synthesize-perspectives"
                    )
            ),

            rule(
                capabilityId =
                    "c2-convey-implied-meaning",

                prerequisites =
                    setOf(
                        "c1-clarify-implicit-meaning"
                    )
            ),

            rule(
                capabilityId =
                    "c2-control-style",

                prerequisites =
                    setOf(
                        "c1-adapt-register",
                        "c1-rhetorical-emphasis"
                    )
            ),

            rule(
                capabilityId =
                    "c2-consolidate-discourse-control",

                prerequisites =
                    setOf(
                        "c1-handle-abstract-topic",
                        "c1-develop-nuanced-argument"
                    )
            )
        )
    }

    private fun rule(
        capabilityId: String,
        prerequisites: Set<String> =
            emptySet(),
        successors: Set<String> =
            emptySet(),
        threshold: Float =
            CapabilityProgressionRule
                .DEFAULT_PREREQUISITE_MASTERY_THRESHOLD
    ): CapabilityProgressionRule {

        return CapabilityProgressionRule(
            capabilityId =
                capabilityId,

            prerequisiteCapabilityIds =
                prerequisites,

            successorCapabilityIds =
                successors,

            prerequisiteMasteryThreshold =
                threshold
        )
    }
}