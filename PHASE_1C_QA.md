# Phase 1C QA Checklist

This checklist exists to validate comfort, reliability, and device consistency before Phase 2 (MVP / App Store readiness). Run on at least two physical Android devices.

**Device log tag:** `PurrEngine` and `PurrAudio`  
**Command:** `adb logcat -s PurrEngine:D PurrAudio:D`

---

## 1. Session Lifecycle

### 1.1 Basic play/pause
- [ ] Press Play → gentle startup ramp (haptic + audio fade in over ~600 ms, no abrupt full-intensity hit)
- [ ] Label changes: "Resting with you." while playing
- [ ] Press Pause → 360 ms release ramp, no snap-off
- [ ] Label returns: "Place the phone on your chest, then press play."
- [ ] Repeat play/pause 10× rapidly — no stuck vibration, no double starts

### 1.2 Back navigation during session
- [ ] Press Back while playing → session stops, returns to Home
- [ ] Back during fade-out (FADING state) → immediate stop, returns to Home

### 1.3 Session re-entry
- [ ] Navigate Home → Session → play → back → Session again → play
- [ ] Verify audio/haptic both start (not stuck in a bad state from the prior session)

---

## 2. Sleep Timer

### 2.1 Timer set and fire
- [ ] Set 10-min timer → play → wait for timer to expire
- [ ] FADING phase: "Softly winding down…" label appears
- [ ] 4-second fade out completes cleanly — no abrupt cut
- [ ] Returns to STOPPED state, motor fully silent, no residual vibration

### 2.2 Timer interrupted by user
- [ ] Start 10-min timer → let it reach FADING → press Pause/Stop during fade
- [ ] Immediate stop, no stuck state

### 2.3 Timer change mid-session
- [ ] Start session with OFF timer → while playing, set 10 min → verify timer now active
- [ ] Start session with 10-min timer → while playing, set OFF → timer should disarm

### 2.4 Timer persistence
- [ ] Set 20-min timer → close app completely → reopen → verify 20-min chip is still selected

---

## 3. Silent Mode

### 3.1 Toggle during session
- [ ] Start playing (with audio) → toggle Silent Purr ON → audio mutes immediately, haptics continue
- [ ] Toggle Silent Purr OFF → audio restarts from position 0, haptics re-align
- [ ] Do this 5× rapidly — no stuck state, no missing haptics

### 3.2 Start in silent mode
- [ ] Toggle Silent Purr BEFORE pressing play → play → verify haptics run, no audio
- [ ] Haptic loop sync fires every ~14.76s (check `adb logcat` for "Silent loop boundary")

### 3.3 Silent mode + timer
- [ ] Silent purr ON + 10-min timer → verify timer still fires, fade runs on haptics only

---

## 4. Chest Mode

### 4.1 Screen dim transition
- [ ] Toggle Chest Mode ON while playing → screen dims over ~800 ms (not instant)
- [ ] Screen brightness drops to approximately 0.08 (near-black) via window attributes
- [ ] UI alpha dims to ~42% visually
- [ ] Toggle Chest Mode OFF → screen restores gradually

### 4.2 Dim on play start
- [ ] Chest Mode already ON → press Play → dim transition begins during startup ramp

### 4.3 Stop while chest mode active
- [ ] Playing with Chest Mode → press Stop → screen should restore after STOPPED state

---

## 5. Audio Focus Interruptions

### 5.1 Phone call
- [ ] Start session → receive phone call → session pauses cleanly (haptics stop, audio mutes)
- [ ] End call → session resumes (haptics restart, audio seeks to 0 and plays from position 0)
- [ ] Check logcat for "Audio focus lost" and "Audio focus gained" entries

### 5.2 Notification sound
- [ ] Start session → trigger a notification → session pauses briefly, resumes after
- [ ] No desync: haptics and audio should be aligned after resume

### 5.3 Other media app
- [ ] Start session → open Spotify and play → session pauses
- [ ] Stop Spotify → session should NOT auto-resume (focus not regained)

---

## 6. Background / Foreground

### 6.1 App minimize
- [ ] Start session → press Home → session should continue (audio + haptics)
- [ ] Return to app → UI shows correct PLAYING state

### 6.2 Task switcher kill
- [ ] Start session → swipe away from task switcher → verify engine disposes cleanly (no orphaned vibration)
- [ ] Reopen app → session shows STOPPED correctly

### 6.3 Screen timeout during session
- [ ] Screen timeout fires mid-session → FLAG_KEEP_SCREEN_ON should prevent this
- [ ] If screen does time out (e.g. some OEMs override): verify motor still runs

---

## 7. Thermal + Battery

### 7.1 Long session taper (5-minute mark)
- [ ] Run a 10-minute session without stopping
- [ ] After ~5 min: logcat should show "Thermal taper active" entries
- [ ] Feel: purr should be subtly softer, NOT noticeably weaker
- [ ] No perceptible audio change (taper is haptic-only)

### 7.2 Device temperature
- [ ] Run a 20-minute session
- [ ] Phone back surface temperature: should not be uncomfortable to hold
- [ ] Compare before/after battery percentage (should be < 4% for 20 min)

### 7.3 CPU wake frequency
- [ ] Verify the timer poll interval (200 ms) and thermal tick (45 s) are not causing excessive wakes
- [ ] Use Android Studio Profiler CPU trace if battery drain is high

---

## 8. Device Variance

### 8.1 Amplitude-control capable device (e.g. Pixel 6+, Galaxy S22+)
- [ ] Full waveform envelope plays with perceptual gamma curve
- [ ] Soft-to-loud-to-soft texture of the purr is perceptible
- [ ] No harsh buzzing at high amplitude sections

### 8.2 No-amplitude-control device (older / mid-range OEM)
- [ ] Check logcat: `hasAmplitudeControl=false`
- [ ] Binary on/off timing still feels rhythmic (not random noise)
- [ ] Rhythm conveys purr pattern — acceptable if not perceptually identical to full device

### 8.3 Weak motor device
- [ ] Motor should not run continuously at max (duty cycle has gaps built in)
- [ ] If motor feels harsh or continuous: lower `MOTOR_ABS_CAP` or `CURVE_GAMMA` in PurrHapticPlayer

---

## 9. Edge Cases

- [ ] Play → immediately flip Silent Purr ON/OFF several times → no crash, no stuck haptics
- [ ] Set sleep timer → immediately change to a different timer → only the new timer fires
- [ ] Fast navigation: Home → Session → Back → Session → Play — verify clean state every time
- [ ] Rotate screen during session (if rotation not locked) — state preserved, no restart

---

## 10. Subjective Comfort (the actual product test)

Run these as real users with phone on chest, lights off:

- [ ] **5-minute session:** Does it feel like a cat purring? Not like a phone buzzing?
- [ ] **15-minute session:** Still comfortable? No skin discomfort from heat?
- [ ] **Silent purr only:** Does haptic alone feel satisfying without audio?
- [ ] **At 2am:** Does the UI feel safe, calm, non-intrusive?
- [ ] **Fade-out experience:** When timer expires, does the fade feel like the cat wandering off — or like the app crashing?

---

## Remaining Blockers Before Phase 2

These require real-device observation and cannot be verified in code:

1. **OEM motor variance** — test on ≥ 3 devices (Pixel, Samsung, OnePlus/nothing) and document `CURVE_GAMMA` / `MOTOR_ABS_CAP` adjustments needed per OEM family
2. **Battery drain** — measure actual mAh/20 min on target device; acceptable ceiling TBD
3. **Thermal comfort** — verify 20-min session doesn't cause skin discomfort on any test device
4. **Chest-mode brightness override** — some OEMs (Xiaomi, Huawei) ignore `screenBrightness` in WindowManager; needs OEM testing
5. **Audio focus behavior on Samsung** — Samsung has non-standard audiofocus behavior (AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK sometimes signals differently); verify resume works
