package com.julesmeister.deutschcraft.feature.chat.presentation.theme

/** Non-visual chat presentation constants, kept separate from appearance and motion tokens. */
internal object ChatPresentationTokens {

    object AdaptiveLearningProcessor {
        const val ProcessorTag = "AdaptiveLearningChat"
        const val FeedbackTag = "AdaptiveFeedback"
    }

    object BesserProcessor {
        const val BesserTag = "ContextualBesser"
        const val RecentCapabilityHistoryLimit = 4
    }

    object Errors {
        const val Load = "DeutschCraft could not load " + "the conversation."
        const val MessageLoad = "The saved conversation could " + "not be loaded."
        const val ConversationLoading = "The conversation is still loading."
        const val Send = "The assistant could not respond. " + "Your message was saved locally."
    }

    object MessageSender {
        const val MessageSenderTag = "ChatMessageSender"
        const val FirstSuggestionCount = 1
        const val MaximumSuggestionCount = 3
    }

    object MissionEvaluationCoordinator {
        const val CoordinatorTag = "ChatMissionEvaluation"
    }

    object MissionLearningProcessor {
        const val ProcessorTag = "ChatMissionLearning"
    }

    object MissionOfferCoordinator {
        const val CoordinatorTag = "ChatMissionOffer"
    }

    object PostSendLearningProcessor {
        const val ProcessorTag = "ChatPostSendLearning"
        const val RecentLearningMessageCount = 12
    }

    object SessionCoordinator {
        const val CoordinatorTag = "ChatSessionCoordinator"
        const val LoadError = "DeutschCraft could not load the conversation."
        const val MessageLoadError = "The saved conversation could not be loaded."
        const val ActiveMissionUnavailableError = "Die aktive Mission ist noch nicht verfügbar."
        const val DefaultMissionTitle = "Gesprächsmission"
    }

    object SessionInitializer {
        const val InitialGreeting = "Hallo! Wie war dein Tag?"
        const val InitialMessageSequenceNumber = 1L
        const val ProvisionalGreetingIdSuffix = "-provisional-greeting"
    }

    object SessionMessageController {
        const val ControllerTag = "ChatSessionMessage"
        const val ConversationLoadingError = "The conversation is still loading."
        const val SendError = "The assistant could not respond. " + "Your message was saved locally."
        const val BesserRefinementError = "Die Verbesserung konnte nicht gespeichert werden."
        const val UnchangedFeedback = "Ändere deinen Satz, damit er die Aufgabe erfüllt."
        const val HintSuggestionSuffix = " Tippe auf „Tipp“, wenn du Hilfe brauchst."
    }

    object SessionMissionController {
        const val ControllerTag = "ChatSessionMission"
        const val MissionStartError = "The mission could not be started."
        const val MissionVariationStartError = "Die neue Richtung konnte nicht gestartet werden."
        const val ConversationUnavailableError = "Die Unterhaltung ist noch nicht verfügbar."
        const val VariationContextUnavailableError = "Die gewählte Missionsrichtung ist nicht mehr verfügbar."
        const val ActiveMissionRequiredError = "Die aktive Mission ist nicht mehr verfügbar."
        const val ActiveMissionChangedError = "Die Mission hat sich inzwischen geändert. " + "Öffne „Andere Richtung“ erneut."
        const val ActiveMissionExtensionError = "Eine abgeschlossene Mission kann nicht erweitert werden, " + "solange bereits eine andere Mission aktiv ist."
        const val VariationCancelledMessage = "Die neue Richtung wurde abgebrochen."
    }

    object SessionObservers {
        const val ObserversTag = "ChatSessionObservers"
    }

    object UiState {
        const val ActiveMissionBranchSuggestionId = "active-mission-open-variations"
        const val ActiveMissionBranchSuggestionText = "✦ Andere Richtung"
        const val MaximumVisibleSuggestionCount = 3
    }

    object UiStateExtensions {
        const val MissionContinuationSuggestionId = "mission-continuation-open"
        const val MissionContinuationDismissSuggestionId = "mission-continuation-dismiss"
        const val MaximumVisibleSuggestions = 3
        const val StrongVariationScoreThreshold = 75
    }

    object ViewModel {
        const val DefaultMissionTitle = "Conversation mission"
    }

    object BesserRefinementHeader {
        const val TitleLabel = "Besser"
        const val EvaluatingLabel = "Wird geprüft…"
    }

    object GlassInputField {
        const val DefaultPlaceholder = "Nachricht"
        const val RefinementPlaceholder = "Verbessere deine Antwort"
    }

    object MessageList {
        const val DateContentType = "date"
        const val MessageContentType = "message"
        const val DateKeyPrefix = "date-"
        const val TodayLabel = "Heute"
        const val YesterdayLabel = "Gestern"
        const val CurrentYearDatePattern = "EEEE, d. MMMM"
        const val OtherYearDatePattern = "d. MMMM yyyy"
    }

    object MessageBubbleContent {
        const val TimestampReservationPrefix = "\u2002\u2002"
        const val TimeFormatPattern = "HH:mm"
    }

    object MissionOfferCard {
        const val MissionLabel = "Conversation mission"
        const val MaybeLaterLabel = "Maybe later"
        const val StartMissionLabel = "Start mission"
    }

    object LevelProgressViewModel {
        const val ViewModelTag = "ChatLevelProgress"
    }

    object SelectImmediateLevelNeedUseCase {
        const val NextStepPrefix = "next_step"
        const val CefrFieldPrefix = "cefr="
        const val ScoreFieldPrefix = "score="
        const val ReinforcementSeparator = " | "
        const val MinimumCapabilitySignalPartCount = 2
    }
}
