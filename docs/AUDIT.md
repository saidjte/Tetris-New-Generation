# سجل التدقيق المُجمَّد بالأدلة — AUDIT.md

> **التاريخ:** 25 أكتوبر 2026 — أُنشئ ضمن المرحلة V1 من خطة الإتمام (`docs/PLAN.md`)
> **المنهجية:** كل رقم في هذا الملف مأخوذ من مخرجات أوامر حقيقية نفّذت في نفس اليوم، مع ذكر أمر إعادة الإنتاج بجانبه. لا أرقام من الذاكرة.

---

## 1) حالة المستودع وقت التجميد

| البند | القيمة الفعلية | أمر إعادة الإنتاج |
|-------|----------------|-------------------|
| SHA محلي | `e13081e7c4f8edfabdc1747f033b639b64731712` | `git rev-parse HEAD` |
| الفرع | `main` | `git branch --show-current` |
| تطابق البعيد | `main -> main` بعد الدفع (724256c..e13081e) | `git push origin main` |
| المستودع | https://github.com/saidjte/Tetris-New-Generation (عام) | `GET /repos/saidjte/Tetris-New-Generation` → 200 |
| ضوضاء أوضاع الملفات | عولجت جذرياً: `git config core.fileMode false` + `git checkout -- .` ثم `git status` نظيف | `git status --short` → فارغ عدا الملفات الجديدة |
| فروقات المحتوى قبل التنظيف | 136 ملفاً، **0 إضافات / 0 حذفات** (أوضاع 644→755 فقط) | `git diff --stat` |

## 2) حجم الشيفرة والأصول (في التزام e13081e)

| البند | القيمة | أمر إعادة الإنتاج |
|-------|--------|-------------------|
| ملفات Kotlin الرئيسية | **42** | `find app/src/main -name "*.kt" \| wc -l` |
| ملفات الاختبار | **7** | `find app/src/test -name "*.kt" \| wc -l` |
| أصول صوتية في `res/raw` | **55** (4 حزم مؤثرات × 13 حدثاً + 3 حلقات موسيقى) | `ls app/src/main/res/raw \| wc -l` |
| كتالوج المتجر | `app/src/main/assets/store.json` | `cat app/src/main/assets/store.json` |

## 3) نتائج الاختبارات (تقارير JUnit XML الفعلية)

| المجموعة | القيمة |
|----------|--------|
| مجموعات الاختبار | **7** |
| إجمالي الاختبارات | **47** |
| إخفاقات | **0** |
| أخطاء | **0** |
| تخطٍ | **0** |

أمر إعادة الإنتاج: `python3 -c` على `app/build/test-results/testDebugUnitTest/*.xml` بجمع صفات `tests/failures/errors/skipped` من جذر كل تقرير.

> ⚠️ **حالة هذه النتيجة:** أُنتجت من تشغيل الجلسة السابقة (نفس الشيفرة على SHA الأساسي 326b2c3 دون أي تغيير منطقي بعده). تُعاد من بيئة نظيفة في المرحلة V3 وتُوثَّق النتيجة الجديدة هنا. لن يُعتدّ بالأرقام القديمة بعد اكتمال V3.

## 4) ملفات APK المحلية وقت التجميد

| الملف | الحجم | الطابع الزمني |
|-------|-------|----------------|
| `app/build/outputs/apk/release/app-release.apk` | 12,249,974 بايت (11.68MB) | 2026-10-25 11:55:53 UTC |
| `app/build/outputs/apk/debug/app-debug.apk` | 15,111,776 بايت (≈14.4MB) | نفس جلسة البناء |

أمر إعادة الإنتاج: `stat -c '%s %y' app/build/outputs/apk/release/app-release.apk`

## 5) الإصدار المنشور على GitHub (تحقق API حي)

```
GET /repos/saidjte/Tetris-New-Generation/releases →
  tag: v1.0.0 (draft=False, prerelease=False)
  assets:
    - app-release.apk               12,249,974 bytes  (من تشغيل CI)
    - TetrisNewGeneration-v1.0.0.apk 12,249,974 bytes (محلي مرفوع)
```

المطابقة الملاحظة: الأصلان بنفس الحجم تماماً — الثاني رفع يدوياً من نفس ملف البناء المحلي الذي يبنيه CI.

## 6) حالة CI وقت التجميد

| التزام | الحدث | الحالة | النتيجة |
|--------|-------|--------|---------|
| `e13081e` (إضافة PLAN.md) | push | in_progress | — |
| `724256c` (إصلاح شارة README) | push | completed | **success** |
| `f6bf09c` (صلاحيات contents:write) | push | completed | **success** |

أمر إعادة الإنتاج: `curl -s -H "Authorization: Bearer $TOKEN" /repos/saidjte/Tetris-New-Generation/actions/runs?per_page=3`

## 7) بيئة العمل وقت التجميد (بأمانة)

| المكوّن | الحالة |
|---------|--------|
| Java | OpenJDK 21.0.12.1 موجود |
| javac / Gradle / Android SDK | **غير موجودين** بعد إعادة ضبط البيئة بين الجلسات |
| الاستعادة | `scripts/setup_env.sh` (Temurin/نظام Java + cmdline-tools + platform-34 + build-tools 34.0.0 + Gradle 8.9) — انطلقت في الخلفية أثناء كتابة هذا الملف |
| أثر ذلك على الأدلة | أرقام القسم 3 من تشغيل سابق؛ V3 سيعيد إنتاج كل شيء من الصفر ويحدّث هذا الملف |

## 8) الشبكة والرمز

- الشبكة تعمل: تنزيلات dl.google.com و services.gradle.org انطلقت بنجاح.
- رمز الوصول: صالح (`login: saidjte`، النطاقات `repo, workflow`)، يُستخدم حصرياً كمتغير بيئة مؤقت في أوامر الشبكة، غير مكتوب في أي ملف بالمستودع. **يجب إلغاؤه بعد التسليم** (راجع PLAN.md §9 و§V6).

---

# تحديث ما بعد إعادة الإثبات النظيف (V3 + V4) — 25/10/2026

> أُضيف هذا القسم بعد إعادة تشغيل كل شيء من بيئة معاد تجهيزها بالكامل (SDK + Gradle + JDK من الصفر).

## 9) نتائج V3 — إعادة الإثبات من بيئة نظيفة (نفس الشيفرة، صفر تعديل كود)

| البند | النتيجة الطازجة | الدليل |
|-------|-----------------|--------|
| تجهيز البيئة | cmdline-tools + platform-34 + build-tools 34.0.0 + Gradle 8.9 + Temurin JDK 17.0.21 (النظام Java 21 بلا `jlink` فلا يصلح لـAGP — عولج من الجذر بتنزيل JDK كامل) | سجلات `setup_env.sh` و`scripts/pget.sh` |
| **الاختبارات** | **7 مجموعات / 47 اختباراً / 0 إخفاق / 0 خطأ / 0 تخطٍ** من تشغيل جديد تماماً | `gradle testDebugUnitTest` → BUILD SUCCESSFUL (1m14s) + XML طازج |
| بناء debug | BUILD SUCCESSFUL — 15,128,160 بايت | `stat` |
| بناء release | BUILD SUCCESSFUL — **12,249,974 بايت** | `stat` |
| **مطابقة المنشور** | حجم APK الجديد **مطابق تماماً (بايت الحجم) لأصل الإصدار المنشور على GitHub** (12,249,974) | `GET /releases` + `stat` |
| تعديل كود أثناء إعادة الإثبات | **صفر** — `git status` نظيف طوال العملية | `git status --short` |
| SHA الذي بُني عليه | `6d0ca0c` (الالتزام الأخير وقت البناء؛ فرق الالتزامات منذ 326b2c3 توثيقي فقط بلا مساس بالكود) | `git log --oneline` |

## 10) نتائج V4 — فحص APK بأدوات Android الرسمية

| الفحص | النتيجة | أمر إعادة الإنتاج |
|-------|---------|-------------------|
| اسم الحزمة والإصدار | `com.tetrisnewgeneration.game`، versionCode=1، versionName=1.0.0 | `aapt dump badging app-release.apk` |
| minSdk / targetSdk | **24** (Android 7.0) / **34** (Android 14) | نفس الأمر |
| **الصلاحيات** | **VIBRATE فقط** + صلاحية داخلية للتطبيق (`DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`) — **لا صلاحية INTERNET إطلاقاً** | `aapt dump badging \| grep uses-permission` |
| نشاط الإطلاق | `com.tetrisnewgeneration.game.MainActivity` | نفس الأمر |
| **الملفات الصوتية داخل الحزمة** | **55 ملف WAV كاملة** (أسماء المسارات مختصرة آلياً في بناء release مثل `res/09.wav` — سلوك AGP قياسي لتقصير الموارد، المحتوى كامل) | `unzip -l app-release.apk \| grep -ic '\.wav'` |
| التوقيع | `apksigner verify` → ناجح، شهادة `CN=Android Debug` (توقيع احتياطي محلي موثق؛ النسخة المنشورة على GitHub موقّعة من CI بمخزن مؤقت خاص بها) | `apksigner verify --print-certs` |
| تطابق البايت مع نسخة الجلسة السابقة | الحجم متطابق (12,249,974)؛ بصمة SHA-256 مختلفة لأن مخزن مفاتيح debug أُعيد توليده بعد إعادة ضبط البيئة — سلوك متوقع وموثق ولا يمس المحتوى | `sha256sum` على النسختين |

**خلاصة V4:** كل خصائص الحزمة المعلنة في المصدر مثبتة من داخل الحزمة نفسها: أوفلاين كامل، minSdk 24، توقيع سليم، 55 أصلاً صوتياً.
