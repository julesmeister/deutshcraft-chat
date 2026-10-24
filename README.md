# DeutschCraft

DeutschCraft is a Telegram-inspired Android chat application for learning German through natural conversation. Instead of presenting traditional lessons, quizzes, or classroom-style exercises, it lets the user chat with an AI German companion while assessment and personalization happen quietly in the background.

The application is being built with Kotlin, Jetpack Compose, Hilt, Spring Boot, PostgreSQL, and a cloud-hosted language model such as Qwen.

## Product Vision

DeutschCraft should feel like a modern messaging app first and a learning platform second.

The user can:

- Chat naturally with an AI companion in German.
- Receive an appropriate AI response after each message.
- See three changing reply suggestions that can be selected or edited.
- View an estimated CEFR level and language progress.
- Track vocabulary, grammar, competency, and recurring struggles.
- Receive conversations that adapt to known weaknesses.
- Stay within the current CEFR difficulty until progression criteria are met.
- Approve or postpone progression to the next CEFR level.
- Experience polished animations, message effects, typing indicators, and responsive interactions.

## Core Product Principle

> Conversation comes first. Learning happens naturally in the background.

DeutschCraft should not feel like a tutorial platform with a chat feature. It should feel like a real chat application with an invisible German learning and assessment layer.

## Planned User Experience

### Chat experience

- Telegram-inspired chat interface
- User and assistant message bubbles
- AI typing indicator
- Automatic scrolling to the newest message
- Animated message appearance
- Text input with send action
- Three dynamic suggested replies
- Suggestions may fill the input instead of sending immediately
- Persistent conversation history
- Loading, retry, and error states

### German learning experience

- Conversation difficulty based on the user's current CEFR level
- Vocabulary and grammar assessment
- Recognition of recurring mistakes
- Tracking of strengths, weaknesses, and competencies
- Personalized conversation opportunities based on known struggles
- Estimated readiness for the next CEFR level
- User-controlled level progression

### Progress experience

The progress area is planned to display:

- Current CEFR level
- Readiness for the next level
- Vocabulary score
- Grammar score
- Conversation competency
- Strong areas
- Areas requiring improvement
- Common mistake categories
- Vocabulary growth
- Conversation activity
- Learning streaks

## Initial Technology Stack

### Android application

- Kotlin
- Jetpack Compose
- Material 3
- Android ViewModel
- StateFlow
- Kotlin Coroutines
- Hilt dependency injection
- Navigation Compose
- Retrofit and OkHttp, planned
- Room, planned

### Backend

- Java
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- Bean Validation
- Docker

### AI integration

- Qwen through a hosted API provider
- Spring Boot as the only service allowed to call the AI provider
- Provider abstraction through a gateway interface
- Structured AI output for messages, suggestions, and assessments

### Hosting plan

The initial low-budget deployment may use:

- A low-cost container host for Spring Boot
- Neon, Supabase, or another managed PostgreSQL provider
- A hosted Qwen-compatible API

AWS services may be introduced later if scaling or enterprise deployment requirements justify them.

## High-Level Architecture

```text
Android application
    |
    | HTTPS
    v
Spring Boot API
    |
    +--> PostgreSQL
    |
    +--> Hosted Qwen API
```

The Android application must never contain the AI provider's private API key. The phone communicates with Spring Boot, and Spring Boot communicates with the AI provider.

## Android Architecture

DeutschCraft follows unidirectional data flow and feature-oriented package organization.

```text
Compose UI
    |
    | UI actions
    v
ViewModel
    |
    | use cases / repository calls
    v
Repository interface
    |
    v
Repository implementation
    |
    +--> Remote API
    +--> Local database
```

### Current package direction

```text
com.julesmeister.deutschcraft
|-- DeutschCraftApplication.kt
|-- MainActivity.kt
|-- core
|   `-- di
|       `-- ChatModule.kt
`-- feature
    `-- chat
        |-- data
        |   `-- FakeChatRepository.kt
        |-- domain
        |   |-- AssistantReply.kt
        |   |-- ChatMessage.kt
        |   |-- ChatRepository.kt
        |   `-- ReplySuggestion.kt
        `-- presentation
            |-- ChatRoute.kt
            |-- ChatScreen.kt
            |-- ChatUiAction.kt
            |-- ChatUiState.kt
            |-- ChatViewModel.kt
            `-- component
                |-- ChatInputBar.kt
                |-- MessageBubble.kt
                |-- ReplySuggestions.kt
                `-- TypingIndicator.kt
```

## Dependency Injection

Hilt is used to construct and provide application dependencies.

Current dependency flow:

```text
ChatViewModel
    |
    | requests ChatRepository
    v
ChatModule
    |
    | binds interface to implementation
    v
FakeChatRepository
```

The fake repository allows the chat interface and state management to be completed before the Spring Boot API is available.

Later, the binding can be changed from `FakeChatRepository` to `RemoteChatRepository` without changing `ChatViewModel`.

## Current Development Milestone

The first vertical slice is an offline chat prototype.

Expected behavior:

1. The application opens to a chat screen.
2. The assistant displays an initial German greeting.
3. The user enters and sends a German message.
4. The user message appears in a right-aligned bubble.
5. An animated typing indicator appears.
6. The fake repository returns an assistant response.
7. The assistant response appears in a left-aligned bubble.
8. Three suggested replies appear.
9. Selecting a suggestion places it in the input field.
10. The user may edit the suggestion before sending it.

## Planned Backend Contract

A future message endpoint may return a response similar to:

```json
{
  "message": {
    "id": "message-123",
    "content": "Das klingt interessant! Wie fühlst du dich dabei?",
    "sender": "ASSISTANT",
    "createdAt": "2026-10-24T10:30:00Z"
  },
  "suggestions": [
    "Ich fühle mich sehr gut.",
    "Ich bin ein bisschen nervös.",
    "Ich weiß es noch nicht."
  ]
}
```

Assessment data should be generated separately from the conversational response so that chat and evaluation remain independently maintainable.

## Planned Assessment Model

DeutschCraft may track:

- Estimated CEFR level
- Grammar score
- Vocabulary score
- Conversation competency
- Comprehension
- Fluency
- Known vocabulary
- Recurring mistake categories
- Strengths
- Struggles
- Readiness for progression

Potential mistake categories include:

```text
ARTICLE
CASE
GENDER
WORD_ORDER
VERB_CONJUGATION
PREPOSITION
VOCABULARY
SPELLING
```

AI-generated scores are learning estimates and must not be presented as official language certification results.

## CEFR Progression Rules

- Conversations remain within the user's current CEFR level.
- The assessment engine accumulates evidence across multiple conversations.
- A single strong message must not trigger progression.
- The application prompts the user when readiness criteria are met.
- The user can approve progression or remain at the current level.
- Harder conversation topics and vocabulary are introduced only after approval.

## Development Roadmap

### Phase 1: Android chat prototype

- [x] Create Android project
- [x] Configure Jetpack Compose
- [x] Configure Hilt
- [x] Introduce chat domain models
- [x] Introduce UI state and actions
- [x] Add fake repository abstraction
- [ ] Complete Telegram-inspired chat screen
- [ ] Add typing and suggestion animations
- [ ] Add Compose previews
- [ ] Add ViewModel unit tests

### Phase 2: Navigation and progress UI

- [ ] Add Navigation Compose
- [ ] Create progress placeholder screen
- [ ] Add animated CEFR indicator
- [ ] Add strengths and struggles sections
- [ ] Add progress charts and statistics

### Phase 3: Spring Boot API

- [ ] Create Spring Boot project
- [ ] Implement conversation endpoints
- [ ] Implement message persistence
- [ ] Add PostgreSQL and database migrations
- [ ] Add validation and global exception handling
- [ ] Add backend unit and integration tests

### Phase 4: AI integration

- [ ] Create `TutorAiGateway` abstraction
- [ ] Add Qwen provider implementation
- [ ] Generate appropriate conversational replies
- [ ] Generate three structured reply suggestions
- [ ] Validate structured AI responses
- [ ] Add token and usage limits
- [ ] Add prompt versioning

### Phase 5: Learning assessment

- [ ] Define CEFR assessment rules
- [ ] Track vocabulary growth
- [ ] Track recurring struggles
- [ ] Generate personalized recommendations
- [ ] Add level readiness calculation
- [ ] Add user-approved progression

### Phase 6: Persistence and authentication

- [ ] Add Room for local chat caching
- [ ] Add Retrofit remote repository
- [ ] Add authentication
- [ ] Secure Spring Boot with JWT
- [ ] Enforce conversation ownership
- [ ] Add account and conversation deletion

### Phase 7: Deployment

- [ ] Containerize Spring Boot
- [ ] Deploy the backend
- [ ] Provision managed PostgreSQL
- [ ] Configure production secrets
- [ ] Add monitoring and error reporting
- [ ] Configure a custom API subdomain

## Security Requirements

- Never store the AI API key in the Android application.
- Never commit credentials or secrets to source control.
- Store production secrets in the hosting provider's secret manager.
- Use HTTPS for all production communication.
- Validate JWTs in Spring Security.
- Verify conversation ownership on every protected operation.
- Avoid logging complete tokens or sensitive conversations.
- Apply request-size, rate, and AI output limits.
- Provide conversation and account deletion capabilities.

## Local Development

### Android prerequisites

- Android Studio compatible with AGP 9
- JDK 17
- Android SDK 36
- Android device or emulator with API 26 or newer

### Build the Android application

From the project root on Windows:

```powershell
.\gradlew.bat assembleDebug
```

Or use Android Studio:

```text
Build > Make Project
```

### Run on a physical Android phone

1. Enable Developer options.
2. Enable USB debugging.
3. Connect the phone through USB.
4. Accept the debugging authorization prompt.
5. Select the device in Android Studio.
6. Run the `app` configuration.

## Configuration Notes

DeutschCraft currently uses Android Gradle Plugin 9 with built-in Kotlin support. The `org.jetbrains.kotlin.android` plugin must not be applied separately because AGP 9 already provides Kotlin support.

Hilt code generation uses KSP rather than KAPT.

Relevant plugins include:

```text
com.android.application
org.jetbrains.kotlin.plugin.compose
com.google.devtools.ksp
com.google.dagger.hilt.android
```

## Testing Strategy

### Android unit tests

- `ChatViewModel` state transitions
- Blank message rejection
- Duplicate-send prevention
- Successful assistant response handling
- Repository failure handling
- Suggestion selection

### Android UI tests

- Initial assistant greeting is visible
- User can enter and send a message
- Typing indicator appears during loading
- Assistant response appears
- Suggested replies appear
- Selected suggestion fills the input

### Backend tests

- Conversation service unit tests
- AI gateway contract tests
- Repository integration tests using PostgreSQL Testcontainers
- Authentication and ownership tests
- Validation and malformed AI response tests

## Design Direction

The visual identity should remain closer to Telegram than to a conventional education dashboard.

Key principles:

- Conversation-first layout
- Minimal instructional clutter
- Friendly but restrained use of color
- Fast, polished animations
- Clear chat hierarchy
- Lightweight progress access from the chat bar
- Suggestions that feel like messaging assistance, not quiz answers
- Learning feedback that supports the conversation rather than interrupting it

## Project Status

DeutschCraft is currently in early Android implementation. Hilt and the initial chat architecture are configured, and the current focus is completing the offline Telegram-style chat experience before adding networking.

## License

A license has not yet been selected. Add an appropriate license before accepting external contributions or distributing the source publicly.
