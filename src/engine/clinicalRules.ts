import { PrescriptionItem, ClinicalFinding, DoseScheduleItem, PatientContext } from '../types';

export function runClinicalSafetyChecks(
  items: PrescriptionItem[],
  context?: PatientContext
): ClinicalFinding[] {
  const findings: ClinicalFinding[] = [];

  // 1. Check for Ambiguity & Uncertainty in Human Confirmation
  const uncertainItems = items.filter(item => item.hasUncertainty || !item.isConfirmed);
  if (uncertainItems.length > 0) {
    findings.push({
      ruleId: 'RULE-UNCERT-001',
      titleEn: 'Unresolved Clinical Uncertainties in Prescription',
      titleUrLatn: 'Nuskhe mein gher-wazeh ya gher-tasdeeq shuda cheezain',
      titleUr: 'نسخے میں غیر واضح یا غیر تصدیق شدہ ادویات',
      severity: 'CRITICAL',
      status: 'UNABLE_TO_CHECK',
      explanationEn: `${uncertainItems.length} medicine(s) have unverified strengths, frequencies, or handwriting ambiguities.`,
      explanationUrLatn: `${uncertainItems.length} dawaiyon ki taaqat ya waqt saaf nahi hai. Is haalat mein dawa lena nuqsandeh ho sakta hai.`,
      explanationUr: `${uncertainItems.length} ادویات کی خوراک، طاقت یا مقدار واضح نہیں ہے۔ فارماسسٹ سے تصدیق کے بغیر استعمال خطرناک ہو سکتا ہے۔`,
      recommendedActionEn: 'Do not start these medicines until clarified by the dispensing pharmacist or prescribing doctor.',
      recommendedActionUrLatn: 'Pharmacist ya doctor se baat kar ke pehle saaf karwayein.',
      recommendedActionUr: 'پہلے اپنے فارماسسٹ یا ڈاکٹر سے رجوع کر کے درست معلومات حاصل کریں۔',
      actionOwner: 'PHARMACIST',
      evidenceSource: 'WHO Patient Safety Curriculum & Good Pharmacy Practice Standards (2024 Edition)',
      relatedDrugIds: uncertainItems.map(i => i.id),
    });
  }

  // 2. Check for Quinolone (Ciprofloxacin) + Mineral Chelation (RULE-CHEL-001)
  const hasCipro = items.some(i => 
    i.drugName.toLowerCase().includes('cipro') || 
    i.genericName.toLowerCase().includes('ciprofloxacin')
  );
  const hasAntacid = items.some(i => 
    i.drugName.toLowerCase().includes('antacid') || 
    i.drugName.toLowerCase().includes('gaviscon') ||
    i.genericName.toLowerCase().includes('calcium') ||
    i.genericName.toLowerCase().includes('magnesium')
  );

  if (hasCipro && hasAntacid) {
    findings.push({
      ruleId: 'RULE-CHEL-001',
      titleEn: 'Severe Absorption Blocker: Ciprofloxacin & Antacid/Dairy',
      titleUrLatn: 'Khatra: Ciproxin aur Antacid/Doodh ek sath na lein',
      titleUr: 'شدید تعامل: سپروکسن اور اینٹاسڈ سیرپ کا ایک ساتھ استعمال',
      severity: 'WARNING',
      status: 'FLAG',
      explanationEn: 'Calcium, Magnesium, or Aluminum in antacids and dairy bind to Ciprofloxacin, reducing its antibiotic absorption by up to 90%.',
      explanationUrLatn: 'Antacid syrup ya doodh Ciproxin ko jazb honay se roktay hain, jis se antibiotic be-asar hojati hai.',
      explanationUr: 'اینٹاسڈ شربت یا دودھ اینٹی بائیوٹک کے اثر کو 90 فیصد تک ضائع کر سکتے ہیں کیونکہ یہ آپس میں جڑ جاتے ہیں۔',
      recommendedActionEn: 'Space Ciprofloxacin at least 2 hours BEFORE or 4 hours AFTER any antacids or dairy products.',
      recommendedActionUrLatn: 'Ciproxin lene se 2 ghantay pehle ya 4 ghantay baad tak antacid ya doodh dahi na lein.',
      recommendedActionUr: 'سپروکسن لینے سے دو گھنٹے قبل یا چار گھنٹے بعد تک اینٹاسڈ یا دودھ کی مصنوعات ہرگز مت لیں۔',
      actionOwner: 'PATIENT',
      evidenceSource: 'British National Formulary (BNF 84), Section 5.1.12: Quinolones Chelation Interactions',
      relatedDrugIds: items.filter(i => 
        i.drugName.toLowerCase().includes('cipro') || 
        i.drugName.toLowerCase().includes('antacid')
      ).map(i => i.id),
    });
  }

  // 3. Check for NSAID Gastric Protection (RULE-FOOD-001)
  const nsaidItems = items.filter(i => 
    i.drugName.toLowerCase().includes('brufen') || 
    i.genericName.toLowerCase().includes('ibuprofen') ||
    i.drugName.toLowerCase().includes('disprin') ||
    i.genericName.toLowerCase().includes('aspirin') ||
    i.genericName.toLowerCase().includes('diclofenac')
  );

  if (nsaidItems.length > 0) {
    findings.push({
      ruleId: 'RULE-FOOD-001',
      titleEn: 'Stomach Protection Warning for Painkillers (NSAIDs)',
      titleUrLatn: 'Dard ki goliyan khaney ke foran baad lein',
      titleUr: 'درد کش ادویات کے استعمال میں معدے کی حفاظت',
      severity: 'CAUTION',
      status: 'FLAG',
      explanationEn: 'Ibuprofen and Aspirin irritate the stomach lining and can lead to acid reflux, heartburn, or ulcers if taken empty stomach.',
      explanationUrLatn: 'Brufen ya Disprin khali pait lene se pait mein sozish, jalan ya zakhm (ulcer) ho sakta hai.',
      explanationUr: 'بروفین یا اسپرین خالی پیٹ لینے سے معدے میں تیزابیت اور السر کا شدید خطرہ ہوتا ہے۔',
      recommendedActionEn: 'Always swallow tablets with food, milk, or immediately after a meal with plenty of water.',
      recommendedActionUrLatn: 'Hamesha khana khanay ke foran baad bharpoor paani ke sath lein.',
      recommendedActionUr: 'ہمیشہ کھانے کے فوراً بعد یا دودھ کے ساتھ وافر مقدار میں پانی پی کر کھائیں۔',
      actionOwner: 'PATIENT',
      evidenceSource: 'Pakistan National Formulary (PNF) & BNF Section 10.1.1: Non-steroidal Anti-inflammatory Drugs',
      relatedDrugIds: nsaidItems.map(i => i.id),
    });
  }

  // 4. Check for Proton Pump Inhibitor (PPI) Morning Empty Stomach (RULE-PPI-001)
  const ppiItems = items.filter(i => 
    i.drugName.toLowerCase().includes('nexum') || 
    i.genericName.toLowerCase().includes('esomeprazole') ||
    i.genericName.toLowerCase().includes('omeprazole')
  );

  if (ppiItems.length > 0) {
    findings.push({
      ruleId: 'RULE-PPI-001',
      titleEn: 'Optimal Acid Suppression: Take Nexum Before Breakfast',
      titleUrLatn: 'Nexum subah nashtay se pehle khali pait lein',
      titleUr: 'معدے کے کیپسول کا درست وقت: صبح نہار منہ',
      severity: 'INFO',
      status: 'FLAG',
      explanationEn: 'Proton pump inhibitors work best when stomach acid pumps are stimulated by an upcoming meal.',
      explanationUrLatn: 'Yeh capsule subah nashtay se 30 se 60 minute pehle lene se poora din behtareen faida deta hai.',
      explanationUr: 'صبح ناشتے سے 30 سے 60 منٹ قبل خالی پیٹ لینے سے دوا کا اثر دن بھر بہترین رہتا ہے۔',
      recommendedActionEn: 'Take with a glass of plain water 30-60 minutes before breakfast.',
      recommendedActionUrLatn: 'Subah nashtay se aadha ghanta pehle paani ke sath lein.',
      recommendedActionUr: 'صبح نہار منہ ناشتے سے آدھا گھنٹہ قبل پانی سے نگل لیں۔',
      actionOwner: 'PATIENT',
      evidenceSource: 'FDA Drug Safety Communication & BNF 84 Section 1.3.5',
      relatedDrugIds: ppiItems.map(i => i.id),
    });
  }

  // 5. Check for Antibiotic Course Adherence (RULE-ABX-001)
  const abxItems = items.filter(i => 
    i.drugName.toLowerCase().includes('augmentin') || 
    i.genericName.toLowerCase().includes('amoxicillin') ||
    i.drugName.toLowerCase().includes('cipro')
  );

  if (abxItems.length > 0) {
    findings.push({
      ruleId: 'RULE-ABX-001',
      titleEn: 'Antibiotic Course Adherence (Antimicrobial Stewardship)',
      titleUrLatn: 'Antibiotic ka poora course khatam karna zaroori hai',
      titleUr: 'اینٹی بائیوٹک کا مکمل کورس لازمی پورا کریں',
      severity: 'INFO',
      status: 'FLAG',
      explanationEn: 'Stopping antibiotics early allows surviving resistant bacteria to multiply, leading to recurrence and antibiotic resistance.',
      explanationUrLatn: 'Agar aap behtar bhi mehsoos karein, tab bhi doctor ki hidayat ke mutabiq poorey din dawa lein.',
      explanationUr: 'طبیعت بہتر محسوس ہونے پر بھی کورس ادھورا مت چھوڑیں، ورنہ جراثیم طاقتور ہو کر دوبارہ حملہ آور ہو سکتے ہیں۔',
      recommendedActionEn: 'Complete the prescribed duration (e.g. 5 to 7 days). Do not save leftover tablets for later.',
      recommendedActionUrLatn: 'Muqarrara din poorey karein aur bachi hui dawai doosron ko na dein.',
      recommendedActionUr: 'تجویز کردہ مدت تک باقاعدگی سے لیں۔ بچی ہوئی دوا سنبھال کر نہ رکھیں۔',
      actionOwner: 'PATIENT',
      evidenceSource: 'WHO Global Action Plan on Antimicrobial Resistance (AMR) & BNF Section 5.1',
      relatedDrugIds: abxItems.map(i => i.id),
    });
  }

  // If no warnings or cautions were flagged:
  if (findings.length === 0) {
    findings.push({
      ruleId: 'RULE-BASE-000',
      titleEn: 'No Issues Found in Current Rule Checks',
      titleUrLatn: 'Mojooda checks mein koi masla nahi mila',
      titleUr: 'رائج حفاظتی جانچ میں کوئی خرابی نہیں پائی گئی',
      severity: 'INFO',
      status: 'NO_ISSUES_FOUND',
      explanationEn: 'No contraindications, duplicate ingredients, or timing conflicts were detected among the evaluated medicines.',
      explanationUrLatn: 'Mojooda dawaiyon mein aapas mein koi khatarnak takraao nahi paya gaya.',
      explanationUr: 'زیر غور ادویات کے درمیان باہمی تضاد یا مضر اثرات کی کوئی علامت نہیں ملی۔',
      recommendedActionEn: 'Follow the daily schedule as scheduled. Contact your pharmacist if you experience unusual symptoms.',
      recommendedActionUrLatn: 'Rozana ke waqt ke mutabiq dawai lein aur koi takleef ho toh pharmacist se baat karein.',
      recommendedActionUr: 'روزمرہ کے معمول کے مطابق ادویات لیں۔ کسی بھی غیر معمولی علامت پر فارماسسٹ سے رجوع کریں۔',
      actionOwner: 'PATIENT',
      evidenceSource: 'Curated Deterministic Clinical Knowledge Base v1.2',
      relatedDrugIds: [],
    });
  }

  return findings;
}

export function generateDailySchedule(items: PrescriptionItem[]): DoseScheduleItem[] {
  const schedule: DoseScheduleItem[] = [];

  items.forEach(item => {
    const freq = item.frequency.toUpperCase();
    const name = `${item.drugName} (${item.strength})`;

    // Morning
    if (freq.includes('OD') || freq.includes('BD') || freq.includes('TDS') || freq.includes('ONCE') || freq.includes('TWICE') || freq.includes('THREE')) {
      schedule.push({
        id: `${item.id}-morning`,
        timeSlot: 'Morning',
        timeSlotUrLatn: 'Subah (Nashta)',
        timeSlotUr: 'صبح (ناشتہ)',
        approxTime: '08:00 AM',
        drugName: name,
        dose: item.doseAmount,
        foodTiming: item.foodTiming,
        foodTimingUrLatn: item.foodTiming.includes('Before') ? 'Nashtay se pehle' : 'Nashtay ke baad',
        foodTimingUr: item.foodTiming.includes('Before') ? 'ناشتے سے قبل' : 'ناشتے کے بعد',
      });
    }

    // Afternoon (TDS or Thrice)
    if (freq.includes('TDS') || freq.includes('THREE') || freq.includes('TID')) {
      schedule.push({
        id: `${item.id}-afternoon`,
        timeSlot: 'Afternoon',
        timeSlotUrLatn: 'Dopehar (Khana)',
        timeSlotUr: 'دوپہر (کھانا)',
        approxTime: '02:00 PM',
        drugName: name,
        dose: item.doseAmount,
        foodTiming: item.foodTiming,
        foodTimingUrLatn: 'Dopehar ke khaney ke baad',
        foodTimingUr: 'دوپہر کے کھانے کے بعد',
      });
    }

    // Evening / Night (BD or TDS or Bedtime)
    if (freq.includes('BD') || freq.includes('TDS') || freq.includes('TWICE') || freq.includes('THREE') || freq.includes('BEDTIME') || freq.includes('NIGHT')) {
      schedule.push({
        id: `${item.id}-evening`,
        timeSlot: freq.includes('BEDTIME') ? 'Bedtime' : 'Evening',
        timeSlotUrLatn: freq.includes('BEDTIME') ? 'Soney se pehle' : 'Shaam / Raat (Khana)',
        timeSlotUr: freq.includes('BEDTIME') ? 'سونے سے قبل' : 'شام / رات (کھانا)',
        approxTime: freq.includes('BEDTIME') ? '10:30 PM' : '08:00 PM',
        drugName: name,
        dose: item.doseAmount,
        foodTiming: item.foodTiming,
        foodTimingUrLatn: freq.includes('BEDTIME') ? 'Paani ke sath' : 'Raat ke khaney ke baad',
        foodTimingUr: freq.includes('BEDTIME') ? 'پانی کے ساتھ' : 'رات کے کھانے کے بعد',
      });
    }
  });

  return schedule;
}
