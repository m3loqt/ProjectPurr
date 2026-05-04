#!/usr/bin/env python3
"""
Build Catpur1AudioHapticEnvelope.kt from res/raw/catpur1.mp3.

Motif: vibration should feel like a **continuous rolled R** (motor “tap‑tap‑tap…”),
not a flat line buzz. We do that with a repeating **carrier** (default: 6 ms ON / 18 ms OFF
in a 24 ms cycle; slightly lower duty than 8/16 for cooler motor / battery while keeping the motif), scaled by RMS loudness
from the decoded audio sampled at each cycle.

Fine audio analysis stays on a 20 ms grid for sync with the waveform; playback loop
still matches TOTAL_MS (sum of timings).
"""
from __future__ import annotations

import math
import os
import struct
import subprocess
import sys
import wave

BIN_MS = 20
# 14760 ms / 24 = 615 cycles (faster taps than 40 ms carrier; still locks to loop length)
CARRIER_CYCLE_MS = 24
PULSE_ON_MS = 6
REST_MS = CARRIER_CYCLE_MS - PULSE_ON_MS

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
MP3 = os.path.join(ROOT, "app/src/main/res/raw/catpur1.mp3")
CAF = os.path.join(ROOT, "build/catpur1.caf")
WAV = os.path.join(ROOT, "build/catpur1.wav")
OUT = os.path.join(
    ROOT,
    "app/src/main/java/com/projectpurr/engine/Catpur1AudioHapticEnvelope.kt",
)

ATTACK = 0.55
RELEASE = 0.18
GAMMA_MAP = 0.62
NOISE_GATE_FRAC = 0.065

AMP_LO_NONZERO = 20
AMP_HI = 53

# Extra shaping on sampled energy inside each tongue-tap pulse
PULSE_GAMMA = 0.78
ENERGY_SILENCE = 0.012


def ensure_wav() -> None:
    os.makedirs(os.path.dirname(WAV), exist_ok=True)
    if os.path.isfile(WAV) and os.path.getmtime(WAV) >= os.path.getmtime(MP3):
        return
    subprocess.check_call(
        ["afconvert", "-f", "caff", "-d", "LEI16", MP3, CAF],
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
    )
    subprocess.check_call(
        ["afconvert", "-f", "WAVE", "-d", "LEI16", CAF, WAV],
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
    )


def box_smooth(a: list[float], radius: int) -> list[float]:
    out = []
    n = len(a)
    for i in range(n):
        lo = max(0, i - radius)
        hi = min(n, i + radius + 1)
        out.append(sum(a[lo:hi]) / (hi - lo))
    return out


def envelope_follower(r: list[float], atk: float, rel: float) -> list[float]:
    out = [r[0]]
    for i in range(1, len(r)):
        prev = out[-1]
        targ = r[i]
        coef = atk if targ > prev else rel
        out.append(prev + coef * (targ - prev))
    return out


def circular_smooth(values: list[float], passes: int = 3) -> list[float]:
    v = values[:]
    n = len(v)
    if n < 3:
        return v
    for _ in range(passes):
        nv = []
        for i in range(n):
            L = v[(i - 1) % n]
            R = v[(i + 1) % n]
            nv.append(0.55 * v[i] + 0.225 * L + 0.225 * R)
        v = nv
    return v


def interp_env(normed: list[float], t_ms: float) -> float:
    n = len(normed)
    if n == 0:
        return 0.0
    max_t = n * BIN_MS - 1e-9
    t = max(0.0, min(t_ms, max_t))
    f = t / BIN_MS
    i = int(f)
    fr = f - i
    if i >= n - 1:
        return float(normed[-1])
    return float(normed[i]) * (1.0 - fr) + float(normed[i + 1]) * fr


def main() -> None:
    ensure_wav()
    with wave.open(WAV, "rb") as w:
        nch = w.getnchannels()
        sw = w.getsampwidth()
        fr = w.getframerate()
        n = w.getnframes()
        raw = w.readframes(n)
    assert sw == 2
    samples = struct.unpack("<" + "h" * (len(raw) // 2), raw)
    mono: list[float] = []
    for i in range(0, len(samples), nch):
        mono.append(sum(samples[i : i + nch]) / float(nch))

    spb = int(round(fr * BIN_MS / 1000.0))
    nbins = len(mono) // spb
    mono = mono[: nbins * spb]

    rms: list[float] = []
    for b in range(nbins):
        chunk = mono[b * spb : (b + 1) * spb]
        m = sum(x * x for x in chunk) / len(chunk)
        rms.append(math.sqrt(max(m, 0.0)))

    sm_pre = box_smooth(rms, 2)
    sm_follow = envelope_follower(sm_pre, ATTACK, RELEASE)
    mx = max(sm_follow) or 1.0
    floor_v = mx * NOISE_GATE_FRAC
    mask_on = [x >= floor_v for x in sm_follow]

    normed: list[float] = []
    for x in sm_follow:
        if x < floor_v:
            normed.append(0.0)
        else:
            t = max(0.0, min(1.0, (x - floor_v) / (mx - floor_v + 1e-9)))
            t = math.pow(t, GAMMA_MAP)
            normed.append(t)

    for i in range(nbins):
        if normed[i] <= 0.0:
            continue
        normed[i] *= 0.90 + 0.10 * (0.5 + 0.5 * math.sin(2 * math.pi * (i / nbins * 6.25)))

    normed = circular_smooth(normed, passes=3)
    mask_f = [1.0 if m else 0.0 for m in mask_on]

    total_ms = nbins * BIN_MS
    if total_ms % CARRIER_CYCLE_MS != 0:
        raise SystemExit(
            f"{total_ms} ms not divisible by carrier {CARRIER_CYCLE_MS}; fix BIN_MS/audio length alignment",
        )

    ncycles = total_ms // CARRIER_CYCLE_MS

    timings: list[int] = []
    amps: list[int] = []
    taps = 0
    sleeps = 0

    for c in range(ncycles):
        t_hit = float(c * CARRIER_CYCLE_MS + PULSE_ON_MS * 0.5)
        e_raw = interp_env(normed, t_hit)
        bm = interp_env(mask_f, t_hit)

        # Require both interpolated energy & mask so quiet stays truly quiet after smoothing.
        if e_raw < ENERGY_SILENCE or bm < 0.42:
            timings.append(CARRIER_CYCLE_MS)
            amps.append(0)
            sleeps += 1
            continue

        e = math.pow(max(0.0, min(1.0, e_raw)), PULSE_GAMMA)
        wig = math.sin(c * 1.8347 + 11.07) * 0.022
        tt = max(0.0, min(1.0, e * (1.0 + wig)))
        pulse = int(round(AMP_LO_NONZERO + tt * (AMP_HI - AMP_LO_NONZERO)))
        pulse = max(AMP_LO_NONZERO, min(AMP_HI, pulse))

        timings.append(PULSE_ON_MS)
        amps.append(pulse)
        timings.append(REST_MS)
        amps.append(0)
        taps += 1

    if sum(timings) != total_ms:
        raise SystemExit(f"timing bug sum={sum(timings)} vs {total_ms}")

    sz = len(timings)
    print(
        "total_ms",
        total_ms,
        "segments",
        sz,
        "cycles",
        ncycles,
        "tap_cycles",
        taps,
        "quiet_cycles",
        sleeps,
        file=sys.stderr,
    )

    chunk = 28
    t_lines = ["    val timingsMs: LongArray = longArrayOf("]
    for i in range(0, sz, chunk):
        part = timings[i : i + chunk]
        t_lines.append("        " + ", ".join(f"{x}L" for x in part) + ("," if i + chunk < sz else ""))
    t_lines.append("    )")

    a_lines = ["    val amplitudesBase: IntArray = intArrayOf("]
    for i in range(0, sz, chunk):
        part = amps[i : i + chunk]
        a_lines.append("        " + ", ".join(str(x) for x in part) + ("," if i + chunk < sz else ""))
    a_lines.append("    )")

    lines = [
        "package com.projectpurr.engine",
        "",
        "/**",
        f" * Loudness-driven rolled‑R carrier (~{1000/CARRIER_CYCLE_MS:.0f} Hz cycle) from `catpur1.mp3` analysis.",
        " * Regenerate after MP3 swaps: `python3 tools/generate_catpur_haptic_envelope.py`",
        " */",
        "object Catpur1AudioHapticEnvelope {",
        f"    const val CARRIER_CYCLE_MS: Long = {CARRIER_CYCLE_MS}L",
        f"    /** ON pulse width inside each articulation-ish cycle (~{PULSE_ON_MS} ms). */",
        f"    const val PULSE_ON_MS: Long = {PULSE_ON_MS}L",
        "    /** One playback loop duration (matches MP3 loop). */",
        f"    const val TOTAL_MS: Long = {total_ms}L",
        "",
        "\n".join(t_lines),
        "",
        "\n".join(a_lines),
        "}",
        "",
    ]

    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    with open(OUT, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))
    print(OUT)


if __name__ == "__main__":
    main()
