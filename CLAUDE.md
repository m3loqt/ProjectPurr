# Project Purr — Claude Code Guide

## What This Project Is

**Project Purr** is a therapeutic Android app that simulates the physical sensation and sound of a cat purring on the user's chest. The core differentiator is **synchronized haptic vibration + audio** — not just a cat sound app.

Current phase: **Phase 1 (hypothesis validation)**. Single question: *Does the phone feel convincingly like a cat purring on the chest?*

Always reference `/PROJECT_CONTEXT.md` for full product intent and constraints.

---

## Architecture at a Glance

```
MainActivity (window, brightness, EdgeToEdge)
    └── PurrViewModel (StateFlow, delegates to engine)
            └── PurrSessionEngine (session clock, timer, fade, thermal mgmt)
                    ├── PurrAudioPlayer (MediaPlayer wrapper, R.raw.catpur1)
                    └── PurrHapticPlayer (Vibrator, perceptual gamma scaling)
                            └── Catpur1AudioHapticEnvelope (generated haptic data)

PurrScreen (Compose UI) ← observes PurrUiState from ViewModel
HouseCatProfile (tuning constants for the House Cat purr profile)
```

**Key design rule:** UI (Compose) and engine (timing/haptics/audio) must stay cleanly separated. Never put timing or motor logic in composables.

---

## Phase 1 In-Scope Features

1. One bundled purr profile: **House Cat**
2. Large Play/Pause button (primary action)
3. Synchronized audio + haptic from shared session clock
4. **Silent Purr** mode — vibration on, audio off
5. **Chest Mode** — screen dimmed (brightness 0.08f), system bars hidden
6. **Sleep timer** — Off / 10 / 20 / 30 minutes
7. **Gentle fade-out** at timer end (4 seconds)
8. Thermal management — amplitude tapers after 5 min to protect motor

## Phase 1 Anti-Goals — Do Not Build

- No accounts, login, subscriptions, ads, or paywalls
- No gamification or streaks
- No analytics pipeline
- No cloud sync or backend
- No social features
- No long onboarding
- No multi-profile system (yet)
- No settings persistence (yet)
- No unit/UI test framework (yet)

---

## Key Technical Details

### Audio
- Asset: `app/src/main/res/raw/catpur1.mp3` (14.76 s loop, 24 kHz, 160 kbps)
- `house_cat_purr.wav` exists but is not currently used in code
- Uses Android MediaPlayer; looping enabled; volume target 0.78f
- Audio attributes: USAGE_MEDIA + CONTENT_TYPE_MUSIC

### Haptics
- Loop period: **14760 ms** (24 ms × 615 carrier cycles = 42 Hz rolled-R motif)
- Envelope generated from MP3 via `tools/generate_catpur_haptic_envelope.py`
- Perceptual gamma scaling: **γ = 0.62** (prevents harsh buzz at low amplitudes)
- Amplitude hard cap: 200 (out of 255) for thermal safety
- Devices without amplitude control: graceful degradation (binary on/off)
- Thermal taper: after 5 min, ramps from 1.0x → 0.88x over 18 min

### Haptic Tuning Workflow
To change the purr feel: edit `catpur1.mp3`, then run:
```bash
python3 tools/generate_catpur_haptic_envelope.py
```
This regenerates `Catpur1AudioHapticEnvelope.kt` automatically.

### Session State Machine
```
STOPPED → PLAYING → FADING → STOPPED
```
- PLAYING→STOPPED: 360ms quick-release (10 steps × 36ms)
- PLAYING→FADING: triggered by sleep timer; 4000ms fade

---

## Stack & Build

| Item | Value |
|------|-------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Min SDK | 26 (Android 8.0) |
| Target/Compile SDK | 35 |
| Version | 0.1.0-phase1 |
| No network libraries | — offline-first |
| No DI framework | — simple manual construction |

---

## Rules for Working in This Codebase

- **Physical device testing required** — emulators do not reproduce haptics
- Preserve the **UI / engine boundary** — no timing logic in composables
- **All haptic tuning** goes in `HouseCatProfile.kt` or the envelope generator, not scattered
- Prefer **small, focused changes** that improve the believability of chest purr
- No new features outside Phase 1 scope without explicit user approval
- No error handling theater — Phase 1 is for validation, not robustness (don't add try-catch for things that can't fail in normal use)
- Sessions must feel sacred: no interruptions, no popups mid-session

---

## Future Backlog (Not Phase 1)

- Proximity sensor auto-start / face-down detection
- Multiple breed/personality profiles
- iOS version (SwiftUI + Core Haptics)
- Bio-feedback sync
- Premium unlocks / expanded library
- Virtual sanctuary scenes
- Settings persistence

---

*This file is for Claude Code. For full product context see PROJECT_CONTEXT.md.*
