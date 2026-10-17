# Test Plan — Samjha? Rx

## 1. Automated Unit Tests (`app/src/test/java/com/example/ClinicalRulesEngineTest.kt`)
- `testNsaidGastricProtectionRuleEvaluated`: Verifies that NSAIDs (Brufen/Aspirin) trigger gastric protection warnings in English and Roman Urdu.
- `testChelationInteractionBetweenCiprofloxacinAndAntacid`: Verifies critical severity alert for fluoroquinolone and mineral antacids.
- `testUnconfirmedOrAmbiguousItemBlocksSchedule`: Verifies that missing strength or frequency blocks the actionable schedule and reports `UNABLE_TO_CHECK`.
- `testConfirmedItemsUnblockSchedule`: Verifies schedule generation with correct daily timing slots when items are confirmed.

## 2. Interactive Manual CUJ Scenarios
1. **Intake & Scenario Switching:** Toggle between scenarios 1 through 4 and verify immediate UI reaction.
2. **Language Switcher:** Toggle English -> Roman Urdu -> Urdu and verify Urdu RTL text layout.
3. **Confirmation Gate:** Uncheck an item, verify schedule blocks, click "Confirm All", verify schedule unblocks.
4. **Pharmacist Route:** Navigate to Pharmacist tab, type counseling note, click "Sign Off".
5. **Referral Modal:** Tap "Ask Pharmacist", verify masked patient notice, copy to clipboard, share intent.
6. **Local Screen Lock:** Set 4-digit PIN, click "Lock Now", verify lock gate screen, enter PIN to unlock.
