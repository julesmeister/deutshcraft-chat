package com.julesmeister.deutschcraft.feature.mission.domain.catalog

import com.julesmeister.deutschcraft.feature.mission.domain.MissionDifficulty
import com.julesmeister.deutschcraft.feature.mission.domain.MissionObjective
import com.julesmeister.deutschcraft.feature.mission.domain.MissionTemplate

object RestaurantOrderingMission {

    const val TEMPLATE_ID =
        "restaurant_ordering"

    const val GREET_WAITER_OBJECTIVE_ID =
        "restaurant_greet_waiter"

    const val ASK_FOR_MENU_OBJECTIVE_ID =
        "restaurant_ask_for_menu"

    const val ORDER_DRINK_OBJECTIVE_ID =
        "restaurant_order_drink"

    const val ORDER_MEAL_OBJECTIVE_ID =
        "restaurant_order_meal"

    const val ASK_FOR_BILL_OBJECTIVE_ID =
        "restaurant_ask_for_bill"

    fun create(): MissionTemplate {
        return MissionTemplate(
            id =
                TEMPLATE_ID,
            title =
                "Restaurant Ordering",
            description =
                "Practice ordering food and interacting " +
                        "with restaurant staff in German.",
            scenario =
                "The learner enters a restaurant in " +
                        "Germany and communicates with a waiter.",
            learnerRole =
                "Restaurant customer",
            assistantRole =
                "Waiter",
            difficulty =
                MissionDifficulty.A2,
            icon =
                "🍽️",
            objectives =
                createObjectives(),
            isEnabled =
                true
        )
    }

    private fun createObjectives():
            List<MissionObjective> {
        return listOf(
            MissionObjective(
                id =
                    GREET_WAITER_OBJECTIVE_ID,
                missionTemplateId =
                    TEMPLATE_ID,
                title =
                    "Greet the waiter",
                description =
                    "Begin the restaurant interaction " +
                            "with an appropriate German greeting.",
                sequenceNumber =
                    1,
                isRequired =
                    true,
                completionHint =
                    "Complete when the learner greets the " +
                            "waiter using an appropriate German " +
                            "greeting such as Hallo, Guten Tag, " +
                            "or Guten Abend."
            ),
            MissionObjective(
                id =
                    ASK_FOR_MENU_OBJECTIVE_ID,
                missionTemplateId =
                    TEMPLATE_ID,
                title =
                    "Ask for the menu",
                description =
                    "Politely ask to see the menu.",
                sequenceNumber =
                    2,
                isRequired =
                    true,
                completionHint =
                    "Complete when the learner asks for the " +
                            "menu or Speisekarte in German."
            ),
            MissionObjective(
                id =
                    ORDER_DRINK_OBJECTIVE_ID,
                missionTemplateId =
                    TEMPLATE_ID,
                title =
                    "Order a drink",
                description =
                    "Choose and order a drink in German.",
                sequenceNumber =
                    3,
                isRequired =
                    true,
                completionHint =
                    "Complete when the learner clearly " +
                            "orders a beverage in German."
            ),
            MissionObjective(
                id =
                    ORDER_MEAL_OBJECTIVE_ID,
                missionTemplateId =
                    TEMPLATE_ID,
                title =
                    "Order a meal",
                description =
                    "Choose and order a main meal in German.",
                sequenceNumber =
                    4,
                isRequired =
                    true,
                completionHint =
                    "Complete when the learner clearly " +
                            "orders food or a main course in " +
                            "German."
            ),
            MissionObjective(
                id =
                    ASK_FOR_BILL_OBJECTIVE_ID,
                missionTemplateId =
                    TEMPLATE_ID,
                title =
                    "Ask for the bill",
                description =
                    "Politely request the bill.",
                sequenceNumber =
                    5,
                isRequired =
                    true,
                completionHint =
                    "Complete when the learner asks for the " +
                            "bill or Rechnung in German."
            )
        )
    }
}