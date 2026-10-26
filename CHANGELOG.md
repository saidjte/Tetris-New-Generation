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

## [1.5.0] — 2026-10-26

### Added | إضافات
- **«البهاء البصري» — the visual splendor release** (approved from the improvement-proposals document, items V1/V2/V3/V4/V6/V8, chapter 10 exit gates).
- **V1 — Image backgrounds + parallax stars**: the three owner-provided backdrop images are now a full-screen layer behind the game and home screens, tinted toward each of the 13 themes, with a slow two-layer drifting starfield. A new "Image backgrounds" switch restores the plain gradient exactly when off (backdrop mapping is unit-tested for all 13 themes).
- **V2 — Real rotation frames for the T99 skin**: all 28 full-piece sprites from the asset pack are integrated; each frame was verified cell-by-cell against the engine's SRS shapes (frame r+1 == rotation r, box-aligned), so the active piece visibly *turns* instead of teleporting. Includes the honest v1.4.0 fix: the sprite style flag now actually reaches the board cells (locked cells draw their real sprites too).
- **V3 — Hard-drop trail & impact dust**: a fading column-wide trail along the swept path (200 ms, piece colour) plus a 6–10 particle dust puff at the landing cells, both fully disabled under reduced motion.
- **V4 — Visual danger pulse**: while the audio DangerTracker is inside its 15/13 hysteresis window, the board edge and the last free row glow red, scaling with proximity to the top. The visual state is read in the same ViewModel tick as the DANGER/SAVED voice events — audio and visuals flip in the same logical frame (now unit-tested).
- **V6 — Themed board frame**: the asset-pack frame is used for *every* theme (not only T99), tinted per theme so each theme stamps the board border; the Hold container glows with the theme accent while the swap is available, dims to a muted gray once consumed, and the first Next slot glows.
- **V8 — Effects-intensity setting**: Low / Medium / High (×0.5 / ×1 / ×1.5) multiplies particle counts, shake amplitude and glow strength. Default Medium; integrates with (and is dominated by) reduced motion.
- **16 new unit tests** (intensity parsing/clamps, trail curve, dust budget bounds, danger intensity/pulse/period, audio-visual danger sync, rotation-frame naming incl. wrap/clamp safety, 13-theme backdrop coverage): **16 suites / 121 tests** total.

### Changed | تغييرات
- Combo glow and shockwave ring brightness scale with the effects-intensity factor.
- `versionCode` 6 → 7, `versionName` 1.4.0 → 1.5.0.

### Not in this release | خارج هذا الإصدار (صدق كامل)
- Audio track items (A1–A7 + V5) and gameplay/UX tracks — mapped to v1.6.0+ in the approved roadmap (docs: improvement-proposals chapters 8–10).

## [1.4.0] — 2026-10-26

### Added | إضافات
- **«الأصول الحقيقية» — the real assets release.** The owner-provided packs (Google Drive) are now fully integrated: a live announcer, real music, an arcade SFX pack, interface sounds, a sprite skin and a game typography set (see `docs/DEV_PLAN_V1.4.0.md` + `docs/ASSET_CREDITS.md`).
- **Announcer — معلّق صوتي حقيقي**: 275 real voice clips react to the game: clear 1/2/3, Tetris, T-spin, perfect clear, back-to-back, combos (2–8 & 10; **9 stays silent by design** — no real take exists), level-ups, danger + relief (stack-height hysteresis 15/13), score milestones 10K→1M, lines milestones 25→200 (sprint ladder 10/20/30/40 in short goal modes), "finish line in sight", 60/30-second warnings, hurry, time-up, records, wins, game over, pause/resume, zen calm every 90s, idle nudges (12s no input, 30s cooldown), warm-up on countdown, mode-flavored intros (zen/ultra/sprint). Random variant per event with no immediate repeat; big moments interrupt smaller ones; own settings toggle + volume (MediaPlayer on a background thread — zero decoded-PCM memory cost).
- **Real music**: menu theme + in-game "Type A", auto-switching to the intense "Type B" at level 8. Zen keeps its calm loop.
- **T99 Sounds — حزمة أصوات خامسة** (store, 150 coins): 22 real arcade recordings with a distinct sound per clear tier (clear/2/3, tetris) and T-spin tier (tspin/2/3) + hold/danger/relief/record. New engine events (HOLD, TSPIN tiers, CLEAR tiers, TETRIS, DANGER, SAVE, RECORD) fall back gracefully on the four synthesized packs via a pure, tested fallback chain.
- **13 interface sounds** (Steam Deck set): buttons, toggles, tabs, modal open/close, game launch, back — following the global SFX switch.
- **T99 Look — مظهر مرئي سادس** (store, 130 coins): real per-piece sprites (T=blue, Z=cyan, L=green, J=pink, I=purple, O=red, S=yellow) + the real board frame and Hold/Next panel frames from the asset pack, in the playfield and previews.
- **Game typography**: Orbitron (score/timer digits — HUD), Changa (headings AR+EN), Cairo (body AR+EN) — full Material typography wired.
- **New launcher icon** (all densities + adaptive + maskable).
- **16 new unit tests** (voice catalog integrity 275-file mirror, combo silent-nine rule, milestone once-only + consume-on-jump, danger hysteresis, SFX suffix/fallback termination): **15 suites / 105 tests** total.

### Changed | تغييرات
- Music: `music_menu`/`music_play` are now the provided MP3 tracks (the synthesized WAVs retired; zen loop untouched).
- `PieceHeld` now plays a dedicated HOLD sound (T99) instead of the UI click.
- APK grows to ~29MB (all audio bundled locally — still no INTERNET permission, nothing streamed).
- `versionCode` 5 → 6, `versionName` 1.3.0 → 1.4.0.

### Updated docs | توثيق
- `docs/DEV_PLAN_V1.4.0.md` (plan), `docs/ASSET_CREDITS.md` (attribution), README features/credits.

### Not in this release | خارج هذا الإصدار (صدق كامل)
- Image backgrounds as a theme, rotation-variant sprites, dynamic danger-layered music — mapped to v1.5.0.

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
