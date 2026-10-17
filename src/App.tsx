import React, { useState, useEffect, useMemo } from 'react';
import { Header } from './components/Header';
import { Navigation, ActiveTab } from './components/Navigation';
import { ConfirmationGate } from './components/ConfirmationGate';
import { TimelineGrid } from './components/TimelineGrid';
import { CameraModal } from './components/CameraModal';
import { PharmacistWorkspace } from './components/PharmacistWorkspace';
import { SavedPrescriptions } from './components/SavedPrescriptions';
import { AccountPrivacy } from './components/AccountPrivacy';
import { SYNTHETIC_SCENARIOS } from './data/syntheticPresets';
import { runClinicalSafetyChecks, generateDailySchedule } from './engine/clinicalRules';
import { Language, PrescriptionItem, PrescriptionReview } from './types';
import { Camera, Sparkles, FileText, BookmarkPlus, KeyRound, ShieldAlert, ArrowRight } from 'lucide-react';

export const App: React.FC = () => {
  const [currentLang, setCurrentLang] = useState<Language>('en');
  const [activeTab, setActiveTab] = useState<ActiveTab>('UNDERSTAND');

  // Active prescription state (defaults to scenario 1)
  const [selectedScenarioId, setSelectedScenarioId] = useState('scenario_1');
  const [items, setItems] = useState<PrescriptionItem[]>(SYNTHETIC_SCENARIOS[0].items);
  const [rawText, setRawText] = useState(SYNTHETIC_SCENARIOS[0].rawText);
  const [customText, setCustomText] = useState('');
  const [showCustomModal, setShowCustomModal] = useState(false);
  const [isCameraOpen, setIsCameraOpen] = useState(false);

  // Saved reviews & PIN lock
  const [savedReviews, setSavedReviews] = useState<PrescriptionReview[]>(() => {
    try {
      const stored = localStorage.getItem('samjha_rx_saved_reviews');
      return stored ? JSON.parse(stored) : [];
    } catch {
      return [];
    }
  });

  const [pinLock, setPinLock] = useState<string | null>(() => {
    try {
      return localStorage.getItem('samjha_rx_pin_lock');
    } catch {
      return null;
    }
  });

  const [isAppLocked, setIsAppLocked] = useState<boolean>(() => {
    try {
      return !!localStorage.getItem('samjha_rx_pin_lock');
    } catch {
      return false;
    }
  });
  const [unlockInput, setUnlockInput] = useState('');
  const [unlockError, setUnlockError] = useState(false);

  // Persistence effects
  useEffect(() => {
    try {
      localStorage.setItem('samjha_rx_saved_reviews', JSON.stringify(savedReviews));
    } catch (e) {
      console.error(e);
    }
  }, [savedReviews]);

  useEffect(() => {
    try {
      if (pinLock) {
        localStorage.setItem('samjha_rx_pin_lock', pinLock);
      } else {
        localStorage.removeItem('samjha_rx_pin_lock');
      }
    } catch (e) {
      console.error(e);
    }
  }, [pinLock]);

  // Clinical safety checks
  const findings = useMemo(() => {
    return runClinicalSafetyChecks(items);
  }, [items]);

  // 24-hr dosing timeline
  const schedule = useMemo(() => {
    return generateDailySchedule(items);
  }, [items]);

  // Schedule is locked if any item has uncertainty or is not confirmed
  const isScheduleLocked = useMemo(() => {
    return items.some(i => i.hasUncertainty || !i.isConfirmed);
  }, [items]);

  // Scenario selection handler
  const handleSelectScenario = (id: string) => {
    setSelectedScenarioId(id);
    const scenario = SYNTHETIC_SCENARIOS.find(s => s.id === id);
    if (scenario) {
      setItems(scenario.items);
      setRawText(scenario.rawText);
    }
  };

  // Human confirmation
  const handleConfirmItem = (id: string) => {
    setItems(prev =>
      prev.map(item =>
        item.id === id ? { ...item, isConfirmed: true, hasUncertainty: false } : item
      )
    );
  };

  const handleUpdateItem = (updated: PrescriptionItem) => {
    setItems(prev => prev.map(item => (item.id === updated.id ? updated : item)));
  };

  // Image analysis callback (simulates OCR extraction)
  const handleImageCaptured = (dataUrl: string) => {
    // Analyze captured label
    const extracted: PrescriptionItem[] = [
      {
        id: `ocr_${Date.now()}_1`,
        drugName: 'Augmentin (Captured Label)',
        genericName: 'Amoxicillin + Clavulanate',
        strength: '625mg',
        dosageForm: 'Tablet',
        doseAmount: '1 tablet',
        frequency: 'Twice daily (BD)',
        foodTiming: 'With meals (PC)',
        duration: '5 days',
        instructionsEn: 'Take 1 tablet every 12 hours with food as captured from the medicine container.',
        instructionsUrLatn: 'Subah aur shaam 1 goli khaney ke sath lein.',
        instructionsUr: 'صبح اور شام ایک گولی کھانے کے ساتھ لیں۔',
        isConfirmed: false,
        hasUncertainty: false,
        confidenceScore: 0.94,
      },
      {
        id: `ocr_${Date.now()}_2`,
        drugName: 'Brufen 400',
        genericName: 'Ibuprofen',
        strength: '400mg',
        dosageForm: 'Tablet',
        doseAmount: '1 tablet',
        frequency: 'Three times daily (TDS)',
        foodTiming: 'Strictly after food (PC)',
        duration: '3 days PRN',
        instructionsEn: 'Take with food for pain. Do not take on empty stomach.',
        instructionsUrLatn: 'Dard ke liye khaney ke foran baad 1 goli lein.',
        instructionsUr: 'درد کے لیے کھانے کے فوراً بعد ایک گولی لیں۔',
        isConfirmed: false,
        hasUncertainty: false,
        confidenceScore: 0.91,
      },
    ];
    setItems(extracted);
    setRawText('Captured Prescription Photo');
    setSelectedScenarioId('custom_scan');
    setActiveTab('UNDERSTAND');
  };

  // Custom prescription text submission
  const handleParseCustomText = () => {
    if (!customText.trim()) return;
    const lines = customText.split('\n').filter(l => l.trim().length > 0);
    const parsed: PrescriptionItem[] = lines.map((line, idx) => ({
      id: `custom_${Date.now()}_${idx}`,
      drugName: line.split(/[\s,]+/)[0] || 'Unknown Drug',
      genericName: 'Prescription Transcribed',
      strength: line.match(/\d+\s*(mg|ml|g)/i)?.[0] || 'Unspecified',
      dosageForm: 'Tablet',
      doseAmount: '1 dose',
      frequency: line.match(/\b(OD|BD|TDS|QDS|PRN)\b/i)?.[0]?.toUpperCase() || 'BD',
      foodTiming: 'With or after food',
      duration: 'As prescribed',
      instructionsEn: `Take as prescribed: ${line}`,
      instructionsUrLatn: `Hidayat ke mutabiq lein: ${line}`,
      instructionsUr: `نسخے کی ہدایت کے مطابق لیں۔`,
      isConfirmed: false,
      hasUncertainty: !line.match(/\d+\s*(mg|ml|g)/i),
      uncertaintyNotes: !line.match(/\d+\s*(mg|ml|g)/i) ? 'Strength not detected in text' : undefined,
      confidenceScore: 0.85,
    }));

    setItems(parsed);
    setRawText(customText);
    setSelectedScenarioId('custom_text');
    setShowCustomModal(false);
    setCustomText('');
  };

  // Save review
  const handleSaveCurrentReview = (pharmacistName?: string, notes?: string) => {
    const newReview: PrescriptionReview = {
      id: `review_${Date.now()}`,
      timestamp: new Date().toISOString(),
      title: items.map(i => i.drugName).slice(0, 2).join(' + ') + (items.length > 2 ? ` (+${items.length - 2} more)` : ''),
      rawText: rawText,
      items: items,
      findings: findings,
      pharmacistNotes: notes,
      isSignedOff: !!pharmacistName,
      signedOffBy: pharmacistName,
      signedOffAt: pharmacistName ? new Date().toISOString() : undefined,
    };
    setSavedReviews(prev => [newReview, ...prev]);
  };

  // Handle PIN unlock
  const handleUnlock = () => {
    if (unlockInput === pinLock) {
      setIsAppLocked(false);
      setUnlockError(false);
      setUnlockInput('');
    } else {
      setUnlockError(true);
    }
  };

  // PIN Gate Screen if locked
  if (isAppLocked && pinLock) {
    return (
      <div className="min-h-screen flex items-center justify-center p-4 bg-slate-900/90 text-white">
        <div className="glass-card-elevated rounded-2xl p-6 sm:p-8 max-w-sm w-full text-center text-slate-800 border border-slate-200 shadow-2xl">
          <div className="w-14 h-14 rounded-2xl bg-indigo-600 text-white flex items-center justify-center mx-auto mb-4 shadow-lg shadow-indigo-600/30">
            <KeyRound className="w-7 h-7" />
          </div>
          <h2 className="text-xl font-bold text-slate-900">Samjha? Rx Protected</h2>
          <p className="text-xs text-slate-500 mt-1 mb-5">
            Enter your 4-digit device PIN to view prescription data
          </p>

          <input
            type="password"
            maxLength={4}
            value={unlockInput}
            onChange={e => setUnlockInput(e.target.value.replace(/\D/g, ''))}
            placeholder="••••"
            className="w-36 text-center text-2xl tracking-widest px-4 py-2.5 bg-slate-50 rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500 mb-3 mx-auto block font-mono"
          />

          {unlockError && (
            <p className="text-xs text-rose-600 font-semibold mb-3">
              Incorrect PIN. Please try again.
            </p>
          )}

          <button
            onClick={handleUnlock}
            disabled={unlockInput.length !== 4}
            className="w-full py-2.5 px-4 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-xl shadow-md disabled:opacity-50 transition-all"
          >
            Unlock Application
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex flex-col pb-16">
      {/* Editorial Header */}
      <Header currentLang={currentLang} onLanguageChange={setCurrentLang} />

      {/* Main Container */}
      <main className="flex-1 max-w-6xl w-full mx-auto px-4 sm:px-6">
        {/* Navigation Tabs */}
        <Navigation
          activeTab={activeTab}
          onTabChange={setActiveTab}
          savedCount={savedReviews.length}
          currentLang={currentLang}
        />

        {/* Tab 1: Understand (Patient Workflow) */}
        {activeTab === 'UNDERSTAND' && (
          <div className="space-y-6">
            {/* Prescription Intake Methods Bar */}
            <div className="glass-card rounded-2xl p-4 sm:p-5 border border-slate-200/90 shadow-sm">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-3">
                <span className="text-xs font-bold text-slate-800 uppercase tracking-wider flex items-center gap-1.5">
                  <Sparkles className="w-4 h-4 text-indigo-600" />
                  Prescription Intake Channels
                </span>
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => setIsCameraOpen(true)}
                    className="flex items-center gap-1.5 text-xs font-semibold px-3 py-1.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl shadow-sm transition-all"
                  >
                    <Camera className="w-4 h-4" /> Scan Label Photo
                  </button>
                  <button
                    onClick={() => setShowCustomModal(true)}
                    className="flex items-center gap-1.5 text-xs font-semibold px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-xl border border-slate-200/80 transition-all"
                  >
                    <FileText className="w-4 h-4" /> Type Rx
                  </button>
                </div>
              </div>

              {/* Scenario Presets Selector */}
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 pt-1">
                {SYNTHETIC_SCENARIOS.map(sc => (
                  <button
                    key={sc.id}
                    onClick={() => handleSelectScenario(sc.id)}
                    className={`p-2.5 rounded-xl border text-left transition-all ${
                      selectedScenarioId === sc.id
                        ? 'bg-indigo-50/90 border-indigo-500 shadow-sm'
                        : 'bg-white/70 border-slate-200/70 hover:bg-white hover:border-slate-300'
                    }`}
                  >
                    <div className="font-bold text-xs text-slate-900 truncate">{sc.title}</div>
                    <div className="text-[10px] text-slate-500 truncate mt-0.5">{sc.subtitle}</div>
                  </button>
                ))}
              </div>
            </div>

            {/* Human-in-the-Loop Confirmation Gate */}
            <ConfirmationGate
              items={items}
              onConfirmItem={handleConfirmItem}
              onUpdateItem={handleUpdateItem}
              onEscalateToPharmacist={() => setActiveTab('PHARMACIST')}
              currentLang={currentLang}
            />

            {/* 24-Hour Dosing Timeline */}
            <TimelineGrid
              schedule={schedule}
              isLocked={isScheduleLocked}
              currentLang={currentLang}
            />

            {/* Save Current Review Action */}
            <div className="flex justify-end pt-2">
              <button
                onClick={() => {
                  handleSaveCurrentReview();
                  alert('Prescription review saved to local device records!');
                }}
                className="flex items-center gap-2 text-xs font-bold px-4 py-2.5 bg-white hover:bg-indigo-50 text-indigo-700 border border-indigo-200 rounded-xl shadow-sm transition-all"
              >
                <BookmarkPlus className="w-4 h-4" /> Save Review Locally
              </button>
            </div>
          </div>
        )}

        {/* Tab 2: Pharmacist Workspace */}
        {activeTab === 'PHARMACIST' && (
          <PharmacistWorkspace
            items={items}
            findings={findings}
            currentLang={currentLang}
            onSaveReview={(name, notes) => {
              handleSaveCurrentReview(name, notes);
              alert('Review officially signed off and archived!');
            }}
          />
        )}

        {/* Tab 3: Saved Prescriptions */}
        {activeTab === 'SAVED' && (
          <SavedPrescriptions
            reviews={savedReviews}
            onLoadReview={review => {
              setItems(review.items);
              setRawText(review.rawText);
              setActiveTab('UNDERSTAND');
            }}
            onDeleteReview={id => {
              setSavedReviews(prev => prev.filter(r => r.id !== id));
            }}
            onClearAll={() => {
              if (confirm('Clear all saved prescription records from this device?')) {
                setSavedReviews([]);
              }
            }}
            currentLang={currentLang}
          />
        )}

        {/* Tab 4: Account & Privacy */}
        {activeTab === 'ACCOUNT' && (
          <AccountPrivacy
            currentLang={currentLang}
            pinLock={pinLock}
            onSetPinLock={setPinLock}
          />
        )}
      </main>

      {/* Camera Capture Modal */}
      <CameraModal
        isOpen={isCameraOpen}
        onClose={() => setIsCameraOpen(false)}
        onImageCaptured={handleImageCaptured}
        currentLang={currentLang}
      />

      {/* Manual Prescription Text Modal */}
      {showCustomModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm">
          <div className="glass-card-elevated rounded-2xl w-full max-w-md p-5 border border-slate-200 shadow-2xl">
            <h3 className="font-bold text-sm text-slate-900 mb-1">
              Type or Paste Prescription Shorthand
            </h3>
            <p className="text-xs text-slate-500 mb-3">
              Enter one drug per line (e.g. "Augmentin 625mg 1 tab BD")
            </p>
            <textarea
              rows={5}
              value={customText}
              onChange={e => setCustomText(e.target.value)}
              placeholder="Tab Augmentin 625mg BD&#10;Tab Brufen 400mg TDS&#10;Tab Panadol 500mg PRN"
              className="w-full p-3 bg-white rounded-xl border border-slate-300 text-xs font-mono focus:outline-none focus:ring-2 focus:ring-indigo-500 mb-3"
            />
            <div className="flex justify-end gap-2">
              <button
                onClick={() => setShowCustomModal(false)}
                className="px-3 py-1.5 text-xs text-slate-600 hover:bg-slate-100 rounded-lg"
              >
                Cancel
              </button>
              <button
                onClick={handleParseCustomText}
                disabled={!customText.trim()}
                className="px-4 py-1.5 text-xs font-bold bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg shadow-sm disabled:opacity-50"
              >
                Analyze Shorthand
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
