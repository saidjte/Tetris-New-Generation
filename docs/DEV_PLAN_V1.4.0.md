# DEV PLAN — v1.4.0 "Real Assets Overhaul | تحديث الأصول الحقيقية"

<div dir="rtl">

## السياق

طلب المستخدم: **«ضع خطة مفصلة لتطوير اللعبة و جعلها أكثر جمالية و قوة و تشويق و خاصة المؤثرات الصوتية و البصرية. استعن الملفات في الروابط التالية فهي تحتوي على العديد من المميزات.»**

وصلت 4 ملفات من Google Drive وحُلّلت بالكامل (26 أكتوبر 2026):

| الملف | المحتوى | الحجم |
|---|---|---|
| 1 | 21 مؤثر صوتي T99 (MP3) + **275 مقطع صوت معلّق حقيقي "Raffina"** + 4 أيقونات | 4.9MB |
| 2 | 24 صوت واجهة Steam Deck (WAV) | 2.8MB |
| 3 | حزمة أصول Tetris: **موسيقى حقيقية** (Main Menu + Type A + Type B)، رسومات Blocks/UI/خلفيات | 13.2MB |
| 4 | 4 خطوط: **Orbitron** (أرقام لاتينية)، **Changa + Cairo** (عربية)، SpicySale | 0.4MB |

**الفجوة**: إصدار v1.3.0 السابق استخدم صوتيات مُركّبة توليدياً (63 WAV) لأن الملفات لم تكن قد وصلت. هذا الإصدار يستبدلها **بالأصول الحقيقية**.

</div>

## Batches

### Batch A — Audio (the core request | المؤثرات الصوتية)

| ID | Item | Detail |
|---|---|---|
| A1 | **T99 SFX pack** (5th sound pack) | 21 MP3s → `res/raw/sfx_t99_*.mp3`. SfxEngine: +9 events (HOLD, TSPIN, TSPIN_DOUBLE/TRIPLE, CLEAR_DOUBLE/TRIPLE, DANGER, SAVE, RECORD) with graceful per-pack fallback chain (old packs fall back to base sounds, never crash). Store item `sfx_t99` (150 coins). |
| A2 | **Announcer VoiceEngine** | 275 real Raffina clips → `res/raw/voice_<event>_<idx>.mp3` (4.4MB). MediaPlayer-based (no PCM memory explosion), single background thread, interrupt semantics (latest big event wins), random variant per event with no-immediate-repeat. Pure-JVM `VoiceCatalog` (counts, file naming, **combo9 stays silent by design**). Settings: announcer toggle + volume. Wired events: start/warm, clear1-3, tetris, tspin, pc, b2b, combo2-10, levelup (→speed voices in survival), danger + saved (stack-height tracker with hysteresis), score milestones 10k→1m, lines milestones 25→200, near (≤4 lines to goal), hurry (10s left), ultra60/ultra30 (timed modes), timeup, over/win/record, zen + zenCalm (90s), pause/resume, idle (12s no input, 30s cooldown), sprintIntro (goal-line modes), ultra (time attack). |
| A3 | **Real music** | `Main Menu.mp3` → menus, `Type A.mp3` → gameplay, `Type B.mp3` → gameplay at level ≥ 8 (intensity ramp). Zen keeps its calm loop. MusicEngine gains PLAY_INTENSE track + auto-switch. |
| A4 | **UI sounds** | 13 curated Steam Deck sounds → OGG/MP3 mono ~96k (~350KB total): toggle on/off, tab, modal show/hide, launch, achievement, toast, back, navigate, scroll. Wired: settings toggles, store/tasks tabs, dialogs, mode launch, home buttons. |

### Batch B — Visual (المؤثرات البصرية)

| ID | Item | Detail |
|---|---|---|
| B1 | **Typography** | Static instances via fonttools: Orbitron 500/700/900 (score, timer, digits, EN display), Changa 400/600/700 (Arabic display/headlines), Cairo 400/600/700 (Arabic body). New Material `Typography` + explicit score font on HUD. |
| B2 | **T99 block pack** | 7 sprite PNGs (per-piece colors) → new `BlockStyle.T99` in BoardCanvas + MiniPiece previews. Plus UI frames: board border (352×672), Hold/Next panel backgrounds — all from the asset pack. Store item `blocks_t99` (130 coins). |
| B3 | **App icon** | New 512/192 icons (+maskable) → all mipmap densities + adaptive foreground. |

### Batch C — Tests, docs, release

- C1: pure-JVM tests: VoiceCatalog (counts/naming/combo9-silence), VoiceMilestones (once-only crossings), DangerTracker (hysteresis 15/13), SfxSoundMap (suffix + fallback termination). Target +18 tests (~107 total).
- C2: strings en/ar (announcer settings, pack names), StoreCatalog + store.json, SettingsScreen voices UI.
- C3: version **v1.4.0 / versionCode 6**, CHANGELOG, README, tag → CI → Release, download/ refresh, worklog.

## Non-goals (→ v1.5.0)

- Image backgrounds as new theme (backgrounds.png/circles.png) — needs screen-level background pipeline.
- Dynamic music layering by danger level; effects-intensity setting; landscape layout.
- rotation-variant sprites (T1-T4 etc.) — flat block sprites already deliver the look.

## APK size budget

12.35MB → ~29MB (music 12.6 + voices 4.4 + T99 SFX 0.46 + UI sounds 0.35 + sprites/fonts/icons ~0.2 − replaced WAVs 1.6MB). Normal for an offline game; all assets bundled, still zero INTERNET permission.
