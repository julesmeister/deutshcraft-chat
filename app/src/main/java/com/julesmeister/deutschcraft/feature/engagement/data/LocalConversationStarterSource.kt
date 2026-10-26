package com.julesmeister.deutschcraft.feature.engagement.data

import com.julesmeister.deutschcraft.feature.engagement.domain.ConversationStarter
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class LocalConversationStarterSource @Inject constructor() {

    fun selectStarter(
        excludedStarterId: String? = null
    ): ConversationStarter {
        val availableStarters =
            STARTERS.filter { starter ->
                starter.id != excludedStarterId
            }

        return availableStarters.random(
            random = Random.Default
        )
    }

    private companion object {

        val STARTERS =
            listOf(
                ConversationStarter(
                    id = "pizza_pineapple",
                    title = "Emil braucht deine Meinung",
                    message =
                        "Lukas und ich diskutieren gerade: " +
                                "Ananas auf Pizza, ja oder nein?"
                ),
                ConversationStarter(
                    id = "mountains_or_sea",
                    title = "Eine Frage von Emil",
                    message =
                        "Würdest du lieber am Meer oder " +
                                "in den Bergen wohnen?"
                ),
                ConversationStarter(
                    id = "favorite_food",
                    title = "Kurze Deutschfrage",
                    message =
                        "Welches Essen könntest du jeden " +
                                "Tag essen?"
                ),
                ConversationStarter(
                    id = "spontaneous_trip",
                    title = "Emil hat eine Reiseidee",
                    message =
                        "Wenn du morgen spontan verreisen " +
                                "könntest, wohin würdest du fahren?"
                ),
                ConversationStarter(
                    id = "bookstore_customer",
                    title = "Etwas Seltsames im Buchladen",
                    message =
                        "Heute suchte jemand im Buchladen " +
                                "ein Buch ohne glückliches Ende. " +
                                "Magst du traurige Geschichten?"
                ),
                ConversationStarter(
                    id = "roommate_dishes",
                    title = "Lukas hat es wieder getan",
                    message =
                        "Lukas hat schon wieder das Geschirr " +
                                "stehen lassen. Was würdest du ihm sagen?"
                ),
                ConversationStarter(
                    id = "morning_or_evening",
                    title = "Emil ist neugierig",
                    message =
                        "Bist du morgens produktiver oder " +
                                "arbeitest du lieber am Abend?"
                ),
                ConversationStarter(
                    id = "perfect_weekend",
                    title = "Wochenendfrage",
                    message =
                        "Wie sieht dein perfektes Wochenende aus?"
                ),
                ConversationStarter(
                    id = "superpower",
                    title = "Eine ungewöhnliche Frage",
                    message =
                        "Welche Superkraft würdest du wählen, " +
                                "wenn du nur eine haben könntest?"
                ),
                ConversationStarter(
                    id = "movie_evening",
                    title = "Filmabend bei Emil",
                    message =
                        "Lukas und ich suchen einen Film. " +
                                "Welche Art von Filmen magst du?"
                ),
                ConversationStarter(
                    id = "coffee_or_tea",
                    title = "Caféfrage von Emil",
                    message =
                        "Kaffee oder Tee? Ich glaube, diese " +
                                "Frage verrät viel über einen Menschen."
                ),
                ConversationStarter(
                    id = "one_language",
                    title = "Emil möchte deine Meinung",
                    message =
                        "Wenn du sofort eine weitere Sprache " +
                                "sprechen könntest, welche wäre es?"
                )
            )
    }
}