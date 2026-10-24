# DeutschCraft

**DeutschCraft** is a Telegram-inspired Android chat application for learning German through natural conversation.

Instead of presenting traditional lessons, quizzes, or classroom-style exercises, DeutschCraft allows users to chat naturally with an AI companion while assessment, vocabulary tracking, and CEFR evaluation happen quietly in the background.

The goal is simple:

> **Conversation first. Learning second.**

DeutschCraft should feel like a modern messaging app, not a language-learning platform.

---

# Vision

Most language-learning apps feel like:

```text
Learning
    ↓
Conversation
```

DeutschCraft should feel like:

```text
Conversation
    ↓
Learning happens naturally
```

The user opens DeutschCraft because they want to chat.

The improvement in German proficiency is a side effect of engaging conversations.

---

# Core Principles

## 1. Offline First

DeutschCraft is designed as an **offline-first application**.

The local device is the primary source of truth.

```text
Room Database
      ↓
Messages
Progress
Assessments
Vocabulary
Mistakes
```

The application should continue functioning even if the device loses network connectivity.

Users should be able to:

- View conversations
- View progress
- View statistics
- View assessments
- View weaknesses
- View vocabulary history

while offline.

---

## 2. Conversation First

DeutschCraft should feel closer to:

```text
Telegram
WhatsApp
Messenger
```

than:

```text
Duolingo
Language Classroom
Tutorial Platform
```

No lesson selection.

No mandatory exercises.

No disruptive quizzes.

Conversation remains the primary experience.

---

## 3. Human-Like Interaction

The AI should feel like a real German conversation partner.

Examples:

```text
German Friend
German Coworker
German Study Partner
German Travel Guide
```

The user should feel like they are chatting with someone rather than interacting with a chatbot.

---

## 4. Gradual Progression

Users remain inside their current CEFR level until they are ready to advance.

Example:

```text
Current Level:
A1
```

Conversation difficulty remains:

```text
A1
```

When enough evidence is gathered:

```text
🎉 You seem comfortable with A1 German.

Would you like to unlock A2 conversations?

[Not Yet]
[Start A2]
```

The user controls progression.

---

# Product Features

## AI Conversations

### User sends message

```text
User:
Heute war ich arbeiten.
```

### AI responds

```text
AI:
Oh schön! Was machst du beruflich?
```

### Suggestions appear

```text
[Ich arbeite als Entwickler]
[Ich studiere derzeit]
[Ich suche Arbeit]
```

Suggestions continuously change depending on context.

---

## Invisible Assessment Engine

Every conversation generates assessment data.

The user does not need to explicitly start an assessment.

Tracked metrics include:

```text
Grammar
Vocabulary
Comprehension
Fluency
Sentence Complexity
Confidence
```

Assessment runs automatically.

---

## Weakness Tracking

DeutschCraft records recurring struggles.

Examples:

```text
Articles
Word Order
Cases
Verb Conjugation
Prepositions
```

Over time:

```text
Strong Areas
✓ Greetings
✓ Travel
✓ Daily Conversation

Weak Areas
✗ Dative Case
✗ Article Usage
✗ Word Order
```

Conversations adapt to encourage practice in weaker areas.

---

## Progress Dashboard

Accessible through a dedicated progress screen.

Example:

```text
Current Level

A2

78% Ready For B1
```

Displays:

```text
Grammar Score
Vocabulary Score
Conversation Score
Learning Streak
Known Weaknesses
Strong Areas
Progress History
```

---

# Subscription Strategy

## Free Users

Everything works locally.

Features:

```text
AI Chat
Assessment
Progress Tracking
Conversation History
Vocabulary Tracking
Mistake Tracking
```

Data stays on-device.

---

## Premium Users

Adds cloud features.

Features:

```text
Cloud Backup
Cloud Restore
Cross-Device Sync
Conversation Recovery
History Synchronization
```

Premium storage is powered by PostgreSQL.

---

# Authentication Strategy

Authentication is intentionally postponed.

## Reason

Authentication solves:

```text
Who are you?
```

Current development focuses on:

```text
Does the product work?
```

Authentication becomes necessary only when:

```text
Cloud Sync
Cloud Backup
Multi-Device Access
```

are implemented.

---

# Technology Stack

## Android

```text
Kotlin
Jetpack Compose
Material 3
ViewModel
StateFlow
Coroutines
Navigation Compose
Room
DataStore
Hilt
```

## Backend

```text
Spring Boot
Spring Security
Spring Data JPA
PostgreSQL
Docker
```

## AI

```text
Qwen
```

Accessed through:

```text
Android
    ↓
Spring Boot
    ↓
Qwen
```

AI API keys are never stored in the Android application.

---

# High-Level Architecture

## Current Direction

```text
Android
│
├── Room
├── DataStore
├── Assessments
├── Progress
└── Conversations
      │
      │ Sync
      ▼
Spring Boot
      │
      ▼
PostgreSQL
```

## AI Flow

```text
Android
    ↓
Spring Boot
    ↓
Qwen
```

Android never communicates directly with the AI provider.

---

# Data Ownership Strategy

## Local Source of Truth

```text
Room Database
```

Always.

Not PostgreSQL.

Not Spring Boot.

The phone owns the data.

## PostgreSQL Purpose

PostgreSQL exists only for:

```text
Cloud Backup
Synchronization
History Recovery
Multi-Device Support
Premium Features
```

Not for day-to-day functionality.

---

# Android Architecture

```text
Compose UI
    ↓
ViewModel
    ↓
Repository
    ↓
Data Source
```

Data Sources may include:

```text
Room
Retrofit
DataStore
Qwen Responses
```

---

# Planned Project Structure

```text
com.julesmeister.deutschcraft
│
├── app
│
├── core
│   ├── database
│   ├── datastore
│   ├── network
│   ├── model
│   └── di
│
├── feature
│   ├── chat
│   ├── assessment
│   ├── progress
│   ├── settings
│   └── profile
│
└── MainActivity.kt
```

---

# Current Development Roadmap

## Phase 1 — Foundation ✅

```text
Compose
Material 3
Project Setup
Domain Models
ViewModel Architecture
Hilt
```

Completed.

---

## Phase 2 — Offline-First Core (Current Priority)

### Room

Store:

```text
Conversations
Messages
Vocabulary
Assessments
Mistakes
Progress
```

### DataStore

Store:

```text
Current CEFR Level
Theme
Settings
Subscription Status
Last Sync Date
```

Goal:

```text
Airplane Mode = usable app
```

---

## Phase 3 — Chat Experience

### Telegram-Like Features

```text
Message Bubbles
Typing Indicator
Reply Suggestions
Auto Scroll
Animations
Message Effects
```

### AI Features

```text
AI Responses
German Conversations
Conversation Continuity
Reply Suggestions
```

---

## Phase 4 — Assessment Engine

Track:

```text
Grammar
Vocabulary
Sentence Construction
Conversation Ability
Confidence
```

Learned weaknesses become:

```text
Personalized Practice Opportunities
```

---

## Phase 5 — Progress Dashboard

Display:

```text
Current Level
Level Readiness
Grammar Score
Vocabulary Score
Conversation Score
Streaks
Weaknesses
Strengths
```

---

## Phase 6 — Sync System

Only after everything works locally.

### Syncable Objects

```text
Conversation
Message
Assessment
Vocabulary
Mistakes
Progress
```

Sync Flow:

```text
Room
   ↓
Spring Boot
   ↓
PostgreSQL
```

Room remains the source of truth.

---

## Phase 7 — Subscription

Premium unlocks:

```text
Sync
Backup
Restore
Cross-Device Access
```

---

## Phase 8 — Authentication

Final major feature.

Possible providers:

```text
Google Login
Email Login
```

Introduced only after cloud functionality exists.

---

# Entity Strategy

Every important entity receives a locally generated ID.

Example:

```kotlin
val id: String = UUID.randomUUID().toString()
```

Applied to:

```text
Conversation
Message
Assessment
Vocabulary
Mistake
Progress Record
```

This simplifies future synchronization.

---

# Security Requirements

```text
Never store AI keys in Android
Never commit secrets
Use HTTPS
Validate server requests
Encrypt sensitive data
Support account deletion
Support conversation deletion
```

---

# Success Criteria

DeutschCraft succeeds when:

1. Users enjoy chatting.
2. Learning feels natural.
3. Progress is visible.
4. The app works offline.
5. Cloud services are optional rather than required.
6. German proficiency improves through conversation.

---

# Immediate Next Milestone

```text
✅ Hilt
⬜ Room Database
⬜ Conversation Entity
⬜ Message Entity
⬜ DAO Layer
⬜ Local Repository
⬜ Chat UI
⬜ AI Integration
```

**Next task: implement Room and establish local persistence before continuing UI development.**