# PHARMA LIBRARY — Android

تطبيق Android أصلي باستخدام Kotlin وJetpack Compose، بواجهة عربية RTL. المستودع يتضمن الآن مصدر التطبيق المستخرج من النسخة التأسيسية، وGradle Wrapper، وWorkflow لبناء APK Debug.

## الحالة الحالية

- واجهة عربية RTL للصفحة الرئيسية والمكتبة ومنطقة الإدارة.
- هيكل تمهيدي للسنوات والفصول والمواد والأسئلة والملخصات.
- Android Gradle Plugin وKotlin/Compose وAndroid SDK 35.
- Gradle Wrapper 8.11.1 مع JDK 17 في GitHub Actions.
- بناء `assembleDebug` متحقق منه محليًا.
- لا توجد في المستودع الحالي قاعدة بيانات أو إعدادات Supabase أو ملفات محتوى أو نظام دخول منفذ بعد؛ الأقسام غير المتصلة تعرض حالات تمهيدية بدل بيانات وهمية.

## البناء محليًا

```bash
export ANDROID_HOME=/path/to/android-sdk
./gradlew assembleDebug
```

الناتج: `app/build/outputs/apk/debug/app-debug.apk`

## البناء من GitHub باستخدام الهاتف

1. افتح تبويب **Actions** في مستودع GitHub.
2. اختر **Build PHARMA LIBRARY APK** ثم **Run workflow**، أو ادفع تحديثًا إلى `main`.
3. بعد نجاح التشغيل افتح Artifact باسم `pharma-library-debug-apk` ونزّل `app-debug.apk`.
4. فعّل السماح بالتثبيت من هذا المصدر عند الطلب، ثم ثبّت APK على Android.

## ما يلزم لإكمال الوظائف السحابية

يلزم توفير Supabase URL وAnon/Publishable key عبر إعداد آمن، ثم تصميم/تطبيق الجداول والعلاقات وسياسات RLS وStorage والمصادقة وصلاحيات المشرفين وطبقة تنزيل الملفات. يجب عدم وضع `service_role key` أو كلمات المرور داخل التطبيق أو GitHub.
