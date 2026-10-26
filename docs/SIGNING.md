# Stable Release Signing | سياسة التوقيع الثابت للإصدارات

<div dir="rtl">

هذه الوثيقة تثبّت سياسة توقيع إصدارات **Tetris New Generation** بعد التوجيه: «حافظ على نفس التوقيع release» (v1.6.1، 2026-10-26). الهدف: أن يُثبَّت كل تحديث مباشرة فوق النسخة المثبتة على الهاتف دون إزالة تثبيت ودون فقدان بيانات اللعب.

</div>

This document fixes the release-signing policy for **Tetris New Generation** following the directive: *«keep the same release signature»* (v1.6.1, 2026-10-26). Goal: every future update installs directly over the installed version — no uninstall, no lost progress.

---

## 1. The problem we are closing | المشكلة التي نغلقها

<div dir="rtl">

قبل v1.6.1 كان توقيع كل إصدار **مؤقتًا مختلفًا**:

- **محليًا**: كان البناء يستخدم `~/.android/debug.keystore` الذي يُعاد توليده تلقائيًا بعد كل إعادة ضبط لبيئة العمل، فتغيرت البصمة بين الجلسات (موثَّق في `docs/AUDIT.md` §10 منذ v1.0.0).
- **في CI**: كان الـ workflow يولّد keystore عشوائيًا جديدًا مع كل وسم إصدار («Generate ephemeral release keystore»).

النتيجة العملية: أندرويد يرفض تثبيت تحديث فوق نسخة مثبتة بتوقيع مختلف («لم يتم تثبيت التطبيق»)، والحل الوحيد حينها إزالة التثبيت — وبها تُفقد النقاط والإنجازات المحفوظة محليًا.

مراجعة أمانة لأواخر الإصدارات: مفتاحا v1.4.0 وv1.5.0 وv1.6.0 **ضاعا نهائيًا** (ولدتا على مشغّلات CI مؤقتة، ولا يوجد أي نسخة من المخزن في أي مكان — تم التحقق بالبحث الشامل وبتفقّد أسرار GitHub الفارغة). لذلك كان انتقال واحد أخير بإزالة تثبيت unavoidable للوصول إلى التوقيع الثابت.

</div>

Before v1.6.1 every release signature was **ephemeral and different**: local builds used `~/.android/debug.keystore`, regenerated after every sandbox reset (documented since v1.0.0 in `docs/AUDIT.md` §10), and CI generated a fresh random keystore per tag. Android therefore refused to update an installed copy signed differently, forcing an uninstall that wipes local progress.

Honest review of recent releases: the keys of v1.4.0 / v1.5.0 / v1.6.0 are **permanently lost** (CI-runner ephemeral keystores; an exhaustive filesystem search plus the empty GitHub secrets list confirm no copy exists anywhere). One final uninstall was therefore unavoidable to land on the stable key.

## 2. The permanent key | المفتاح الدائم

| Property / الخاصية | Value / القيمة |
|---|---|
| Keystore file / ملف المخزن | `tng-release.keystore` |
| Key algorithm / الخوارزمية | RSA 2048, SHA256withRSA |
| Alias / الاسم المستعار | `tng` |
| Certificate DN | `CN=Tetris New Generation, OU=Release, O=TNG, C=MA` |
| Validity / الصلاحية | 10,950 days (30 years) — until 2056-10-18 |
| **SHA-256 fingerprint / البصمة** | `D3:35:A7:AB:1D:8F:59:CF:B6:E3:B8:66:2A:B9:1F:F8:F5:9C:D4:59:61:A7:B6:AE:86:7B:7C:1F:FA:02:F0:C6` |
| First used in / أول استخدام | v1.6.1 (versionCode 9) |

<div dir="rtl">

كلمة مرور المخزن والمفتاح واحدة، **لا تُكتب في هذا المستودع العام إطلاقًا** — تُحفظ في أسرار GitHub وفي ملف البيئة المحلي وفي نسخة المالك الاحتياطية (§3، §6).

</div>

The store and key passwords are identical and are **never written to this public repository** — they live only in GitHub secrets, the local env file, and the owner's backup (§3, §6).

## 3. Where the keystore lives | مواضع حفظ المخزن

<div dir="rtl">

المخزن موجود في **ثلاثة مواضع مستقلة** (تكرار مقصود ضد ضياع المفتاح مجددًا):

1. **أسرار GitHub Actions** (المصدر الرسمي لبناء CI):
   - `TNG_KEYSTORE_B64` — الملف كاملاً بترميز Base64.
   - `TNG_KEYSTORE_PASSWORD` و `TNG_KEY_ALIAS` و `TNG_KEY_PASSWORD`.
2. **نسخة محلية**: `/home/z/my-project/keystore/tng-release.keystore` مع بيانات البيئة في `tng-release.env` (صلاحيات 0600، خارج المستودع).
3. **نسخة المالك الاحتياطية**: أرشيف `TNG-release-keystore-backup.zip` في مجلد التسليم — **المالك يحتفظ به في مكان آمن خارج الأجهزة المؤقتة**.

قاعدة صارمة: يُمنع وضع ملف المخزن أو كلمة المرور داخل مستودع GitHub العام.

</div>

The keystore exists in **three independent places** (deliberate redundancy so the key can never be lost again):

1. **GitHub Actions secrets** (official source for CI builds): `TNG_KEYSTORE_B64` + `TNG_KEYSTORE_PASSWORD` / `TNG_KEY_ALIAS` / `TNG_KEY_PASSWORD`.
2. **Local copy**: `/home/z/my-project/keystore/tng-release.keystore` with the env values in `tng-release.env` (0600, outside the repo).
3. **Owner's backup**: `TNG-release-keystore-backup.zip` in the deliverables folder — the owner should keep it somewhere safe and durable.

Hard rule: the keystore file or its password must never be committed to the public repository.

## 4. How CI signs a release | كيف يوقّع CI الإصدار

<div dir="rtl">

في `.github/workflows/android.yml`، مسار الوسوم `v*` يمر بثلاث خطوات إلزامية بعد الاختبارات:

1. **استرجاع المخزن**: فك ترميز `TNG_KEYSTORE_B64` إلى `/tmp/tng-release.keystore` وضبط متغيرات البيئة. إن غاب السر يعود التوليد المؤقت القديم مع تحذير `::warning::` صريح.
2. **بناء APK**: `gradle :app:assembleRelease` —Gradle يوقّع تلقائيًا لأن `TNG_KEYSTORE` مضبوط (انظر §5).
3. **بوابة البصمة (fingerprint gate)**: تُقرأ شهادة APK الناتج بـ `apksigner verify --print-certs`، ويُطابق SHA-256 الخاص بها مع بصمة المفتاح الدائم. **عدم التطابق يُفشل الإصدار** بخطأ صريح — لا يمكن مبدئيًا نشر إصدار بتوقيع مختلف.

</div>

In `.github/workflows/android.yml` the `v*` tag path enforces three mandatory steps after tests:

1. **Restore keystore**: decode `TNG_KEYSTORE_B64` to `/tmp/tng-release.keystore` and export the signing variables. If the secret is missing, the workflow falls back to the old ephemeral generation with an explicit `::warning::`.
2. **Build APK**: `gradle :app:assembleRelease` — Gradle signs automatically because `TNG_KEYSTORE` is set (see §5).
3. **Fingerprint gate**: the produced APK's certificate is read with `apksigner verify --print-certs` and its SHA-256 must match the permanent fingerprint. **A mismatch fails the release** with a clear error — publishing a differently-signed release is now structurally impossible.

## 5. Building locally with the stable key | البناء المحلي بالمفتاح الثابت

```bash
# one-time per shell — values from the local env file (never committed)
set -a; source /home/z/my-project/keystore/tng-release.env; set +a
export TNG_KEYSTORE=/home/z/my-project/keystore/tng-release.keystore

gradle :app:assembleRelease
# verify the fingerprint matches §2
$ANDROID_HOME/build-tools/34.0.0/apksigner verify --print-certs \
  app/build/outputs/apk/release/app-release.apk
```

<div dir="rtl">

آلية Gradle قائمة منذ v1.0: وجود `TNG_KEYSTORE` يفعّل signingConfig المسمّى `ci` ويوقّع به إصدار release؛ غيابه يرجع إلى مفتاح debug المحلي (للبناء التجريبي فقط — لا تنشر منه إصدارًا).

</div>

The Gradle mechanism has existed since v1.0: setting `TNG_KEYSTORE` activates the `ci` signingConfig for release builds; without it, release falls back to the local debug key (fine for debugging — never publish from it).

## 6. Owner checklist | قائمة المالك

<div dir="rtl">

1. **احتفظ** بـ `TNG-release-keystore-backup.zip` في مكان دائم (تخزين شخصي، بريد، قرص خارجي). إن ضاعت الأسرار من GitHub وضاعت النسخة المحلية، فهذه النسخة هي الوحيدة القادرة على توقيع تحديثات مستقبلية متوافقة.
2. **لا تحذف** الأسرار الأربعة من إعدادات Actions في المستودع.
3. عند إنشاء مخزن بديل في أي مستقبل: هذا يعني تغيير التوقيع، ويستلزم من كل مستخدم إزالة تثبيت واحدة — تجنّبه ما أمكن.
4. التحقق السريع من أي APK منشور: `apksigner verify --print-certs <apk>` ثم قارن البصمة مع §2.

</div>

1. **Keep** `TNG-release-keystore-backup.zip` in durable storage. If the GitHub secrets and the local copy were ever lost, that archive is the only remaining way to sign compatible updates.
2. **Never delete** the four Actions secrets from the repository settings.
3. If a replacement keystore is ever created, the signature changes and every user needs one uninstall — avoid it whenever possible.
4. Quick check of any published APK: `apksigner verify --print-certs <apk>`, then compare the SHA-256 with §2.

## 7. Evidence for v1.6.1 | أدلة v1.6.1

- Keystore generated 2026-10-26 08:27 UTC, RSA-2048, validity 10,950 days; fingerprint `D3:35:A7:AB:…:02:F0:C6` (§2).
- Secrets `TNG_KEYSTORE_B64`, `TNG_KEYSTORE_PASSWORD`, `TNG_KEY_ALIAS`, `TNG_KEY_PASSWORD` uploaded and listed back via the GitHub API on the same day.
- Local release build signed with the permanent key, fingerprint re-verified with `apksigner` before push; CI fingerprint gate reproduces the same check on the runner.
- v1.6.0 delivered APK verified as signed by `CN=Tetris New Generation, OU=CI, O=TNG` (ephemeral) — confirming the loss analysis above.
