export type Language = 'en' | 'ur-Latn' | 'ur';

export type CheckSeverity = 'INFO' | 'CAUTION' | 'WARNING' | 'CRITICAL';
export type CheckStatus = 'NO_ISSUES_FOUND' | 'FLAG' | 'UNABLE_TO_CHECK';

export interface PrescriptionItem {
  id: string;
  drugName: string;
  genericName: string;
  strength: string;
  dosageForm: string; // tablet, syrup, capsule
  doseAmount: string; // 1 tab, 5ml
  frequency: string;  // Once daily (OD), Twice daily (BD), Thrice daily (TDS), PRN
  foodTiming: string; // Before food (AC), With/After food (PC), Empty stomach
  duration: string;   // 5 days, 1 month
  instructionsEn: string;
  instructionsUrLatn: string;
  instructionsUr: string;
  isConfirmed: boolean;
  hasUncertainty: boolean;
  uncertaintyNotes?: string;
  confidenceScore: number; // 0.0 - 1.0
}

export interface ClinicalFinding {
  ruleId: string;
  titleEn: string;
  titleUrLatn: string;
  titleUr: string;
  severity: CheckSeverity;
  status: CheckStatus;
  explanationEn: string;
  explanationUrLatn: string;
  explanationUr: string;
  recommendedActionEn: string;
  recommendedActionUrLatn: string;
  recommendedActionUr: string;
  actionOwner: 'PATIENT' | 'PHARMACIST' | 'PRESCRIBER';
  evidenceSource: string;
  relatedDrugIds: string[];
}

export interface DoseScheduleItem {
  id: string;
  timeSlot: 'Morning' | 'Afternoon' | 'Evening' | 'Bedtime';
  timeSlotUrLatn: string;
  timeSlotUr: string;
  approxTime: string;
  drugName: string;
  dose: string;
  foodTiming: string;
  foodTimingUrLatn: string;
  foodTimingUr: string;
  specialNote?: string;
}

export interface PatientContext {
  ageGroup: 'ADULT' | 'ELDERLY' | 'PAEDIATRIC' | 'PREGNANT' | 'BREASTFEEDING';
  hasRenalImpairment: boolean;
  hasPepticUlcer: boolean;
  hasDiabetes: boolean;
  notes: string;
}

export interface PrescriptionReview {
  id: string;
  timestamp: string;
  title: string;
  rawText: string;
  items: PrescriptionItem[];
  findings: ClinicalFinding[];
  pharmacistNotes?: string;
  isSignedOff?: boolean;
  signedOffBy?: string;
  signedOffAt?: string;
}
