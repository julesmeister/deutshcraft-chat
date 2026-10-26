package com.julesmeister.deutschcraft.feature.mission.domain.recommendation

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import javax.inject.Inject
import javax.inject.Singleton

/*
 * CEFR-aware curriculum of real-world conversation missions.
 *
 * Recommendations should begin here rather than allowing the
 * latest conversation topic to completely determine the next
 * mission.
 */
@Singleton
class CefrMissionScenarioCatalog
@Inject constructor() {

    fun scenariosFor(
        level: CefrLevel
    ): List<MissionScenarioDefinition> {
        return allScenarios.filter { scenario ->
            scenario.supports(
                level =
                    level
            )
        }
    }

    fun scenarioFor(
        type: MissionScenarioType
    ): MissionScenarioDefinition? {
        return allScenarios.firstOrNull { scenario ->
            scenario.type ==
                    type
        }
    }

    private val allScenarios:
            List<MissionScenarioDefinition> by lazy {
        buildList {
            addAll(
                a1Scenarios()
            )

            addAll(
                a2Scenarios()
            )

            addAll(
                b1Scenarios()
            )

            addAll(
                b2Scenarios()
            )

            addAll(
                c1Scenarios()
            )

            addAll(
                c2Scenarios()
            )
        }
    }

    private fun a1Scenarios():
            List<MissionScenarioDefinition> {
        return listOf(
            scenario(
                type =
                    MissionScenarioType
                        .INTRODUCING_YOURSELF,
                title =
                    "Sich vorstellen",
                description =
                    "Führe ein erstes kurzes Gespräch mit " +
                            "einer neuen Person.",
                learnerRole =
                    "Eine Person, die sich vorstellt",
                assistantRole =
                    "Eine freundliche neue Bekanntschaft",
                minimumLevel =
                    CefrLevel.A1,
                maximumLevel =
                    CefrLevel.A2,
                targetSkills =
                    setOf(
                        "Begrüßung",
                        "Persönliche Angaben",
                        "Einfache Fragen",
                        "Gesprächsabschluss"
                    ),
                objectives =
                    listOf(
                        "Die andere Person begrüßen",
                        "Den eigenen Namen nennen",
                        "Nach dem Namen fragen",
                        "Den eigenen Wohnort nennen",
                        "Nach dem Wohnort fragen",
                        "Eine Sprache nennen",
                        "Ein Hobby nennen",
                        "Eine einfache Rückfrage stellen",
                        "Auf eine persönliche Frage antworten",
                        "Das Gespräch höflich beenden"
                    ),
                keywords =
                    setOf(
                        "name",
                        "heiße",
                        "wohne",
                        "komme",
                        "vorstellen"
                    )
            ),
            scenario(
                type =
                    MissionScenarioType
                        .GROCERY_SHOPPING,
                title =
                    "Im Supermarkt",
                description =
                    "Finde Produkte und erledige einen " +
                            "einfachen Einkauf.",
                learnerRole =
                    "Kundin oder Kunde",
                assistantRole =
                    "Mitarbeiterin oder Mitarbeiter im Supermarkt",
                minimumLevel =
                    CefrLevel.A1,
                maximumLevel =
                    CefrLevel.A2,
                targetSkills =
                    setOf(
                        "Produkte",
                        "Mengen",
                        "Preise",
                        "Höfliche Fragen"
                    ),
                objectives =
                    listOf(
                        "Eine Mitarbeiterin oder einen Mitarbeiter ansprechen",
                        "Nach einem Produkt fragen",
                        "Nach dem Standort des Produkts fragen",
                        "Eine Menge angeben",
                        "Nach einer Alternative fragen",
                        "Nach dem Preis fragen",
                        "Eine Rückfrage verstehen",
                        "Eine Auswahl bestätigen",
                        "Nach der Kasse fragen",
                        "Sich bedanken"
                    ),
                keywords =
                    setOf(
                        "supermarkt",
                        "einkaufen",
                        "lebensmittel",
                        "preis"
                    )
            ),
            scenario(
                type =
                    MissionScenarioType
                        .ASKING_FOR_DIRECTIONS,
                title =
                    "Nach dem Weg fragen",
                description =
                    "Bitte um Hilfe und verstehe eine einfache " +
                            "Wegbeschreibung.",
                learnerRole =
                    "Eine Person, die den Weg sucht",
                assistantRole =
                    "Eine ortskundige Person",
                minimumLevel =
                    CefrLevel.A1,
                maximumLevel =
                    CefrLevel.A2,
                targetSkills =
                    setOf(
                        "Orte",
                        "Richtungen",
                        "Höfliche Fragen",
                        "Verständnissicherung"
                    ),
                objectives =
                    listOf(
                        "Eine Person höflich ansprechen",
                        "Nach einem Ort fragen",
                        "Den Zielort genauer nennen",
                        "Eine Richtungsangabe verstehen",
                        "Nach links oder rechts fragen",
                        "Nach der Entfernung fragen",
                        "Nach einem Orientierungspunkt fragen",
                        "Eine Anweisung wiederholen",
                        "Das Verständnis bestätigen",
                        "Sich für die Hilfe bedanken"
                    ),
                keywords =
                    setOf(
                        "weg",
                        "bahnhof",
                        "straße",
                        "wo",
                        "richtung"
                    )
            ),
            scenario(
                type =
                    MissionScenarioType
                        .DAILY_ROUTINE,
                title =
                    "Mein Tagesablauf",
                description =
                    "Sprich über einen normalen Tag.",
                learnerRole =
                    "Eine Person, die ihren Tag beschreibt",
                assistantRole =
                    "Eine interessierte Gesprächspartnerin oder ein interessierter Gesprächspartner",
                minimumLevel =
                    CefrLevel.A1,
                maximumLevel =
                    CefrLevel.A2,
                targetSkills =
                    setOf(
                        "Uhrzeiten",
                        "Alltag",
                        "Präsens",
                        "Reihenfolge"
                    ),
                objectives =
                    listOf(
                        "Sagen, wann du aufstehst",
                        "Über das Frühstück sprechen",
                        "Den Weg zur Arbeit oder Schule beschreiben",
                        "Eine Vormittagsaktivität nennen",
                        "Über das Mittagessen sprechen",
                        "Eine Nachmittagsaktivität nennen",
                        "Über Freizeit sprechen",
                        "Eine Abendroutine beschreiben",
                        "Eine Uhrzeit nennen",
                        "Eine Rückfrage zum Alltag stellen"
                    ),
                keywords =
                    setOf(
                        "morgen",
                        "alltag",
                        "arbeit",
                        "schule",
                        "tagesablauf"
                    )
            ),
            scenario(
                type =
                    MissionScenarioType
                        .RESTAURANT_ORDERING,
                title =
                    "Im Restaurant bestellen",
                description =
                    "Bestelle eine vollständige Mahlzeit in " +
                            "einem Restaurant.",
                learnerRole =
                    "Restaurantgast",
                assistantRole =
                    "Servicekraft im Restaurant",
                minimumLevel =
                    CefrLevel.A1,
                maximumLevel =
                    CefrLevel.A2,
                targetSkills =
                    setOf(
                        "Speisekarte",
                        "Bestellung",
                        "Rückfragen",
                        "Bezahlen"
                    ),
                objectives =
                    listOf(
                        "Die Servicekraft begrüßen",
                        "Nach der Speisekarte fragen",
                        "Nach einem Gericht fragen",
                        "Nach einer Empfehlung fragen",
                        "Ein Getränk bestellen",
                        "Ein Hauptgericht bestellen",
                        "Auf eine Rückfrage reagieren",
                        "Eine Änderung oder Beilage wählen",
                        "Nach der Rechnung fragen",
                        "Das Gespräch höflich beenden"
                    ),
                keywords =
                    setOf(
                        "restaurant",
                        "essen",
                        "speisekarte",
                        "gericht"
                    )
            ),
            scenario(
                type =
                    MissionScenarioType
                        .CAFE_ORDERING,
                title =
                    "Im Café bestellen",
                description =
                    "Bestelle ein Getränk und etwas Kleines " +
                            "zu essen.",
                learnerRole =
                    "Gast im Café",
                assistantRole =
                    "Servicekraft im Café",
                minimumLevel =
                    CefrLevel.A1,
                maximumLevel =
                    CefrLevel.A2,
                targetSkills =
                    setOf(
                        "Getränke",
                        "Bestellung",
                        "Größe",
                        "Bezahlen"
                    ),
                objectives =
                    listOf(
                        "Die Servicekraft begrüßen",
                        "Nach den Getränken fragen",
                        "Nach einer Empfehlung fragen",
                        "Ein Getränk auswählen",
                        "Eine Größe auswählen",
                        "Eine Änderung wünschen",
                        "Etwas zu essen bestellen",
                        "Auf eine Rückfrage reagieren",
                        "Nach dem Preis fragen",
                        "Bezahlen und sich verabschieden"
                    ),
                keywords =
                    setOf(
                        "café",
                        "kaffee",
                        "tee",
                        "bestellen"
                    )
            )
        )
    }

    private fun a2Scenarios():
            List<MissionScenarioDefinition> {
        return listOf(
            scenario(
                type =
                    MissionScenarioType
                        .HOTEL_CHECK_IN,
                title =
                    "Im Hotel einchecken",
                description =
                    "Checke in einem Hotel ein und kläre " +
                            "wichtige Informationen.",
                learnerRole =
                    "Hotelgast",
                assistantRole =
                    "Mitarbeiterin oder Mitarbeiter an der Rezeption",
                minimumLevel =
                    CefrLevel.A2,
                maximumLevel =
                    CefrLevel.B1,
                targetSkills =
                    setOf(
                        "Reservierung",
                        "Persönliche Angaben",
                        "Hotelausstattung",
                        "Rückfragen"
                    ),
                objectives =
                    listOf(
                        "Die Rezeption begrüßen",
                        "Eine Reservierung nennen",
                        "Den eigenen Namen buchstabieren",
                        "Die Aufenthaltsdauer bestätigen",
                        "Nach dem Frühstück fragen",
                        "Nach dem WLAN fragen",
                        "Nach der Zimmerlage fragen",
                        "Eine Rückfrage beantworten",
                        "Nach der Abreisezeit fragen",
                        "Sich bedanken"
                    ),
                keywords =
                    setOf(
                        "hotel",
                        "reise",
                        "zimmer",
                        "reservierung"
                    )
            ),
            scenario(
                type =
                    MissionScenarioType
                        .DOCTOR_APPOINTMENT,
                title =
                    "Beim Arzt",
                description =
                    "Beschreibe einfache Beschwerden und " +
                            "verstehe grundlegende Empfehlungen.",
                learnerRole =
                    "Patientin oder Patient",
                assistantRole =
                    "Ärztin oder Arzt",
                minimumLevel =
                    CefrLevel.A2,
                maximumLevel =
                    CefrLevel.B1,
                targetSkills =
                    setOf(
                        "Beschwerden",
                        "Dauer",
                        "Rückfragen",
                        "Empfehlungen"
                    ),
                objectives =
                    listOf(
                        "Die Beschwerden nennen",
                        "Sagen, seit wann sie bestehen",
                        "Die Stärke beschreiben",
                        "Eine weitere Beobachtung nennen",
                        "Eine Rückfrage beantworten",
                        "Nach einer möglichen Ursache fragen",
                        "Nach einer Behandlung fragen",
                        "Nach Medikamenten fragen",
                        "Eine Empfehlung wiederholen",
                        "Das Gespräch höflich beenden"
                    ),
                keywords =
                    setOf(
                        "arzt",
                        "krank",
                        "schmerzen",
                        "termin",
                        "gesundheit"
                    )
            ),
            scenario(
                type =
                    MissionScenarioType
                        .RETURNING_A_PRODUCT,
                title =
                    "Ein Produkt zurückgeben",
                description =
                    "Erkläre ein Problem und bitte um eine " +
                            "passende Lösung.",
                learnerRole =
                    "Kundin oder Kunde",
                assistantRole =
                    "Mitarbeiterin oder Mitarbeiter im Geschäft",
                minimumLevel =
                    CefrLevel.A2,
                maximumLevel =
                    CefrLevel.B1,
                targetSkills =
                    setOf(
                        "Problem beschreiben",
                        "Kaufdetails",
                        "Lösung erfragen",
                        "Höflichkeit"
                    ),
                objectives =
                    listOf(
                        "Eine Mitarbeiterin oder einen Mitarbeiter ansprechen",
                        "Das Produkt nennen",
                        "Den Kaufzeitpunkt nennen",
                        "Das Problem beschreiben",
                        "Einen Beleg erwähnen",
                        "Auf eine Rückfrage antworten",
                        "Um Umtausch oder Erstattung bitten",
                        "Eine Alternative besprechen",
                        "Die Lösung bestätigen",
                        "Sich bedanken"
                    ),
                keywords =
                    setOf(
                        "zurückgeben",
                        "umtausch",
                        "kaputt",
                        "produkt",
                        "kaufen"
                    )
            ),
            scenario(
                type =
                    MissionScenarioType
                        .PLANNING_A_TRIP,
                title =
                    "Eine Reise planen",
                description =
                    "Plane gemeinsam eine kurze Reise.",
                learnerRole =
                    "Mitreisende Person",
                assistantRole =
                    "Reisepartnerin oder Reisepartner",
                minimumLevel =
                    CefrLevel.A2,
                maximumLevel =
                    CefrLevel.B1,
                targetSkills =
                    setOf(
                        "Vorschläge",
                        "Zeit",
                        "Verkehrsmittel",
                        "Unterkunft"
                    ),
                objectives =
                    listOf(
                        "Ein Reiseziel vorschlagen",
                        "Einen Grund für das Ziel nennen",
                        "Einen Termin vorschlagen",
                        "Über die Reisedauer sprechen",
                        "Ein Verkehrsmittel auswählen",
                        "Über die Unterkunft sprechen",
                        "Eine Aktivität vorschlagen",
                        "Auf einen Gegenvorschlag reagieren",
                        "Eine Entscheidung bestätigen",
                        "Den nächsten Planungsschritt vereinbaren"
                    ),
                keywords =
                    setOf(
                        "reise",
                        "urlaub",
                        "fahren",
                        "fliegen",
                        "hotel"
                    )
            )
        )
    }

    private fun b1Scenarios():
            List<MissionScenarioDefinition> {
        return listOf(
            scenario(
                type =
                    MissionScenarioType
                        .JOB_INTERVIEW,
                title =
                    "Vorstellungsgespräch",
                description =
                    "Führe ein strukturiertes " +
                            "Vorstellungsgespräch.",
                learnerRole =
                    "Bewerberin oder Bewerber",
                assistantRole =
                    "Interviewer oder Interviewerin",
                minimumLevel =
                    CefrLevel.B1,
                maximumLevel =
                    CefrLevel.B2,
                targetSkills =
                    setOf(
                        "Berufserfahrung",
                        "Begründungen",
                        "Stärken",
                        "Rückfragen"
                    ),
                objectives =
                    listOf(
                        "Sich professionell vorstellen",
                        "Den eigenen beruflichen Hintergrund erklären",
                        "Eine relevante Erfahrung beschreiben",
                        "Eine persönliche Stärke nennen",
                        "Die Stärke mit einem Beispiel begründen",
                        "Über eine Herausforderung sprechen",
                        "Die Motivation für die Stelle erklären",
                        "Nach den Aufgaben fragen",
                        "Nach Arbeitsbedingungen fragen",
                        "Das Gespräch professionell beenden"
                    ),
                keywords =
                    setOf(
                        "arbeit",
                        "bewerbung",
                        "interview",
                        "beruf",
                        "stelle"
                    )
            ),
            scenario(
                type =
                    MissionScenarioType
                        .MAKING_A_DECISION,
                title =
                    "Gemeinsam eine Entscheidung treffen",
                description =
                    "Vergleiche Möglichkeiten und treffe eine " +
                            "begründete Entscheidung.",
                learnerRole =
                    "Eine Person, die mitentscheidet",
                assistantRole =
                    "Diskussionspartnerin oder Diskussionspartner",
                minimumLevel =
                    CefrLevel.B1,
                maximumLevel =
                    CefrLevel.B2,
                targetSkills =
                    setOf(
                        "Meinung",
                        "Vergleich",
                        "Begründung",
                        "Entscheidung"
                    ),
                objectives =
                    listOf(
                        "Eine Präferenz ausdrücken",
                        "Einen Grund nennen",
                        "Nach der Meinung der anderen Person fragen",
                        "Zwei Möglichkeiten vergleichen",
                        "Einen Vorteil erklären",
                        "Einen Nachteil erklären",
                        "Auf einen Einwand reagieren",
                        "Einen Kompromiss vorschlagen",
                        "Eine Entscheidung treffen",
                        "Die Entscheidung zusammenfassen"
                    ),
                keywords =
                    setOf(
                        "entscheiden",
                        "wahl",
                        "besser",
                        "vergleich",
                        "meinung"
                    )
            ),
            scenario(
                type =
                    MissionScenarioType
                        .MAKING_A_COMPLAINT,
                title =
                    "Eine Beschwerde klären",
                description =
                    "Beschreibe ein Problem sachlich und finde " +
                            "eine Lösung.",
                learnerRole =
                    "Kundin oder Kunde mit einem Problem",
                assistantRole =
                    "Mitarbeiterin oder Mitarbeiter im Kundenservice",
                minimumLevel =
                    CefrLevel.B1,
                maximumLevel =
                    CefrLevel.B2,
                targetSkills =
                    setOf(
                        "Problemschilderung",
                        "Details",
                        "Lösungsvorschläge",
                        "Höfliche Beharrlichkeit"
                    ),
                objectives =
                    listOf(
                        "Das Anliegen höflich einleiten",
                        "Das Problem klar beschreiben",
                        "Den Zeitpunkt oder Kontext nennen",
                        "Die Auswirkungen erklären",
                        "Auf eine Rückfrage reagieren",
                        "Eine gewünschte Lösung nennen",
                        "Auf eine erste Lösung reagieren",
                        "Eine Alternative vorschlagen",
                        "Eine Vereinbarung bestätigen",
                        "Das Gespräch sachlich beenden"
                    ),
                keywords =
                    setOf(
                        "beschwerde",
                        "problem",
                        "service",
                        "lösung",
                        "unzufrieden"
                    )
            ),
            scenario(
                type =
                    MissionScenarioType
                        .TELLING_A_STORY,
                title =
                    "Eine Geschichte erzählen",
                description =
                    "Erzähle ein Erlebnis verständlich und " +
                            "zusammenhängend.",
                learnerRole =
                    "Erzählende Person",
                assistantRole =
                    "Interessierte Zuhörerin oder interessierter Zuhörer",
                minimumLevel =
                    CefrLevel.B1,
                maximumLevel =
                    CefrLevel.B2,
                targetSkills =
                    setOf(
                        "Reihenfolge",
                        "Vergangenheit",
                        "Details",
                        "Reaktion"
                    ),
                objectives =
                    listOf(
                        "Den Kontext der Geschichte nennen",
                        "Zeit und Ort nennen",
                        "Die beteiligten Personen vorstellen",
                        "Das erste Ereignis beschreiben",
                        "Die weiteren Ereignisse verbinden",
                        "Ein unerwartetes Detail nennen",
                        "Die eigene Reaktion beschreiben",
                        "Das Ergebnis erklären",
                        "Eine persönliche Bewertung geben",
                        "Auf eine Rückfrage antworten"
                    ),
                keywords =
                    setOf(
                        "geschichte",
                        "erlebnis",
                        "passiert",
                        "wochenende",
                        "damals"
                    )
            ),
            scenario(
                type =
                    MissionScenarioType
                        .WORKPLACE_SMALL_TALK,
                title =
                    "Small Talk am Arbeitsplatz",
                description =
                    "Führe ein natürliches Gespräch mit einer " +
                            "Kollegin oder einem Kollegen.",
                learnerRole =
                    "Kollegin oder Kollege",
                assistantRole =
                    "Kollegin oder Kollege",
                minimumLevel =
                    CefrLevel.B1,
                maximumLevel =
                    CefrLevel.B2,
                targetSkills =
                    setOf(
                        "Gesprächseinstieg",
                        "Rückfragen",
                        "Erfahrungen",
                        "Gesprächsabschluss"
                    ),
                objectives =
                    listOf(
                        "Ein Gespräch beginnen",
                        "Nach dem Befinden fragen",
                        "Ein aktuelles Thema ansprechen",
                        "Eine eigene Erfahrung teilen",
                        "Eine passende Rückfrage stellen",
                        "Eine Meinung ausdrücken",
                        "Einen Grund nennen",
                        "Auf Gemeinsamkeiten reagieren",
                        "Zum Arbeitsthema überleiten",
                        "Das Gespräch natürlich beenden"
                    ),
                keywords =
                    setOf(
                        "arbeit",
                        "kollege",
                        "büro",
                        "pause",
                        "smalltalk"
                    )
            )
        )
    }

    private fun b2Scenarios():
            List<MissionScenarioDefinition> {
        return listOf(
            scenario(
                type =
                    MissionScenarioType.TEAM_MEETING,
                title =
                    "Im Teammeeting",
                description =
                    "Besprich Fortschritt, Probleme und nächste " +
                            "Schritte in einem Team.",
                learnerRole =
                    "Teammitglied",
                assistantRole =
                    "Teamleiterin oder Teamleiter",
                minimumLevel =
                    CefrLevel.B2,
                maximumLevel =
                    CefrLevel.C1,
                targetSkills =
                    setOf(
                        "Fortschrittsbericht",
                        "Problemanalyse",
                        "Vorschläge",
                        "Prioritäten"
                    ),
                objectives =
                    listOf(
                        "Ein kurzes Update geben",
                        "Einen Fortschritt erklären",
                        "Ein Problem benennen",
                        "Die Ursache des Problems erläutern",
                        "Eine Auswirkung beschreiben",
                        "Einen Lösungsvorschlag machen",
                        "Auf eine Rückfrage reagieren",
                        "Eine Priorität begründen",
                        "Verantwortlichkeiten klären",
                        "Die nächsten Schritte zusammenfassen"
                    ),
                keywords =
                    setOf(
                        "meeting",
                        "team",
                        "projekt",
                        "arbeit",
                        "fortschritt"
                    )
            ),
            scenario(
                type =
                    MissionScenarioType.NEGOTIATION,
                title =
                    "Eine Vereinbarung aushandeln",
                description =
                    "Vertrete Interessen und arbeite auf einen " +
                            "Kompromiss hin.",
                learnerRole =
                    "Verhandlungspartnerin oder Verhandlungspartner",
                assistantRole =
                    "Verhandlungspartnerin oder Verhandlungspartner",
                minimumLevel =
                    CefrLevel.B2,
                maximumLevel =
                    CefrLevel.C1,
                targetSkills =
                    setOf(
                        "Positionen",
                        "Begründungen",
                        "Einwände",
                        "Kompromisse"
                    ),
                objectives =
                    listOf(
                        "Das Verhandlungsziel nennen",
                        "Die eigene Position erläutern",
                        "Eine Priorität begründen",
                        "Nach den Interessen der anderen Seite fragen",
                        "Auf einen Einwand reagieren",
                        "Ein Gegenargument nennen",
                        "Eine Alternative anbieten",
                        "Einen Kompromiss formulieren",
                        "Bedingungen klären",
                        "Die Vereinbarung zusammenfassen"
                    ),
                keywords =
                    setOf(
                        "verhandeln",
                        "vereinbarung",
                        "kompromiss",
                        "angebot",
                        "bedingungen"
                    )
            ),
            scenario(
                type =
                    MissionScenarioType
                        .DEFENDING_AN_OPINION,
                title =
                    "Eine Meinung überzeugend vertreten",
                description =
                    "Begründe eine Position und reagiere auf " +
                            "Gegenargumente.",
                learnerRole =
                    "Diskussionsteilnehmerin oder Diskussionsteilnehmer",
                assistantRole =
                    "Gesprächspartnerin oder Gesprächspartner mit einer anderen Ansicht",
                minimumLevel =
                    CefrLevel.B2,
                maximumLevel =
                    CefrLevel.C1,
                targetSkills =
                    setOf(
                        "Argumentation",
                        "Beispiele",
                        "Gegenargumente",
                        "Nuance"
                    ),
                objectives =
                    listOf(
                        "Eine klare Position formulieren",
                        "Das wichtigste Argument nennen",
                        "Ein Beispiel geben",
                        "Ein zweites Argument ergänzen",
                        "Ein Gegenargument anerkennen",
                        "Auf das Gegenargument reagieren",
                        "Die eigene Position differenzieren",
                        "Eine Folge oder Auswirkung erklären",
                        "Eine Rückfrage stellen",
                        "Die Position abschließend zusammenfassen"
                    ),
                keywords =
                    setOf(
                        "meinung",
                        "argument",
                        "diskussion",
                        "dafür",
                        "dagegen"
                    )
            )
        )
    }

    private fun c1Scenarios():
            List<MissionScenarioDefinition> {
        return listOf(
            scenario(
                type =
                    MissionScenarioType
                        .CONFLICT_RESOLUTION,
                title =
                    "Einen Konflikt lösen",
                description =
                    "Vermittle zwischen unterschiedlichen " +
                            "Positionen und entwickle eine Lösung.",
                learnerRole =
                    "Vermittelnde Person",
                assistantRole =
                    "Person in einem beruflichen Konflikt",
                minimumLevel =
                    CefrLevel.C1,
                maximumLevel =
                    CefrLevel.C2,
                targetSkills =
                    setOf(
                        "Diplomatie",
                        "Perspektiven",
                        "Klärung",
                        "Lösungsentwicklung"
                    ),
                objectives =
                    listOf(
                        "Den Konflikt neutral zusammenfassen",
                        "Nach einer Perspektive fragen",
                        "Die zugrunde liegenden Interessen klären",
                        "Ein Missverständnis identifizieren",
                        "Eine emotionale Reaktion anerkennen",
                        "Eine gemeinsame Grundlage formulieren",
                        "Mehrere Lösungen vorschlagen",
                        "Konsequenzen abwägen",
                        "Eine tragfähige Vereinbarung entwickeln",
                        "Die nächsten Schritte präzise festhalten"
                    ),
                keywords =
                    setOf(
                        "konflikt",
                        "streit",
                        "lösung",
                        "team",
                        "missverständnis"
                    )
            ),
            scenario(
                type =
                    MissionScenarioType
                        .BUSINESS_PITCH,
                title =
                    "Eine Idee präsentieren",
                description =
                    "Präsentiere eine Idee überzeugend und " +
                            "beantworte kritische Fragen.",
                learnerRole =
                    "Präsentierende Person",
                assistantRole =
                    "Kritische Entscheidungsträgerin oder kritischer Entscheidungsträger",
                minimumLevel =
                    CefrLevel.C1,
                maximumLevel =
                    CefrLevel.C2,
                targetSkills =
                    setOf(
                        "Struktur",
                        "Überzeugung",
                        "Nutzen",
                        "Kritische Fragen"
                    ),
                objectives =
                    listOf(
                        "Das Problem präzise definieren",
                        "Die Idee kurz vorstellen",
                        "Den wichtigsten Nutzen erklären",
                        "Die Zielgruppe beschreiben",
                        "Ein konkretes Beispiel geben",
                        "Mögliche Risiken ansprechen",
                        "Auf eine kritische Frage reagieren",
                        "Eine Alternative abgrenzen",
                        "Einen nächsten Schritt vorschlagen",
                        "Mit einer überzeugenden Zusammenfassung schließen"
                    ),
                keywords =
                    setOf(
                        "idee",
                        "pitch",
                        "geschäft",
                        "projekt",
                        "präsentation"
                    )
            ),
            scenario(
                type =
                    MissionScenarioType
                        .STRATEGIC_DISCUSSION,
                title =
                    "Eine Strategie diskutieren",
                description =
                    "Analysiere Optionen und entwickle eine " +
                            "begründete strategische Empfehlung.",
                learnerRole =
                    "Strategische Beraterin oder strategischer Berater",
                assistantRole =
                    "Entscheidungsträgerin oder Entscheidungsträger",
                minimumLevel =
                    CefrLevel.C1,
                maximumLevel =
                    CefrLevel.C2,
                targetSkills =
                    setOf(
                        "Analyse",
                        "Abwägung",
                        "Risiken",
                        "Empfehlung"
                    ),
                objectives =
                    listOf(
                        "Das strategische Ziel klären",
                        "Die Ausgangslage analysieren",
                        "Eine erste Option darstellen",
                        "Eine zweite Option darstellen",
                        "Die Optionen vergleichen",
                        "Risiken erklären",
                        "Annahmen offenlegen",
                        "Auf einen Einwand reagieren",
                        "Eine Empfehlung begründen",
                        "Die Umsetzungsschritte zusammenfassen"
                    ),
                keywords =
                    setOf(
                        "strategie",
                        "unternehmen",
                        "entscheidung",
                        "risiko",
                        "planung"
                    )
            )
        )
    }

    private fun c2Scenarios():
            List<MissionScenarioDefinition> {
        return listOf(
            scenario(
                type =
                    MissionScenarioType
                        .ACADEMIC_DEBATE,
                title =
                    "Eine akademische Debatte führen",
                description =
                    "Vertrete eine differenzierte These und " +
                            "prüfe konkurrierende Argumente.",
                learnerRole =
                    "Teilnehmerin oder Teilnehmer einer akademischen Debatte",
                assistantRole =
                    "Fachperson mit einer konkurrierenden Position",
                minimumLevel =
                    CefrLevel.C2,
                maximumLevel =
                    CefrLevel.C2,
                targetSkills =
                    setOf(
                        "These",
                        "Evidenz",
                        "Nuance",
                        "Widerlegung"
                    ),
                objectives =
                    listOf(
                        "Eine präzise These formulieren",
                        "Zentrale Begriffe definieren",
                        "Eine theoretische Grundlage nennen",
                        "Evidenz zur Unterstützung anführen",
                        "Eine Gegenposition fair darstellen",
                        "Eine Schwäche der Gegenposition analysieren",
                        "Die eigene These qualifizieren",
                        "Eine Ausnahme diskutieren",
                        "Implikationen ableiten",
                        "Eine differenzierte Schlussfolgerung formulieren"
                    ),
                keywords =
                    setOf(
                        "wissenschaft",
                        "debatte",
                        "these",
                        "theorie",
                        "argument"
                    )
            ),
            scenario(
                type =
                    MissionScenarioType
                        .REGISTER_ADAPTATION,
                title =
                    "Sprache an den Kontext anpassen",
                description =
                    "Formuliere dieselbe Botschaft in " +
                            "unterschiedlichen Registern.",
                learnerRole =
                    "Kommunikationsverantwortliche Person",
                assistantRole =
                    "Gesprächspartnerin oder Gesprächspartner in wechselnden Rollen",
                minimumLevel =
                    CefrLevel.C2,
                maximumLevel =
                    CefrLevel.C2,
                targetSkills =
                    setOf(
                        "Register",
                        "Ton",
                        "Präzision",
                        "Stil"
                    ),
                objectives =
                    listOf(
                        "Eine informelle Botschaft formulieren",
                        "Dieselbe Botschaft neutral formulieren",
                        "Eine formelle Version erstellen",
                        "Den Ton für eine Beschwerde anpassen",
                        "Diplomatische Sprache verwenden",
                        "Eine direkte Formulierung abschwächen",
                        "Eine vage Aussage präzisieren",
                        "Eine Fachformulierung erklären",
                        "Auf einen Registerwechsel reagieren",
                        "Die stilistischen Entscheidungen begründen"
                    ),
                keywords =
                    setOf(
                        "formell",
                        "informell",
                        "register",
                        "stil",
                        "ton"
                    )
            )
        )
    }

    private fun scenario(
        type: MissionScenarioType,
        title: String,
        description: String,
        learnerRole: String,
        assistantRole: String,
        minimumLevel: CefrLevel,
        maximumLevel: CefrLevel,
        targetSkills: Set<String>,
        objectives: List<String>,
        keywords: Set<String>
    ): MissionScenarioDefinition {
        require(
            objectives.size in
                    MINIMUM_OBJECTIVE_COUNT..
                    MAXIMUM_OBJECTIVE_COUNT
        ) {
            "Mission scenario $type must contain between " +
                    "$MINIMUM_OBJECTIVE_COUNT and " +
                    "$MAXIMUM_OBJECTIVE_COUNT objective blueprints."
        }

        return MissionScenarioDefinition(
            type =
                type,
            title =
                title,
            description =
                description,
            learnerRole =
                learnerRole,
            assistantRole =
                assistantRole,
            minimumLevel =
                minimumLevel,
            maximumLevel =
                maximumLevel,
            targetSkills =
                targetSkills,
            objectiveBlueprints =
                objectives,
            topicKeywords =
                keywords
        )
    }

    private companion object {
        const val MINIMUM_OBJECTIVE_COUNT =
            8

        const val MAXIMUM_OBJECTIVE_COUNT =
            10
    }
}