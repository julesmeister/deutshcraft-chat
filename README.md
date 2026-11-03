# DeutschCraft

**DeutschCraft** is an offline-first, Telegram-inspired Android application for learning German through natural AI conversations.

Rather than teaching through lessons, quizzes, flashcards, or exercises, DeutschCraft teaches through meaningful communication.

Learners practice German by chatting naturally with a persistent AI companion while the application quietly performs assessment, correction, CEFR evaluation, mission tracking, vocabulary analysis, progress measurement, and personalized learning support in the background.

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

The user opens DeutschCraft because they want to communicate.

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

Learning systems should support the conversation without interrupting it.

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

while maintaining a consistent personality.

---

## Offline First

DeutschCraft follows an offline-first architecture.

The user owns their data.

```text
Room Database
      ↓
Conversations
Messages
Corrections
Assessments
Progress
Missions
Vocabulary
Activity Tracking
Settings
```

The application remains functional even without internet access.

Available offline:

- View conversations
- Read previous chats
- Progress dashboard
- Mission history
- Vocabulary history
- Archived threads
- Activity statistics
- Settings
- Learning history

---

## Room as Source of Truth

All persistent learner data is stored locally.

```text
AI Response
      ↓
Repository
      ↓
Room
      ↓
Flow
      ↓
Compose UI
```

Room owns:

- Conversations
- Messages
- Besser Corrections
- Assessments
- Progress Records
- Missions
- Mission Objectives
- Vocabulary
- Activity Tracking

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

Rather than exams, the application gathers evidence from real conversations.

Tracked skills:

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
- Undo archive
- Reply suggestions
- Message actions
- Keyboard-aware layout
- Auto-scroll behavior
- Typing indicator animations
- Message arrival animations
- Message press animations
- Navigation animations

---

## AI Conversation

Current provider:

```text
Qwen
```

Features:

- Natural German conversation
- Persona-based responses
- CEFR-aware responses
- Conversation continuity
- Topic adaptation
- Mission participation

---

## Besser Corrections

Example:

```text
User:
Ich go heute zum Markt.

Besser:
Ich gehe heute zum Markt.
```

Corrections are stored separately from user messages.

Original messages are always preserved.

---

## Message Actions

Every message supports contextual actions:

```text
Translate
Explain
Rephrase
```

Future actions:

```text
Save Vocabulary
Challenge Me
Add To Practice
```

---

# Mission System

## Purpose

Teach German through realistic communication scenarios.

Example:

```text
🎯 Restaurant Ordering
```

Objectives:

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

New missions suggested inside ordinary chats use the same 8–10-goal range as
thread-list missions, aiming for 10 distinct steps from opening to closing the
conversation. Short generated lists receive the existing single repair attempt
before an offer is accepted. Already-started missions retain their saved goals
and progress.

```text
Conversation
      ↓
Mission Offered
      ↓
Mission Accepted
      ↓
Objectives Detected
      ↓
Mission Completed
      ↓
Performance Review
```

Mission progress is saved locally and survives application restarts.

## Thread-List Mission Recommendations

The mission cell reuses eligible recommendations stored in Room before asking
Qwen to generate more. New scenarios use recent ordinary learner messages as
a local, soft novelty signal; conversation text is not added to the generation
prompt. CEFR suitability, adaptive reinforcement, objectives, hints, vocabulary,
and examples remain intact.

Completed scenario identities are never recommended again, across CEFR changes,
archives, and history age. A small completion ledger retains those identities
after thread deletion, with existing identifiable completions backfilled during
the database upgrade. Active and paused mission scenarios are also excluded;
abandoned attempts do not permanently block a scenario.

Identity comes from the source recommendation, the seeded restaurant template,
or an exact legacy template match. Unlinked, freely generated missions without
a recognizable scenario identity are not guessed from conversation keywords.

The visible cell observes persisted eligibility, and starting a recommendation
rechecks it in the same transaction that creates its conversation and mission.
Skipping never restores an ineligible suggestion. Refills share generation
locks, stop after an exhausted generation pass, and back off failed scenarios
for one minute instead of immediately repeating their AI requests.

---

## Mission Infrastructure

Implemented:

- Mission templates
- Mission entities
- Mission repository
- Mission DAO
- Active mission tracking
- Objective progress tracking
- Objective completion detection
- Mission completion detection
- Mission history
- Mission persistence
- Objective completion events

---

## Mission Top Bar

The mission header expands downward using a subtle chevron without moving its
navigation and progress controls. Mission paging and auto-advance pause while
expanded. An upward chevron below the details, the header chevron, or Android
Back collapses it.

The expansion shows the scenario, completed/current goals, and existing
progressive hints. Vocabulary and grammar are generated on demand for the
mission's CEFR level and cached locally for up to 32 mission contexts. Branch
changes produce fresh support; ordinary goal progress does not regenerate it.
Vocabulary follows the target-vocabulary setting; grammar and German examples
follow the example-phrases setting. When both are disabled no support request is
made. Failures show an explicit retry action rather than placeholder content.

The chat screen contains a dynamic learning-assistant top bar.

Features:

```text
🎯 Active Objectives
💡 Guidance
📈 Progress
✅ Completion Feedback
```

Implemented:

- Animated transitions
- Objective progress display
- Mission completion events
- Assessment status
- Typing status
- Auto-rotating content
- Swipeable horizontal mission pager
- Manual horizontal exploration
- Automatic advancement
- Progress indicators

The learner can swipe horizontally to explore mission information without waiting for automatic rotation.

---

# Progress Dashboard

The Progress screen combines language assessment and mission achievements.

Displays:

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

Mission achievements now appear alongside language-development metrics.

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

Mission completion updates automatically through Room flows.

---

# Settings System

Implemented:

```text
Font Size
Reduce Animations
Mission Hints
Example Phrases
Target Vocabulary
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

Settings are persistent across application launches.

Font size multiplies Android's system font scale throughout the app; chat and
the font preview do not apply it a second time. Reduce animations disables
Compose animation durations app-wide while retaining Android's own animator
duration scale when the preference is off. The mission pager also stops
automatic rotation and scrolling guidance text in reduced-motion mode.

Mission hints, example phrases, and target vocabulary switches control the
corresponding learner-facing mission guidance sections. Turning support off
hides its controls and previously revealed content without deleting saved hint
usage, changing independence analytics, or removing context from AI prompts.
Mission hints also control the chat top-bar guidance text. Progress language
is selectable from Settings as well as from the progress dashboard.

---

# Learning Support

Supported learning assistance:

```text
Mission Hints
Example Phrases
Vocabulary Guidance
Besser Corrections
CEFR Assessment
```

The goal is to provide help without interrupting conversation flow.

---

# Activity Tracking

Tracked metrics:

- Active days
- Conversation activity
- Assessment activity
- Mission participation
- Language growth evidence

Future metrics:

- Weekly streaks
- Monthly consistency
- Conversation duration
- Mission success rate

---

# AI Architecture

Current architecture:

```text
Android
    ↓
AI Gateway
    ↓
Qwen
```

Planned production architecture:

```text
Android
    ↓
Spring Boot
    ↓
Qwen
```

AI credentials are never stored inside the Android application.

## AI Cost Controls

Chat persona and active-mission instructions use compact wording while retaining
identity, CEFR adaptation, conversation behavior, learner-role suggestions, and
mission coaching rules. Conversation history, model, thinking-disabled mode,
response-token limits, and chat/mission repair attempts are unchanged.

Sentence correction and sentence analysis send their rules and JSON schemas in a
stable system prompt, with only sentence data in the user message. Both request
JSON-object output to reduce formatting failures.

Corrections, sentence analyses (translation, explanation, and all three rephrases),
and contextual marked-phrase explanations reuse validated results in bounded,
process-local caches (64 results per gateway). Identical concurrent requests share
one operation. Correction/analysis keys trim only surrounding whitespace, retaining
case and punctuation; phrase keys include the source sentence and phrase type.
Valid "no correction needed" results are reusable; failures and invalid results are
not. Caches are not written to disk and are discarded when the app process ends.

Conversational replies, mission generation, and contextual learning evaluations
are not served from these caches. No features or assessment cadence are disabled.
Actual token/cost savings depend on provider tokenization and repeated requests;
shorter prompts do not by themselves prove unchanged model response quality.

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
```

## Backend (Planned)

```text
Spring Boot
Spring Security
PostgreSQL
Docker
```

## AI

```text
Qwen
```

---

# Project Structure

```text
com.julesmeister.deutschcraft
│
├── app
│   └── navigation
│
├── core
│   ├── database
│   ├── datastore
│   ├── di
│   └── network
│
├── feature
│   ├── chat
│   ├── threads
│   ├── archivedthreads
│   ├── mission
│   ├── progress
│   ├── assessment
│   ├── settings
│   ├── engagement
│   └── messageactions
│
└── MainActivity.kt
```

---

# Development Roadmap

## Foundation ✅

- Compose
- Hilt
- Navigation
- Room
- Repositories
- DataStore

---

## Offline Learning Engine ✅

- CEFR assessment
- Progress tracking
- Activity tracking
- Persistent conversations
- Mission persistence

---

## Conversation Experience ✅

Implemented:

- Threads
- Archive
- Reply suggestions
- Message actions
- Typing indicators
- Top-bar guidance
- Animated mission experiences

Remaining:

- UX polish
- Accessibility refinements

---

## Mission Platform 🚧

Implemented:

```text
✅ Mission entities
✅ Mission repository
✅ Mission persistence
✅ Active mission tracking
✅ Objective tracking
✅ Mission history
✅ Top-bar integration
✅ Swipeable mission pager
✅ Mission completion events
✅ Mission progress dashboard
```

Remaining:

```text
⬜ Mission offer cards
⬜ Objective evaluator refinement
⬜ Mission review screen
⬜ Mission rewards
⬜ Mission difficulty progression
```

---

# Future Features

## Vocabulary System

```text
Saved Vocabulary
Challenge Words
Dictionary
Mastery Tracking
```

## Adaptive Reinforcement

```text
Reuse Difficult Words
Confidence Tracking
Mastery Detection
Weak Vocabulary Practice
```

## Audio

```text
Speech Playback
Pronunciation Practice
Shadowing
Listening Missions
```

## Cloud Sync

```text
Backup
Restore
Multi-Device Sync
```

## Authentication

```text
Google Sign-In
Email Login
Subscriptions
```

---

# Animation Roadmap

Chat presentation appearance and motion constants live in
`feature/chat/presentation/theme/ChatDesignTokens.kt`. Shared tokens remain at
the top level, while component-specific dimensions, colors, typography, and
timings are grouped by component (for example, `TopBar`, `ReplySuggestions`,
and `LevelProgressIndicator`). Non-visual presentation constants (including log
tags, text labels, errors, formatter patterns, and presentation behavior limits)
live in component/owner groups in `ChatPresentationTokens.kt` in the same theme
folder. Data and domain constants remain outside the presentation theme layer.

Implemented:

```text
✅ Message Arrival Animation
✅ Message Bubble Press Animation
✅ Typing Indicator
✅ Navigation Animations
✅ Mission Top Bar Animations
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

Room remains the primary source of truth.

```text
Phone Owns Data
```

Future cloud services exist only for:

```text
Backup
Restore
Cross-Device Sync
Premium Features
```

Never for normal application functionality.

---

# Security Requirements

- Never store AI keys in Android
- Never commit secrets
- Use HTTPS
- Validate AI responses
- Validate API requests
- Support account deletion
- Support data deletion
- Encrypt sensitive information

---

# Success Criteria

DeutschCraft succeeds when:

1. Users enjoy chatting.
2. Learning feels natural.
3. Missions feel meaningful.
4. Progress is visible.
5. The application works offline.
6. Cloud services remain optional.
7. German proficiency improves through real conversation.
8. Learners return because they enjoy talking, not because they feel forced to study.

---

# Immediate Next Milestone

## Restaurant Ordering Mission MVP

```text
✅ Mission persistence
✅ Active mission observation
✅ Mission top-bar system
✅ Mission history
✅ Objective tracking
✅ Mission completion tracking

⬜ Restaurant mission template polish
⬜ Objective evaluator
⬜ Mission offer card
⬜ Mission review screen
⬜ Mission rewards
```

> The current priority is delivering the first complete end-to-end conversation mission experience while preserving DeutschCraft's conversation-first philosophy.