# Project Purr — Product & Engineering Context

This document captures the vision, constraints, and development direction for **Project Purr** so future work in Cursor stays aligned with the product intent.

---

## What It Is

**Project Purr** is a therapeutic mobile app that simulates the feeling of a cat purring on the user’s chest using **synchronized haptic vibration** and **audio playback**.

### Core Experience

1. The user opens the app.
2. They press play.
3. They place the phone on their chest.
4. The phone **feels and sounds** like a calm cat resting on them and purring.

### What It Is Not

- **Not** a novelty soundboard.
- **Not** “just” a cat sound app.
- **Not** a toy-first experience.

It is a **sensory comfort tool**: sound plus physical sensation, designed for emotional regulation and comfort.

---

## Emotional Goal

The app should feel:

- **Calming** — quiet, unhurried, no pressure.
- **Intimate** — like a small ritual, not a product demo.
- **Warm** — premium and human, not clinical or gimmicky.
- **Safe at 2am** — usable in the dark, alone, when the user is vulnerable.

The interface should **disappear during the session** and leave **primarily the physical sensation** (and optional audio). The companion feeling matters more than screens or features.

---

## Primary Users

- Cat owners **away from** their pets.
- People who **cannot own cats** (allergies, rental rules, travel, lifestyle).
- People dealing with **stress, anxiety, loneliness, or sleep** difficulty.
- Cat lovers who want a **comforting sensory ritual**.

### Secondary / Future Audiences (Out of Scope for Early Phases)

Therapists, parents, elder-care settings — note for later; do not design Phase 1 around them.

---

## Core Differentiation

Existing cat purr videos and apps can offer **sound only**. They cannot reliably deliver the **physical sensation** of purring against the body.

**Project Purr’s advantage:** tightly **synchronized haptic vibration + audio**, tuned to feel chest-plausible and organic, not like a generic phone buzz.

---

## Phase 1 — Single Question

**Build only enough to answer:**

> Does the phone feel convincingly like a cat purring on the chest?

### Phase 1 Anti-Goals (Do Not Build Yet)

- Accounts, login, or profiles tied to identity.
- Subscriptions, ads, or paywalls.
- Gamification, streaks, or achievements.
- Analytics pipelines (beyond what the platform may require minimally; prefer none).
- Cloud sync, backends, or remote config.
- Social features.
- Long or branching onboarding.

**Principle:** validate the **sensory thesis** before expanding the product surface.

---

## Technical Direction (Android-First)

| Area | Direction |
|------|-----------|
| Language | **Kotlin** |
| UI | **Jetpack Compose**, **Material 3** |
| Data / network | **Offline-first**, **no backend** |
| Auth | **None** — no login |
| Media | **Local bundled assets** (purr audio, etc.) |
| Audio | **MediaPlayer** or **ExoPlayer** (choose one path; keep abstraction clear) |
| Haptics | **VibratorManager** / **VibrationEffect** (Android APIs appropriate to min SDK) |

### Testing Reality

- **Physical device testing is required** — emulators do not reproduce haptics faithfully.
- Plan for **device variance** (motor strength, amplitude support, OEM behavior).

---

## Phase 1 Features (In Scope)

1. **One default purr profile:** *House Cat* (bundled).
2. **Large Play / Pause** control — primary action, easy in low light.
3. **Synchronized** audio + haptic playback from a shared “session clock” or engine.
4. **Silent Purr mode:** vibration **on**, audio **off** (e.g. partner sleeping, public spaces).
5. **Chest Mode:** while session is active, screen is **dimmed or black** (reduce distraction and light).
6. **Basic sleep timer:** Off, **10 / 20 / 30** minutes.
7. **Gentle fade-out** at timer end (audio volume down + haptic ramp-down; no abrupt cut).
8. **Architecture:** clear separation between **UI (Compose)** and **purr engine** (timing, haptics, audio, modes, timer) so tuning does not entangle with screens.

---

## Haptic Design Principles

- Must **not** feel like a flat, harsh phone buzz.
- Target **organic**, **soft**, **rhythmic**, **chest-friendly** patterns.
- Use **ramp-up** and **ramp-down**; avoid sustained **maximum** intensity.
- Evoke **breathing-like** and **purr-like** rhythm (quasi-periodic, not metronome-rigid).
- **Fallbacks** for devices **without** fine amplitude control (graceful degradation, still pleasant).

Implementation detail belongs in code, but **all haptic curves should be tunable** in one place (engine / config), not scattered in composables.

---

## Visual & UX Design Principles

- **Warm, calm, premium** — not childish or “app store gimmick.”
- **Minimal UI** — few controls; what exists is large and legible.
- **No cartoon mascot** in Phase 1.
- **No clutter** — no secondary upsells on the main path.
- **No jarring sounds or interruptions** during a session.
- **Sessions feel sacred:** no popups, ads, upgrade prompts, or rating nags **while** purring is active (ideally never interrupt mid-session).

---

## Future Ideas (TODO Only — Not Phase 1)

Track as backlog / future specs; **do not implement** until Phase 1 is validated.

- Proximity sensor **auto-start**
- **Face-down** detection for chest placement
- Multiple **breed / personality** profiles
- **Pet-to-purr** touch interaction (e.g. photo or symbolic link — unspecified)
- **Bio-feedback** sync (heart rate / breathing — unspecified)
- **Virtual sanctuary** (environment / scene — unspecified)
- **Premium** unlocks / expanded library
- **iOS** version: **SwiftUI** + **Core Haptics** (separate codebase / module strategy TBD)

---

## How to Use This Doc in Cursor

- Treat Phase 1 as **hypothesis validation**, not feature accumulation.
- Prefer **small, testable** changes that improve **believability** of chest purr.
- When adding code, preserve **UI vs engine** boundaries and keep **House Cat** the default until profiles are explicitly in scope.

---

*Last updated: product context established at project kickoff.*
