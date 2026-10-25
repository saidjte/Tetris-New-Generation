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
