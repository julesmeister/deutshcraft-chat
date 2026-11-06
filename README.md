# DeutschCraft

**DeutschCraft** is an offline-first, Telegram-inspired Android application for learning German through natural AI conversations.

Rather than teaching through lessons, quizzes, flashcards, or isolated exercises, DeutschCraft teaches through meaningful communication.

Learners practice German by chatting naturally with a persistent AI companion while the application quietly performs assessment, correction, CEFR evaluation, mission tracking, vocabulary analysis, progress measurement, adaptive learning, and personalized learning support in the background.

> **Conversation first. Learning happens naturally.**

---

# Vision

Most language-learning applications follow this pattern:

```text
Lesson
   ↓
Exercise
   ↓
Quiz
   ↓
Conversation
```

DeutschCraft reverses that relationship:

```text
Natural Conversation
          ↓
Real Communication
          ↓
Learning Happens Naturally
```

The learner opens DeutschCraft because the learner wants to communicate.

Improvement in German proficiency becomes a natural outcome of engaging conversations rather than completing lessons.

---

# Core Philosophy

## Conversation First

DeutschCraft should feel closer to:

```text
Telegram
WhatsApp
Messenger
```

than:

```text
Language Course
Classroom App
Exercise Platform
```

The conversation itself is the product.

Learning systems support the conversation without unnecessarily interrupting it.

---

## Human-Like AI Companion

DeutschCraft uses a consistent German AI companion named **Emil**.

Example profile:

```text
Name:
Emil

Location:
Leipzig

Interests:
Books
Travel
Football
Cooking
German Culture
```

Emil adapts to:

- CEFR level
- Mission context
- Previous conversations
- Vocabulary familiarity
- Learning progress
- Adaptive-learning signals

while maintaining a consistent personality.

---

# Offline-First Architecture

DeutschCraft follows an offline-first architecture.

Room remains the primary source of truth.

```text
Room Database
      ↓
Repositories
      ↓
Flow / StateFlow
      ↓
ViewModels
      ↓
Jetpack Compose
```

Persistent local data includes:

```text
Conversations
Messages
Corrections
Assessments
Progress
Missions
Mission Templates
Mission Objectives
Mission Objective Progress
Vocabulary
Activity Tracking
Adaptive Learning
Besser Challenges
Settings
```

The application remains useful without continuous internet access.

Available offline:

- View conversations
- Read previous chats
- View archived threads
- View the progress dashboard
- View mission history
- Continue locally stored conversations and missions
- View vocabulary history
- View activity statistics
- View learning history
- Change settings

New AI-generated replies require a working connection to the configured AI backend.

---

## Room as Source of Truth

All persistent learner data is written locally first.

```text
User Action / AI Response
          ↓
Repository
          ↓
Room Transaction
          ↓
Flow / StateFlow
          ↓
Compose UI
```

Cloud synchronization does not replace Room.

Local mutations follow:

```text
Local Mutation
      ↓
Room Commit
      ↓
UI Updates
      ↓
Background Sync Requested
      ↓
AWS
```

Network operations are not performed inside Room transactions.

A failed cloud request therefore does not remove a successfully committed conversation, message, correction, or mission update.

Room owns:

- Conversations
- Messages
- Besser corrections and challenges
- Assessments
- Progress records
- Missions
- Mission templates
- Mission objectives
- Mission objective progress
- Vocabulary and marked phrases
- Activity tracking
- Adaptive-learning evidence
- Learner state

---

# Authentication

DeutschCraft includes authenticated account support backed by Amazon Cognito.

Google sign-in is integrated through the Cognito authentication flow.

```text
Google Sign-In
      ↓
Amazon Cognito
      ↓
Access Token
      ↓
Android Auth Session
      ↓
Authenticated API Requests
```

Protected API requests carry the current Cognito access token.

Cloud ownership is derived from the authenticated Cognito `sub` rather than from an arbitrary user identifier supplied by Android.

For authentication diagnostics in debug builds, filter Android Studio Logcat with:

```text
tag:DeutschCraftAuth
```

Diagnostics expose safe metadata such as:

- Signed-in state
- Authorization-header presence
- Token availability
- Token type
- Expiration information
- Issuer/client configuration comparison
- Refresh outcomes
- HTTP response status
- AWS gateway request IDs

Tokens, credentials, personal claims, and chat bodies are excluded from authentication diagnostic logs.

---

# Cloud Synchronization

DeutschCraft includes authenticated cloud synchronization while preserving Room as the operational source of truth.

## Architecture

```text
Room
  ↓
SyncScheduler
  ↓
WorkManager
  ↓
DeutschCraftSyncWorker
  ↓
SyncRepository
  ↓
Retrofit / OkHttp
  ↓
Amazon API Gateway
  ↓
JWT Authorizer
  ↓
AWS Lambda
  ↓
Amazon DynamoDB
```

Cloud synchronization currently supports:

```text
✅ Conversations
✅ Messages
✅ Conversation Missions
✅ Mission Template Snapshots
✅ Mission Objective Snapshots
✅ Mission Objective Progress
```

The synchronization system provides:

- Authenticated per-user cloud storage
- Initial cloud backup
- Incremental cloud downloads
- Batched uploads
- Immediate synchronization after supported local mutations
- Periodic synchronization
- Archive synchronization
- Restore synchronization
- Tombstone-based deletion
- Mission-state synchronization
- Generated mission-template preservation
- Objective-progress preservation
- Cross-device synchronization infrastructure

---

## Sync Scheduling

Synchronization is coordinated by `SyncScheduler`.

Two forms of WorkManager work are maintained:

```text
Immediate Sync
     +
Periodic Sync
```

After sign-in:

```text
Signed In
   ↓
Immediate Sync
   ↓
Upload Pending Changes
   ↓
Download Remote Changes
```

A separate periodic worker is registered with an initial delay:

```text
Periodic Sync
     ↓
Initial Delay: 15 Minutes
     ↓
Background Synchronization
```

The initial delay prevents the periodic worker from duplicating the immediate sign-in synchronization.

Rapid local mutations use unique WorkManager work so unnecessary duplicate workers can be collapsed.

---

## Hilt and WorkManager

`DeutschCraftSyncWorker` uses Hilt-assisted construction.

```text
WorkManager
     ↓
HiltWorkerFactory
     ↓
DeutschCraftSyncWorker
     ↓
SyncRepository
```

DeutschCraft supplies a custom WorkManager configuration through the application class.

The default WorkManager initializer is disabled so WorkManager uses the Hilt worker factory.

---

## Immediate Mutation Sync

Supported local mutations request synchronization after the local write succeeds.

Examples include:

```text
Create Conversation
Save Message
Generate Conversation Title
Update Message Correction
Archive Conversation
Restore Conversation
Delete Conversation
Refine Besser Message
Offer Mission
Start Mission
Branch Mission
Complete Mission Objective
Dismiss Mission
Complete Mission
Delete Conversation Missions
```

The ordering is intentionally:

```text
Room Transaction
      ↓
Commit
      ↓
requestImmediateSync()
      ↓
WorkManager
```

This preserves DeutschCraft's offline-first behavior.

---

## Upload Batching

The synchronization API accepts a maximum of:

```text
100 changes per upload request
```

Android automatically splits larger pending datasets into batches.

Example:

```text
350 Pending Changes
       ↓
Batch 1: 100
Batch 2: 100
Batch 3: 100
Batch 4: 50
```

Each successful batch is acknowledged independently.

Successfully acknowledged records are marked synchronized after each batch.

If a later batch fails, already acknowledged records do not need to be uploaded again.

---

## Incremental Downloads

DeutschCraft maintains a per-user synchronization cursor.

Cloud downloads use:

```text
GET /v1/sync?since=<cursor>
```

The synchronization Lambda returns records newer than the supplied cursor.

```text
Previous Cursor
      ↓
GET /v1/sync?since=<cursor>
      ↓
Only Newer Changes
      ↓
Apply to Room
      ↓
Store New Cursor
```

When no new changes exist, the server returns a small response instead of redownloading the learner's complete history.

---

## Synchronization Metadata

Synchronized Room entities track metadata including:

```text
syncStatus
remoteVersion
lastSyncedAt
updatedAt
isDeleted
deletedAt
```

Local dirty states include:

```text
LOCAL_ONLY
PENDING_UPLOAD
PENDING_DELETE
```

Remote changes do not overwrite locally dirty entities.

Client timestamps and server versions are compared before incoming changes are applied.

---

## Deletion Tombstones

Deletion uses tombstones instead of immediately forgetting cloud identity.

```text
Local Delete
     ↓
isDeleted = true
syncStatus = PENDING_DELETE
     ↓
POST /v1/sync
     ↓
DynamoDB
deleted = true
```

Another device can therefore learn that the entity was intentionally deleted.

This prevents an older local copy from resurrecting deleted data.

Tombstone infrastructure currently covers:

```text
Conversations
Messages
Conversation Missions
```

---

# Conversation Sync

Conversation records use:

```text
CONVERSATION#<conversationId>
```

Synchronized conversation state includes:

- Title
- Title source
- CEFR level
- Local date
- Epoch day
- Creation timestamp
- Update timestamp
- Closed state
- Archive state
- Message count
- Preview text
- Deletion state

Archive and restore operations mark the conversation pending for upload and trigger immediate synchronization.

---

# Message Sync

Message records use:

```text
MESSAGE#<conversationId>#<paddedSequenceNumber>
```

Synchronized message state includes:

- Message ID
- Conversation ID
- Content
- Sender
- Sequence number
- Creation timestamp
- Update timestamp
- Correction
- Deletion state

Sequence numbers are padded in cloud keys to preserve deterministic ordering.

---

# Mission Cloud Sync

Conversation missions participate in cloud synchronization.

Mission records use:

```text
CONVERSATION_MISSION#<conversationId>#<conversationMissionId>
```

Each mission is synchronized as a self-contained snapshot:

```text
ConversationMission
        +
MissionTemplate
        +
MissionObjectives
        +
ObjectiveProgress
        ↓
Cloud Mission Snapshot
```

This matters because generated and branched mission templates can exist only on the device where the mission branch was created.

The receiving device can reconstruct the mission without assuming that the generated template already exists locally.

---

## Conversation Mission Snapshot

A synchronized mission contains:

```text
Conversation ID
Mission Template ID
Root Mission Template ID
Source Recommendation ID
Target Capability IDs
Adaptive Focus Keys
Mission Status
Started At
Completed At
Dismissed At
Created At
Deleted At
Mission Template
Mission Objectives
Mission Objective Progress
```

Mission states include:

```text
OFFERED
ACTIVE
COMPLETED
DISMISSED
```

Mission changes become pending for synchronization when:

- A mission is offered
- A mission starts
- The active branch changes
- An objective completes
- A mission is dismissed
- A mission completes
- Conversation mission data is deleted

---

## Mission Template Snapshot

Mission snapshots include the current mission template.

Template information includes:

- Template ID
- Title
- Description
- Scenario
- Learner role
- Assistant role
- CEFR difficulty
- Icon
- Enabled state
- Creation timestamp
- Update timestamp

This allows generated mission branches to survive cloud restoration.

---

## Mission Objective Snapshot

Mission objectives are bundled with the mission snapshot.

Objective information includes:

- Objective ID
- Mission template ID
- Title
- Description
- Sequence number
- Required state
- Completion hint
- Coaching hints
- Target vocabulary
- Example phrases

---

## Mission Objective Progress

Objective progress preserves:

```text
Conversation Mission ID
Objective ID
Completed State
Completed At
Completed By Message ID
Confidence
Updated At
```

Completing an objective marks the parent mission pending for synchronization.

When all required objectives are completed, the mission can transition to `COMPLETED`.

---

## Mission Progress Identity

Mission synchronization maintains a canonical mission identity.

The expected invariant is:

```text
DynamoDB entityId
        =
ConversationMissionEntity.id
        =
Final Mission ID in entityKey
        =
objectiveProgress[*].conversationMissionId
```

Objective progress is normalized against the enclosing mission ID during serialization.

Downloaded objective progress is normalized to the downloaded mission's canonical entity ID before being persisted into Room.

This protects branched and historical missions from mismatched progress ownership.

---

# CEFR Progression

DeutschCraft supports:

```text
A1
A2
B1
B2
C1
C2
```

Assessment is evidence-based.

Rather than relying exclusively on exams, DeutschCraft gathers evidence from real conversations.

Tracked skills include:

- Grammar
- Vocabulary
- Accuracy
- Fluency
- Complexity
- Coherence
- Comprehension

Level advancement is recommended rather than forced.

---

# Current Features

## Chat Experience

Implemented:

- Telegram-style conversations
- Multiple conversation threads
- Automatic chat title generation
- Thread archiving
- Thread restoration
- Reply suggestions
- Message actions
- Keyboard-aware layout
- Auto-scroll behavior
- Typing indicator animations
- Message arrival animations
- Message press animations
- Navigation animations
- Custom chat composer
- Dark mode
- Light mode
- Theme-aware UI
- Contextual learning bubbles

The chat header and in-list typing indicator share the same animated dots.

The chat level indicator and threads overflow menu use custom cradled dropdown surfaces.

The threads menu uses a compact 280dp body below the adjacent streak control with an 8dp minimum window gutter and fade, slide, and scale transitions.

Its cradle matches the menu button's 40dp diameter without an expanded collar so the adjacent controls remain in their resting positions.

The chat level indicator retains its larger curved cradle.

---

## Chat Error Handling

New AI replies require connectivity and access to the AI backend.

Failed requests display a dismissible error state rather than fabricating an assistant reply.

```text
Learner Message
      ↓
Room ✅
      ↓
AI Request
      ↓
Failure
      ↓
Error UI
```

The learner's saved message remains in Room.

Further sends are blocked while the error state is displayed.

The compact Retry control clears the error and re-enables manual sending.

It does not automatically duplicate the saved message or silently repeat the failed request.

---

# AI Conversation

Current provider:

```text
Qwen
```

Features include:

- Natural German conversation
- Persona-based responses
- CEFR-aware responses
- Conversation continuity
- Topic adaptation
- Mission participation
- Structured correction
- Structured evaluation
- Mission generation
- Adaptive-learning support

---

# AI Architecture

Current architecture:

```text
Android
    ↓
Amazon Cognito Authentication
    ↓
Amazon API Gateway
    ↓
AWS Lambda
    ↓
Qwen
```

AI provider credentials are never stored inside the Android application.

```text
Android APK
   ❌ Qwen API Key

AWS Backend
   ✅ Provider Credential
```

Protected Android requests use Cognito authentication.

The Lambda backend controls external provider access.

---

# AWS Architecture

DeutschCraft currently uses AWS for authentication, protected AI access, and cloud synchronization.

```text
Android
   ↓
Amazon Cognito
   ↓
Access Token
   ↓
Amazon API Gateway
   ├── /v1/chat/completions
   └── /v1/sync
          ↓
       AWS Lambda
          ↓
   ┌──────┴──────┐
   ↓             ↓
 Qwen        DynamoDB
```

The AWS architecture currently provides:

```text
Authentication
Protected AI Proxy
Conversation Backup
Message Backup
Mission Backup
Incremental Restore
Cross-Device Sync Infrastructure
```

---

# DynamoDB Sync Model

Cloud records are partitioned by authenticated Cognito identity.

Conceptually:

```text
userId = Cognito sub
```

Cloud entity keys include:

```text
CONVERSATION#<conversationId>

MESSAGE#<conversationId>#<sequenceNumber>

CONVERSATION_MISSION#
<conversationId>#
<conversationMissionId>
```

Cloud records contain synchronization metadata including:

```text
entityType
entityId
payloadJson
clientUpdatedAt
serverUpdatedAt
deleted
schemaVersion
```

Supported entity types currently include:

```text
CONVERSATION
MESSAGE
CONVERSATION_MISSION
```

---

# Besser System

DeutschCraft contains a contextual Besser correction and challenge system.

Example:

```text
Learner:
Ich go heute zum Markt.

Besser:
Ich gehe heute zum Markt.
```

The Besser system supports:

- Contextual corrections
- Refinement challenges
- Progressive hints
- Assistance tracking
- Evaluation scores
- Evaluation feedback
- Evaluation evidence
- Capability relationships
- Mastery-oriented learning evidence

Original learner messages are preserved unless the learner explicitly completes a refinement flow that updates the source message.

When Besser refinement changes the source message, that message is marked pending for synchronization and an immediate sync is requested.

Besser challenge state itself remains part of the broader learner-state synchronization roadmap.

---

# Message Actions

Messages support contextual actions including:

```text
Translate
Explain
Rephrase
```

Planned or expanding actions include:

```text
Save Vocabulary
Challenge Me
Add To Practice
```

---

# Mission System

## Purpose

Missions teach German through realistic communication scenarios.

Example:

```text
🎯 Restaurant Ordering
```

Objectives might include:

```text
✓ Greet the waiter
✓ Ask for a menu
□ Order a drink
□ Order a meal
□ Ask for the bill
```

Objectives are completed naturally through conversation.

---

## Mission Flow

```text
Conversation
      ↓
Mission Offered
      ↓
Mission Accepted
      ↓
Mission Active
      ↓
Objectives Detected
      ↓
Mission Completed
      ↓
Performance Review
```

New missions suggested inside ordinary chats use a multi-goal structure similar to thread-list missions.

Generated mission branches preserve their original mission template as a semantic root while allowing the current scenario and objectives to evolve.

Mission progress survives application restarts and now participates in cloud synchronization.

---

## Mission Branching

Mission variations can continue or reshape an active mission through flows such as:

```text
Noch eine Runde
Neue Situation
Neue Wörter
Grammatik-Stufe
```

The original mission remains the root anchor.

A branch may produce:

```text
Existing ConversationMission
        ↓
New Generated MissionTemplate
        ↓
Preserved Completed Objectives
        +
New Branch Objectives
        ↓
Rebuilt Objective Progress
```

The `ConversationMission` retains its identity while the active template changes.

This mission identity is preserved by cloud synchronization.

---

## Thread-List Mission Recommendations

The mission cell reuses eligible recommendations stored in Room before asking Qwen to generate additional recommendations.

New scenarios use recent ordinary learner messages as a local novelty signal without directly adding raw conversation history to the recommendation-generation prompt.

Recommendation logic preserves:

- CEFR suitability
- Adaptive reinforcement
- Objectives
- Hints
- Vocabulary
- Examples
- Scenario diversity

Completed scenario identities are excluded from future recommendations where they can be reliably identified.

A completion ledger can preserve scenario identity even when the original thread is later removed.

Active or paused mission scenarios can also be excluded from duplicate recommendations.

Skipping does not restore an otherwise ineligible recommendation.

Recommendation refill operations use generation locks and temporary failure backoff to avoid unnecessary repeated AI requests.

---

## Mission Infrastructure

Implemented:

- Mission templates
- Mission entities
- Conversation missions
- Mission repository
- Mission DAO
- Active mission tracking
- Objective progress tracking
- Objective completion detection
- Mission completion detection
- Mission history
- Mission persistence
- Objective completion events
- Mission branching
- Mission variation flows
- Mission cloud snapshots
- Mission objective cloud snapshots
- Objective-progress cloud snapshots
- Mission tombstones
- Immediate mission synchronization

---

## Mission Top Bar

During an active mission, a small handle beneath the top bar exposes a mission details drawer.

The drawer follows drag gestures and settles open or closed according to movement and velocity.

The drawer overlays the conversation so messages do not move.

Mission paging and automatic advancement pause while the drawer is expanded.

Reduced-motion mode avoids unnecessary animated transitions.

The expanded mission interface includes:

```text
Progress Ring
CEFR Badge
Current Goal
Goal Timeline
Progressive Hints
Vocabulary Support
Grammar Support
Example Phrases
```

The chat top bar can display:

```text
🎯 Active Objectives
💡 Guidance
📈 Progress
✅ Completion Feedback
```

Post-send mission evaluation runs before supplementary Besser and adaptive-learning processing.

New goal-completion confirmations can interrupt rotating mission pages as soon as the completion reaches Room.

While a mission objective is active, the full objective description can appear as the empty composer's placeholder.

It is not inserted into the draft or sent as the learner's response.

---

# Progress Dashboard

The Progress screen combines language assessment and mission achievements.

Displayed information includes:

```text
Current CEFR Level
Readiness Percentage
Skill Breakdown
Activity Statistics
Evidence Confidence
Mission Progress
Completed Missions
Mission History
```

Example:

```text
Current Level:
A2

Readiness For B1:
78%
```

Mission achievements update through Room flows.

---

## Mission Progress

Implemented:

```text
Completed Mission Count
Recent Mission History
Animated Mission Progress
Mission Completion Records
```

Example:

```text
✅ Weather Conversation
✅ Restaurant Ordering
✅ Job Interview
```

Mission completion updates automatically through Room.

---

# Adaptive Learning

DeutschCraft contains an adaptive-learning foundation that tracks learner evidence, capabilities, and learning focus areas.

Adaptive data can influence:

- Mission recommendations
- Progress interpretation
- Capability reinforcement
- Vocabulary focus
- Besser challenges
- Future learning priorities

The broader adaptive-learning state currently remains primarily local.

Cloud synchronization for the wider learner model is planned as a later synchronization phase.

---

# Settings System

Implemented settings include:

```text
Theme
Font Size
Reduce Animations
Mission Hints
Example Phrases
Target Vocabulary
Progress Language
```

Architecture:

```text
DataStore
      ↓
SettingsRepository
      ↓
SettingsViewModel
      ↓
Settings UI
```

Settings persist across application launches.

Font size works with Android's system font scale without applying the same scaling twice.

Reduced-motion settings limit application-controlled Compose animations.

The mission pager also stops automatic rotation and unnecessary scrolling behavior in reduced-motion mode.

Mission hints, example phrases, and target-vocabulary settings control their corresponding learner-facing support sections without deleting stored evidence or progress.

---

# Learning Support

Learning assistance includes:

```text
Mission Hints
Example Phrases
Vocabulary Guidance
Grammar Guidance
Besser Corrections
Besser Challenges
CEFR Assessment
Adaptive Reinforcement
```

The goal is to provide support without unnecessarily interrupting natural conversation.

---

# Activity Tracking

Tracked information includes:

- Active days
- Conversation activity
- Assessment activity
- Mission participation
- Language-growth evidence
- Current streak information

Future and expanding analytics include:

- Weekly consistency
- Monthly consistency
- Conversation duration
- Mission success rate
- Cross-device activity synchronization

---

# AI Cost Controls

Chat persona and active-mission instructions use compact prompts while retaining:

- Emil's identity
- CEFR adaptation
- Conversation behavior
- Learner-role guidance
- Mission coaching rules

Structured sentence correction and sentence analysis use stable system prompts and JSON-oriented responses.

Validated results for selected corrections, analyses, and contextual phrase explanations can be reused through bounded process-local caches.

Identical concurrent requests can share one operation.

Cache keys preserve meaningful differences such as case, punctuation, source sentence, and phrase type where applicable.

Valid no-correction results can be reused.

Failures and invalid model responses are not cached.

Caches are not persisted to disk and are discarded when the application process ends.

Conversational replies, mission generation, and contextual learning evaluation are not blindly served from these caches.

---

# Technology Stack

## Android

```text
Kotlin
Jetpack Compose
Material 3
Room
DataStore
Hilt
Navigation Compose
Coroutines
StateFlow
ViewModel
WorkManager
Retrofit
OkHttp
Kotlin Serialization
```

## AWS

```text
Amazon Cognito
Amazon API Gateway
AWS Lambda
Amazon DynamoDB
Amazon CloudWatch
```

## AI

```text
Qwen
```

## Possible Future Backend Expansion

```text
Spring Boot
Spring Security
PostgreSQL
Docker
```

The current AWS-backed architecture already removes direct AI credentials from Android and provides authenticated synchronization.

---

# Project Structure

```text
com.julesmeister.deutschcraft
│
├── app
│   └── navigation
│
├── core
│   ├── cefr
│   ├── database
│   │   ├── dao
│   │   ├── entity
│   │   └── migration
│   ├── datastore
│   ├── di
│   └── network
│
├── feature
│   ├── auth
│   ├── chat
│   ├── threads
│   ├── archivedthreads
│   ├── mission
│   ├── progress
│   ├── assessment
│   ├── adaptivelearning
│   ├── besser
│   ├── settings
│   ├── engagement
│   ├── messageactions
│   └── sync
│       ├── data
│       ├── domain
│       ├── scheduling
│       └── worker
│
├── DeutschCraftApplication.kt
└── MainActivity.kt
```

---

# Database Evolution

DeutschCraft uses explicit Room migrations as the local model evolves.

Recent cloud synchronization migrations include:

```text
Room 19
   ↓
19 → 20
Conversation and Message Sync Metadata
   ↓
Room 20
   ↓
20 → 21
Conversation Mission Sync Metadata
   ↓
Room 21
```

Synchronization metadata is introduced through migrations rather than destructive database recreation so existing learner history can be preserved.

---

# Development Roadmap

## Foundation ✅

- Jetpack Compose
- Hilt
- Navigation
- Room
- Repositories
- DataStore
- Coroutines
- StateFlow
- WorkManager

---

## Offline Learning Engine ✅

- CEFR assessment
- Progress tracking
- Activity tracking
- Persistent conversations
- Mission persistence
- Objective persistence
- Besser foundation
- Adaptive-learning foundation

---

## Conversation Experience ✅

Implemented:

- Threads
- Archive and restore
- Reply suggestions
- Message actions
- Typing indicators
- Top-bar guidance
- Theme support
- Animated mission experiences
- Error-aware AI messaging

Continuing improvements:

- UX polish
- Accessibility refinements

---

## Authentication and Secure AI Gateway ✅

Implemented:

```text
✅ Amazon Cognito integration
✅ Google sign-in flow
✅ Access-token authentication
✅ API Gateway JWT protection
✅ Lambda-based AI proxy
✅ AI API key removed from Android
✅ Debug authentication diagnostics
```

---

## Cloud Sync Phase 3.1 ✅

Conversation and message synchronization:

```text
✅ Conversation upload
✅ Message upload
✅ Initial cloud backup
✅ Cloud download
✅ Incremental since cursor
✅ 100-record upload batching
✅ Per-batch acknowledgement
✅ Immediate mutation sync
✅ Periodic background sync
✅ Hilt WorkManager integration
✅ Duplicate sign-in sync removed
✅ Archive synchronization
✅ Restore synchronization
✅ Conversation deletion tombstones
✅ Message deletion tombstones
✅ Cognito sub user isolation
✅ DynamoDB persistence
```

---

## Cloud Sync Phase 3.2 🚧

Mission synchronization:

```text
✅ ConversationMission sync metadata
✅ Room 20 → 21 migration
✅ CONVERSATION_MISSION cloud entity
✅ Mission snapshot upload
✅ Mission template snapshot
✅ Mission objective snapshot
✅ Mission objective progress snapshot
✅ Mission-offer synchronization
✅ Mission-start synchronization
✅ Objective-completion synchronization
✅ Mission-completion synchronization
✅ Mission-dismissal synchronization
✅ Mission-branch synchronization
✅ Mission deletion tombstones
✅ Canonical mission ID normalization

🚧 Cross-device active mission continuation verification
🚧 Device-to-device mission progress convergence testing
🚧 Mission recommendation/completion identity synchronization
```

---

## Cloud Sync Phase 3.3 Planned

Broader learner-state synchronization:

```text
⬜ Learner Profile
⬜ CEFR Progress State
⬜ Assessment Evidence
⬜ Activity and Streak Data
⬜ Adaptive Learning Focus
⬜ Adaptive Learning Evidence
⬜ Besser Challenge State
⬜ Besser Mastery / Capability State
⬜ Marked Phrases
⬜ Appropriate User Settings
```

The goal is full learner continuity across devices without removing Room's role as the local working source of truth.

---

# Mission Platform 🚧

Implemented:

```text
✅ Mission entities
✅ Mission repository
✅ Mission persistence
✅ Active mission tracking
✅ Objective tracking
✅ Objective progress
✅ Mission history
✅ Mission top-bar integration
✅ Mission details drawer
✅ Swipeable mission pager
✅ Mission completion events
✅ Mission progress dashboard
✅ Dynamic mission recommendations
✅ Mission branching
✅ Mission variation flows
✅ Mission cloud snapshots
✅ Mission objective synchronization
✅ Objective-progress synchronization
```

Continuing work:

```text
🚧 Cross-device mission continuation verification
🚧 Objective evaluator refinement
⬜ Mission review experience
⬜ Mission rewards
⬜ Mission difficulty progression
```

---

# Vocabulary Roadmap

```text
Saved Vocabulary
Challenge Words
Dictionary
Mastery Tracking
Cross-Device Vocabulary State
```

---

# Adaptive Reinforcement Roadmap

```text
Reuse Difficult Words
Confidence Tracking
Mastery Detection
Weak Vocabulary Practice
Capability Progression
Adaptive Mission Selection
```

---

# Audio Roadmap

```text
Speech Playback
Pronunciation Practice
Shadowing
Listening Missions
```

---

# Cloud Sync Roadmap

Implemented:

```text
✅ Conversation Backup
✅ Message Backup
✅ Conversation Restore Infrastructure
✅ Message Restore Infrastructure
✅ Archive State Sync
✅ Deletion Tombstones
✅ Incremental Sync
✅ Mission Backup
✅ Mission Template Snapshot
✅ Mission Objective Snapshot
✅ Mission Progress Snapshot
```

Next:

```text
🚧 Cross-Device Mission Continuation
⬜ Learner Profile Backup
⬜ Assessment Backup
⬜ CEFR Progress Backup
⬜ Streak Backup
⬜ Adaptive Learning Backup
⬜ Besser Mastery Backup
⬜ Marked Phrase Backup
⬜ Full Learner-State Multi-Device Continuity
```

---

# Authentication Roadmap

```text
✅ Google Sign-In
✅ Amazon Cognito
✅ Protected AWS APIs

⬜ Email Login
⬜ Account Management
⬜ Subscription Support
⬜ User Account Deletion Flow
```

---

# Animation Roadmap

Chat presentation appearance and motion constants are centralized through design tokens.

Chat-related appearance and motion constants live in:

```text
feature/chat/presentation/theme/ChatDesignTokens.kt
```

Non-visual chat presentation constants live in:

```text
feature/chat/presentation/theme/ChatPresentationTokens.kt
```

Implemented:

```text
✅ Message Arrival Animation
✅ Message Bubble Press Animation
✅ Typing Indicator
✅ Navigation Animations
✅ Mission Top Bar Animations
✅ Mission Drawer Motion
✅ Swipeable Mission Pager
✅ Progress Dashboard Animations
✅ Animated Skill Progress Bars
✅ Mission Completion Animations
```

Planned:

```text
⬜ Vocabulary Saved Animation
⬜ Streak Celebration
⬜ Mission Reward Animation
⬜ Achievement Unlock Animation
```

---

# Data Ownership Strategy

Room remains the primary operational source of truth.

```text
Local Device
     ↓
Room
     ↓
User Experience
```

Cloud infrastructure provides continuity:

```text
Room
  ↕
AWS Sync
  ↕
Other Devices
```

Cloud infrastructure currently supports:

```text
Backup
Restore
Cross-Device Synchronization
Authentication
Protected AI Access
```

Normal local application behavior should not require DynamoDB to remain continuously reachable.

---

# Security Requirements

DeutschCraft follows these security principles:

- Never store AI provider keys in Android
- Never commit secrets
- Use HTTPS
- Authenticate protected API requests
- Derive cloud ownership from Cognito identity
- Never trust arbitrary client-supplied user IDs for cloud ownership
- Validate AI responses
- Validate synchronization payloads
- Use bounded upload batch sizes
- Maintain deletion tombstones
- Avoid logging authentication tokens
- Avoid logging credentials
- Avoid logging sensitive chat content in authentication diagnostics
- Keep sensitive backend configuration outside Android
- Support account deletion
- Support user-data deletion
- Keep release diagnostics appropriately restricted

---

# Reliability Principles

## Local Writes First

```text
Write to Room
     ↓
Commit
     ↓
Update UI
     ↓
Attempt Sync
```

## Network Failure Does Not Destroy Local Work

```text
Network Offline
      ↓
Pending Local Change
      ↓
WorkManager Waits / Retries
      ↓
Sync When Connectivity Returns
```

## Cloud Changes Are Incremental

```text
Last Cursor
     ↓
GET /v1/sync?since=<cursor>
     ↓
Only Newer Changes
```

## Large Uploads Are Batched

```text
Pending Changes
      ↓
Groups of ≤ 100
      ↓
Independent Acknowledgement
```

## Deletes Do Not Resurrect

```text
Delete
  ↓
Tombstone
  ↓
Sync
  ↓
Other Device Learns Deletion
```

## Mission Snapshots Are Self-Contained

```text
ConversationMission
        +
MissionTemplate
        +
Objectives
        +
ObjectiveProgress
        ↓
Cloud Snapshot
```

This allows generated mission branches to be reconstructed on another device.

---

# Success Criteria

DeutschCraft succeeds when:

1. Learners enjoy chatting.
2. Learning feels natural rather than forced.
3. Emil remains consistent across conversations.
4. Missions feel like meaningful communication rather than quizzes.
5. Progress is visible and evidence-based.
6. The application remains useful offline.
7. Local work survives temporary backend failures.
8. Cloud synchronization provides continuity without replacing Room.
9. Conversations can continue across devices.
10. Mission progress can continue across devices.
11. German proficiency improves through real communication.
12. Learners return because they enjoy talking rather than because they feel forced to study.

---

# Immediate Next Milestone

## Phase 3.2: Cross-Device Mission Continuity

Current mission synchronization infrastructure:

```text
✅ Conversation mission upload
✅ Mission template snapshot
✅ Mission objective snapshot
✅ Objective progress snapshot
✅ Mission status synchronization
✅ Generated branch preservation
✅ Canonical mission ID normalization
✅ Incremental cloud download

🚧 Fresh-device active mission restoration
🚧 Continue a partially completed mission on Device B
🚧 Sync Device B mission progress back to Device A
🚧 Verify COMPLETED state across devices
🚧 Verify DISMISSED state across devices
🚧 Synchronize mission recommendation/completion identity
```

Acceptance target:

```text
Device A

Start Mission
     ↓
Complete Objective 1
     ↓
Complete Objective 2
     ↓
AWS Sync
     ↓

Device B

Sign In
     ↓
Cloud Restore
     ↓
Open Same Conversation
     ↓
Same Mission
Same Generated Branch
Same Objectives
Same Completed Goals
Same Remaining Goals
     ↓
Continue Mission
     ↓
AWS Sync
     ↓

Device A

Next Sync
     ↓
Receives Device B Progress
```

After reliable cross-device mission continuation is verified, the next synchronization phase expands into broader learner state:

```text
Learner Profile
CEFR Progress
Assessment Evidence
Activity and Streaks
Adaptive Learning
Besser Mastery
Marked Phrases
```

> **The current priority is completing reliable cross-device mission continuity while preserving DeutschCraft's offline-first, conversation-first architecture.**