// app/src/main/java/com/julesmeister/deutschcraft/feature/chat/presentation/progress/BuiltInCapabilityTeachingSupport.kt

package com.julesmeister.deutschcraft.feature.chat.presentation.progress

object BuiltInCapabilityTeachingSupport {

    private val supportByCapabilityId:
            Map<String, CapabilityTeachingSupport> =
        listOf(
            support(
                capabilityId =
                    "a1-introduce-yourself",

                learnerTitle =
                    "Introduce yourself in complete sentences",

                explanation =
                    "Combine your name, origin, work, studies, " +
                            "and home into a short connected introduction.",

                grammarTargets =
                    listOf(
                        "Ich heiße ...",
                        "Ich komme aus ...",
                        "Ich arbeite als ...",
                        "Ich wohne in ..."
                    ),

                vocabularyTargets =
                    listOf(
                        "der Beruf",
                        "die Stadt",
                        "arbeiten",
                        "studieren"
                    ),

                examplePhrase =
                    "Ich heiße Jules, komme aus den Philippinen " +
                            "und arbeite als Softwareentwickler."
            ),

            support(
                capabilityId =
                    "a1-state-preference",

                learnerTitle =
                    "State what you prefer",

                explanation =
                    "Use simple preference language instead of naming " +
                            "an option without saying whether you like it.",

                grammarTargets =
                    listOf(
                        "Ich mag ...",
                        "Ich mag ... lieber.",
                        "Am liebsten ...",
                        "Ich möchte ..."
                    ),

                vocabularyTargets =
                    listOf(
                        "gern",
                        "lieber",
                        "am liebsten",
                        "bevorzugen"
                    ),

                examplePhrase =
                    "Ich trinke lieber Tee, aber am liebsten trinke ich Kaffee."
            ),

            support(
                capabilityId =
                    "a1-describe-simply",

                learnerTitle =
                    "Add useful descriptive details",

                explanation =
                    "Describe a person, place, activity, or object with " +
                            "basic qualities instead of naming it only.",

                grammarTargets =
                    listOf(
                        "Das ist ...",
                        "Es ist ...",
                        "Er / Sie hat ...",
                        "Dort gibt es ..."
                    ),

                vocabularyTargets =
                    listOf(
                        "groß",
                        "klein",
                        "ruhig",
                        "interessant"
                    ),

                examplePhrase =
                    "Die Wohnung ist klein, aber sie ist ruhig und sehr hell."
            ),

            support(
                capabilityId =
                    "a1-ask-basic-information",

                learnerTitle =
                    "Ask clearer basic questions",

                explanation =
                    "Use question words to request the exact information " +
                            "you need.",

                grammarTargets =
                    listOf(
                        "Wo ist ...?",
                        "Wann beginnt ...?",
                        "Wie viel kostet ...?",
                        "Was bedeutet ...?"
                    ),

                vocabularyTargets =
                    listOf(
                        "wo",
                        "wann",
                        "wie viel",
                        "welcher"
                    ),

                examplePhrase =
                    "Entschuldigung, wann beginnt der Kurs und wo ist der Raum?"
            ),

            support(
                capabilityId =
                    "a1-express-basic-need",

                learnerTitle =
                    "Express what you need clearly",

                explanation =
                    "Say what you need or want using a complete request.",

                grammarTargets =
                    listOf(
                        "Ich brauche ...",
                        "Ich möchte ...",
                        "Ich hätte gern ...",
                        "Kann ich ...?"
                    ),

                vocabularyTargets =
                    listOf(
                        "brauchen",
                        "möchten",
                        "helfen",
                        "suchen"
                    ),

                examplePhrase =
                    "Ich brauche bitte ein Ticket nach Berlin."
            ),

            support(
                capabilityId =
                    "a1-talk-about-routine",

                learnerTitle =
                    "Describe your routine in order",

                explanation =
                    "Connect common daily actions with simple time expressions.",

                grammarTargets =
                    listOf(
                        "Am Morgen ...",
                        "Dann ...",
                        "Danach ...",
                        "Am Abend ..."
                    ),

                vocabularyTargets =
                    listOf(
                        "aufstehen",
                        "anfangen",
                        "arbeiten",
                        "nach Hause gehen"
                    ),

                examplePhrase =
                    "Am Morgen stehe ich um sieben Uhr auf, dann fahre ich zur Arbeit."
            ),

            support(
                capabilityId =
                    "a1-state-location",

                learnerTitle =
                    "Describe where something is",

                explanation =
                    "Use simple location phrases to make positions and " +
                            "directions understandable.",

                grammarTargets =
                    listOf(
                        "in + Dativ",
                        "auf + Dativ",
                        "neben + Dativ",
                        "zwischen + Dativ"
                    ),

                vocabularyTargets =
                    listOf(
                        "links",
                        "rechts",
                        "neben",
                        "gegenüber"
                    ),

                examplePhrase =
                    "Die Apotheke ist neben dem Supermarkt, gegenüber der Bank."
            ),

            support(
                capabilityId =
                    "a1-state-possession",

                learnerTitle =
                    "Show who something belongs to",

                explanation =
                    "Use possessive forms to distinguish your things from " +
                            "another person's things.",

                grammarTargets =
                    listOf(
                        "mein / meine",
                        "dein / deine",
                        "sein / seine",
                        "ihr / ihre"
                    ),

                vocabularyTargets =
                    listOf(
                        "gehören",
                        "haben",
                        "meins",
                        "deins"
                    ),

                examplePhrase =
                    "Das ist meine Tasche, aber der Schlüssel gehört meinem Freund."
            ),

            support(
                capabilityId =
                    "a1-add-basic-detail",

                learnerTitle =
                    "Add one useful detail",

                explanation =
                    "Develop a short answer by adding when, where, who, " +
                            "or what kind.",

                grammarTargets =
                    listOf(
                        "und ...",
                        "mit ...",
                        "am / um ...",
                        "in ..."
                    ),

                vocabularyTargets =
                    listOf(
                        "auch",
                        "zusammen",
                        "normalerweise",
                        "manchmal"
                    ),

                examplePhrase =
                    "Ich lerne Deutsch am Abend zusammen mit einem Freund."
            ),

            support(
                capabilityId =
                    "a1-connect-simple-ideas",

                learnerTitle =
                    "Connect simple ideas",

                explanation =
                    "Stop separating every idea into a different sentence. " +
                            "Join related thoughts.",

                grammarTargets =
                    listOf(
                        "und",
                        "aber",
                        "oder",
                        "denn"
                    ),

                vocabularyTargets =
                    listOf(
                        "auch",
                        "dann",
                        "aber",
                        "deshalb"
                    ),

                examplePhrase =
                    "Ich lerne Deutsch, denn ich möchte in Deutschland arbeiten."
            ),

            support(
                capabilityId =
                    "a2-give-simple-reason",

                learnerTitle =
                    "Explain why",

                explanation =
                    "Add a reason after your opinion, preference, or decision.",

                grammarTargets =
                    listOf(
                        "weil + Nebensatz",
                        "denn + Hauptsatz",
                        "deshalb + Hauptsatz",
                        "darum + Hauptsatz"
                    ),

                vocabularyTargets =
                    listOf(
                        "der Grund",
                        "wichtig",
                        "praktisch",
                        "passend"
                    ),

                examplePhrase =
                    "Ich nehme den Zug, weil er schneller und bequemer ist."
            ),

            support(
                capabilityId =
                    "a2-add-supporting-detail",

                learnerTitle =
                    "Support your idea with a detail",

                explanation =
                    "Add a relevant fact, description, or small example " +
                            "after the main point.",

                grammarTargets =
                    listOf(
                        "zum Beispiel",
                        "besonders",
                        "außerdem",
                        "auch"
                    ),

                vocabularyTargets =
                    listOf(
                        "das Beispiel",
                        "die Erfahrung",
                        "besonders",
                        "außerdem"
                    ),

                examplePhrase =
                    "Die Gegend ist praktisch. Zum Beispiel gibt es dort viele Geschäfte."
            ),

            support(
                capabilityId =
                    "a2-explain-preference",

                learnerTitle =
                    "Explain your preference",

                explanation =
                    "Say which option you prefer and give a simple reason.",

                grammarTargets =
                    listOf(
                        "Ich bevorzuge ..., weil ...",
                        "Ich würde lieber ...",
                        "Für mich ist ... besser.",
                        "Am liebsten würde ich ..."
                    ),

                vocabularyTargets =
                    listOf(
                        "bevorzugen",
                        "bequemer",
                        "günstiger",
                        "wichtiger"
                    ),

                examplePhrase =
                    "Ich würde lieber zu Hause arbeiten, weil ich mich dort besser konzentrieren kann."
            ),

            support(
                capabilityId =
                    "a2-make-comparison",

                learnerTitle =
                    "Compare two options directly",

                explanation =
                    "Use comparative forms and clear comparison words.",

                grammarTargets =
                    listOf(
                        "besser als",
                        "größer als",
                        "so ... wie",
                        "im Vergleich zu"
                    ),

                vocabularyTargets =
                    listOf(
                        "günstiger",
                        "schneller",
                        "ruhiger",
                        "praktischer"
                    ),

                examplePhrase =
                    "Die erste Wohnung ist günstiger als die zweite, aber weniger zentral."
            ),

            support(
                capabilityId =
                    "a2-describe-past-experience",

                learnerTitle =
                    "Describe what happened",

                explanation =
                    "Use the conversational past to explain a completed experience.",

                grammarTargets =
                    listOf(
                        "Perfekt mit haben",
                        "Perfekt mit sein",
                        "zuerst ...",
                        "danach ..."
                    ),

                vocabularyTargets =
                    listOf(
                        "passieren",
                        "besuchen",
                        "erleben",
                        "zurückkommen"
                    ),

                examplePhrase =
                    "Am Wochenende bin ich nach Köln gefahren und habe den Dom besucht."
            ),

            support(
                capabilityId =
                    "a2-describe-future-plan",

                learnerTitle =
                    "Describe a future plan",

                explanation =
                    "Explain what you intend to do and when you will do it.",

                grammarTargets =
                    listOf(
                        "Ich werde ...",
                        "Ich möchte ...",
                        "Ich habe vor, ... zu ...",
                        "Nächste Woche ..."
                    ),

                vocabularyTargets =
                    listOf(
                        "planen",
                        "vorhaben",
                        "später",
                        "demnächst"
                    ),

                examplePhrase =
                    "Nächstes Jahr habe ich vor, einen Deutschkurs in Berlin zu besuchen."
            ),

            support(
                capabilityId =
                    "a2-sequence-events",

                learnerTitle =
                    "Put events in a clear order",

                explanation =
                    "Use time connectors so the listener can follow what happened.",

                grammarTargets =
                    listOf(
                        "zuerst",
                        "dann",
                        "danach",
                        "schließlich"
                    ),

                vocabularyTargets =
                    listOf(
                        "anfangen",
                        "fortsetzen",
                        "beenden",
                        "passieren"
                    ),

                examplePhrase =
                    "Zuerst haben wir gegessen, danach sind wir spazieren gegangen."
            ),

            support(
                capabilityId =
                    "a2-give-simple-advice",

                learnerTitle =
                    "Give practical advice",

                explanation =
                    "Recommend a clear action instead of only describing the problem.",

                grammarTargets =
                    listOf(
                        "Du solltest ...",
                        "Du könntest ...",
                        "An deiner Stelle würde ich ...",
                        "Es ist besser, ... zu ..."
                    ),

                vocabularyTargets =
                    listOf(
                        "empfehlen",
                        "versuchen",
                        "vermeiden",
                        "helfen"
                    ),

                examplePhrase =
                    "An deiner Stelle würde ich früher buchen, weil die Tickets dann günstiger sind."
            ),

            support(
                capabilityId =
                    "a2-make-polite-request",

                learnerTitle =
                    "Make your request more polite",

                explanation =
                    "Use polite modal forms instead of a direct command.",

                grammarTargets =
                    listOf(
                        "Könnten Sie ...?",
                        "Würden Sie bitte ...?",
                        "Ich hätte gern ...",
                        "Darf ich ...?"
                    ),

                vocabularyTargets =
                    listOf(
                        "bitte",
                        "Entschuldigung",
                        "möglich",
                        "helfen"
                    ),

                examplePhrase =
                    "Könnten Sie mir bitte sagen, wann der nächste Zug fährt?"
            ),

            support(
                capabilityId =
                    "a2-explain-consequence",

                learnerTitle =
                    "Explain the result",

                explanation =
                    "Connect an action or situation with its consequence.",

                grammarTargets =
                    listOf(
                        "deshalb",
                        "darum",
                        "daher",
                        "also"
                    ),

                vocabularyTargets =
                    listOf(
                        "die Folge",
                        "das Ergebnis",
                        "führen zu",
                        "passieren"
                    ),

                examplePhrase =
                    "Der Bus hatte Verspätung, deshalb bin ich zu spät angekommen."
            ),

            support(
                capabilityId =
                    "b1-develop-opinion",

                learnerTitle =
                    "Develop your opinion beyond one sentence",

                explanation =
                    "State your position, give a reason, and add one supporting detail.",

                grammarTargets =
                    listOf(
                        "meiner Meinung nach",
                        "weil + Nebensatz",
                        "außerdem",
                        "zum Beispiel"
                    ),

                vocabularyTargets =
                    listOf(
                        "die Meinung",
                        "der Grund",
                        "überzeugend",
                        "sinnvoll"
                    ),

                examplePhrase =
                    "Meiner Meinung nach ist Homeoffice sinnvoll, weil es Zeit spart. Außerdem kann man sich oft besser konzentrieren."
            ),

            support(
                capabilityId =
                    "b1-support-opinion-example",

                learnerTitle =
                    "Support your opinion with an example",

                explanation =
                    "After giving a reason, demonstrate what you mean with a concrete example.",

                grammarTargets =
                    listOf(
                        "zum Beispiel",
                        "beispielsweise",
                        "das zeigt sich daran, dass ...",
                        "ein gutes Beispiel dafür ist ..."
                    ),

                vocabularyTargets =
                    listOf(
                        "das Beispiel",
                        "die Erfahrung",
                        "belegen",
                        "zeigen"
                    ),

                examplePhrase =
                    "Viele Menschen lernen online erfolgreicher. Ein gutes Beispiel dafür sind flexible Abendkurse."
            ),

            support(
                capabilityId =
                    "b1-explain-cause-consequence",

                learnerTitle =
                    "Connect causes and consequences",

                explanation =
                    "Explain both why something happened and what followed from it.",

                grammarTargets =
                    listOf(
                        "weil / da + Nebensatz",
                        "deshalb / daher",
                        "sodass + Nebensatz",
                        "aus diesem Grund"
                    ),

                vocabularyTargets =
                    listOf(
                        "die Ursache",
                        "die Folge",
                        "verursachen",
                        "führen zu"
                    ),

                examplePhrase =
                    "Da die Mieten gestiegen sind, ziehen viele Menschen weiter nach außen, sodass ihre Arbeitswege länger werden."
            ),

            support(
                capabilityId =
                    "b1-narrate-connected-events",

                learnerTitle =
                    "Tell events as a connected story",

                explanation =
                    "Link background, main events, and outcomes instead of listing isolated actions.",

                grammarTargets =
                    listOf(
                        "als + Vergangenheit",
                        "während + Nebensatz",
                        "nachdem + Plusquamperfekt",
                        "bevor + Nebensatz"
                    ),

                vocabularyTargets =
                    listOf(
                        "zunächst",
                        "anschließend",
                        "plötzlich",
                        "schließlich"
                    ),

                examplePhrase =
                    "Nachdem wir angekommen waren, haben wir zunächst eingecheckt. Während wir warteten, hat es plötzlich stark geregnet."
            ),

            support(
                capabilityId =
                    "b1-explain-problem",

                learnerTitle =
                    "Explain a problem precisely",

                explanation =
                    "Describe what is wrong, when it occurs, and why it matters.",

                grammarTargets =
                    listOf(
                        "Das Problem besteht darin, dass ...",
                        "Seit ...",
                        "Immer wenn ...",
                        "Dadurch ..."
                    ),

                vocabularyTargets =
                    listOf(
                        "das Problem",
                        "die Ursache",
                        "betreffen",
                        "verhindern"
                    ),

                examplePhrase =
                    "Das Problem besteht darin, dass die Verbindung jeden Abend abbricht. Dadurch kann ich nicht zuverlässig arbeiten."
            ),

            support(
                capabilityId =
                    "b1-propose-solution",

                learnerTitle =
                    "Propose and justify a solution",

                explanation =
                    "Suggest an action and explain how it could improve the situation.",

                grammarTargets =
                    listOf(
                        "Wir könnten ...",
                        "Eine Möglichkeit wäre ...",
                        "Wenn wir ..., könnten wir ...",
                        "Dadurch würde ..."
                    ),

                vocabularyTargets =
                    listOf(
                        "die Lösung",
                        "die Möglichkeit",
                        "verbessern",
                        "vermeiden"
                    ),

                examplePhrase =
                    "Eine Möglichkeit wäre, flexible Arbeitszeiten einzuführen. Dadurch könnten die Mitarbeitenden den Berufsverkehr vermeiden."
            ),

            support(
                capabilityId =
                    "b1-justify-decision",

                learnerTitle =
                    "Justify your decision with criteria",

                explanation =
                    "Name your choice, explain the deciding criterion, and acknowledge a trade-off.",

                grammarTargets =
                    listOf(
                        "Ich habe mich für ... entschieden, weil ...",
                        "Der wichtigste Grund ist ...",
                        "Obwohl ..., würde ich ...",
                        "Ausschlaggebend ist ..."
                    ),

                vocabularyTargets =
                    listOf(
                        "die Entscheidung",
                        "ausschlaggebend",
                        "der Vorteil",
                        "der Nachteil"
                    ),

                examplePhrase =
                    "Ich habe mich für die kleinere Wohnung entschieden, weil die Lage wichtiger ist, obwohl die andere günstiger wäre."
            ),

            support(
                capabilityId =
                    "b1-express-uncertainty",

                learnerTitle =
                    "Express uncertainty more precisely",

                explanation =
                    "Show whether something is possible, probable, or still unknown.",

                grammarTargets =
                    listOf(
                        "Ich bin nicht sicher, ob ...",
                        "Es könnte sein, dass ...",
                        "Vielleicht ...",
                        "Wahrscheinlich ..."
                    ),

                vocabularyTargets =
                    listOf(
                        "möglicherweise",
                        "vermutlich",
                        "wahrscheinlich",
                        "unklar"
                    ),

                examplePhrase =
                    "Ich bin nicht sicher, ob der Termin noch frei ist, aber wahrscheinlich können wir ihn verschieben."
            ),

            support(
                capabilityId =
                    "b1-discuss-advantages-disadvantages",

                learnerTitle =
                    "Balance advantages and disadvantages",

                explanation =
                    "Present meaningful positive and negative aspects before reaching a conclusion.",

                grammarTargets =
                    listOf(
                        "einerseits ... andererseits",
                        "zwar ... aber",
                        "ein Vorteil ist, dass ...",
                        "dagegen spricht ..."
                    ),

                vocabularyTargets =
                    listOf(
                        "der Vorteil",
                        "der Nachteil",
                        "dafür",
                        "dagegen"
                    ),

                examplePhrase =
                    "Einerseits ist die Wohnung zentral, andererseits ist sie deutlich teurer."
            ),

            support(
                capabilityId =
                    "b1-handle-disagreement",

                learnerTitle =
                    "Disagree without ending the discussion",

                explanation =
                    "Acknowledge the other view before explaining where your position differs.",

                grammarTargets =
                    listOf(
                        "Ich verstehe deinen Punkt, aber ...",
                        "Da stimme ich nur teilweise zu.",
                        "Das sehe ich anders, weil ...",
                        "Trotzdem ..."
                    ),

                vocabularyTargets =
                    listOf(
                        "der Standpunkt",
                        "zustimmen",
                        "widersprechen",
                        "trotzdem"
                    ),

                examplePhrase =
                    "Ich verstehe deinen Punkt, aber ich sehe das anders, weil die langfristigen Kosten höher sind."
            ),

            support(
                capabilityId =
                    "b2-qualify-opinion",

                learnerTitle =
                    "Add limits and exceptions to your opinion",

                explanation =
                    "Avoid absolute claims by showing when your opinion applies and when it may not.",

                grammarTargets =
                    listOf(
                        "zwar ... aber",
                        "obwohl + Nebensatz",
                        "wenn / falls + Nebensatz",
                        "unter der Voraussetzung, dass ..."
                    ),

                vocabularyTargets =
                    listOf(
                        "grundsätzlich",
                        "allerdings",
                        "teilweise",
                        "unter bestimmten Umständen"
                    ),

                examplePhrase =
                    "Grundsätzlich stimme ich zu, allerdings gilt das nur, wenn genügend Unterstützung vorhanden ist."
            ),

            support(
                capabilityId =
                    "b2-acknowledge-counterargument",

                learnerTitle =
                    "Acknowledge a counterargument",

                explanation =
                    "Recognize a reasonable opposing point before developing your own position.",

                grammarTargets =
                    listOf(
                        "Zwar ..., aber ...",
                        "Es stimmt, dass ..., dennoch ...",
                        "Obwohl dieser Einwand berechtigt ist, ...",
                        "Man könnte einwenden, dass ..."
                    ),

                vocabularyTargets =
                    listOf(
                        "der Einwand",
                        "der Gegenpunkt",
                        "berechtigt",
                        "dennoch"
                    ),

                examplePhrase =
                    "Es stimmt, dass die Lösung teuer ist, dennoch könnte sie langfristig Kosten sparen."
            ),

            support(
                capabilityId =
                    "b2-contrast-perspectives",

                learnerTitle =
                    "Contrast different perspectives",

                explanation =
                    "Show clearly how two positions differ in assumptions, priorities, or consequences.",

                grammarTargets =
                    listOf(
                        "während + Nebensatz",
                        "hingegen",
                        "im Gegensatz dazu",
                        "einerseits ... andererseits"
                    ),

                vocabularyTargets =
                    listOf(
                        "die Perspektive",
                        "der Standpunkt",
                        "der Gegensatz",
                        "unterschiedlich gewichten"
                    ),

                examplePhrase =
                    "Während die eine Seite vor allem die Kosten berücksichtigt, legt die andere größeren Wert auf die Umweltfolgen."
            ),

            support(
                capabilityId =
                    "b2-support-claim-evidence",

                learnerTitle =
                    "Support your claim with stronger evidence",

                explanation =
                    "Back your position with a fact, observation, example, or explained relationship.",

                grammarTargets =
                    listOf(
                        "Das zeigt sich daran, dass ...",
                        "Dafür spricht, dass ...",
                        "Ein Beispiel dafür ist ...",
                        "Dies lässt sich dadurch erklären, dass ..."
                    ),

                vocabularyTargets =
                    listOf(
                        "der Beleg",
                        "die Beobachtung",
                        "nachweisen",
                        "darauf hindeuten"
                    ),

                examplePhrase =
                    "Dafür spricht, dass die Zahl der Teilnehmenden gestiegen ist, seit der Kurs flexibler angeboten wird."
            ),

            support(
                capabilityId =
                    "b2-evaluate-alternatives",

                learnerTitle =
                    "Evaluate alternatives using clear criteria",

                explanation =
                    "Compare several options according to cost, impact, practicality, or risk before deciding.",

                grammarTargets =
                    listOf(
                        "im Hinblick auf",
                        "verglichen mit",
                        "je ..., desto ...",
                        "unter Berücksichtigung von"
                    ),

                vocabularyTargets =
                    listOf(
                        "das Kriterium",
                        "abwägen",
                        "geeignet",
                        "langfristig"
                    ),

                examplePhrase =
                    "Unter Berücksichtigung der Kosten und der Entfernung ist die zweite Möglichkeit langfristig geeigneter."
            ),

            support(
                capabilityId =
                    "b2-hedge-claim",

                learnerTitle =
                    "Make strong claims more cautious",

                explanation =
                    "Express the appropriate degree of probability instead of sounding completely certain.",

                grammarTargets =
                    listOf(
                        "Es könnte sein, dass ...",
                        "Es scheint, als ob ...",
                        "dürfte + Infinitiv",
                        "lässt vermuten, dass ..."
                    ),

                vocabularyTargets =
                    listOf(
                        "vermutlich",
                        "möglicherweise",
                        "offenbar",
                        "wahrscheinlich"
                    ),

                examplePhrase =
                    "Das dürfte vor allem daran liegen, dass viele Beschäftigte flexiblere Arbeitszeiten bevorzugen."
            ),

            support(
                capabilityId =
                    "b2-explain-exception",

                learnerTitle =
                    "Explain when a general rule does not apply",

                explanation =
                    "Identify an exception and explain the condition that makes it different.",

                grammarTargets =
                    listOf(
                        "außer wenn ...",
                        "es sei denn, ...",
                        "in dem Fall, dass ...",
                        "eine Ausnahme besteht, wenn ..."
                    ),

                vocabularyTargets =
                    listOf(
                        "die Ausnahme",
                        "gelten",
                        "abweichen",
                        "Sonderfall"
                    ),

                examplePhrase =
                    "Die Regel ist sinnvoll, außer wenn dadurch Menschen mit besonderen Bedürfnissen benachteiligt werden."
            ),

            support(
                capabilityId =
                    "b2-multi-step-argument",

                learnerTitle =
                    "Build a multi-step argument",

                explanation =
                    "Develop your position through several connected reasons rather than relying on one point.",

                grammarTargets =
                    listOf(
                        "zunächst",
                        "darüber hinaus",
                        "entscheidend ist außerdem, dass ...",
                        "daraus folgt, dass ..."
                    ),

                vocabularyTargets =
                    listOf(
                        "das Argument",
                        "der Zusammenhang",
                        "entscheidend",
                        "folglich"
                    ),

                examplePhrase =
                    "Zunächst spart die Maßnahme Zeit. Darüber hinaus senkt sie die Kosten. Daraus folgt, dass sie langfristig sinnvoll ist."
            ),

            support(
                capabilityId =
                    "b2-negotiate-disagreement",

                learnerTitle =
                    "Seek common ground during disagreement",

                explanation =
                    "State your disagreement while proposing a workable compromise.",

                grammarTargets =
                    listOf(
                        "Ich kann nachvollziehen, dass ...",
                        "Könnten wir uns darauf einigen, dass ...?",
                        "Ein Kompromiss wäre ...",
                        "Unter dieser Bedingung könnte ich ..."
                    ),

                vocabularyTargets =
                    listOf(
                        "der Kompromiss",
                        "sich einigen",
                        "entgegenkommen",
                        "gemeinsame Lösung"
                    ),

                examplePhrase =
                    "Ich kann deine Bedenken nachvollziehen. Ein Kompromiss wäre, die Änderung zunächst drei Monate lang zu testen."
            ),

            support(
                capabilityId =
                    "b2-reformulate-precisely",

                learnerTitle =
                    "Reformulate vague ideas more precisely",

                explanation =
                    "Restate your idea when the first version is too broad, vague, or easy to misunderstand.",

                grammarTargets =
                    listOf(
                        "Genauer gesagt ...",
                        "Damit meine ich, dass ...",
                        "Anders formuliert ...",
                        "Es geht nicht um ..., sondern um ..."
                    ),

                vocabularyTargets =
                    listOf(
                        "genauer gesagt",
                        "konkret",
                        "präzisieren",
                        "missverständlich"
                    ),

                examplePhrase =
                    "Anders formuliert geht es nicht darum, weniger zu arbeiten, sondern die Arbeitszeit flexibler zu verteilen."
            )
        )
            .associateBy(
                CapabilityTeachingSupport::capabilityId
            )

    fun get(
        capabilityId: String
    ): CapabilityTeachingSupport? {
        return supportByCapabilityId[
            capabilityId
        ]
    }

    fun requireOrFallback(
        capabilityId: String,
        capabilityName: String,
        capabilityDescription: String
    ): CapabilityTeachingSupport {
        return get(
            capabilityId =
                capabilityId
        )
            ?: CapabilityTeachingSupport(
                capabilityId =
                    capabilityId,

                learnerTitle =
                    capabilityName,

                explanation =
                    capabilityDescription,

                grammarTargets =
                    fallbackGrammarTargets(
                        capabilityId =
                            capabilityId
                    ),

                vocabularyTargets =
                    fallbackVocabularyTargets(
                        capabilityId =
                            capabilityId
                    ),

                examplePhrase =
                    fallbackExamplePhrase(
                        capabilityId =
                            capabilityId
                    )
            )
    }

    private fun fallbackGrammarTargets(
        capabilityId: String
    ): List<String> {
        return when {
            capabilityId.startsWith(
                prefix = "c2-"
            ) ->
                listOf(
                    "insofern als ...",
                    "nicht zuletzt deshalb, weil ...",
                    "unter der Annahme, dass ..."
                )

            capabilityId.startsWith(
                prefix = "c1-"
            ) ->
                listOf(
                    "wenngleich + Nebensatz",
                    "insofern ... als",
                    "unter Berücksichtigung dessen, dass ..."
                )

            capabilityId.startsWith(
                prefix = "b2-"
            ) ->
                listOf(
                    "zwar ... aber",
                    "obwohl + Nebensatz",
                    "unter der Voraussetzung, dass ..."
                )

            capabilityId.startsWith(
                prefix = "b1-"
            ) ->
                listOf(
                    "weil + Nebensatz",
                    "obwohl + Nebensatz",
                    "deshalb + Hauptsatz"
                )

            capabilityId.startsWith(
                prefix = "a2-"
            ) ->
                listOf(
                    "weil + Nebensatz",
                    "dann",
                    "deshalb"
                )

            else ->
                listOf(
                    "und",
                    "aber",
                    "Ich möchte ..."
                )
        }
    }

    private fun fallbackVocabularyTargets(
        capabilityId: String
    ): List<String> {
        return when {
            capabilityId.startsWith(
                prefix = "c2-"
            ) ->
                listOf(
                    "in gewisser Hinsicht",
                    "implizit",
                    "differenziert"
                )

            capabilityId.startsWith(
                prefix = "c1-"
            ) ->
                listOf(
                    "allerdings",
                    "maßgeblich",
                    "differenzieren"
                )

            capabilityId.startsWith(
                prefix = "b2-"
            ) ->
                listOf(
                    "grundsätzlich",
                    "dennoch",
                    "unter bestimmten Umständen"
                )

            capabilityId.startsWith(
                prefix = "b1-"
            ) ->
                listOf(
                    "der Grund",
                    "der Vorteil",
                    "allerdings"
                )

            capabilityId.startsWith(
                prefix = "a2-"
            ) ->
                listOf(
                    "deshalb",
                    "zum Beispiel",
                    "besser"
                )

            else ->
                listOf(
                    "gern",
                    "auch",
                    "aber"
                )
        }
    }

    private fun fallbackExamplePhrase(
        capabilityId: String
    ): String {
        return when {
            capabilityId.startsWith(
                prefix = "c2-"
            ) ->
                "In gewisser Hinsicht ist diese Position nachvollziehbar, wobei ihre impliziten Voraussetzungen genauer betrachtet werden müssten."

            capabilityId.startsWith(
                prefix = "c1-"
            ) ->
                "Wenngleich dieses Argument überzeugend wirkt, sollte berücksichtigt werden, dass die Folgen je nach Kontext unterschiedlich ausfallen."

            capabilityId.startsWith(
                prefix = "b2-"
            ) ->
                "Grundsätzlich stimme ich zu, allerdings gilt das nur unter bestimmten Umständen."

            capabilityId.startsWith(
                prefix = "b1-"
            ) ->
                "Meiner Meinung nach ist das sinnvoll, weil es einen wichtigen Vorteil bietet."

            capabilityId.startsWith(
                prefix = "a2-"
            ) ->
                "Ich finde diese Möglichkeit besser, weil sie praktischer ist."

            else ->
                "Ich mag diese Möglichkeit, aber ich möchte noch mehr Informationen."
        }
    }

    private fun support(
        capabilityId: String,
        learnerTitle: String,
        explanation: String,
        grammarTargets: List<String>,
        vocabularyTargets: List<String>,
        examplePhrase: String
    ): CapabilityTeachingSupport {
        return CapabilityTeachingSupport(
            capabilityId =
                capabilityId,

            learnerTitle =
                learnerTitle,

            explanation =
                explanation,

            grammarTargets =
                grammarTargets,

            vocabularyTargets =
                vocabularyTargets,

            examplePhrase =
                examplePhrase
        )
    }
}