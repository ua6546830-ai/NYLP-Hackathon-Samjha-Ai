# Integrations and Provider Policy — Samjha? Rx

## 1. Clinical AI Provider Policy
Under Google Gemini API Terms of Service (effective March 2026), models may not be used to provide medical advice or act as clinical decision-makers.
- **Permitted Role:** Multimodal optical transcription of prescription handwriting/photos and plain-language language translation.
- **Prohibited Role:** Autonomous clinical safety evaluation, dose adjustment, or medical diagnosis.
- **Architecture Gate:** Clinical evaluation is performed locally and deterministically by `ClinicalRulesEngine.kt`, referencing BNF 84 and the Pakistan National Formulary.

## 2. External Drug Datasets & Concept Mapping
- **RxNorm:** Useful for normalizing brand names to ingredient concepts (e.g., Augmentin -> Amoxicillin/Clavulanate). Not a safety engine.
- **openFDA:** Reference for FDA package insert text; does not replace qualified pharmacist review.
- **Pakistan Market Formulary:** Accounts for local brands (Brufen, Disprin, Nexum, Calpol, Ciproxin, Panadol, Azomax) rather than solely US formulary terms.
