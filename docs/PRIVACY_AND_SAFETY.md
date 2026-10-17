# Privacy and Safety Policy — Samjha? Rx

## 1. On-Device Sandboxing
All saved prescription records and PIN configurations are stored strictly within the app's private sandbox (`SharedPreferences` / local app storage). No records are uploaded to remote advertising servers or unverified cloud databases.

## 2. Truthful Local Security Lock
The 4-digit PIN lock provides local privacy against casual observers. It is transparently disclosed that:
- It is not an encrypted cloud vault.
- It is an on-device UI screen lock.
- Identifiable personal contact details (names, phone numbers, addresses) are omitted from pharmacist referral summaries.

## 3. Human Confirmation Gate
Extracted medications must be confirmed item-by-item by the user or pharmacist. If any critical item is marked ambiguous or unconfirmed, actionable schedule generation is automatically locked.
