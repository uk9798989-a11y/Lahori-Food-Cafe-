# LAHORI FOOD CAFE — Setup & Deployment Guide

## 1. Official Business & Menu Verification
- **Business Name:** LAHORI FOOD CAFE (`Lahori Food Cafe` in launcher / Play metadata)
- **Location:** Main Chowk, Sidhu Wali Road, Taranda Muhammad Panah, Punjab, Pakistan
- **Official Phone / WhatsApp:** `0304-3689241` / `+923043689241`
- **Delivery Policy:** Free Delivery (`Minimum Delivery Order: Rs. 700`)
- **Social Channels:** Facebook, Instagram, TikTok — `LAHORI CAFÉ TMP`
- **Admin Verification Items:**
  - **Soft Drinks** (`Regular`, `500 ml`, `1 Liter`, `1.5 Liter`, `Jumbo Liter`, `Small Water`, `Large Water`) did not have printed prices on the supplied flyer and are marked `priceVerified = false` until set by Admin in **Admin Dashboard → Menu**.
  - **XL Pizza Extra Toppings** (`Extra Chicken` / `Extra Cheese` for `XL`) had no printed rate on the pizza flyer (only `S=70`, `M=100`, `L=170` were printed) and are blocked for `XL` until verified.

## 2. Firebase Configuration & Security Rules
- **Database:** Provisioned Cloud Firestore named database configured in `app/src/main/res/values/firebase_applet_config.xml` (`firestore_database_id`) and `app/google-services.json`.
- **Authentication:** Google Sign-In via Android Jetpack `CredentialManager` (`GetSignInWithGoogleOption` / `GetGoogleIdOption`).
- **Admin Access:** Bootstrapped owner email `uk9798989@gmail.com` (requires `email_verified == true` in Firebase Auth token) plus optional manager UIDs in `/admins/{uid}`.
- **Security Rules:** Defined in `firestore.rules`, validated with `@firebase/rules-unit-testing` (`firestore.test.js`) and Robolectric emulator tests (`CafeRepositoryRuleTest`), and deployed via `DeployRules`.

### Optional Backend Extensions (Firebase Console)
1. **Cloud Storage for Remote Product Photos:** Admin currently selects local device photos using the Android Photo Picker (`ActivityResultContracts.PickVisualMedia`, zero storage permissions). To sync custom admin photos across all customer devices, enable Firebase Storage in the Firebase Console and upload picked URIs before saving `Product.imageUrl`.
2. **Firebase Cloud Messaging (FCM) Push Alerts:** When an order is created or updated, the app writes real-time documents to `/notifications` and triggers local notifications while open. To deliver background push alerts when the app is closed, enable FCM and add a Cloud Function trigger on `/orders/{orderId}` and `/notifications/{notificationId}`.

## 3. Building APK & Google Play App Bundle (AAB)
- **Debug APK (Local Testing):**
  ```bash
  gradle :app:assembleDebug
  ```
  Output: `app/build/outputs/apk/debug/app-debug.apk`
- **Signed Release APK / Play Store AAB:**
  Set environment variables before running Gradle release tasks (do **not** commit keystore passwords or use `local.properties`):
  - `KEYSTORE_PATH`: path to upload `.jks` keystore
  - `STORE_PASSWORD`: keystore password
  - `KEY_PASSWORD`: `upload` key alias password
  ```bash
  gradle :app:bundleRelease
  gradle :app:assembleRelease
  ```
  Outputs:
  - `app/build/outputs/bundle/release/app-release.aab` (Google Play Console)
  - `app/build/outputs/apk/release/app-release.apk` (Direct distribution)
