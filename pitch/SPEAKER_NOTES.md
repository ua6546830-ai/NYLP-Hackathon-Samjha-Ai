# Samjha? Rx (سمجھا؟) — Marketing Pitch Deck Speaker Notes & Guide

**Presentation Title:** Samjha? Rx: AI Prescription Intelligence & Pharmacist Assistance Platform  
**Target Audience:** Healthcare investors, Google AI Hackathon judges, pharmacy chain executives, clinical directors  
**Format:** 10-Slide Deck (`pitch/Samjha_Rx_Pitch_Deck.pptx`) | 3 to 5 minutes duration  
**Founder:** Uzair Ahmad (Pharm.D Candidate, FUUAST Karachi)

---

## Slide 1: Title & Hook (Time: 0:00 - 0:30)
- **Visual:** Minimalist glassmorphic title card, indigo gradient, dual-script branding (*Samjha? Rx • سمجھا؟*).
- **Speaker Script:**
  > "Good morning/afternoon, judges and esteemed guests. Understanding a doctor's prescription shouldn't require a medical degree. Yet today, millions of families in bilingual healthcare markets walk out of clinics clutching scraps of paper covered in Latin abbreviations they cannot read, instructions in a language they don't speak, and dosing schedules that put them at risk.
  >
  > My name is Uzair Ahmad, a final-year Doctor of Pharmacy candidate. Today, I am proud to introduce **Samjha? Rx** — an AI-powered prescription intelligence and pharmacist companion that makes prescriptions make sense in English, Roman Urdu, and native Urdu script."

---

## Slide 2: The Healthcare Blind Spot (Time: 0:30 - 1:00)
- **Visual:** High-contrast statistics callout: 60%+ outpatient medication errors, illegible handwriting sample.
- **Speaker Script:**
  > "Let's look at the reality on the ground. Over 60% of outpatient medication errors in developing regions occur *after* the patient leaves the pharmacy. Why?
  >
  > First: Illegible handwriting and arcane Latin shorthand like TDS, PC, or 5/7.
  > Second: The language gap. Doctors prescribe in English, but families speak and comprehend in Urdu or Roman Urdu.
  > And third: When panicked patients turn to generic AI chatbots, the results can be catastrophic — generic LLMs hallucinate dosages, guess unreadable drug names, and fail to understand dangerous chelation or timing interactions."

---

## Slide 3: The Solution — Samjha? Rx (Time: 1:00 - 1:30)
- **Visual:** Mobile mockup showing high-resolution CameraX scan, 24-hour visual timeline, and Urdu translation card.
- **Speaker Script:**
  > "Samjha? Rx bridges this gap with a patient-first mobile companion built on Android Jetpack Compose.
  >
  > Patients can snap a photo with our zero-lag CameraX scanner or choose a prescription. Instantly, the app translates medical jargon into colloquial, culturally fluent wording — like *'Subah aur shaam khaney ke baad lein'*.
  >
  > It renders a clear 24-hour daily timeline grid with meal timing rules (AC/PC), and schedules on-device alarms through Android AlarmManager so patients never miss a critical dose."

---

## Slide 4: The Core Innovation: The Uncertainty Gate (Time: 1:30 - 2:00)
- **Visual:** Flowchart comparing blind AI guesses vs. Samjha? Rx's Human-in-the-Loop Uncertainty Gate.
- **Speaker Script:**
  > "Our primary differentiator is our Clinical Safety Contract. General AI wants to guess; Samjha? Rx enforces strict scientific humility.
  >
  > If a drug strength is smudged, handwriting confidence falls below 85%, or dosing frequency is missing, that item is **never inferred from context**. It is placed in an explicit Uncertainty Gate.
  >
  > The daily dosage schedule stays locked until a human verifies the field, or the patient taps one button to escalate directly to their community pharmacist."

---

## Slide 5: Multilingual & Cultural Tailoring (Time: 2:00 - 2:30)
- **Visual:** Side-by-side view of English, Roman Urdu, and native Urdu Nastaliq typography with top national brands.
- **Speaker Script:**
  > "Healthcare is inherently cultural. Samjha? Rx is pre-configured for the top national pharmaceutical brands in Pakistan — Augmentin, Brufen, Glucophage, Lipitor, Disprin, Nexum, and Ciproxin.
  >
  > We support three linguistic tiers: English for medical precision, Roman Urdu for effortless messaging comprehension, and RTL Urdu script for grandparents and senior family members. Everyone in the household understands exactly what they are taking."

---

## Slide 6: Deterministic Clinical Rules Engine (Time: 2:30 - 3:00)
- **Visual:** Neuro-symbolic architecture diagram: Gemini OCR assisting data extraction, separated from deterministic BNF/PNF rules.
- **Speaker Script:**
  > "Under the hood, we employ a neuro-symbolic architecture: Generative AI assists with OCR transcription and language translation, but **AI is never the authority on clinical safety**.
  >
  > All clinical checks run through a deterministic rules engine strictly benchmarked against the British National Formulary (BNF 84), the Pakistan National Formulary, and WHO Antimicrobial Stewardship guidelines.
  >
  > The engine evaluates NSAID gastric toxicity, Ciprofloxacin milk chelation, duplicate paracetamol toxicity, and antibiotic course adherence."

---

## Slide 7: Dedicated Pharmacist Review Route (Time: 3:00 - 3:30)
- **Visual:** Pharmacist review screen with rule IDs, SBAR communication builder, and digital sign-off button.
- **Speaker Script:**
  > "Pharmacists are the most accessible healthcare professionals on the planet. Samjha? Rx features a dedicated Pharmacist Review Mode.
  >
  > Community pharmacists see complete rule audit trails, evidence citations, and missing field alerts. If a clinical issue arises, our built-in SBAR builder automatically formats a structured query for the prescribing physician. Pharmacists can digitally sign off, creating trust across the entire care continuum."

---

## Slide 8: Privacy-by-Design & Security (Time: 3:30 - 4:00)
- **Visual:** Privacy architecture: Android Room SQLite database sandbox, cloud backup exclusion badge, PIN lock modal.
- **Speaker Script:**
  > "We treat patient health information as sacred.
  >
  > Prescriptions are saved exclusively in a local on-device Jetpack Room database (`samjha_rx.db`), which is explicitly excluded from Android cloud auto-backups. There are zero third-party tracking SDKs or behavioral ad pixels.
  >
  > When a patient exports a referral note for a pharmacist, all personally identifiable information is automatically masked."

---

## Slide 9: Modern Android Tech Stack (Time: 4:00 - 4:30)
- **Visual:** Tech stack grid: Kotlin Compose, CameraX, Room DB, Gemini 3.8 Flash, AlarmManager, Material 3.
- **Speaker Script:**
  > "Technically, this is a production-grade native Android system:
  > - 100% Kotlin Jetpack Compose with Material Design 3.
  > - CameraX lifecycle-aware viewfinder.
  > - Android Jetpack Room with Kotlin Coroutines and StateFlow.
  > - Google Gemini 3.8 Flash REST API with graceful offline fallbacks.
  > - AlarmManager exact dose scheduling."

---

## Slide 10: Traction, Roadmap & The Ask (Time: 4:30 - 5:00)
- **Visual:** Roadmap milestones: Phase 1 (Built), Phase 2 (Pharmacy Pilots), Phase 3 (On-Device Gemini Nano).
- **Speaker Script:**
  > "Where are we today? Phase 1 is fully implemented and compiled — native Android app, CameraX OCR, Room DB, and rules engine.
  >
  > For Phase 2, we are launching pilot programs with community pharmacies in Karachi and telemedicine clinics. In Phase 3, we will deploy Gemini Nano on-device for zero-latency, offline operation in rural clinics.
  >
  > We are seeking clinical pilot partners, pharmacy network integrations, and strategic mentorship to make medication literacy universal. Thank you, and I look forward to your questions."
