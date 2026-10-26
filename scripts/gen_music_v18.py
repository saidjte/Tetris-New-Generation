#!/usr/bin/env python3
"""
v1.8.0 'Type A reborn' — rebuild the in-game gameplay music.

The complaint: the current gameplay music is bad. The fix: replace
music_play + music_play_intense with ORIGINAL, modern chiptune-EDM
arrangements of the public-domain Russian folk melody "Korobeiniki"
(the tune Tetris made famous) in a Tetris-99-like energetic style.

Legal: Korobeiniki is a 19th-century folk song (public domain). This is
our own synthesis and arrangement — no Nintendo / Tetris Holding
recordings are used or copied.

Outputs (res/raw/):
  music_play.ogg          — main gameplay theme  (30 bars @149 BPM, ~48.3 s)
  music_play_intense.ogg  — danger/level-8 theme (20 bars @162 BPM, ~29.6 s)
QA artifacts (scripts/): spectrogram PNGs + stats JSON.

OGG is used instead of MP3 because Android MediaPlayer loops Vorbis far
tighter than MP3 (no encoder-delay gap at the loop point). Resource names
R.raw.music_play / R.raw.music_play_intense are extension-agnostic, so no
Kotlin change is required.
"""
import json
import os
import subprocess
import sys

import numpy as np
from scipy.signal import lfilter

SR = 44100
SEMI = {'C': 0, 'C#': 1, 'D': 2, 'D#': 3, 'E': 4, 'F': 5, 'F#': 6,
        'G': 7, 'G#': 8, 'A': 9, 'A#': 10, 'B': 11}


def midi(name):
    """'G#4' -> 68, 'C6' -> 84, None -> None."""
    if name is None:
        return None
    i = 2 if (len(name) > 1 and name[1] in '#b') else 1
    return 12 * (int(name[i:]) + 1) + SEMI[name[:i]]


def hz(m):
    return 440.0 * 2.0 ** ((m - 69) / 12.0)


# ---------------------------------------------------------------- oscillators
def adsr(n, a=0.004, r=0.07, gate=0.92, s=0.86):
    e = np.full(n, s)
    an = max(1, int(a * SR))
    e[:an] = np.linspace(0, 1, an)
    gp = max(an + 1, int(n * gate))
    rn = min(max(1, int(r * SR)), max(1, gp - an))
    e[gp - rn:gp] = np.linspace(1.0, 0.0, rn)
    e[gp:] = 0.0
    return e


def phase_of(f, n, vib_hz=5.6, vib_cents=8, vib_delay=0.16):
    t = np.arange(n) / SR
    ramp = np.clip(t / vib_delay, 0, 1)
    fm = 1 + (2 ** (vib_cents / 1200) - 1) * np.sin(2 * np.pi * vib_hz * t) * ramp
    return np.cumsum(2 * np.pi * f * fm / SR)


def pulse(ph, duty):
    return np.where((ph / (2 * np.pi)) % 1.0 < duty, 1.0, -1.0)


def saw(ph):
    return 2 * ((ph / (2 * np.pi)) % 1.0) - 1.0


def tri(ph):
    return 2 / np.pi * np.arcsin(np.sin(ph))


def onepole_lp(x, fc):
    a = float(np.exp(-2 * np.pi * fc / SR))
    return lfilter([1 - a], [1, -a], x)


def onepole_hp(x, fc):
    return x - onepole_lp(x, fc)


# ---------------------------------------------------------------- instruments
def lead(f, dur, style='pulse', gate=0.92):
    n = int(dur * SR)
    ph = phase_of(f, n)
    if style == 'pulse':
        w = 0.60 * pulse(ph, 0.25) + 0.26 * pulse(ph, 0.125) + 0.18 * saw(ph)
    else:  # 'saw2' — dual detuned saws, darker/bigger
        ph2 = phase_of(f * 2 ** (7 / 1200), n, vib_cents=10)
        w = 0.52 * saw(ph) + 0.44 * saw(ph2) + 0.16 * pulse(ph, 0.35)
        w = onepole_lp(w, 5200)
    return w * adsr(n, a=0.004, r=0.07, gate=gate, s=0.86)


def bass(f, dur, style='tri', gate=0.9):
    n = int(dur * SR)
    ph = 2 * np.pi * f * np.arange(n) / SR
    if style == 'tri':
        w = tri(ph) + 0.55 * np.sin(ph) + 0.20 * saw(2 * ph)
        w = onepole_lp(w, 1150)
    else:  # 'saw' — driving 16th variant
        w = saw(ph) + 0.40 * np.sin(ph)
        w = onepole_lp(w, 1600)
    return w * adsr(n, a=0.003, r=0.045, gate=gate, s=0.9)


def stab(f, dur=0.14):
    n = int(dur * SR)
    ph = 2 * np.pi * f * np.arange(n) / SR
    w = pulse(ph, 0.33) + 0.3 * saw(ph)
    w = onepole_hp(w, 320)
    return w * adsr(n, a=0.003, r=0.05, gate=1.0, s=0.85)


def kick(amp=1.0):
    n = int(0.20 * SR)
    t = np.arange(n) / SR
    f = 46 + 116 * np.exp(-t / 0.034)
    body = np.sin(np.cumsum(2 * np.pi * f / SR)) * np.exp(-t / 0.085)
    click = np.random.RandomState(7).randn(n) * np.exp(-t / 0.0035) * 0.35
    return (body + click) * amp


def snare(amp=1.0, decay=0.13):
    n = int(0.30 * SR)
    t = np.arange(n) / SR
    no = np.random.RandomState(11).randn(n)
    no = onepole_lp(onepole_hp(no, 900), 6500)
    body = np.sin(2 * np.pi * 196 * t) * np.exp(-t / 0.05)
    return (no * np.exp(-t / decay) * 0.85 + body * 0.65) * amp


def hat(open_=False, amp=1.0):
    d = 0.30 if open_ else 0.048
    n = int(d * SR)
    t = np.arange(n) / SR
    rs = 3 if open_ else 5
    no = onepole_hp(np.random.RandomState(rs).randn(n), 7400)
    return no * np.exp(-t / (d * 0.45)) * amp * 0.5


def crash(amp=1.0):
    n = int(1.1 * SR)
    t = np.arange(n) / SR
    no = onepole_hp(np.random.RandomState(23).randn(n), 5200)
    return no * np.exp(-t / 0.42) * amp * 0.35


# ------------------------------------------------------------------- FX tools
def duck_env(n_total, kick_times, depth=0.38, rec=0.12):
    g = np.ones(n_total)
    span = int(rec * 4 * SR)
    idx = np.arange(span)
    curve = depth * np.exp(-idx / (rec * SR))
    for kt in kick_times:
        i0 = int(kt * SR)
        if i0 >= n_total:
            continue
        m = min(span, n_total - i0)
        g[i0:i0 + m] = np.minimum(g[i0:i0 + m], 1.0 - curve[:m])
    return g


def _comb_block(x, D, fb):
    y = np.zeros_like(x)
    y[:D] = x[:D]
    nb = (len(x) + D - 1) // D
    for k in range(1, nb):
        a, b = k * D, min((k + 1) * D, len(x))
        y[a:b] = x[a:b] + fb * y[a - D:b - D]
    return y


def _allpass_block(x, D, g):
    y = np.zeros_like(x)
    xp = np.concatenate([np.zeros(D), x])
    prev = np.zeros(D)
    nb = (len(x) + D - 1) // D
    for k in range(nb):
        a, b = k * D, min((k + 1) * D, len(x))
        y[a:b] = xp[a:b] - g * x[a:b] + g * prev[:b - a]
        prev = y[a:b].copy()
    return y


def reverb(L, R, wet=0.30):
    """Simple stereo Schroeder reverb (blockwise, O(N))."""
    def net(x, delays, aps):
        total = np.zeros_like(x)
        for d, fb in zip(delays, [0.74, 0.76, 0.78, 0.72]):
            total += _comb_block(np.pad(x, (0, d)), d, fb)[:len(x)]
        total /= 4
        for D, g in aps:
            total = _allpass_block(total, D, g)
        return onepole_lp(total, 6200)

    pd = int(0.02 * SR)
    Lp, Rp = np.pad(L, (pd, 0))[:len(L)], np.pad(R, (pd, 0))[:len(R)]
    wL = net(Lp, [1491, 1817, 2011, 2451], [(347, 0.5), (113, 0.5)])
    wR = net(Rp, [1541, 1877, 2071, 2521], [(359, 0.5), (127, 0.5)])
    return L + wL * wet, R + wR * wet


def echo(bus, dt, taps=(0.30, 0.10), lp=4200):
    """Rhythmic feedback echo (dotted eighth) on a stereo bus."""
    D = int(dt * SR)
    L, R = bus
    outL, outR = L.copy(), R.copy()
    srcL = onepole_lp(L, lp)
    srcR = onepole_lp(R, lp)
    for i, g in enumerate(taps):
        s = D * (i + 1)
        if s >= len(L):
            break
        outL[s:] += srcL[:len(L) - s] * g
        outR[s:] += srcR[:len(R) - s] * g
    return outL, outR


def haas(bus, ms=11.0, bal=0.94):
    D = int(ms / 1000 * SR)
    L, R = bus
    return L, np.pad(R * bal, (D, 0))[:len(R)]


def pan(mono, gl, gr):
    return mono * gl, mono * gr

# ------------------------------------------------------------- arrangement
# Korobeiniki (public-domain folk melody), E minor, as Tetris made it famous.
A_MELODY = [
    [('E5', 2), ('B4', 1), ('C5', 1), ('D5', 2), ('C5', 1), ('B4', 1)],
    [('A4', 2), ('A4', 1), ('C5', 1), ('E5', 2), ('D5', 1), ('C5', 1)],
    [('B4', 3), ('C5', 1), ('D5', 2), ('E5', 2)],
    [('C5', 2), ('A4', 2), ('A4', 2), (None, 2)],
    [(None, 1), ('D5', 2), ('F5', 1), ('A5', 2), ('G5', 1), ('F5', 1)],
    [('E5', 3), ('C5', 1), ('E5', 2), ('D5', 1), ('C5', 1)],
    [('B4', 2), ('B4', 1), ('C5', 1), ('D5', 2), ('E5', 2)],
    [('C5', 2), ('A4', 2), ('A4', 2), (None, 2)],
]
B_MELODY = [
    [('E5', 4), ('C5', 4)],
    [('D5', 4), ('B4', 4)],
    [('C5', 4), ('A4', 4)],
    [('G#4', 4), ('B4', 4)],
    [('E5', 4), ('C5', 4)],
    [('D5', 4), ('B4', 4)],
    [('C5', 2), ('E5', 2), ('A5', 4)],
    [('G#5', 6), (None, 2)],
]
RUN_MELODY = [
    [('A5', 1), ('B5', 1), ('C6', 1), ('B5', 1), ('A5', 1), ('G#5', 1), ('E5', 1), ('F#5', 1)],
    [('G#5', 2), ('F#5', 2), ('E5', 2), ('D#5', 2)],
]

CHORD_TONES = {  # mid register, for the arpeggiator
    'Em': [64, 67, 71, 76], 'Am': [60, 64, 69, 72], 'G': [59, 62, 67, 71],
    'B7': [59, 63, 66, 69], 'Dm': [62, 65, 69, 74], 'C': [60, 64, 67, 72],
    'E': [64, 68, 71, 76],
}
STABS = {
    'Em': [64, 67, 71], 'Am': [64, 69, 72], 'G': [62, 67, 71],
    'B7': [63, 66, 71], 'Dm': [65, 69, 74], 'C': [64, 67, 72],
    'E': [68, 71, 76],
}
ROOTS = {'Em': 40, 'Am': 45, 'G': 43, 'B7': 47, 'Dm': 38, 'C': 36, 'E': 40}

INTRO_CH = ['Em', 'Am', 'G', 'B7']
A_CH = ['Em', 'Am', ('G', 'B7'), 'Am', 'Dm', 'C', 'G', 'Am']
B_CH = ['C', 'G', 'Am', 'E', 'C', 'G', 'Am', 'E']
RUN_CH = ['Am', 'B7']

BASS_A = [0, 12, 7, 12, 0, 12, 7, 12]     # eighth pump
BASS_B = [(0, 4), (7, 4)]                  # half-time root / fifth
BASS_RUN = [0, 0, 7, 7, 12, 12, 7, 7]
ARP_PAT = [0, 1, 2, 3, 3, 2, 1, 0, 0, 1, 2, 3, 3, 2, 1, 0]  # 16 sixteenths


class Track:
    """Stereo bus collection for one render."""

    def __init__(self, n):
        self.n = n
        self.b = {k: (np.zeros(n), np.zeros(n)) for k in
                  ['kick', 'snare', 'hat', 'crash', 'bass', 'lead',
                   'lead2', 'arp', 'stab']}
        self.kick_times = []

    def add(self, bus, sigL, sigR, t, gL=1.0, gR=1.0):
        i0 = int(t * SR)
        if i0 >= self.n:
            return
        m = min(len(sigL), self.n - i0)
        self.b[bus][0][i0:i0 + m] += sigL[:m] * gL
        self.b[bus][1][i0:i0 + m] += sigR[:m] * gR

    def add_mono(self, bus, sig, t, gL=1.0, gR=1.0):
        self.add(bus, sig, sig, t, gL, gR)


def render(spec):
    """spec: dict with bpm, bars, chords (per-bar list), lead_style,
    bass_style, hat16_a2, kick_a2, duck, hot."""
    bpm = spec['bpm']
    u = 60.0 / bpm / 2            # eighth-note seconds
    bar = 8 * u
    bars = spec['bars']
    n_total = int(round(bars * bar * SR))
    T = Track(n_total)

    chords = spec['chords']
    assert len(chords) == bars, (len(chords), bars)

    def bar_chord(b, unit):
        c = chords[b]
        if isinstance(c, tuple):
            return c[0] if unit < 4 else c[1]
        return c

    # ---------------------------------------------------------- drums + bass
    for b in range(bars):
        t0 = b * bar
        sec = spec['section'][b]
        if sec in ('intro', 'run') or (sec == 'b' and spec['drive_b']) or \
           (sec == 'a' and spec['kick_a2'] and b >= spec['a2_start']):
            kicks = [0, 1, 2, 3]
        elif sec == 'a':
            kicks = [0, 4] + ([3.5] if b % 4 == 3 else [])
        else:  # half-time 'b'
            kicks = [0]
        for kbeats in kicks:
            tu = t0 + kbeats * 2 * u
            T.kick_times.append(tu)
            T.add_mono('kick', kick(), tu)
        snares = ([2, 6] if sec != 'b' else [4])
        if sec == 'intro' and b < 2:
            snares = []
        for sb in snares:
            T.add_mono('snare', snare(), t0 + sb * u, 0.9, 0.9)
        # hats
        if spec['hat16'] and (sec in ('run', 'b') or
                              (sec == 'a' and b >= spec.get('a2_start', 12))):
            slots = np.arange(0, 8, 0.5)
        elif sec == 'b':
            slots = [0, 2, 4, 6]
        else:
            slots = np.arange(8)
        for s in slots:
            open_ = (s == 7 and b % 2 == 1 and sec != 'b')
            T.add_mono('hat', hat(open_), t0 + s * u, 0.62, 0.95)
        # fills
        if sec == 'intro' and b == bars and False:
            pass
        if sec == 'intro' and b == 3:      # snare roll into the theme
            for i, s in enumerate(np.arange(0, 8, 0.5)):
                T.add_mono('snare', snare(0.18 + 0.5 * i / 16), t0 + s * u, 0.8, 0.8)
        if sec == 'a' and b == spec.get('a2_end', 19):   # fill into B
            for i, s in enumerate(np.arange(4, 8, 0.5)):
                T.add_mono('snare', snare(0.3 + 0.45 * i / 8), t0 + s * u, 0.85, 0.85)
        if sec == 'b' and b == bars - 3:   # build into the run
            for i, s in enumerate(np.arange(0, 8, 1.0)):
                T.add_mono('snare', snare(0.22 + 0.45 * i / 8), t0 + s * u, 0.85, 0.85)

    # bass
    for b in range(bars):
        t0 = b * bar
        sec = spec['section'][b]
        c = chords[b]
        if sec == 'b' and not spec['drive_b']:
            for off, span in BASS_B:
                name = c[0] if isinstance(c, tuple) else c
                root = ROOTS[name] + (off if off <= 7 else 0)
                T.add_mono('bass', bass(hz(root + 12), span * u * 0.98,
                                        spec['bass_style'], gate=0.95),
                           t0 + off * u)
        else:
            pat = BASS_RUN if sec == 'run' else BASS_A
            if isinstance(c, tuple):
                seq = [(c[0], 0, 4), (c[1], 4, 4)]
            else:
                seq = [(c, 0, 8)]
            for name, start, span in seq:
                for i in range(span):
                    root = ROOTS[name] + pat[i % len(pat)]
                    acc = 1.0 if i % 4 == 0 else 0.86
                    T.add_mono('bass', bass(hz(root + 12), u * 0.92,
                                            spec['bass_style']) * acc,
                               t0 + (start + i) * u)

    # ------------------------------------------------------------- melody
    def place(section, at_bar, gain, gate=0.92, octave2=False):
        t0 = at_bar * bar
        unit = 0
        for notes in section:
            for name, dur in notes:
                if name is not None:
                    m = midi(name)
                    T.add_mono('lead', lead(hz(m), dur * u, spec['lead_style'],
                                            gate=gate) * gain,
                               t0 + unit * u)
                    if octave2:
                        f2 = hz(m + 12) * 2 ** (4 / 1200)
                        T.add_mono('lead2', lead(f2, dur * u,
                                                 spec['lead_style'], gate=gate),
                                   t0 + unit * u + 0.006)
                unit += dur

    sections_data = {'a': A_MELODY, 'b': B_MELODY, 'run': RUN_MELODY}
    for sec_name, at_bar, oct2 in spec['plan']:
        gain = spec.get('lead_gain', 1.0) * (0.94 if sec_name == 'b' else 1.0)
        place(sections_data[sec_name], at_bar, gain,
              gate=0.97 if sec_name == 'b' else 0.92, octave2=oct2)

    # arpeggiator (16ths)
    arp_from = spec.get('arp_from', 12)
    for b in range(arp_from, bars):
        t0 = b * bar
        for s in range(16):
            name = bar_chord(b, s // 2)
            tone = CHORD_TONES[name][ARP_PAT[s]]
            gl = 1.0 if s % 2 == 0 else 0.45
            gr = 1.0 - (gl - 0.45) / 0.55 * 0.55 if s % 2 == 0 else 1.0
            T.add_mono('arp', stab(hz(tone), 0.11) * 0.5,
                       t0 + s * u * 0.5, gl, gr)

    # stabs (intro offbeats + half-time backbeat + run)
    for b in range(bars):
        t0 = b * bar
        sec = spec['section'][b]
        slots = []
        if sec == 'intro':
            slots = [1, 3, 5, 7]
        elif sec == 'b':
            slots = [2, 6]
        elif sec == 'run':
            slots = [2, 6]
        for s in slots:
            for j, tone in enumerate(STABS[bar_chord(b, s)]):
                T.add_mono('stab', stab(hz(tone)) * 0.5,
                           t0 + s * u + j * 0.004, 0.85, 0.6)

    # crashes at section entries
    for b in spec.get('crash_bars', [4, 20, 28]):
        if b < bars:
            T.add_mono('crash', crash(), b * bar, 0.8, 0.8)

    # ------------------------------------------------------------- mixdown
    gains = {'kick': 0.98, 'snare': 0.72, 'hat': 0.30, 'crash': 0.5,
             'bass': 0.82, 'arp': 0.30, 'stab': 0.34,
             'lead': 0.62, 'lead2': 0.22}
    ducked = ('bass', 'arp', 'stab')
    # base = everything except lead (echoed later) and ducked buses
    mixL = sum(T.b[k][0] * g for k, g in gains.items() if k not in ducked
               and k not in ('lead', 'lead2'))
    mixR = sum(T.b[k][1] * g for k, g in gains.items() if k not in ducked
               and k not in ('lead', 'lead2'))

    # sidechain duck on the rhythmic buses only
    dg = duck_env(n_total, T.kick_times, depth=spec['duck'])
    for k in ducked:
        mixL += (T.b[k][0] * dg) * gains[k]
        mixR += (T.b[k][1] * dg) * gains[k]

    # lead + octave double, widened (haas), with a dotted-eighth echo
    lead_total = T.b['lead'][0] * gains['lead'] + T.b['lead2'][0] * gains['lead2']
    D = int(0.011 * SR)
    leadL = lead_total
    leadR = np.pad(lead_total * 0.96, (D, 0))[:n_total]
    eL, eR = echo((leadL, leadR), u * 1.5)
    mixL += eL
    mixR += eR

    mixL, mixR = reverb(mixL, mixR, wet=0.26)
    mix = np.stack([mixL, mixR])
    mix = lfilter([1.0, -0.996], [1.0], mix, axis=-1)      # DC/infra cut
    mix = np.tanh(mix * 1.15) / 1.15
    mix *= 0.88 / max(1e-9, np.abs(mix).max())
    return mix, u, bar

# ------------------------------------------------------------------- render
def write_ogg(mix, path):
    import soundfile as sf
    tmp = path + '.tmp.wav'
    sf.write(tmp, mix.T, SR, subtype='PCM_16')
    subprocess.run(['ffmpeg', '-y', '-v', 'error', '-i', tmp,
                    '-c:a', 'libvorbis', '-q:a', '6', path], check=True)
    os.remove(tmp)


def spectro(mix, path, title):
    import matplotlib
    matplotlib.use('Agg')
    import matplotlib.pyplot as plt
    from scipy.signal import spectrogram
    f, t, S = spectrogram(mix[0], SR, nperseg=2048, noverlap=512)
    Sd = 10 * np.log10(S + 1e-10)
    fig, ax = plt.subplots(figsize=(14, 4.6), dpi=110, constrained_layout=True)
    pc = ax.pcolormesh(t, f, Sd, shading='auto', cmap='magma',
                       vmin=Sd.max() - 80, vmax=Sd.max())
    ax.set_yscale('log')
    ax.set_ylim(40, 18000)
    ax.set_title(title)
    ax.set_xlabel('time (s)')
    ax.set_ylabel('Hz')
    fig.colorbar(pc, ax=ax, label='dB')
    fig.savefig(path)
    plt.close(fig)


PLAY = dict(
    bpm=149, bars=30,
    section=['intro'] * 4 + ['a'] * 16 + ['b'] * 8 + ['run'] * 2,
    chords=INTRO_CH + A_CH + A_CH + B_CH + RUN_CH,
    lead_style='pulse', bass_style='tri', drive_b=False, kick_a2=False,
    a1_start=4, a2_start=12, a2_end=19, hat16=True, arp_from=12, duck=0.38,
    crash_bars=[4, 20, 28], lead_gain=1.0,
    plan=[('a', 4, False), ('a', 12, True), ('b', 20, False), ('run', 28, False)])

INTENSE = dict(
    bpm=162, bars=20,
    section=['intro'] * 2 + ['a'] * 8 + ['b'] * 8 + ['run'] * 2,
    chords=['Em', 'B7'] + A_CH + B_CH + RUN_CH,
    lead_style='saw2', bass_style='saw', drive_b=True, kick_a2=True,
    a1_start=2, a2_start=2, a2_end=9, hat16=True, arp_from=2, duck=0.45,
    crash_bars=[2, 10, 18], lead_gain=0.85,
    plan=[('a', 2, False), ('b', 10, False), ('run', 18, False)])

RAW = '/home/z/Tetris-New-Generation/app/src/main/res/raw'
stats = {}
for name, spec in [('music_play', PLAY), ('music_play_intense', INTENSE)]:
    mix, u, bar = render(spec)
    out = os.path.join(RAW, name + '.ogg')
    write_ogg(mix, out)
    spectro(mix, f'/home/z/my-project/scripts/{name}_spec.png', name)
    dur = mix.shape[1] / SR
    rms = float(np.sqrt(np.mean(mix ** 2)))
    side = float(np.sqrt(np.mean((mix[0] - mix[1]) ** 2)) / (rms + 1e-12))
    stats[name] = dict(
        duration_s=round(dur, 3), bars=spec['bars'], bpm=spec['bpm'],
        loop_bar_s=round(bar, 4), peak=round(float(np.abs(mix).max()), 3),
        rms=round(rms, 4), rms_dbfs=round(20 * np.log10(rms + 1e-12), 1),
        side_ratio=round(side, 3), bytes=os.path.getsize(out))

json.dump(stats, open('/home/z/my-project/scripts/music_v18_stats.json', 'w'),
          indent=2)
print(json.dumps(stats, indent=2))

