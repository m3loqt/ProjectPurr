# Project Purr Progress

Last updated: 2026-05-08

## Current Phase

Phase 1C (productization validation): prove that Project Purr is stable, comfortable, and trustworthy enough for repeated real-world use at night.

Phase 1A (technical feasibility) and Phase 1B (believability tuning) are complete:
- Synchronized audio + haptics engine working
- Waveform envelope generated from real cat audio
- Chest-mode immersion working
- Purr no longer feels like a generic phone buzz

## Latest Update — Phase 1C Stabilization Pass

### Critical bug fixes
- **Haptics no longer fail to resume after audio focus loss.** `handleFocusLost()` was calling `haptics.stop()` (which cleared `loopTimings`), so `restartLoop()` on focus gain silently did nothing. Fixed: `handleFocusLost()` now calls `haptics.pause()` (vibrator cancel, no state loss).
- **Fade-out to true silence.** `restartWaveform()` was coercing `perceptualMotorAmp() = 0` to amplitude 1, causing faint residual vibration even at zero intensity. Fixed: removed incorrect outer `coerceIn(1, MOTOR_ABS_CAP)` — `perceptualMotorAmp` already handles its own floor.
- **Silent loop sync negative delay guard.** `startSilentPurrLoopSync()` `delay()` could receive negative values on scheduling slippage. Fixed with `coerceAtLeast(0L)`.
- **Stale audio completion listener on release.** `PurrAudioPlayer.release()` now clears the completion listener before releasing, preventing `onAudioLoopComplete` from firing after cleanup.

### Session UX improvements
- **Startup fade-in ramp** (600 ms): sessions now open at silence and gently ramp to full intensity. Haptics and audio both ramp together. `startupRampJob` is properly cancelled on user stop, focus loss, or silent mode toggle.
- **Animated chest mode dim** (800 ms): the `0.42f` alpha dim now animates with `tween(800)` instead of snapping instantly.
- **Sleep timer persistence**: last-used timer is now saved to DataStore on change and restored on next launch.

### Diagnostic logging
- `PurrEngine` and `PurrAudio` log tags active in debug builds
- Key events logged: session start/stop, loop boundaries, focus loss/gain, thermal taper, timer fire, fade phases

### Device variance documentation
- `PurrHapticPlayer` annotated with `[OEM_TUNING]` markers at the three most sensitive constants (`CURVE_GAMMA`, `MOTOR_ABS_CAP`, binary fallback path)
- `hasAmplitudeControl` is now `val` (accessible for logging/diagnostics)

### QA infrastructure
- `PHASE_1C_QA.md` created — full test matrix covering session lifecycle, sleep timer, silent mode, chest mode, audio focus, background/foreground, thermal, device variance, and subjective comfort

## Progress Snapshot

### Done
- Core architecture in place (UI + engine separation).
- House Cat profile implemented with generated waveform envelope from `catpur1.mp3`.
- Synchronized audio + haptics playback engine.
- Silent purr mode.
- Chest mode (brightness + immersive bars during active session), with animated dim transition.
- Sleep timer options (Off / 10 / 20 / 30 min), fade-out logic, and persistence of last-used timer.
- Navigation flow: onboarding → home → session.
- Warm visual language foundation (theme, typography, glass card component).
- Android build compiles and debug install path works when device permissions are granted.
- Phase 1C stabilization: focus handling, fade accuracy, startup ramp, logging, OEM annotations.

### In Progress / Active Tuning
- Physical-device calibration across OEM hardware (motor strength, amplitude behavior).
- Chest-mode brightness override behavior on OEM devices (Xiaomi, Samsung edge cases).
- 15–20 minute session thermal/comfort testing.

### Not Started (Phase-1 adjacent or deferred)
- Automated test coverage (unit/UI instrumentation) — minimal, intentionally deferred.
- Multi-profile companions (Ragdoll/Maine Coon are placeholders only).
- Rich onboarding imagery/motion polish from design targets is partial.
- Any backend/account/subscription/social features remain intentionally out of scope.

## Technical Health
- Build baseline: AGP + Kotlin + Compose setup is stable.
- All Phase 1C engine changes compile cleanly.
- No known code-level blockers.
- Main risk is device-specific hardware variance and OEM behavior differences.

## Remaining Blockers Before Phase 2 (MVP / App Store)

1. **OEM motor validation** — test on ≥ 3 physical devices; document `CURVE_GAMMA` / `MOTOR_ABS_CAP` adjustments needed per OEM family
2. **Battery drain measurement** — mAh/20-min baseline on target device
3. **Thermal comfort** — 20-min session skin-temp on target device, accept/reject
4. **OEM brightness override** — some OEMs ignore `screenBrightness` in WindowManager
5. **Samsung audio focus** — non-standard AUDIOFOCUS behavior; verify resume logic
6. **App signing + Play Store metadata** — not yet started

## Reference Files
- Engine: `app/src/main/java/com/projectpurr/engine/`
- UI: `app/src/main/java/com/projectpurr/ui/`
- App entry: `app/src/main/java/com/projectpurr/MainActivity.kt`
- Product context: `PROJECT_CONTEXT.md`
- Design direction: `DESIGN.md`
- Haptic envelope generator: `tools/generate_catpur_haptic_envelope.py`
- Phase 1C QA checklist: `PHASE_1C_QA.md`
