// app/src/main/java/com/julesmeister/deutschcraft/feature/mission/domain/recommendation/CefrMissionScenarioCatalog.kt

package com.julesmeister.deutschcraft.feature.mission.domain.recommendation

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CefrMissionScenarioCatalog
@Inject constructor() {

    fun scenariosFor(
        level: CefrLevel
    ): List<MissionScenarioDefinition> {
        return allScenarios.filter { scenario ->
            scenario.supports(
                level = level
            )
        }
    }

    fun scenarioFor(
        type: MissionScenarioType
    ): MissionScenarioDefinition? {
        return allScenarios.firstOrNull { scenario ->
            scenario.type == type
        }
    }

    private val allScenarios:
            List<MissionScenarioDefinition> by lazy {
        buildList {
            addAll(a1Scenarios())
            addAll(a2Scenarios())
            addAll(b1Scenarios())
            addAll(b2Scenarios())
            addAll(c1Scenarios())
            addAll(c2Scenarios())
        }
    }

    private fun a1Scenarios():
            List<MissionScenarioDefinition> {

        return listOf(
            scenario(
                type =
                    MissionScenarioType.INTRODUCING_YOURSELF,
                title =
                    "Sich vorstellen",
                description =
                    "Führe ein erstes kurzes Gespräch mit einer neuen Person.",
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
                    MissionScenarioType.MEETING_A_NEIGHBOR,
                title =
                    "Die Nachbarin oder den Nachbarn kennenlernen",
                description =
                    "Lerne eine neue Person in deiner Nachbarschaft kennen.",
                learnerRole =
                    "Neue Nachbarin oder neuer Nachbar",
                assistantRole =
                    "Person aus der Nachbarschaft",
                minimumLevel =
                    CefrLevel.A1,
                maximumLevel =
                    CefrLevel.A2,
                targetSkills =
                    setOf(
                        "Begrüßung",
                        "Wohnort",
                        "Small Talk",
                        "Einfache Rückfragen"
                    ),
                objectives =
                    listOf(
                        "Die andere Person begrüßen",
                        "Sich kurz vorstellen",
                        "Sagen, wo du wohnst",
                        "Fragen, wo die andere Person wohnt",
                        "Fragen, wie lange die Person dort wohnt",
                        "Etwas über die Nachbarschaft fragen",
                        "Ein eigenes Interesse nennen",
                        "Eine einfache Rückfrage stellen",
                        "Eine hilfreiche Information bestätigen",
                        "Sich freundlich verabschieden"
                    ),
                keywords =
                    setOf(
                        "nachbar",
                        "nachbarschaft",
                        "wohnen",
                        "haus",
                        "straße"
                    )
            ),

            scenario(
                type =
                    MissionScenarioType.TALKING_ABOUT_YOUR_ROOM,
                title =
                    "Mein Zimmer beschreiben",
                description =
                    "Beschreibe dein Zimmer und die wichtigsten Dinge darin.",
                learnerRole =
                    "Person, die ihr Zimmer beschreibt",
                assistantRole =
                    "Interessierte Besucherin oder interessierter Besucher",
                minimumLevel =
                    CefrLevel.A1,
                maximumLevel =
                    CefrLevel.A2,
                targetSkills =
                    setOf(
                        "Möbel",
                        "Farben",
                        "Positionen",
                        "Einfache Beschreibungen"
                    ),
                objectives =
                    listOf(
                        "Sagen, wie groß dein Zimmer ist",
                        "Ein Möbelstück nennen",
                        "Eine Farbe beschreiben",
                        "Sagen, wo ein Gegenstand steht",
                        "Einen weiteren Gegenstand beschreiben",
                        "Sagen, was dir im Zimmer gefällt",
                        "Nach dem Zimmer der anderen Person fragen",
                        "Eine einfache Ortsangabe verwenden",
                        "Eine Rückfrage beantworten",
                        "Das Zimmer kurz zusammenfassen"
                    ),
                keywords =
                    setOf(
                        "zimmer",
                        "bett",
                        "tisch",
                        "stuhl",
                        "wohnung"
                    )
            ),

            scenario(
                type =
                    MissionScenarioType.BUYING_A_TRAIN_TICKET,
                title =
                    "Eine Fahrkarte kaufen",
                description =
                    "Kaufe eine Fahrkarte und kläre einfache Reiseinformationen.",
                learnerRole =
                    "Reisende Person",
                assistantRole =
                    "Mitarbeiterin oder Mitarbeiter am Fahrkartenschalter",
                minimumLevel =
                    CefrLevel.A1,
                maximumLevel =
                    CefrLevel.A2,
                targetSkills =
                    setOf(
                        "Reiseziel",
                        "Uhrzeiten",
                        "Preise",
                        "Einfache Rückfragen"
                    ),
                objectives =
                    listOf(
                        "Das gewünschte Reiseziel nennen",
                        "Nach einer Verbindung fragen",
                        "Eine Abfahrtszeit verstehen",
                        "Eine gewünschte Uhrzeit nennen",
                        "Nach einer einfachen oder Hin- und Rückfahrt fragen",
                        "Nach dem Preis fragen",
                        "Eine Fahrkartenoption auswählen",
                        "Eine Rückfrage beantworten",
                        "Die Abfahrtsinformation bestätigen",
                        "Sich bedanken"
                    ),
                keywords =
                    setOf(
                        "fahrkarte",
                        "ticket",
                        "zug",
                        "bahnhof",
                        "fahren"
                    )
            ),

            scenario(
                type =
                    MissionScenarioType.ASKING_ABOUT_OPENING_HOURS,
                title =
                    "Nach Öffnungszeiten fragen",
                description =
                    "Finde heraus, wann ein Geschäft oder eine Einrichtung geöffnet ist.",
                learnerRole =
                    "Person, die Informationen benötigt",
                assistantRole =
                    "Mitarbeiterin oder Mitarbeiter einer Einrichtung",
                minimumLevel =
                    CefrLevel.A1,
                maximumLevel =
                    CefrLevel.A2,
                targetSkills =
                    setOf(
                        "Uhrzeiten",
                        "Wochentage",
                        "Fragen",
                        "Verständnissicherung"
                    ),
                objectives =
                    listOf(
                        "Die Einrichtung nennen",
                        "Nach der Öffnungszeit fragen",
                        "Eine Uhrzeit verstehen",
                        "Nach der Schließzeit fragen",
                        "Nach einem bestimmten Wochentag fragen",
                        "Nach Öffnungszeiten am Wochenende fragen",
                        "Eine Information wiederholen",
                        "Das Verständnis bestätigen",
                        "Eine letzte Rückfrage stellen",
                        "Sich bedanken"
                    ),
                keywords =
                    setOf(
                        "geöffnet",
                        "öffnungszeiten",
                        "uhr",
                        "geschlossen",
                        "samstag"
                    )
            ),

            scenario(
                type =
                    MissionScenarioType.JOINING_A_BEGINNER_CLASS,
                title =
                    "Einen Anfängerkurs besuchen",
                description =
                    "Informiere dich über einen Kurs und stelle dich kurz vor.",
                learnerRole =
                    "Neue Kursteilnehmerin oder neuer Kursteilnehmer",
                assistantRole =
                    "Kursleiterin oder Kursleiter",
                minimumLevel =
                    CefrLevel.A1,
                maximumLevel =
                    CefrLevel.A2,
                targetSkills =
                    setOf(
                        "Vorstellung",
                        "Interessen",
                        "Termine",
                        "Einfache Fragen"
                    ),
                objectives =
                    listOf(
                        "Die Kursleitung begrüßen",
                        "Sich kurz vorstellen",
                        "Sagen, warum du den Kurs besuchen möchtest",
                        "Nach dem Kurstag fragen",
                        "Nach der Uhrzeit fragen",
                        "Nach benötigten Materialien fragen",
                        "Eine eigene Erfahrung nennen",
                        "Eine Rückfrage beantworten",
                        "Die Teilnahme bestätigen",
                        "Sich freundlich verabschieden"
                    ),
                keywords =
                    setOf(
                        "kurs",
                        "anfänger",
                        "lernen",
                        "termin",
                        "unterricht"
                    )
            ),

            scenario(
                type =
                    MissionScenarioType.MAKING_SIMPLE_WEEKEND_PLANS,
                title =
                    "Pläne fürs Wochenende machen",
                description =
                    "Plane eine einfache Aktivität für das Wochenende.",
                learnerRole =
                    "Person, die etwas unternehmen möchte",
                assistantRole =
                    "Freundin oder Freund",
                minimumLevel =
                    CefrLevel.A1,
                maximumLevel =
                    CefrLevel.A2,
                targetSkills =
                    setOf(
                        "Vorschläge",
                        "Zeit",
                        "Freizeit",
                        "Zustimmung"
                    ),
                objectives =
                    listOf(
                        "Nach den Wochenendplänen fragen",
                        "Eine Aktivität vorschlagen",
                        "Einen möglichen Tag nennen",
                        "Eine Uhrzeit vorschlagen",
                        "Nach dem Treffpunkt fragen",
                        "Auf einen Gegenvorschlag reagieren",
                        "Eine Präferenz nennen",
                        "Die gemeinsame Aktivität bestätigen",
                        "Den Treffpunkt bestätigen",
                        "Das Gespräch freundlich beenden"
                    ),
                keywords =
                    setOf(
                        "wochenende",
                        "samstag",
                        "sonntag",
                        "treffen",
                        "freizeit"
                    )
            ),

            scenario(
                type =
                    MissionScenarioType.GROCERY_SHOPPING,
                title =
                    "Im Supermarkt",
                description =
                    "Finde Produkte und erledige einen einfachen Einkauf.",
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
                    MissionScenarioType.ASKING_FOR_DIRECTIONS,
                title =
                    "Nach dem Weg fragen",
                description =
                    "Bitte um Hilfe und verstehe eine einfache Wegbeschreibung.",
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
                    MissionScenarioType.DAILY_ROUTINE,
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
                    MissionScenarioType.RESTAURANT_ORDERING,
                title =
                    "Im Restaurant bestellen",
                description =
                    "Bestelle eine vollständige Mahlzeit in einem Restaurant.",
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
                    MissionScenarioType.CAFE_ORDERING,
                title =
                    "Im Café bestellen",
                description =
                    "Bestelle ein Getränk und etwas Kleines zu essen.",
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
                    MissionScenarioType.HOTEL_CHECK_IN,
                title =
                    "Im Hotel einchecken",
                description =
                    "Checke in einem Hotel ein und kläre wichtige Informationen.",
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
                    MissionScenarioType.VIEWING_AN_APARTMENT,
                title =
                    "Eine Wohnung besichtigen",
                description =
                    "Besichtige eine Wohnung und kläre wichtige Details.",
                learnerRole =
                    "Interessentin oder Interessent an einer Wohnung",
                assistantRole =
                    "Vermieterin, Vermieter oder vermittelnde Person",
                minimumLevel =
                    CefrLevel.A2,
                maximumLevel =
                    CefrLevel.B1,
                targetSkills =
                    setOf(
                        "Wohnung",
                        "Kosten",
                        "Ausstattung",
                        "Rückfragen"
                    ),
                objectives =
                    listOf(
                        "Sich kurz vorstellen",
                        "Nach der Größe der Wohnung fragen",
                        "Nach der Anzahl der Zimmer fragen",
                        "Nach der Miete fragen",
                        "Nach zusätzlichen Kosten fragen",
                        "Nach Möbeln oder Ausstattung fragen",
                        "Nach der Umgebung fragen",
                        "Eine eigene Präferenz nennen",
                        "Nach dem möglichen Einzug fragen",
                        "Das weitere Vorgehen klären"
                    ),
                keywords =
                    setOf(
                        "wohnung",
                        "miete",
                        "zimmer",
                        "besichtigung",
                        "einziehen"
                    )
            ),

            scenario(
                type =
                    MissionScenarioType.MISSING_A_TRAIN,
                title =
                    "Den Zug verpasst",
                description =
                    "Finde nach einem verpassten Zug eine neue Verbindung.",
                learnerRole =
                    "Reisende Person mit einem verpassten Zug",
                assistantRole =
                    "Mitarbeiterin oder Mitarbeiter am Bahnhof",
                minimumLevel =
                    CefrLevel.A2,
                maximumLevel =
                    CefrLevel.B1,
                targetSkills =
                    setOf(
                        "Problem erklären",
                        "Verbindungen",
                        "Uhrzeiten",
                        "Alternativen"
                    ),
                objectives =
                    listOf(
                        "Erklären, dass du den Zug verpasst hast",
                        "Das Reiseziel nennen",
                        "Nach der nächsten Verbindung fragen",
                        "Eine neue Abfahrtszeit verstehen",
                        "Nach einem Umstieg fragen",
                        "Nach dem richtigen Gleis fragen",
                        "Nach der Gültigkeit der Fahrkarte fragen",
                        "Eine Alternative vergleichen",
                        "Die gewählte Verbindung bestätigen",
                        "Sich für die Hilfe bedanken"
                    ),
                keywords =
                    setOf(
                        "zug",
                        "verpasst",
                        "bahnhof",
                        "verbindung",
                        "gleis"
                    )
            ),

            scenario(
                type =
                    MissionScenarioType.CHANGING_AN_APPOINTMENT,
                title =
                    "Einen Termin verschieben",
                description =
                    "Verschiebe einen bestehenden Termin und finde eine neue Zeit.",
                learnerRole =
                    "Person mit einem bestehenden Termin",
                assistantRole =
                    "Mitarbeiterin oder Mitarbeiter bei der Terminplanung",
                minimumLevel =
                    CefrLevel.A2,
                maximumLevel =
                    CefrLevel.B1,
                targetSkills =
                    setOf(
                        "Termine",
                        "Zeitangaben",
                        "Begründungen",
                        "Alternativen"
                    ),
                objectives =
                    listOf(
                        "Den bestehenden Termin nennen",
                        "Sagen, dass du den Termin ändern möchtest",
                        "Einen einfachen Grund nennen",
                        "Nach einem anderen Tag fragen",
                        "Eine alternative Uhrzeit vorschlagen",
                        "Auf einen Gegenvorschlag reagieren",
                        "Eine passende Zeit auswählen",
                        "Datum und Uhrzeit wiederholen",
                        "Den neuen Termin bestätigen",
                        "Sich höflich bedanken"
                    ),
                keywords =
                    setOf(
                        "termin",
                        "verschieben",
                        "datum",
                        "uhrzeit",
                        "absagen"
                    )
            ),

            scenario(
                type =
                    MissionScenarioType.ASKING_A_NEIGHBOR_FOR_HELP,
                title =
                    "Eine Nachbarin oder einen Nachbarn um Hilfe bitten",
                description =
                    "Bitte in einer Alltagssituation höflich um Hilfe.",
                learnerRole =
                    "Person, die Hilfe benötigt",
                assistantRole =
                    "Hilfsbereite Person aus der Nachbarschaft",
                minimumLevel =
                    CefrLevel.A2,
                maximumLevel =
                    CefrLevel.B1,
                targetSkills =
                    setOf(
                        "Bitten",
                        "Problem erklären",
                        "Zeit",
                        "Dank"
                    ),
                objectives =
                    listOf(
                        "Die andere Person begrüßen",
                        "Kurz erklären, warum du Hilfe brauchst",
                        "Eine konkrete Bitte formulieren",
                        "Sagen, wann du Hilfe benötigst",
                        "Auf eine Rückfrage reagieren",
                        "Eine Alternative vorschlagen",
                        "Die vereinbarte Hilfe bestätigen",
                        "Eine wichtige Information ergänzen",
                        "Sich bedanken",
                        "Sich freundlich verabschieden"
                    ),
                keywords =
                    setOf(
                        "nachbar",
                        "hilfe",
                        "bitte",
                        "problem",
                        "helfen"
                    )
            ),

            scenario(
                type =
                    MissionScenarioType.JOINING_A_SPORTS_CLUB,
                title =
                    "Einem Sportverein beitreten",
                description =
                    "Informiere dich über einen Sportverein und die Teilnahme.",
                learnerRole =
                    "Interessierte neue Person",
                assistantRole =
                    "Trainerin, Trainer oder Vereinsmitglied",
                minimumLevel =
                    CefrLevel.A2,
                maximumLevel =
                    CefrLevel.B1,
                targetSkills =
                    setOf(
                        "Interessen",
                        "Erfahrung",
                        "Termine",
                        "Bedingungen"
                    ),
                objectives =
                    listOf(
                        "Sagen, für welchen Sport du dich interessierst",
                        "Nach den Trainingszeiten fragen",
                        "Nach dem Trainingsort fragen",
                        "Die eigene Erfahrung beschreiben",
                        "Nach dem Niveau der Gruppe fragen",
                        "Nach benötigter Ausrüstung fragen",
                        "Nach den Kosten fragen",
                        "Nach einem Probetraining fragen",
                        "Die Teilnahmeabsicht ausdrücken",
                        "Das weitere Vorgehen klären"
                    ),
                keywords =
                    setOf(
                        "sport",
                        "verein",
                        "training",
                        "mitglied",
                        "probetraining"
                    )
            ),

            scenario(
                type =
                    MissionScenarioType.BOOKING_AN_ACTIVITY,
                title =
                    "Eine Aktivität buchen",
                description =
                    "Buche eine Freizeitaktivität und kläre die wichtigsten Details.",
                learnerRole =
                    "Person, die eine Aktivität buchen möchte",
                assistantRole =
                    "Mitarbeiterin oder Mitarbeiter eines Freizeitanbieters",
                minimumLevel =
                    CefrLevel.A2,
                maximumLevel =
                    CefrLevel.B1,
                targetSkills =
                    setOf(
                        "Buchung",
                        "Termine",
                        "Preise",
                        "Rückfragen"
                    ),
                objectives =
                    listOf(
                        "Nach einer verfügbaren Aktivität fragen",
                        "Einen gewünschten Tag nennen",
                        "Nach einer verfügbaren Uhrzeit fragen",
                        "Die Anzahl der Personen nennen",
                        "Nach dem Preis fragen",
                        "Nach der Dauer fragen",
                        "Nach Voraussetzungen oder Ausrüstung fragen",
                        "Eine Option auswählen",
                        "Die Buchungsdetails bestätigen",
                        "Sich bedanken"
                    ),
                keywords =
                    setOf(
                        "buchen",
                        "aktivität",
                        "freizeit",
                        "termin",
                        "preis"
                    )
            ),

            scenario(
                type =
                    MissionScenarioType.EXPLAINING_A_DELIVERY_PROBLEM,
                title =
                    "Ein Lieferproblem klären",
                description =
                    "Erkläre ein Problem mit einer Lieferung und bitte um Hilfe.",
                learnerRole =
                    "Kundin oder Kunde mit einem Lieferproblem",
                assistantRole =
                    "Mitarbeiterin oder Mitarbeiter im Kundenservice",
                minimumLevel =
                    CefrLevel.A2,
                maximumLevel =
                    CefrLevel.B1,
                targetSkills =
                    setOf(
                        "Problem beschreiben",
                        "Bestelldetails",
                        "Zeitangaben",
                        "Lösung erfragen"
                    ),
                objectives =
                    listOf(
                        "Das Anliegen einleiten",
                        "Die Bestellung nennen",
                        "Sagen, wann die Lieferung erwartet wurde",
                        "Das Lieferproblem erklären",
                        "Eine relevante Bestellinformation nennen",
                        "Auf eine Rückfrage antworten",
                        "Nach dem aktuellen Status fragen",
                        "Um eine Lösung bitten",
                        "Die vereinbarte Lösung bestätigen",
                        "Das Gespräch höflich beenden"
                    ),
                keywords =
                    setOf(
                        "lieferung",
                        "paket",
                        "bestellung",
                        "verspätet",
                        "kundenservice"
                    )
            ),

            scenario(
                type =
                    MissionScenarioType.PLANNING_A_BIRTHDAY,
                title =
                    "Eine Geburtstagsfeier planen",
                description =
                    "Plane gemeinsam eine kleine Geburtstagsfeier.",
                learnerRole =
                    "Person, die eine Feier mitorganisiert",
                assistantRole =
                    "Freundin oder Freund, die oder der mitplant",
                minimumLevel =
                    CefrLevel.A2,
                maximumLevel =
                    CefrLevel.B1,
                targetSkills =
                    setOf(
                        "Vorschläge",
                        "Einladungen",
                        "Zeit",
                        "Organisation"
                    ),
                objectives =
                    listOf(
                        "Einen Termin für die Feier vorschlagen",
                        "Einen Ort vorschlagen",
                        "Über mögliche Gäste sprechen",
                        "Eine Aktivität vorschlagen",
                        "Über Essen oder Getränke sprechen",
                        "Eine Aufgabe übernehmen",
                        "Der anderen Person eine Aufgabe vorschlagen",
                        "Auf einen Gegenvorschlag reagieren",
                        "Die wichtigsten Entscheidungen bestätigen",
                        "Den nächsten Planungsschritt vereinbaren"
                    ),
                keywords =
                    setOf(
                        "geburtstag",
                        "feier",
                        "gäste",
                        "einladen",
                        "planen"
                    )
            ),

            scenario(
                type =
                    MissionScenarioType.DOCTOR_APPOINTMENT,
                title =
                    "Beim Arzt",
                description =
                    "Beschreibe einfache Beschwerden und verstehe grundlegende Empfehlungen.",
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
                    MissionScenarioType.RETURNING_A_PRODUCT,
                title =
                    "Ein Produkt zurückgeben",
                description =
                    "Erkläre ein Problem und bitte um eine passende Lösung.",
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
                    MissionScenarioType.PLANNING_A_TRIP,
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
                    MissionScenarioType.JOB_INTERVIEW,
                title =
                    "Vorstellungsgespräch",
                description =
                    "Führe ein strukturiertes Vorstellungsgespräch.",
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
                    MissionScenarioType.CHOOSING_BETWEEN_APARTMENTS,
                title =
                    "Zwischen zwei Wohnungen entscheiden",
                description =
                    "Vergleiche zwei Wohnungen und begründe deine Wahl.",
                learnerRole =
                    "Person auf Wohnungssuche",
                assistantRole =
                    "Freundin oder Freund, die oder der bei der Entscheidung hilft",
                minimumLevel =
                    CefrLevel.B1,
                maximumLevel =
                    CefrLevel.B2,
                targetSkills =
                    setOf(
                        "Vergleichen",
                        "Begründungen",
                        "Vor- und Nachteile",
                        "Entscheidungen"
                    ),
                objectives =
                    listOf(
                        "Die beiden Wohnungen kurz beschreiben",
                        "Die Lage vergleichen",
                        "Die Mietkosten vergleichen",
                        "Die Größe oder Ausstattung vergleichen",
                        "Einen Vorteil der ersten Wohnung erklären",
                        "Einen Vorteil der zweiten Wohnung erklären",
                        "Einen Nachteil nennen",
                        "Auf eine andere Meinung reagieren",
                        "Eine begründete Entscheidung treffen",
                        "Die wichtigsten Gründe zusammenfassen"
                    ),
                keywords =
                    setOf(
                        "wohnung",
                        "miete",
                        "vergleichen",
                        "lage",
                        "entscheiden"
                    )
            ),

            scenario(
                type =
                    MissionScenarioType.COMPARING_JOB_OFFERS,
                title =
                    "Zwei Jobangebote vergleichen",
                description =
                    "Vergleiche zwei berufliche Möglichkeiten und begründe deine Präferenz.",
                learnerRole =
                    "Person mit zwei Jobangeboten",
                assistantRole =
                    "Freundin, Freund oder beratende Person",
                minimumLevel =
                    CefrLevel.B1,
                maximumLevel =
                    CefrLevel.B2,
                targetSkills =
                    setOf(
                        "Vergleich",
                        "Arbeitsbedingungen",
                        "Prioritäten",
                        "Begründungen"
                    ),
                objectives =
                    listOf(
                        "Die beiden Jobangebote kurz vorstellen",
                        "Die Aufgaben vergleichen",
                        "Arbeitszeiten vergleichen",
                        "Einen Unterschied bei den Bedingungen nennen",
                        "Einen Vorteil des ersten Angebots erklären",
                        "Einen Vorteil des zweiten Angebots erklären",
                        "Eine persönliche Priorität nennen",
                        "Auf eine Empfehlung reagieren",
                        "Eine Präferenz begründen",
                        "Eine vorläufige Entscheidung zusammenfassen"
                    ),
                keywords =
                    setOf(
                        "job",
                        "stelle",
                        "angebot",
                        "gehalt",
                        "arbeitszeit"
                    )
            ),

            scenario(
                type =
                    MissionScenarioType.HANDLING_A_TRAIN_CANCELLATION,
                title =
                    "Mit einem Zugausfall umgehen",
                description =
                    "Reagiere auf einen Zugausfall und finde eine brauchbare Alternative.",
                learnerRole =
                    "Reisende Person mit ausgefallenem Zug",
                assistantRole =
                    "Mitarbeiterin oder Mitarbeiter am Bahnhof",
                minimumLevel =
                    CefrLevel.B1,
                maximumLevel =
                    CefrLevel.B2,
                targetSkills =
                    setOf(
                        "Problem erklären",
                        "Alternativen vergleichen",
                        "Rückfragen",
                        "Entscheidungen"
                    ),
                objectives =
                    listOf(
                        "Den ausgefallenen Zug nennen",
                        "Das Reiseziel erklären",
                        "Nach verfügbaren Alternativen fragen",
                        "Nach der schnellsten Verbindung fragen",
                        "Nach einer Verbindung mit weniger Umstiegen fragen",
                        "Zwei Alternativen vergleichen",
                        "Nach Auswirkungen auf die Fahrkarte fragen",
                        "Eine Alternative auswählen und begründen",
                        "Die neue Reiseverbindung bestätigen",
                        "Sich für die Unterstützung bedanken"
                    ),
                keywords =
                    setOf(
                        "zugausfall",
                        "zug",
                        "verbindung",
                        "verspätung",
                        "alternative"
                    )
            ),

            scenario(
                type =
                    MissionScenarioType.RESOLVING_A_SCHEDULING_CONFLICT,
                title =
                    "Einen Terminkonflikt lösen",
                description =
                    "Finde gemeinsam eine Lösung für zwei kollidierende Termine.",
                learnerRole =
                    "Person mit einem Terminkonflikt",
                assistantRole =
                    "Kollegin, Kollege oder andere beteiligte Person",
                minimumLevel =
                    CefrLevel.B1,
                maximumLevel =
                    CefrLevel.B2,
                targetSkills =
                    setOf(
                        "Problem erklären",
                        "Prioritäten",
                        "Vorschläge",
                        "Kompromisse"
                    ),
                objectives =
                    listOf(
                        "Den Terminkonflikt erklären",
                        "Die beiden Verpflichtungen beschreiben",
                        "Erklären, warum ein Termin wichtig ist",
                        "Nach der Flexibilität der anderen Person fragen",
                        "Eine alternative Zeit vorschlagen",
                        "Auf einen Gegenvorschlag reagieren",
                        "Einen möglichen Kompromiss formulieren",
                        "Die Auswirkungen des Kompromisses prüfen",
                        "Eine gemeinsame Lösung bestätigen",
                        "Die neue Vereinbarung zusammenfassen"
                    ),
                keywords =
                    setOf(
                        "termin",
                        "konflikt",
                        "zeit",
                        "verschieben",
                        "kompromiss"
                    )
            ),

            scenario(
                type =
                    MissionScenarioType.ORGANIZING_AN_EVENT,
                title =
                    "Eine Veranstaltung organisieren",
                description =
                    "Plane gemeinsam eine kleine Veranstaltung und verteile Aufgaben.",
                learnerRole =
                    "Mitorganisierende Person",
                assistantRole =
                    "Weitere organisierende Person",
                minimumLevel =
                    CefrLevel.B1,
                maximumLevel =
                    CefrLevel.B2,
                targetSkills =
                    setOf(
                        "Planung",
                        "Vorschläge",
                        "Aufgaben",
                        "Prioritäten"
                    ),
                objectives =
                    listOf(
                        "Das Ziel der Veranstaltung nennen",
                        "Einen möglichen Termin vorschlagen",
                        "Einen Ort diskutieren",
                        "Über die Anzahl der Gäste sprechen",
                        "Eine wichtige Aufgabe identifizieren",
                        "Eine Aufgabe übernehmen",
                        "Der anderen Person eine Aufgabe vorschlagen",
                        "Ein mögliches Problem ansprechen",
                        "Eine Lösung für das Problem vorschlagen",
                        "Die nächsten Schritte zusammenfassen"
                    ),
                keywords =
                    setOf(
                        "veranstaltung",
                        "event",
                        "organisieren",
                        "gäste",
                        "planung"
                    )
            ),

            scenario(
                type =
                    MissionScenarioType.GIVING_ADVICE_TO_A_FRIEND,
                title =
                    "Einer Freundin oder einem Freund Rat geben",
                description =
                    "Besprich ein Alltagsproblem und gib begründeten Rat.",
                learnerRole =
                    "Person, die Rat gibt",
                assistantRole =
                    "Freundin oder Freund mit einem Problem",
                minimumLevel =
                    CefrLevel.B1,
                maximumLevel =
                    CefrLevel.B2,
                targetSkills =
                    setOf(
                        "Ratschläge",
                        "Begründungen",
                        "Alternativen",
                        "Empathische Rückfragen"
                    ),
                objectives =
                    listOf(
                        "Nach dem Problem fragen",
                        "Eine wichtige Einzelheit klären",
                        "Das Problem kurz zusammenfassen",
                        "Einen ersten Rat geben",
                        "Den Rat begründen",
                        "Eine zweite Möglichkeit vorschlagen",
                        "Vor- und Nachteile vergleichen",
                        "Auf einen Einwand reagieren",
                        "Eine Empfehlung formulieren",
                        "Nachfragen, was die andere Person tun möchte"
                    ),
                keywords =
                    setOf(
                        "rat",
                        "problem",
                        "empfehlung",
                        "sollte",
                        "entscheidung"
                    )
            ),

            scenario(
                type =
                    MissionScenarioType.DISCUSSING_REMOTE_VS_OFFICE_WORK,
                title =
                    "Homeoffice oder Büro",
                description =
                    "Diskutiere die Vor- und Nachteile von Homeoffice und Büroarbeit.",
                learnerRole =
                    "Mitarbeitende Person mit einer Präferenz",
                assistantRole =
                    "Kollegin oder Kollege mit einer anderen Perspektive",
                minimumLevel =
                    CefrLevel.B1,
                maximumLevel =
                    CefrLevel.B2,
                targetSkills =
                    setOf(
                        "Meinung",
                        "Vergleichen",
                        "Begründungen",
                        "Vor- und Nachteile"
                    ),
                objectives =
                    listOf(
                        "Die eigene Präferenz nennen",
                        "Einen Vorteil von Homeoffice erklären",
                        "Einen Nachteil von Homeoffice erklären",
                        "Einen Vorteil des Büros erklären",
                        "Einen Nachteil des Büros erklären",
                        "Nach der Meinung der anderen Person fragen",
                        "Auf ein Gegenargument reagieren",
                        "Eine persönliche Erfahrung nennen",
                        "Einen möglichen Kompromiss vorschlagen",
                        "Die eigene Position zusammenfassen"
                    ),
                keywords =
                    setOf(
                        "homeoffice",
                        "büro",
                        "arbeit",
                        "remote",
                        "arbeitsplatz"
                    )
            ),

            scenario(
                type =
                    MissionScenarioType.PLANNING_A_GROUP_TRIP,
                title =
                    "Eine Gruppenreise planen",
                description =
                    "Plane eine Reise für mehrere Personen und berücksichtige unterschiedliche Wünsche.",
                learnerRole =
                    "Mitglied einer Reisegruppe",
                assistantRole =
                    "Weiteres Mitglied der Reisegruppe",
                minimumLevel =
                    CefrLevel.B1,
                maximumLevel =
                    CefrLevel.B2,
                targetSkills =
                    setOf(
                        "Vorschläge",
                        "Vergleichen",
                        "Kompromisse",
                        "Begründungen"
                    ),
                objectives =
                    listOf(
                        "Ein Reiseziel vorschlagen",
                        "Den Vorschlag begründen",
                        "Nach den Wünschen der Gruppe fragen",
                        "Zwei Reiseoptionen vergleichen",
                        "Über das Budget sprechen",
                        "Über die Reisedauer sprechen",
                        "Eine Aktivität priorisieren",
                        "Auf einen unterschiedlichen Wunsch reagieren",
                        "Einen Kompromiss vorschlagen",
                        "Die gemeinsame Entscheidung zusammenfassen"
                    ),
                keywords =
                    setOf(
                        "gruppenreise",
                        "reise",
                        "budget",
                        "gruppe",
                        "urlaub"
                    )
            ),

            scenario(
                type =
                    MissionScenarioType.MAKING_A_DECISION,
                title =
                    "Gemeinsam eine Entscheidung treffen",
                description =
                    "Vergleiche Möglichkeiten und treffe eine begründete Entscheidung.",
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
                    MissionScenarioType.MAKING_A_COMPLAINT,
                title =
                    "Eine Beschwerde klären",
                description =
                    "Beschreibe ein Problem sachlich und finde eine Lösung.",
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
                    MissionScenarioType.TELLING_A_STORY,
                title =
                    "Eine Geschichte erzählen",
                description =
                    "Erzähle ein Erlebnis verständlich und zusammenhängend.",
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
                    MissionScenarioType.WORKPLACE_SMALL_TALK,
                title =
                    "Small Talk am Arbeitsplatz",
                description =
                    "Führe ein natürliches Gespräch mit einer Kollegin oder einem Kollegen.",
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
                    "Besprich Fortschritt, Probleme und nächste Schritte in einem Team.",
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
                    "Vertrete Interessen und arbeite auf einen Kompromiss hin.",
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
                    MissionScenarioType.DEFENDING_AN_OPINION,
                title =
                    "Eine Meinung überzeugend vertreten",
                description =
                    "Begründe eine Position und reagiere auf Gegenargumente.",
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
                    MissionScenarioType.CONFLICT_RESOLUTION,
                title =
                    "Einen Konflikt lösen",
                description =
                    "Vermittle zwischen unterschiedlichen Positionen und entwickle eine Lösung.",
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
                    MissionScenarioType.BUSINESS_PITCH,
                title =
                    "Eine Idee präsentieren",
                description =
                    "Präsentiere eine Idee überzeugend und beantworte kritische Fragen.",
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
                    MissionScenarioType.STRATEGIC_DISCUSSION,
                title =
                    "Eine Strategie diskutieren",
                description =
                    "Analysiere Optionen und entwickle eine begründete strategische Empfehlung.",
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
                    MissionScenarioType.ACADEMIC_DEBATE,
                title =
                    "Eine akademische Debatte führen",
                description =
                    "Vertrete eine differenzierte These und prüfe konkurrierende Argumente.",
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
                    MissionScenarioType.REGISTER_ADAPTATION,
                title =
                    "Sprache an den Kontext anpassen",
                description =
                    "Formuliere dieselbe Botschaft in unterschiedlichen Registern.",
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
        type:
        MissionScenarioType,

        title:
        String,

        description:
        String,

        learnerRole:
        String,

        assistantRole:
        String,

        minimumLevel:
        CefrLevel,

        maximumLevel:
        CefrLevel,

        targetSkills:
        Set<String>,

        objectives:
        List<String>,

        keywords:
        Set<String>
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

        require(
            title.isNotBlank()
        ) {
            "Mission scenario $type must have a title."
        }

        require(
            description.isNotBlank()
        ) {
            "Mission scenario $type must have a description."
        }

        require(
            learnerRole.isNotBlank()
        ) {
            "Mission scenario $type must have a learner role."
        }

        require(
            assistantRole.isNotBlank()
        ) {
            "Mission scenario $type must have an assistant role."
        }

        require(
            targetSkills.isNotEmpty()
        ) {
            "Mission scenario $type must have target skills."
        }

        require(
            minimumLevel.rank <=
                    maximumLevel.rank
        ) {
            "Mission scenario $type has an invalid CEFR range."
        }

        return MissionScenarioDefinition(
            type =
                type,

            title =
                title.trim(),

            description =
                description.trim(),

            learnerRole =
                learnerRole.trim(),

            assistantRole =
                assistantRole.trim(),

            minimumLevel =
                minimumLevel,

            maximumLevel =
                maximumLevel,

            targetSkills =
                targetSkills
                    .map(String::trim)
                    .filter(String::isNotBlank)
                    .toSet(),

            objectiveBlueprints =
                objectives
                    .map(String::trim)
                    .filter(String::isNotBlank)
                    .distinct(),

            topicKeywords =
                keywords
                    .map(String::trim)
                    .filter(String::isNotBlank)
                    .toSet()
        )
    }

    private val CefrLevel.rank:
            Int
        get() =
            when (this) {
                CefrLevel.A1 -> 1
                CefrLevel.A2 -> 2
                CefrLevel.B1 -> 3
                CefrLevel.B2 -> 4
                CefrLevel.C1 -> 5
                CefrLevel.C2 -> 6
            }

    private companion object {

        const val MINIMUM_OBJECTIVE_COUNT =
            8

        const val MAXIMUM_OBJECTIVE_COUNT =
            10
    }
}