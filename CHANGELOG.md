# Changelog | سجل التغييرات

<div dir="rtl">

سياسة إلزامية: **كل إصلاح أو ترقية يغيّر رقم النسخة.** الحارس الآلي في CI يرفض أي وسم إصدار لا يطابق `versionName` في `app/build.gradle.kts`، فلا يمكن نشر إصدار دون رفع الرقم.

</div>

**Mandatory policy: every fix or upgrade MUST change the version number.** The CI guard rejects any release tag that does not match `versionName` in `app/build.gradle.kts` — no release can be published without a version bump.

## Versioning rules | قواعد الإصدار

| Change type / نوع التغيير | Version bump / رفع النسخة | Example / مثال |
|---|---|---|
| Bug fix / إصلاح خلل | Patch: `v1.0.x` | v1.0.1 → v1.0.2 |
| New feature, mode, theme / ميزة أو نمط أو موضوع جديد | Minor: `v1.x.0` | v1.0.1 → v1.1.0 |
| Breaking change / تغيير جوهري | Major: `v(x+1).0.0` | v1.0.1 → v2.0.0 |
| Docs only / توثيق فقط بلا كود | No release / بلا إصدار | — |

<div dir="rtl">

قواعد ملزمة:

- `versionCode` يزيد 1 وحدة مع **كل** إصدار منشور (شرط أندرويد: تصاعدي دائماً).
- مسار الإصدار: ارفع `versionCode` و`versionName` → أضف سطراً في هذا السجل → التزم → `git tag vX.Y.Z && git push origin vX.Y.Z` → CI يشغّل الاختبارات ويبني وينشر الإصدار مع APK تلقائياً.
- حارس CI: وسم لا يطابق `versionName` يُفشل البناء برسالة خطأ صريحة.
- التغيير التوثيقي الصرف (بلا كود) لا يرفع النسخة ولا يصدر إصداراً جديداً.

</div>

- Release flow: bump `versionCode` + `versionName` → add a CHANGELOG entry → commit → `git tag vX.Y.Z && git push origin vX.Y.Z` → CI runs tests, builds, and publishes the release with the APK automatically.
- CI guard: a tag not matching `versionName` fails the build with a clear error.

## [1.3.0] — 2026-10-26

### Added | إضافات
- **«العرض والصوت» — the presentation & audio release.** The paid effect packs finally render, every moment now looks and sounds the part. All items were mined from a code audit of the project's own spec files (see `docs/DEV_PLAN_V1.3.0.md`).
- **Effect packs now actually visible** (was: purchased packs changed nothing on screen):
  - **Sparks** = long flying rays, **Burst** = circles (previous look), **Trail** = velocity streaks, **Shatter** = tumbling rotating squares, **Confetti** = fluttering ribbons.
  - Particle count now follows the purchased pack's density (with per-moment multipliers for TETRIS / T-SPIN / PERFECT / RECORD / COMBO).
- **Cleared-row beams**: every removed line burns away with a white-hot sweep (~260 ms, ease-out) — powered by a new engine capability.
- **Floating score popups**: "+N" (the actual points earned) pops in, rises and fades on every clear.
- **Screen shake**: hard drop 4 px, T-spin 6 px, Tetris 8 px, perfect clear 10 px — damped sine that always lands back exactly in place (disabled under reduced-motion).
- **Shockwave ring** expands from the board center on Tetris and perfect clear.
- **Combo heat glow**: the board frame warms up orange, growing thicker as the combo chain grows (full heat at combo 5).
- **Ghost piece breathing**: ghost outline alpha breathes 0.14 ↔ 0.34 over ~950 ms (constant under reduced-motion).
- **Round-start countdown**: 3 · 2 · 1 · GO! overlay with its own sounds and haptic ticks before every fresh round (skipped when resuming a saved session; the round opens with the GO flash).
- **Time-warning beep**: timed modes (Time Attack / Quick Play / challenges) beep once in the final 10 seconds.
- **Richer sound synthesis v2** (all 4 packs regenerated): sub-octave body layer + gently detuned partner oscillator per note, upward shimmer sweeps under success arpeggios, sharp noise transients on percussive hits — pack personalities preserved (original = warm sine, arcade = chip square, future = FM bell, calm = soft triangle). Deterministic seeds (no more per-run randomness).
- **2 new events × 4 packs**: `countdown` beep + `go` flash → audio set is now **63 files** (60 SFX + 3 music).
- **Engine capability (B1)**: `LinesCleared` now carries `clearedRows` (pre-collapse indices) with a source-compatible default — the door for row-level visual effects.
- **Pure FX math**: new `ui/fx` package — `FxMath` (shake/beam/ring/popup/heat/ghost curves) + `CountdownSteps`, fully JVM-tested.
- **20 new unit tests** (FxMath curves, countdown structure, engine row-carrying): **13 suites / 89 tests** total.

### Updated docs | توثيق
- `docs/DEV_PLAN_V1.3.0.md` documents the full audit (7 findings), item gates and honest non-goals for this release.

### Not in this release | خارج هذا الإصدار (صدق كامل)
- Dynamic multi-layer music, an "effects intensity" setting, new themes/sound packs — mapped to v1.4.0.

### Changed | تغييرات
- `versionCode` 4 → 5, `versionName` 1.2.0 → 1.3.0.

## [1.2.0] — 2026-10-26

### Changed | تغييرات
- **Touch gestures v2 — full-screen and impossible to miss** (based on real-device feedback that the board-only gestures felt weak):
  - Gesture surface expanded from the board box to the **entire play screen**; buttons, HUD and dialogs keep hit-testing priority so nothing breaks.
  - **Fast flick down = hard drop** (velocity-verified) — the classic slam gesture now exists; slow drag down remains soft drop.
  - **Swipe up = hold** — deliberate upward drag swaps the piece.
  - **Haptic tick on every gesture action** (light for cell moves, strong for rotate/hard-drop/hold) so each gesture is felt, not just seen.
  - Soft drop now starts after ~1 cell of downward travel (was 1.2) and cancels immediately on upward correction.
- **Event banner v2 — bigger, animated, color-coded** (based on real-device feedback that it was barely noticeable):
  - Text enlarged 17sp → **24sp black-weight** with accent letter-spacing, on a near-opaque dark pill readable on any theme.
  - **Color-coded per event** (TETRIS cyan, T-SPIN purple, MINI lilac, B2B orange, PERFECT gold, LEVEL-UP green, RECORD yellow, COMBO sky).
  - **Spring scale-in pop + fade** on entry, smooth shrink-out on exit, plus a gentle infinite pulse while visible (all disabled under reduced-motion).
  - **Duration now scales with the moment**: 1.8s for TETRIS/PERFECT/RECORD, 1.5s for T-SPIN/B2B/LEVEL-UP, 1.3s for MINI/COMBO (was flat 1.2s).
  - **Every kind now confirms with haptics** — LEVEL-UP, COMBO, MINI and B2B previously stayed silent.
- New pure `GestureClassifier` (flick/hold decisions) with **10 JVM unit tests** — total suite now **67 tests**.

### Updated docs | توثيق
- Settings label, "How to play" screen (AR/EN) and README now describe the full-screen gesture scheme.

## [1.1.0] — 2026-10-25

### Added | إضافات
- **Mini T-Spin distinction**: pure `TSpinRules` classification (full vs mini by front-corner rule), separate guideline scoring (mini: 100/200/400 × level; full: 400/800/1200/1600), mini clears count for Back-to-Back, and dedicated `TSPIN_MINI` / `B2B` event banners.
- **Visible event banner** in the game screen: TETRIS / T-SPIN / MINI T-SPIN / Back-to-Back / PERFECT CLEAR / COMBO / LEVEL UP — localized AR/EN, non-blocking, auto-dismisses.
- **Optional board gestures** (Settings): tap = rotate, horizontal drag = move (cell-per-step), drag down = soft drop. The on-screen button pad always remains available as the primary control.
- **"How to play" screen** (AR/EN): goal, controls (buttons + gestures), full scoring table including mini T-spin and B2B — shown once on first launch, always reachable from Home.
- **Share round result** from the summary dialog via the system share sheet (text-only, zero permissions, fully offline).
- **New content**: puzzles 9–12 (hand-verified unobstructed descent corridors) and adventure stages 11–15 — including the first garbage-clear stage, a 60-second score storm, a survival-ramp stage, a combo-target stage and a level-8 finale (12 puzzles / 15 stages total).
- **10 new unit tests**: mini/full T-spin classification + scoring + B2B interplay, and catalog structural validation — total **57 tests**.

### Changed | تغييرات
- `versionCode` 2 → 3, `versionName` 1.0.1 → 1.1.0.

## [1.0.1] — 2026-10-25

### Added | إضافات
- Versioning policy documented in this changelog + CI guard step that rejects release tags not matching `versionName` (enforcement of the "every fix/upgrade bumps the version" rule).

### Changed | تغييرات
- `versionCode` 1 → 2, `versionName` 1.0.0 → 1.0.1 (first application of the policy; build-system upgrade).

## [1.0.0] — 2026-10-25

### Added | إضافات
- Initial release: pure-Kotlin guideline engine (SRS rotation + wall kicks, O-piece stability, 7-bag, hold, ghost, 5-piece preview, lock delay, T-spin, back-to-back, combos, perfect clears, guideline speed/scoring).
- 11 real game modes: Classic, Marathon, Quick Play, 8 Challenges, Time Attack (1/3/5 min), Survival, Speed (8/12/18), 8 Puzzles, Zen, 10-stage Adventure, Practice.
- Local store: 12+ themes, 5 block packs, 5 effect packs, 4 sound packs; atomic purchase logic (no double-charge); catalog in `assets/store.json`.
- Progression: XP/levels, coins, 16 achievements, 3 daily tasks, per-mode best scores and stats, post-round summary.
- Bilingual Arabic/English UI with RTL/LTR switching, dark mode, reduced-motion option.
- 55 original procedurally generated audio assets (4 SFX packs × 13 events + 3 music loops).
- 47 green unit tests; fully offline (no INTERNET permission); Room + DataStore persistence.
