# DeutschCraft

**DeutschCraft** is an offline-first, Telegram-inspired Android application for learning German through natural, AI-powered conversation.

Instead of presenting traditional lessons, quizzes, or classroom-style exercises, DeutschCraft allows learners to chat naturally with an AI conversation companion while assessment, corrections, vocabulary analysis, CEFR evaluation, mission tracking, and progress measurement happen quietly in the background.

> **Conversation first. Learning happens naturally.**

---

# Vision

Most language-learning applications follow this structure:

```text
Learning activity
      ↓
Practice exercise
      ↓
Conversation
```

DeutschCraft reverses that relationship:

```text
Natural conversation
        ↓
Real communication practice
        ↓
Learning happens quietly
```

The learner opens DeutschCraft because they want to talk, express ideas, and practice communicating in German.

Improvement in German proficiency should become a natural result of having useful, engaging, and increasingly challenging conversations.

---

# Core Principles

## Offline First

DeutschCraft is designed as an offline-first application.

The local device is the primary source of truth.

```text
Room Database
      ↓
Conversations
Messages
Besser Corrections
Assessments
Progress
Missions
Vocabulary
Activity Days
```

The application should continue functioning even without internet access.

Supported offline functionality:

- View conversations
- View assessments
- View progress
- View vocabulary history
- View mission progress
- View archived threads
- Restore previous sessions

---

## Room as Source of Truth

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

Room owns all persistent learner data.

Examples:

- Conversations
- Messages
- Corrections
- Assessments
- Missions
- Mission Objectives
- Progress Records

---

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

Conversation remains the primary experience.

---

## Human-Like Companion

DeutschCraft uses a consistent German conversation companion named **Emil**.

Example profile:

```text
Name:
Emil

Lives:
Leipzig

Works:
Independent bookstore

Interests:
Books
Travel
Football
Cooking
German culture
```

Emil adapts conversation naturally while staying within the learner's CEFR level.

---

## CEFR Progression

Supported levels:

```text
A1
A2
B1
B2
C1
C2
```

DeutschCraft gathers evidence from conversation rather than exams.

Tracked skills:

- Grammar
- Vocabulary
- Accuracy
- Fluency
- Complexity
- Comprehension

Level progression is recommended rather than forced.

---

# Current Features

## Chat Experience

Implemented:

- Telegram-style conversations
- Multiple conversation threads
- Automatic thread title generation
- Archived threads
- Undo archive
- Reply suggestions
- Message actions
- Auto-scroll
- Keyboard-aware layout
- Message arrival animations
- Message bubble press animations
- Typing indicator animations

---

## Besser Corrections

Example:

```text
User:
Ich go heute zum Markt.

Besser:
Ich gehe heute zum Markt.
```

Original messages are preserved.

Corrections are stored separately and displayed as helper content.

---

## Message Actions

Tap any message to:

- Translate
- Explain
- Rephrase

These are available through the message action bottom sheet.

---

## Progress Tracking

Implemented:

- CEFR assessment
- Skill scores
- Progress percentages
- Activity tracking
- Streak tracking
- Progress dashboard

Tracked dimensions:

```text
Grammar
Vocabulary
Accuracy
Complexity
Coherence
Confidence
```

---

# Mission System

## Goal

Teach through realistic communication scenarios.

Example:

```text
🎯 Restaurant Ordering
```

Objectives:

```text
✓ Greet the waiter
✓ Ask for the menu
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
Objectives Detected
      ↓
Mission Completed
      ↓
Performance Review
```

Mission progress is persisted locally.

---

## Current Mission Infrastructure

Implemented:

- Mission entities
- Mission DAO
- Mission repository
- Mission progress tracking
- Objective completion tracking
- Active mission observation
- Mission top-bar integration
- Objective completion event architecture

Current focus:

```text
Restaurant Ordering Mission MVP
```

---

# Progress Dashboard

Displays:

```text
Current CEFR Level
Readiness Percentage
Grammar Score
Vocabulary Score
Conversation Score
Activity Days
Streak
```

Example:

```text
Current Level:
A2

Readiness For B1:
78%
```

---

# AI Architecture

Current AI provider:

```text
Qwen
```

Response Flow:

```text
Android
    ↓
AI Gateway
    ↓
Qwen
```

Future Production Flow:

```text
Android
    ↓
Spring Boot
    ↓
Qwen
```

API keys are never stored in the Android application.

---

# Technology Stack

## Android

```text
Kotlin
Jetpack Compose
Material 3
Room
Hilt
ViewModel
StateFlow
Coroutines
Navigation Compose
WorkManager
DataStore
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
│   ├── progress
│   ├── assessment
│   ├── mission
│   ├── engagement
│   ├── messageactions
│   └── settings
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

- CEFR Assessment
- Progress Tracking
- Activity Tracking
- Streak Tracking
- Persistent Conversations

---

## Conversation Experience 🚧

Implemented:

- Threads
- Archive
- Reply Suggestions
- Message Actions
- Typing Indicator
- Animations

Remaining:

- Polish
- UX refinements

---

## Restaurant Ordering Mission MVP 🚧

Current Priority

```text
✅ Mission entities
✅ Mission repositories
✅ Active mission tracking
✅ Mission events

⬜ Mission offer card
⬜ Objective evaluation
⬜ Objective completion animation
⬜ Mission review screen
⬜ Mission completion rewards
```

---

## Future

### Vocabulary System

```text
Saved Vocabulary
Challenge Words
Dictionary
Mastery Tracking
```

### Adaptive Reinforcement

```text
Reuse difficult words
Track confidence
Detect mastery
```

### Cloud Sync

```text
Backup
Restore
Device Sync
```

### Authentication

```text
Google Sign-In
Email Login
Subscriptions
```

---

# Animation Roadmap

Implemented:

```text
✅ Message Arrival Animation
✅ Message Bubble Press Animation
✅ Improved Typing Indicator
✅ Navigation Animations
✅ Top Bar Transitions
✅ Progress Animations
```

Planned:

```text
⬜ Vocabulary Saved Animation
⬜ Streak Celebration
⬜ Thread Archive Animation
```

---

# Data Ownership Strategy

Room remains the source of truth.

```text
Phone Owns Data
```

PostgreSQL exists only for:

```text
Cloud Backup
Sync
Restore
Premium Features
```

Never for day-to-day application functionality.

---

# Security Requirements

- Never store AI keys in Android
- Never commit secrets
- Use HTTPS
- Validate AI responses
- Validate API requests
- Support data deletion
- Support account deletion
- Encrypt sensitive information

---

# Success Criteria

DeutschCraft succeeds when:

1. Users enjoy chatting.
2. Learning feels natural.
3. Progress is visible.
4. Missions feel helpful rather than instructional.
5. The app works offline.
6. Cloud services remain optional.
7. German proficiency improves through conversation.

---

# Immediate Next Milestone

## Restaurant Ordering Mission MVP

```text
✅ Mission persistence
✅ Active mission observation
✅ Mission top-bar system

⬜ Restaurant mission template
⬜ Objective evaluator
⬜ Offer card
⬜ Mission completion review
⬜ Objective completion animation
```

> The current focus is completing the first end-to-end mission experience while preserving the conversation-first philosophy.