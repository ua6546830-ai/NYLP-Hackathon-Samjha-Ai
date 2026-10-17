import React, { useState } from 'react';
import { Shield, Lock, Unlock, Download, FileSpreadsheet, ExternalLink, KeyRound, CheckCircle } from 'lucide-react';
import { Language } from '../types';

interface AccountPrivacyProps {
  currentLang: Language;
  pinLock: string | null;
  onSetPinLock: (pin: string | null) => void;
}

export const AccountPrivacy: React.FC<AccountPrivacyProps> = ({
  currentLang,
  pinLock,
  onSetPinLock,
}) => {
  const [pinInput, setPinInput] = useState('');
  const [showPinModal, setShowPinModal] = useState(false);
  const [pinSuccess, setPinSuccess] = useState(false);

  const handleSavePin = () => {
    if (pinInput.length === 4) {
      onSetPinLock(pinInput);
      setPinSuccess(true);
      setTimeout(() => {
        setPinSuccess(false);
        setShowPinModal(false);
        setPinInput('');
      }, 1000);
    }
  };

  const handleDisablePin = () => {
    onSetPinLock(null);
  };

  return (
    <div className="space-y-6 max-w-3xl mx-auto">
      {/* Privacy Center */}
      <div className="glass-card-elevated rounded-2xl p-6 border border-slate-200">
        <div className="flex items-center gap-3 mb-4 pb-3 border-b border-slate-200">
          <div className="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center border border-emerald-200/80">
            <Shield className="w-5 h-5" />
          </div>
          <div>
            <h2 className="text-base font-bold text-slate-900">Privacy & Security Sandbox</h2>
            <p className="text-xs text-slate-500">Truthful disclosures regarding data custody and device storage</p>
          </div>
        </div>

        <div className="space-y-3 text-xs text-slate-600 leading-relaxed">
          <div className="p-3 bg-slate-50 rounded-xl border border-slate-200/70">
            <div className="font-semibold text-slate-800 mb-1">🔒 Local On-Device Persistence</div>
            <p>
              Your prescriptions are stored exclusively in your browser/device local database sandbox. No health records are sold, profiled, or transmitted to third-party advertising networks.
            </p>
          </div>

          <div className="p-3 bg-slate-50 rounded-xl border border-slate-200/70">
            <div className="font-semibold text-slate-800 mb-1">🚫 Zero Advertising SDKs</div>
            <p>
              Samjha? Rx operates with strict clinical neutrality. There are zero tracking pixels, behavioral cookies, or commercial sponsored drug placements.
            </p>
          </div>
        </div>
      </div>

      {/* Local Device PIN Lock */}
      <div className="glass-card rounded-2xl p-6 border border-slate-200 shadow-sm">
        <div className="flex items-center justify-between mb-4 pb-3 border-b border-slate-200">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-indigo-50 text-indigo-600 flex items-center justify-center border border-indigo-200/80">
              <KeyRound className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-bold text-slate-900">Device Security PIN Lock</h2>
              <p className="text-xs text-slate-500">Prevent casual shoulder-surfing on shared household devices</p>
            </div>
          </div>

          {pinLock ? (
            <button
              onClick={handleDisablePin}
              className="text-xs font-semibold px-3 py-1.5 rounded-lg bg-rose-50 text-rose-700 border border-rose-200 hover:bg-rose-100"
            >
              Disable PIN
            </button>
          ) : (
            <button
              onClick={() => setShowPinModal(true)}
              className="text-xs font-semibold px-4 py-1.5 rounded-lg bg-indigo-600 text-white shadow-sm hover:bg-indigo-700"
            >
              Set 4-Digit PIN
            </button>
          )}
        </div>

        <div className="text-xs text-slate-600">
          {pinLock ? (
            <div className="flex items-center gap-2 text-emerald-700 bg-emerald-50 p-3 rounded-xl border border-emerald-200 font-medium">
              <CheckCircle className="w-4 h-4 text-emerald-600" />
              <span>PIN Protection is ACTIVE. The app prompts for your PIN when re-opened.</span>
            </div>
          ) : (
            <p>
              No passcode is currently configured. Anyone with access to this browser can view saved prescription reviews.
            </p>
          )}
        </div>

        {showPinModal && (
          <div className="mt-4 p-4 rounded-xl bg-indigo-50/70 border border-indigo-200 text-xs">
            <label className="block text-slate-700 font-semibold mb-1">Enter 4-Digit Passcode</label>
            <div className="flex items-center gap-2">
              <input
                type="password"
                maxLength={4}
                value={pinInput}
                onChange={e => setPinInput(e.target.value.replace(/\D/g, ''))}
                placeholder="••••"
                className="w-28 text-center text-lg tracking-widest px-3 py-1.5 bg-white rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
              <button
                onClick={handleSavePin}
                disabled={pinInput.length !== 4}
                className="px-4 py-2 font-bold bg-indigo-600 text-white rounded-lg shadow-sm hover:bg-indigo-700 disabled:opacity-50"
              >
                {pinSuccess ? 'Saved!' : 'Activate Lock'}
              </button>
              <button
                onClick={() => setShowPinModal(false)}
                className="px-3 py-2 text-slate-600 hover:bg-slate-200 rounded-lg"
              >
                Cancel
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Presentation & Code Download Hub */}
      <div className="glass-card rounded-2xl p-6 border border-slate-200 shadow-sm">
        <h3 className="text-sm font-bold text-slate-900 mb-2 flex items-center gap-2">
          <Download className="w-4 h-4 text-indigo-600" />
          Project Artifacts & Distribution Files
        </h3>
        <p className="text-xs text-slate-500 mb-4">
          Download the investor pitch deck PowerPoint file and the clean GitHub-ready distribution bundle.
        </p>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs">
          <a
            href="pitch/Samjha_Rx_Pitch_Deck.pptx"
            download
            className="p-3.5 rounded-xl border border-slate-200 bg-white hover:border-indigo-300 hover:bg-indigo-50/40 flex items-center gap-3 transition-all"
          >
            <div className="w-9 h-9 rounded-lg bg-indigo-100 text-indigo-700 flex items-center justify-center shrink-0">
              <FileSpreadsheet className="w-5 h-5" />
            </div>
            <div>
              <div className="font-bold text-slate-900">Pitch Deck (.pptx)</div>
              <div className="text-[11px] text-slate-500">10 slides with speaker notes</div>
            </div>
          </a>

          <a
            href="samjha_rx_github_ready_package.zip"
            download
            className="p-3.5 rounded-xl border border-slate-200 bg-white hover:border-indigo-300 hover:bg-indigo-50/40 flex items-center gap-3 transition-all"
          >
            <div className="w-9 h-9 rounded-lg bg-emerald-100 text-emerald-700 flex items-center justify-center shrink-0">
              <Download className="w-5 h-5" />
            </div>
            <div>
              <div className="font-bold text-slate-900">GitHub Ready Package (.zip)</div>
              <div className="text-[11px] text-slate-500">80 source files & CI/CD workflow</div>
            </div>
          </a>
        </div>
      </div>
    </div>
  );
};
