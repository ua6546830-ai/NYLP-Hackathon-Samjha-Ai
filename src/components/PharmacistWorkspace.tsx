import React, { useState } from 'react';
import { PrescriptionItem, ClinicalFinding, Language } from '../types';
import { Stethoscope, ShieldAlert, FileText, CheckCircle2, Copy, Check, Share2, Info, Building } from 'lucide-react';

interface PharmacistWorkspaceProps {
  items: PrescriptionItem[];
  findings: ClinicalFinding[];
  currentLang: Language;
  onSaveReview: (pharmacistName: string, notes: string) => void;
}

export const PharmacistWorkspace: React.FC<PharmacistWorkspaceProps> = ({
  items,
  findings,
  currentLang,
  onSaveReview,
}) => {
  const [pharmacistName, setPharmacistName] = useState('Pharm. Uzair Ahmad (R.Ph)');
  const [regNumber, setRegNumber] = useState('PK-PC-84920');
  const [counselingNotes, setCounselingNotes] = useState('');
  const [isSignedOff, setIsSignedOff] = useState(false);
  const [copiedSbar, setCopiedSbar] = useState(false);

  const isUrdu = currentLang === 'ur';

  // Build SBAR Text for Prescriber
  const sbarText = `--- SBAR CLINICAL CLARIFICATION QUERY ---
DATE: ${new Date().toLocaleDateString()}
FROM: ${pharmacistName} (Registration: ${regNumber})
TO: Prescribing Physician

[S] SITUATION:
Clarification requested regarding prescription for: ${items.map(i => i.drugName).join(', ')}.

[B] BACKGROUND:
Patient presented with outpatient prescription. Active findings:
${findings.map(f => `• [${f.ruleId}] ${f.titleEn} (Severity: ${f.severity})`).join('\n')}

[A] ASSESSMENT:
${findings.some(f => f.ruleId === 'RULE-CHEL-001') ? '• Concomitant administration of Ciprofloxacin and multivalent cations creates non-absorbable chelates, drastically impairing antibiotic efficacy.\n' : ''}${findings.some(f => f.ruleId === 'RULE-UNCERT-001') ? '• Critical parameters (strength/dose frequency) were not verified with patient safety certainty from the handwritten script.\n' : ''}
[R] RECOMMENDATION:
${findings.some(f => f.ruleId === 'RULE-CHEL-001') ? '• Suggest staggering Ciprofloxacin by minimum 2 hours from mineral supplements or substituting with a non-interacting agent.\n' : ''}• Please confirm verified dosing schedule for patient medical record.
-----------------------------------------`;

  const handleCopySbar = () => {
    navigator.clipboard.writeText(sbarText);
    setCopiedSbar(true);
    setTimeout(() => setCopiedSbar(false), 2000);
  };

  const handleSignOff = () => {
    setIsSignedOff(true);
    onSaveReview(pharmacistName, counselingNotes);
  };

  return (
    <div className="space-y-6">
      {/* Role Banner */}
      <div className="glass-card-elevated rounded-2xl p-5 border-l-4 border-l-indigo-600 bg-white/95">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-indigo-50 border border-indigo-200/80 flex items-center justify-center text-indigo-600">
              <Stethoscope className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-bold text-slate-900">
                Pharmacist Clinical Audit & Handoff Workspace
              </h2>
              <p className="text-xs text-slate-500">
                Rule IDs, evidence citations (BNF 84, PNF), and SBAR prescriber liaison tools
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2 self-start sm:self-auto">
            <span className="text-xs font-semibold px-2.5 py-1 bg-slate-100 text-slate-700 rounded-lg border border-slate-200">
              Role: Licensed Reviewer
            </span>
          </div>
        </div>
      </div>

      {/* Audit Findings */}
      <div className="glass-card rounded-2xl p-5 border border-slate-200/90 shadow-sm">
        <h3 className="text-sm font-bold text-slate-900 mb-3 flex items-center gap-2">
          <ShieldAlert className="w-4 h-4 text-indigo-600" />
          Deterministic Rule Audit Trail ({findings.length} findings)
        </h3>

        <div className="space-y-3">
          {findings.map(finding => {
            const isWarn = finding.severity === 'WARNING' || finding.severity === 'CRITICAL';
            const isCaution = finding.severity === 'CAUTION';

            return (
              <div
                key={finding.ruleId}
                className={`p-4 rounded-xl border text-xs ${
                  isWarn
                    ? 'bg-rose-50/60 border-rose-200 text-rose-950'
                    : isCaution
                    ? 'bg-amber-50/60 border-amber-200 text-amber-950'
                    : 'bg-emerald-50/50 border-emerald-200 text-emerald-950'
                }`}
              >
                <div className="flex flex-wrap items-center justify-between gap-2 mb-1.5">
                  <div className="flex items-center gap-2">
                    <span className="font-mono font-bold text-[11px] px-2 py-0.5 rounded bg-white/80 border border-slate-200/60">
                      {finding.ruleId}
                    </span>
                    <span className="font-bold text-xs">
                      {currentLang === 'en' ? finding.titleEn : currentLang === 'ur-Latn' ? finding.titleUrLatn : finding.titleUr}
                    </span>
                  </div>
                  <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full uppercase ${
                    isWarn ? 'bg-rose-100 text-rose-700' : isCaution ? 'bg-amber-100 text-amber-800' : 'bg-emerald-100 text-emerald-800'
                  }`}>
                    {finding.severity}
                  </span>
                </div>

                <p className={`mt-1 text-slate-700 leading-relaxed ${isUrdu ? 'urdu-text text-sm' : ''}`}>
                  {currentLang === 'en' ? finding.explanationEn : currentLang === 'ur-Latn' ? finding.explanationUrLatn : finding.explanationUr}
                </p>

                <div className="mt-2.5 pt-2 border-t border-slate-200/60 flex flex-wrap items-center justify-between gap-2 text-[11px] text-slate-500">
                  <span><strong>Evidence:</strong> {finding.evidenceSource}</span>
                  <span><strong>Action Owner:</strong> {finding.actionOwner}</span>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* SBAR Query Generator */}
      <div className="glass-card rounded-2xl p-5 border border-slate-200/90 shadow-sm">
        <div className="flex items-center justify-between mb-3">
          <h3 className="text-sm font-bold text-slate-900 flex items-center gap-2">
            <FileText className="w-4 h-4 text-indigo-600" />
            SBAR Prescriber Communication Note
          </h3>
          <button
            onClick={handleCopySbar}
            className="flex items-center gap-1 text-xs font-semibold px-3 py-1.5 bg-indigo-50 hover:bg-indigo-100 text-indigo-700 rounded-lg border border-indigo-200 transition-colors"
          >
            {copiedSbar ? (
              <>
                <Check className="w-3.5 h-3.5 text-emerald-600" /> Copied to Clipboard
              </>
            ) : (
              <>
                <Copy className="w-3.5 h-3.5" /> Copy SBAR Draft
              </>
            )}
          </button>
        </div>

        <pre className="p-3.5 bg-slate-900 text-slate-100 rounded-xl text-xs font-mono whitespace-pre-wrap overflow-x-auto leading-relaxed border border-slate-800">
          {sbarText}
        </pre>
      </div>

      {/* Pharmacist Digital Sign-off */}
      <div className="glass-card rounded-2xl p-5 border border-slate-200/90 shadow-sm">
        <h3 className="text-sm font-bold text-slate-900 mb-3 flex items-center gap-2">
          <CheckCircle2 className="w-4 h-4 text-emerald-600" />
          Clinical Verification & Sign-Off
        </h3>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 mb-3 text-xs">
          <div>
            <label className="block text-slate-600 font-medium mb-1">Pharmacist Name</label>
            <input
              type="text"
              value={pharmacistName}
              onChange={e => setPharmacistName(e.target.value)}
              className="w-full px-3 py-2 bg-white rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>
          <div>
            <label className="block text-slate-600 font-medium mb-1">Pharmacy Council Registration #</label>
            <input
              type="text"
              value={regNumber}
              onChange={e => setRegNumber(e.target.value)}
              className="w-full px-3 py-2 bg-white rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>
        </div>

        <div className="mb-4 text-xs">
          <label className="block text-slate-600 font-medium mb-1">Patient Counseling & Intervention Notes</label>
          <textarea
            rows={3}
            value={counselingNotes}
            onChange={e => setCounselingNotes(e.target.value)}
            placeholder="Document any patient counsel regarding food timing, potential side effects, or prescriber outreach..."
            className="w-full px-3 py-2 bg-white rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500"
          />
        </div>

        <div className="flex items-center justify-between pt-2 border-t border-slate-100">
          <div className="text-xs text-slate-500">
            {isSignedOff ? (
              <span className="text-emerald-700 font-semibold flex items-center gap-1">
                <CheckCircle2 className="w-4 h-4" /> Signed off on {new Date().toLocaleDateString()}
              </span>
            ) : (
              'Digital sign-off archives review to local SQLite/browser storage'
            )}
          </div>
          <button
            onClick={handleSignOff}
            className="px-5 py-2 text-xs font-bold bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl shadow-md transition-all"
          >
            {isSignedOff ? 'Update Sign-Off' : 'Sign Off & Save Review'}
          </button>
        </div>
      </div>
    </div>
  );
};
