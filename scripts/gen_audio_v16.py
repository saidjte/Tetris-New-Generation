#!/usr/bin/env python3
"""
v1.6.0 (A5) — generates the two new synthesized SFX that v1.5.0 lacked:
  perfect  : dedicated perfect-clear sting (ascending major arpeggio + sub layer)
  b2b      : back-to-back bonus blip (two quick notes, rising fifth)

Eight files total: 4 synthesized packs (original/arcade/future/calm) x 2 sounds.
Same engineering as the v1.3.0 "SFX v2" pass: sub layer under the main tone,
upward sweep on success stings, noise transient on attacks, longer ADSR.
22050 Hz / 16-bit / mono WAV, written straight into app/src/main/res/raw/.

The T99 pack keeps its real-recordings-only character: it has no perfect/b2b
files and falls back through the tested SfxSoundMap chain (PERFECT->TETRIS,
B2B->COMBO->CLEAR).
"""
import math
import os
import struct
import wave

SR = 22050
OUT = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "res", "raw")


def env(t, dur, attack=0.012, release=None):
    """Simple ADSR-ish envelope: fast attack, sustain, exponential release."""
    if release is None:
        release = max(0.08, dur * 0.55)
    if t < attack:
        return t / attack
    if t > dur - release:
        r = (dur - t) / release
        return max(0.0, r) ** 2
    return 1.0


def osc(shape, phase):
    if shape == "sine":
        return math.sin(phase)
    if shape == "square":
        return 0.6 if math.sin(phase) >= 0 else -0.6
    if shape == "triangle":
        return 2.0 / math.pi * math.asin(math.sin(phase))
    if shape == "fm":
        # 2:1 carrier:modulator FM with a modest index — the "future" flavor.
        return math.sin(phase + 1.4 * math.sin(2.0 * phase))
    raise ValueError(shape)


def render(notes, shape, dur_each, sweep=False, sub=True, noise=0.10):
    """notes: list of (freq, gain). Renders sequentially, slight overlap."""
    total = int(SR * dur_each * len(notes)) + int(SR * 0.18)
    out = [0.0] * total
    for i, (f0, gain) in enumerate(notes):
        f = f0
        start = int(SR * dur_each * i)
        n = int(SR * dur_each)
        for j in range(n):
            t = j / SR
            # upward sweep for the success stings (richness, v1.3.0 style)
            freq = f * (1.0 + (0.06 * t / dur_each) if sweep else 0.0)
            ph = 2 * math.pi * freq * t
            a = gain * env(t, dur_each) * osc(shape, ph)
            if sub:
                a += 0.35 * gain * env(t, dur_each) * math.sin(2 * math.pi * freq / 4 * t)
            if noise and t < 0.02:
                a += noise * gain * (1 - t / 0.02) * (math.sin(91.3 * j) * 0.5)
            idx = start + j
            if idx < total:
                out[idx] += a
    # soft clip
    peak = max(1e-6, max(abs(s) for s in out))
    k = 0.88 / peak
    return [max(-1.0, min(1.0, s * k)) for s in out]


def write_wav(name, samples):
    path = os.path.join(OUT, name)
    with wave.open(path, "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(SR)
        frames = b"".join(struct.pack("<h", int(s * 32767)) for s in samples)
        w.writeframes(frames)
    print("wrote", name, len(samples) * 2, "bytes")


PACK_SHAPES = {
    "original": "sine",
    "arcade": "square",
    "future": "fm",
    "calm": "triangle",
}

# C5 E5 G5 C6 — bright major arpeggio for the perfect clear.
PERFECT_NOTES = [(523.25, 0.9), (659.25, 0.8), (783.99, 0.85), (1046.5, 1.0)]
# G4 -> D5 — tight rising fifth, the back-to-back "again!" blip.
B2B_NOTES = [(392.0, 0.85), (587.33, 1.0)]


def main():
    for pack, shape in PACK_SHAPES.items():
        write_wav(f"sfx_{pack}_perfect.wav", render(PERFECT_NOTES, shape, 0.17, sweep=True))
        write_wav(f"sfx_{pack}_b2b.wav", render(B2B_NOTES, shape, 0.11, sweep=False, noise=0.16))


if __name__ == "__main__":
    main()
