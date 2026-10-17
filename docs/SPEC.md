# Samjha? Rx (سمجھا؟) — Product Specification

## 1. Product Summary
- **App Name:** Samjha? Rx (سمجھا؟)
- **Tagline:** Make your prescription make sense. (نسخہ سمجھیں، بے فکری سے صحت مند رہیں)
- **Primary Audience:** Patients, caregivers, and community pharmacists in Pakistan and bilingual global communities.
- **Core Value Proposition:** Translate complex prescription wording into understandable daily directions across English, Roman Urdu, and Urdu, while surfacing clinical uncertainty and providing a structured pharmacist handoff.

## 2. Key Design & UX Rules
1. **Editorial Glassmorphism:** Misty white backgrounds, subtle indigo/violet accents, clean slate typography, soft borders, and comfortable 48dp touch targets.
2. **Everyday Language:** Explain timing in plain terms (*"Take with breakfast"*, *"Khaney ke baad lein"*, *"کھانے کے ساتھ لیں"*).
3. **No Clinical Pretensions:** Never display "Safe" or "Approved". Best status is *"No issues found in the checks that ran"*.
4. **Deterministic Rules Authority:** LLMs assist with OCR and translation; deterministic, peer-reviewed rules execute clinical safety checks.

## 3. Supported Scenarios
- **Scenario 1:** Acute Infection & Pain (Augmentin 625mg BD + Brufen 400mg TDS + Panadol 500mg PRN)
- **Scenario 2:** Chronic Cardiometabolic (Glucophage 500mg BD + Disprin 75mg OD + Lipitor 20mg Bedtime + Nexum 20mg AC)
- **Scenario 3:** Antibiotic Chelation (Ciprofloxacin 500mg BD + Calcium/Antacid Suspension)
- **Scenario 4:** Ambiguous Handwritten Rx (Missing strength and unreadable frequency)
