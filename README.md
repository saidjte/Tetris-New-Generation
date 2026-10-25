# Tetris New Generation | تيتريس الجيل الجديد

<div dir="rtl">

لعبة ألغاز أصلية للقطع المتساقطة لهواتف **Android**، مبنية بالكامل بلغة **Kotlin** و**Jetpack Compose**، وتعمل **دون إنترنت 100%** بعد التثبيت.

مشروع مشجّعين غير رسمي مستوحى من أسلوب اللعب الكلاسيكي — غير مرتبط بشركة The Tetris Company ولا معتمد منها. جميع الشيفرة والأصول (الأصوات، الموسيقى، الأيقونات) **أصلية ومولّدة خصيصًا لهذا المشروع**.

[![Android CI](https://github.com/saidjte/Tetris-New-Generation/actions/workflows/android.yml/badge.svg)](https://github.com/saidjte/Tetris-New-Generation/actions/workflows/android.yml)

</div>

---

**Tetris New Generation** is an original falling-blocks puzzle game for **Android**, built entirely with **Kotlin** and **Jetpack Compose**, and works **100% offline** after installation. An unofficial fan-made project — not affiliated with or endorsed by The Tetris Company. All code and assets (sounds, music, icons) are **original content created for this project**.

## ⬇️ Download / التحميل

Grab the latest installable APK from **[GitHub Releases](../../releases)**:

1. Open the [Releases page](../../releases) and download the APK from the **latest release** (currently v1.1.0).
2. On your Android phone, open the file and allow *Install unknown apps* for your browser/file manager if asked.
3. Install and play — no account, no internet, no ads.

You can also build it yourself (see below) or download the APK artifact from any successful CI run.

<div dir="rtl">

أو حمّل الملف من صفحة الإصدارات أعلاه، ثبّته، والعب فورًا دون إنترنت ودون تسجيل.

</div>

## ✨ Features / الميزات

- **11 real game modes** — every mode is a genuine engine configuration, not a decorative button:
  - **Classic / الكلاسيكي** — pure traditional experience.
  - **Marathon / الماراثون** — endless with rising difficulty.
  - **Quick Play / اللعب السريع** — short 2-minute rounds.
  - **Challenges / التحديات** — 8 goal-based challenges with one-time rewards.
  - **Time Attack / سباق الوقت** — 1, 3 or 5 minute score sprints.
  - **Survival / البقاء** — gravity ramps up every 20 seconds.
  - **Speed Mode / السرعة القصوى** — start at level 8 / 12 / 18.
  - **Puzzle Mode / الألغاز** — 12 handcrafted stages with fixed piece sets and move limits (each verified to have an unobstructed solving corridor).
  - **Zen / الاسترخاء** — calm gravity, no pressure.
  - **Adventure / المغامرة** — 15 sequential stages with goals, rewards and unlock progression (incl. garbage-clear, timed score storm, survival ramp and combo castle).
  - **Practice / الممارسة** — relaxed speed for training.
- **Guideline-style engine** in pure Kotlin: SRS rotation with wall kicks, 7-bag randomizer, hold, ghost piece, 5-piece preview, lock delay with reset cap, T-spin detection with **full vs mini distinction**, back-to-back, combos, perfect clears, guideline speed curve and scoring.
- **Visible event banners** for Tetris / T-Spin / Mini T-Spin / Back-to-Back / Perfect Clear / Combo / Level-up (AR/EN).
- **Local store** — 13 visual themes (Neon Cyber, Galaxy, Aurora, Minimal, Retro Arcade, Crystal, Lava, Ocean, Monochrome, Golden, Nature, Pixel Art + default), 5 block packs, 5 effect packs and 4 sound packs. Bought with locally earned coins; atomic purchase logic can never double-charge. Catalog lives in `assets/store.json` (prices editable without code changes).
- **Progression** — XP/levels, coins, 16 permanent achievements, 3 rotating daily tasks, per-mode best scores, lifetime statistics and a post-round summary with rewards.
- **Audio** — 4 procedurally generated SFX packs + 3 original music loops (menu / play / zen), separate volume controls, auto-pause on background. All files are local WAVs created by `scripts/gen_audio.py` (no licensing risk, no downloads).
- **Bilingual UI** — full Arabic + English with automatic **RTL/LTR** switching, per-app language setting.
- **Comfort options** — dark themes, reduced-motion mode, haptics toggle, per-channel volumes, **optional board gestures** (tap = rotate, drag = move/soft-drop) alongside the always-available button pad.
- **First-run help + result sharing** — a bilingual "How to play" screen (shown once, always in the menu) and one-tap sharing of round results.
- **Offline-first by design** — the manifest requests **no INTERNET permission at all**; nothing can or does go online.

## 🧱 Tech stack / التقنيات

| Layer | Choice |
|---|---|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose (BOM 2024.09.03) + Material 3, Canvas-based playfield |
| Architecture | Single-activity, MVVM (`ViewModel` + `StateFlow`), manual DI container |
| Async | Kotlin Coroutines |
| Persistence | Room 2.6.1 (scores, stats, achievements, purchases, progress) + DataStore 1.1.1 (settings, wallet, session) |
| Audio | SoundPool (SFX) + MediaPlayer (music loops) |
| Build | Gradle 8.9 + Kotlin DSL, AGP 8.5.2, version catalog |
| CI | GitHub Actions (tests + debug APK always, release APK + GitHub Release on `v*` tags) |

Package layout (feature packages as planned):

```
com.tetrisnewgeneration.game
├── engine/        # pure-Kotlin game engine (board, pieces, SRS, scoring, save)
├── modes/         # ModeConfig catalog, puzzles, challenges, adventure stages
├── ui/            # Compose screens, components, view models, navigation
├── audio/         # SfxEngine (SoundPool), MusicEngine (MediaPlayer)
├── themes/        # 12 themes + block/effect pack definitions
├── store/         # StoreItem model + catalog (loaded from assets/store.json)
├── data/          # Room database, DataStore settings, repositories
└── progression/   # achievements + daily task definitions
```

## 🔨 Build requirements / متطلبات البناء

- JDK 17 (Temurin recommended)
- Android SDK: platform **34**, build-tools **34.0.0**
- Android Studio Koala (or newer) *or* plain Gradle 8.9

### Open in Android Studio / الفتح في Android Studio

1. `File → Open` and select the project root.
2. Let Gradle sync (wrapper is included).
3. Run the `app` configuration on a device/emulator (API 24+).

### Command line / سطر الأوامر

```bash
# unit tests
./gradlew :app:testDebugUnitTest

# debug APK  → app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:assembleDebug

# release APK → app/build/outputs/apk/release/app-release.apk
# (signed with the debug key unless TNG_KEYSTORE* env vars are provided)
./gradlew :app:assembleRelease
```

Release signing: define `TNG_KEYSTORE`, `TNG_KEYSTORE_PASSWORD`, `TNG_KEY_ALIAS`, `TNG_KEY_PASSWORD` to sign with your own keystore. Without them the release APK falls back to the debug key so it stays installable — clearly documented, no pretending.

## 🤖 CI/CD (GitHub Actions)

`.github/workflows/android.yml` on every push/PR:

1. Sets up JDK 17 + Android SDK.
2. Runs all unit tests.
3. Builds the debug APK and uploads it as a workflow artifact.
4. On a `v*` tag: builds the release APK, generates an ephemeral keystore when no signing secrets exist, and **publishes a GitHub Release with the APK attached**.

To publish a new version — **version-bump policy (mandatory)**: every fix or upgrade MUST change the version number.

1. Bump `versionCode` (+1, always) and `versionName` in `app/build.gradle.kts`.
2. Add an entry to [CHANGELOG.md](CHANGELOG.md).
3. `git tag vX.Y.Z && git push origin vX.Y.Z` → CI runs tests, builds, and publishes the release with the APK.

A tag that does not match `versionName` fails the build (CI guard). Docs-only changes do not create a release.

## 📚 Project documentation / وثائق المشروع

| Doc / الوثيقة | Purpose / الغرض |
|---|---|
| [CHANGELOG.md](CHANGELOG.md) | Version history + mandatory version-bump policy / سجل التغييرات وسياسة الإصدار الإلزامية |
| [docs/PLAN.md](docs/PLAN.md) | Detailed completion plan with honesty charter & audit baseline / خطة الإتمام وميثاق الأمانة |
| [docs/AUDIT.md](docs/AUDIT.md) | Evidence-frozen audit log incl. clean-env re-verification & APK inspection / سجل التدقيق بالأدلة |
| [docs/FINAL_REPORT.md](docs/FINAL_REPORT.md) | Final report: 12 acceptance criteria × evidence + install guide / التقرير النهائي ومصفوفة القبول |
| [docs/QA_CHECKLIST.md](docs/QA_CHECKLIST.md) | 10-item on-device QA checklist / قائمة ضبط الجودة على جهازك |

## 🧪 Tests / الاختبارات

57 JVM unit tests cover: piece shapes & rotations (SRS + O-piece stability + wall kicks), collision & bounds, line clearing, scoring values (level multiplier, back-to-back, combos, perfect clear), **mini vs full T-spin classification and scoring**, gravity curve, hold semantics, hard/soft drop scoring, ghost piece, top-out/game-over, time-limit win, goal completion, puzzle configs, pause/resume, save/restore round-trip (serialization), store purchases (single debit, duplicate rejection, insufficient funds, free grants), XP progression and **catalog structural validation** (puzzles/adventure).

```bash
./gradlew :app:testDebugUnitTest     # reports in app/build/reports/tests/
```

## 📱 Verified environment / البيئة المُختبرة

- Compiled and unit-tested on JDK 17, AGP 8.5.2, Gradle 8.9.
- APK targets Android 8.0+ (minSdk 24, targetSdk 34); installable and playable fully offline (airplane-mode safe — the app has no network permission to begin with).
- Portrait-first design; the playfield scales across screen sizes. Orientation is locked to portrait in v1 (see limitations).

## ⚠️ Known limitations / قيود معروفة

- Screenshots will be added to this README as they become available.
- Portrait orientation only in v1 (responsive within portrait sizes).
- T-spins are classified as full or mini by the front-corner rule; the rare "TST kick upgrade" (mini promoted to full by the 5th kick index) is not implemented (documented limitation).
- Adventure/Puzzle stages ship with the 15 + 12 built-in levels listed above.
- Sound packs share the same synthesis pipeline with different voicing (sine / square / FM / triangle).
- Release APKs built without signing secrets are debug-signed (installable, documented).

## 📄 Licenses / التراخيص

- **Code**: MIT — see [LICENSE](LICENSE).
- **Audio & icons**: generated by this project's scripts (`scripts/gen_audio.py`, `scripts/gen_icons.py`) — original content, CC0-style dedication within this repo.
- **Trademark notice**: "Tetris" is a registered trademark of The Tetris Company, LLC. This unofficial project uses the name descriptively at its owner's request, implements only uncopyrightable game mechanics, and ships no assets from any official Tetris product. Verify trademark requirements before any public/commercial distribution.

<div dir="rtl">

**تنبيه**: كلمة "Tetris" علامة تجارية مسجلة لشركة The Tetris Company. هذا المشروع غير رسمي ولا يتضمن أي أصول من منتجات Tetris الرسمية؛ تحقق من متطلبات العلامة التجارية قبل أي نشر عام أو تجاري.

</div>
