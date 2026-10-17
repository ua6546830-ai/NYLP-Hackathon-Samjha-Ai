# Samjha? Rx (سمجھا؟) — GitHub & Cloud Deployment Guide

This guide provides end-to-end instructions for deploying **Samjha? Rx**: pushing the codebase to GitHub, setting up automated CI/CD workflows, configuring API secrets, and building release packages for distribution on Android / Google Play Store.

---

## 📦 What's Inside This Repository

- **Android Jetpack Compose Source (`app/src/main/`):** Full native Android application with Material 3 editorial glassmorphic styling.
- **Multilingual Support:** English, Roman Urdu (*"Subah aur shaam khaney ke baad"*), and native Urdu Nastaliq (RTL).
- **CameraX Engine (`ui/components/CameraXCaptureView.kt`):** Zero-lag prescription label capture with viewfinder reticle.
- **On-Device Encrypted Room DB (`data/local/`):** SQLite persistence (`samjha_rx.db`) excluded from cloud backups for patient privacy.
- **Medication Reminder Scheduler (`notification/`):** AlarmManager exact alarms with rich notifications and meal timing icons.
- **Clinical Rules Engine (`engine/ClinicalRulesEngine.kt`):** Deterministic, versioned safety rules citing BNF 84, PNF, and FDA.
- **Pharmacist Workflow (`ui/screens/PharmacistScreen.kt` & `PharmacistChatScreen.kt`):** SBAR query builder and digital sign-off.
- **Pitch Deck Presentation (`pitch/Samjha_Rx_Pitch_Deck.pptx`):** 10-slide PowerPoint presentation ready for investor/hackathon pitching.
- **Distribution Archive (`samjha_rx_github_ready_package.zip`):** Standalone clean zip package ready to share or clone.

---

## 🚀 Quick Start: Pushing to GitHub

### Option A: Automated One-Line Script (Recommended)

Run the included push helper script:
```bash
./push_to_github.sh
```
This script will:
1. Initialize the local Git repository and create the `main` branch.
2. Package the latest pitch deck and distribution zip.
3. Stage all source files and commit with a standard release message.
4. Prompt for your GitHub repository URL and push automatically.

---

### Option B: Manual Git Commands

1. **Create a new empty repository on GitHub:**
   - Go to [github.com/new](https://github.com/new).
   - Name it `samjha-rx-android` (or your preferred name).
   - Do **not** initialize with a README, .gitignore, or license (they are already included).

2. **Initialize and push from your terminal:**
   ```bash
   # Initialize repository
   git init -b main

   # Add all files
   git add .

   # Commit
   git commit -m "feat: initial commit for Samjha? Rx Android application"

   # Connect to your GitHub repository
   git remote add origin https://github.com/YOUR_USERNAME/samjha-rx-android.git

   # Push to main
   git push -u origin main
   ```

---

## ⚙️ Configuring GitHub Repository Secrets

The app utilizes the **Secrets Gradle Plugin** to securely read `GEMINI_API_KEY` at build time without committing keys to public source control.

1. In your GitHub repository, navigate to:
   **Settings** > **Secrets and variables** > **Actions** > **New repository secret**.
2. Add the following secret:
   - **Name:** `GEMINI_API_KEY`
   - **Value:** Your Google AI Studio Gemini API Key.
3. *(Optional for Release Signing)* Add keystore secrets:
   - `KEYSTORE_BASE64`: Base64 encoded upload keystore.
   - `STORE_PASSWORD`: Keystore password.
   - `KEY_PASSWORD`: Key alias password.

---

## 🤖 Automated CI/CD Pipeline (GitHub Actions)

A pre-configured GitHub Actions workflow is located at `.github/workflows/android.yml`.

### What the Pipeline Does:
1. **On Push / Pull Request to `main`:**
   - Checks out the repository.
   - Installs Java 17 Temurin & Android SDK 36.
   - Injects `GEMINI_API_KEY` from repository secrets into `.env`.
   - Runs all unit and Robolectric tests (`./gradlew testDebugUnitTest`).
   - Builds debug APK (`./gradlew assembleDebug`).
   - Uploads `app-debug.apk` as a downloadable GitHub Actions artifact.
2. **On Tag Release (`v*.*.*`):**
   - Automatically generates a GitHub Release.
   - Attaches `app-debug.apk` and `pitch/Samjha_Rx_Pitch_Deck.pptx` for instant download.

To trigger an automated release:
```bash
git tag v1.0.0
git push origin v1.0.0
```

---

## 📱 Building Locally with Android Studio

1. Open Android Studio (Ladybug or newer).
2. Select **Open** and choose the project root directory.
3. Wait for Gradle sync to complete.
4. Run the app on an Android device or emulator running API 24 to 36.

---

## 🛡️ Building a Signed Release APK / AAB for Google Play

To distribute on the Google Play Store:

1. **Build an Android App Bundle (AAB):**
   ```bash
   ./gradlew :app:bundleRelease
   ```
   Output location: `app/build/outputs/bundle/release/app-release.aab`

2. **Build a Standalone Release APK:**
   ```bash
   ./gradlew :app:assembleRelease
   ```
   Output location: `app/build/outputs/apk/release/app-release.apk`

3. **Google Play Console Upload Steps:**
   - Log in to [play.google.com/console](https://play.google.com/console).
   - Create an app listing:
     - **Title:** `Samjha? Rx`
     - **Short Description:** `Make your prescription make sense with clinical safety checks and Urdu translation.`
     - **Category:** Medical / Health & Fitness.
   - Under **Release > Testing > Internal testing**, upload `app-release.aab`.
   - Complete the Data Safety questionnaire (Zero advertising trackers, on-device local storage, health data not shared with third parties).
