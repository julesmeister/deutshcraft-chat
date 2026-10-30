// app/src/main/java/com/julesmeister/deutschcraft/feature/mission/domain/recommendation/MissionScenarioContextCategory.kt

package com.julesmeister.deutschcraft.feature.mission.domain.recommendation

enum class MissionScenarioContextCategory {
    INTRODUCTION,
    PERSONAL_LIFE,
    FOOD_AND_DINING,
    SHOPPING,
    TRAVEL_AND_TRANSPORT,
    ACCOMMODATION,
    HEALTH,
    SOCIAL_PLANNING,
    SERVICES_AND_PROBLEMS,
    WORK_AND_CAREER,
    DECISION_AND_OPINION,
    STORYTELLING,
    PROFESSIONAL_COLLABORATION,
    NEGOTIATION_AND_CONFLICT,
    PRESENTATION_AND_PERSUASION,
    ACADEMIC_AND_ABSTRACT
}

val MissionScenarioType.contextCategory:
        MissionScenarioContextCategory
    get() =
        when (this) {
            MissionScenarioType.INTRODUCING_YOURSELF,
            MissionScenarioType.MEETING_SOMEONE ->
                MissionScenarioContextCategory.INTRODUCTION

            MissionScenarioType.TALKING_ABOUT_FAMILY,
            MissionScenarioType.DAILY_ROUTINE,
            MissionScenarioType.TALKING_ABOUT_HOBBIES,
            MissionScenarioType.TALKING_ABOUT_WEATHER,
            MissionScenarioType.DESCRIBING_YOUR_HOME,
            MissionScenarioType.MEETING_A_NEIGHBOR,
            MissionScenarioType.TALKING_ABOUT_YOUR_ROOM,
            MissionScenarioType.DESCRIBING_A_PERSON,
            MissionScenarioType.DESCRIBING_A_PLACE ->
                MissionScenarioContextCategory.PERSONAL_LIFE

            MissionScenarioType.CAFE_ORDERING,
            MissionScenarioType.RESTAURANT_ORDERING ->
                MissionScenarioContextCategory.FOOD_AND_DINING

            MissionScenarioType.GROCERY_SHOPPING,
            MissionScenarioType.CLOTHING_SHOPPING,
            MissionScenarioType.RETURNING_A_PRODUCT ->
                MissionScenarioContextCategory.SHOPPING

            MissionScenarioType.ASKING_FOR_DIRECTIONS,
            MissionScenarioType.PUBLIC_TRANSPORT,
            MissionScenarioType.BUYING_A_TRAIN_TICKET,
            MissionScenarioType.MISSING_A_TRAIN,
            MissionScenarioType.PLANNING_A_TRIP,
            MissionScenarioType.HANDLING_A_TRAIN_CANCELLATION,
            MissionScenarioType.PLANNING_A_GROUP_TRIP,
            MissionScenarioType.DISCUSSING_TRAVEL_EXPERIENCES ->
                MissionScenarioContextCategory.TRAVEL_AND_TRANSPORT

            MissionScenarioType.HOTEL_CHECK_IN,
            MissionScenarioType.VIEWING_AN_APARTMENT,
            MissionScenarioType.CHOOSING_BETWEEN_APARTMENTS ->
                MissionScenarioContextCategory.ACCOMMODATION

            MissionScenarioType.VISITING_A_PHARMACY,
            MissionScenarioType.DOCTOR_APPOINTMENT ->
                MissionScenarioContextCategory.HEALTH

            MissionScenarioType.MAKING_SIMPLE_WEEKEND_PLANS,
            MissionScenarioType.JOINING_A_BEGINNER_CLASS,
            MissionScenarioType.JOINING_A_SPORTS_CLUB,
            MissionScenarioType.BOOKING_AN_ACTIVITY,
            MissionScenarioType.PLANNING_A_DAY_OUT,
            MissionScenarioType.INVITING_SOMEONE,
            MissionScenarioType.ACCEPTING_OR_DECLINING_AN_INVITATION,
            MissionScenarioType.MAKING_SOCIAL_PLANS,
            MissionScenarioType.PLANNING_A_BIRTHDAY,
            MissionScenarioType.ORGANIZING_AN_EVENT,
            MissionScenarioType.GIVING_ADVICE_TO_A_FRIEND ->
                MissionScenarioContextCategory.SOCIAL_PLANNING

            MissionScenarioType.BASIC_PHONE_CALL,
            MissionScenarioType.MAKING_A_SIMPLE_APPOINTMENT,
            MissionScenarioType.CHANGING_AN_APPOINTMENT,
            MissionScenarioType.ASKING_ABOUT_OPENING_HOURS,
            MissionScenarioType.ASKING_A_NEIGHBOR_FOR_HELP,
            MissionScenarioType.DESCRIBING_A_PROBLEM,
            MissionScenarioType.EXPLAINING_A_DELIVERY_PROBLEM,
            MissionScenarioType.ASKING_FOR_INFORMATION,
            MissionScenarioType.SOLVING_AN_EVERYDAY_PROBLEM,
            MissionScenarioType.MAKING_A_COMPLAINT,
            MissionScenarioType.ARRANGING_A_SERVICE,
            MissionScenarioType.DEALING_WITH_A_MISUNDERSTANDING,
            MissionScenarioType.FORMAL_COMPLAINT,
            MissionScenarioType.HANDLING_A_CUSTOMER_REQUEST,
            MissionScenarioType.RESPONDING_TO_CRITICISM ->
                MissionScenarioContextCategory.SERVICES_AND_PROBLEMS

            MissionScenarioType.JOB_INTERVIEW,
            MissionScenarioType.WORKPLACE_SMALL_TALK,
            MissionScenarioType.EXPLAINING_WORK_EXPERIENCE,
            MissionScenarioType.COMPARING_JOB_OFFERS,
            MissionScenarioType.DISCUSSING_REMOTE_VS_OFFICE_WORK,
            MissionScenarioType.FORMAL_INTERVIEW ->
                MissionScenarioContextCategory.WORK_AND_CAREER

            MissionScenarioType.EXPRESSING_AN_OPINION,
            MissionScenarioType.GIVING_REASONS,
            MissionScenarioType.COMPARING_OPTIONS,
            MissionScenarioType.MAKING_A_DECISION,
            MissionScenarioType.EXPLAINING_ADVANTAGES_AND_DISADVANTAGES,
            MissionScenarioType.RESOLVING_A_SCHEDULING_CONFLICT,
            MissionScenarioType.DEFENDING_AN_OPINION,
            MissionScenarioType.DISCUSSING_A_SOCIAL_TOPIC,
            MissionScenarioType.PARTICIPATING_IN_A_DEBATE,
            MissionScenarioType.COMPARING_VIEWPOINTS,
            MissionScenarioType.GIVING_DETAILED_FEEDBACK ->
                MissionScenarioContextCategory.DECISION_AND_OPINION

            MissionScenarioType.TALKING_ABOUT_PAST_EXPERIENCES,
            MissionScenarioType.TELLING_A_STORY,
            MissionScenarioType.DESCRIBING_A_WEEKEND ->
                MissionScenarioContextCategory.STORYTELLING

            MissionScenarioType.TEAM_MEETING,
            MissionScenarioType.PROJECT_DISCUSSION,
            MissionScenarioType.PROBLEM_SOLVING_AT_WORK,
            MissionScenarioType.EXPLAINING_A_COMPLEX_PROCESS,
            MissionScenarioType.LEADERSHIP_CONVERSATION,
            MissionScenarioType.STRATEGIC_DISCUSSION,
            MissionScenarioType.SUMMARIZING_COMPLEX_INFORMATION ->
                MissionScenarioContextCategory.PROFESSIONAL_COLLABORATION

            MissionScenarioType.NEGOTIATION,
            MissionScenarioType.CONFLICT_RESOLUTION,
            MissionScenarioType.DIPLOMATIC_DISAGREEMENT,
            MissionScenarioType.COMPLEX_NEGOTIATION,
            MissionScenarioType.ADVANCED_MEDIATION ->
                MissionScenarioContextCategory.NEGOTIATION_AND_CONFLICT

            MissionScenarioType.PRESENTING_AN_IDEA,
            MissionScenarioType.BUSINESS_PITCH,
            MissionScenarioType.PROFESSIONAL_PRESENTATION,
            MissionScenarioType.PERSUASIVE_ARGUMENT,
            MissionScenarioType.MODERATING_A_DISCUSSION,
            MissionScenarioType.IMPROMPTU_ARGUMENTATION ->
                MissionScenarioContextCategory.PRESENTATION_AND_PERSUASION

            MissionScenarioType.POLICY_DISCUSSION,
            MissionScenarioType.CULTURAL_ANALYSIS,
            MissionScenarioType.ABSTRACT_PROBLEM_SOLVING,
            MissionScenarioType.ACADEMIC_DEBATE,
            MissionScenarioType.EXPERT_PANEL_DISCUSSION,
            MissionScenarioType.RHETORICAL_ANALYSIS,
            MissionScenarioType.NUANCED_ETHICAL_DISCUSSION,
            MissionScenarioType.CRITICAL_LITERARY_DISCUSSION,
            MissionScenarioType.TECHNICAL_EXPERT_EXPLANATION,
            MissionScenarioType.REGISTER_ADAPTATION ->
                MissionScenarioContextCategory.ACADEMIC_AND_ABSTRACT
        }