# Samjha? Rx (سمجھا؟)

**Make your prescription make sense • نسخہ سمجھیں، بے فکری سے صحت مند رہیں**

[![Android CI/CD](https://github.com/placeholder/samjha-rx-android/actions/workflows/android.yml/badge.svg)](https://github.com)
[![Platform](https://img.shields.io/badge/Platform-Android%2024%2B-brightgreen.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Kotlin-2.0-blue.svg)](https://kotlinlang.org)
[![UI Framework](https://img.shields.io/badge/Jetpack%20Compose-M3%20Editorial-indigo.svg)](https://developer.android.com/jetpack/compose)
[![Clinical Standard](https://img.shields.io/badge/Clinical%20Standards-BNF%2084%20%7C%20PNF-teal.svg)](https://www.bnf.org)
[![License](https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg)](LICENSE)

Samjha? Rx is a patient-first prescription-understanding Android application with a deterministic clinical safety engine, a dedicated pharmacist review workspace, multilingual support (English, Roman Urdu, and Urdu Nastaliq RTL), CameraX high-resolution label capture, on-device Room database persistence, and automated medication reminder notifications.

---

## 🚀 Quick Start: Pushing to GitHub & Deploying

To push this entire repository to your GitHub account in seconds:

```bash
# 1. Run the included deployment helper script:
./push_to_github.sh
```

Or manually:
```bash
git init -b main
git add .
git commit -m "Release: Samjha? Rx v1.0.0"
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git
git push -u origin main
```

A complete step-by-step guide is available at **[docs/DEPLOYMENT_GUIDE.md](docs/DEPLOYMENT_GUIDE.md)**, detailing GitHub Actions CI/CD setup, API secrets, and Google Play Store release tracks.

---

## 📊 Marketing Pitch Deck & Downloadable Package

- **PowerPoint Pitch Deck:** Located at `pitch/Samjha_Rx_Pitch_Deck.pptx` (10 professionally designed presentation slides ready for investors and hackathon presentations).
- **Pitch Deck Speaker Notes:** [pitch/SPEAKER_NOTES.md](pitch/SPEAKER_NOTES.md) with word-for-word scripts and timings.
- **Standalone Download Package:** `samjha_rx_github_ready_package.zip` (clean archive containing source code, documentation, pitch deck, and CI/CD workflows, excluding local build caches).
- **Regenerate Deck & Zip Anytime:**
  ```bash
  python3 scripts/generate_pitch_deck.py
  python3 scripts/package_github_release.py
  ```

---

## 🌟 Core Architectural Features

### 1. Patient Prescription Understanding & Intake
- **Zero-Lag CameraX Capture:** Custom Jetpack Compose camera interface (`CameraXCaptureView.kt`) with live reticle guide, flash toggle, and high-resolution photo analysis.
- **Android Photo Picker:** Native zero-permission image picker (`PickVisualMedia`).
- **4 Synthetic Clinical Scenarios:** Pre-loaded test cases for acute infection (Augmentin), chronic cardiometabolic (Glucophage/Lipitor), antibiotic chelation (Ciprofloxacin), and ambiguous handwriting.
- **Vernacular Multilingual Engine:**
  - **English:** Clinically accurate plain language.
  - **Roman Urdu:** Everyday phonetic phrasing (*"Subah aur shaam khaney ke baad lein"*).
  - **Urdu Script (اردو):** Native RTL Arabic-script layout with Nastaliq typography.

### 2. Human-in-the-Loop Uncertainty Gate
- **Scientific Humility Contract:** Missing or ambiguous critical fields (strength, dose, frequency, duration) stay marked as unresolved. The app **never** guesses or hallucinates missing medical information.
- **Schedule Lockout:** The 24-hour dosage schedule remains locked until critical uncertainties are confirmed by a human or escalated to a pharmacist.

### 3. Traceable Deterministic Clinical Rules Engine
- **Separation of Concerns:** Google Gemini 3.8 Flash assists with OCR transcription and language translation, but is **never the clinical authority**.
- **Evidence-Based Rules:** Strict deterministic rules benchmarked against:
  - British National Formulary (BNF 84)
  - Pakistan National Formulary (PNF)
  - FDA Drug Safety Communications
  - WHO Antimicrobial Stewardship Guidelines
- **Rule Outcomes:** `NO_ISSUES_FOUND` (never misleads with "approved"), `FLAG`, or `UNABLE_TO_CHECK`.

### 4. Medication Reminder Scheduler (AlarmManager)
- **Exact Alarms:** Schedules local alarms based on extracted prescription frequency (`notification/MedicationNotificationScheduler.kt`).
- **Rich Notifications:** Displays medicine name, dose, meal timing icons (Before/After Food), and direct action buttons.
- **Battery Optimization:** Uses Android `AlarmManager.setExactAndAllowWhileIdle()` to guarantee timely dosing without draining battery.

### 5. On-Device Room Database Persistence
- **Local SQLite DB:** Android Jetpack Room (`data/local/AppDatabase.kt`) storing prescription entities, review timestamps, and clinical findings.
- **Cloud Backup Exclusion:** Explicitly excluded from Android cloud auto-backups via `data_extraction_rules.xml` to protect sensitive health records.
- **PIN Security Lock:** 4-digit device PIN lock preventing casual shoulder-surfing.

### 6. Pharmacist Clinical Review & SBAR Query Builder
- **Pharmacist Mode:** Dedicated clinical route displaying rule audit IDs, evidence citations, and missing parameter flags.
- **SBAR Physician Query Generator:** Formats structured Situation-Background-Assessment-Recommendation notes for prescribers.
- **Digital Sign-Off:** Records licensed pharmacist review with timestamp.

---

## 🛠️ Technology Stack

| Layer | Technologies |
|---|---|
| **Language & Architecture** | 100% Kotlin 2.0, MVVM, Clean Architecture, Kotlin Coroutines, StateFlow |
| **UI & Theming** | Jetpack Compose, Material Design 3, Glassmorphism, Accompanist |
| **Camera & Image** | Android CameraX (Core, Camera2, Lifecycle, View), Coil Compose |
| **Local Persistence** | Android Jetpack Room (Entity, DAO, Database), SharedPreferences |
| **Notifications** | Android AlarmManager, BroadcastReceiver, NotificationCompat |
| **AI Intelligence** | Google Gemini 3.8 Flash REST API with graceful offline fallback |
| **CI/CD & DevOps** | GitHub Actions (`.github/workflows/android.yml`), Python packaging scripts |

---

## 🧪 Testing

Run standard unit and Robolectric tests:
```bash
gradle :app:testDebugUnitTest
```

Compile and build debug APK:
```bash
gradle :app:assembleDebug
```

---

## 📄 License & Clinical Disclaimer

This software is distributed under the Apache License 2.0.  
*Clinical Disclaimer: Samjha? Rx contains prototype safety rules and demonstration logic. It is not a certified medical device and does not provide formal medical diagnoses or prescriptions. Always consult a licensed physician or pharmacist.*
