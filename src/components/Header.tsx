import React from 'react';
import { Language } from '../types';
import { Pill, Sparkles, Globe, ShieldCheck } from 'lucide-react';

interface HeaderProps {
  currentLang: Language;
  onLanguageChange: (lang: Language) => void;
}

export const Header: React.FC<HeaderProps> = ({ currentLang, onLanguageChange }) => {
  const isUrdu = currentLang === 'ur';

  return (
    <header className="glass-card-elevated sticky top-0 z-40 px-4 py-3 sm:px-6 sm:py-4 border-b border-slate-200/80 mb-6">
      <div className="max-w-6xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-3">
        {/* Logo & Title */}
        <div className="flex items-center gap-3">
          <div className="w-11 h-11 rounded-2xl bg-gradient-to-tr from-indigo-600 to-violet-500 flex items-center justify-center text-white shadow-md shadow-indigo-500/20">
            <Pill className="w-6 h-6 rotate-45" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900">
                Samjha? Rx <span className="font-urdu text-indigo-600 text-lg sm:text-xl font-normal ml-1">سمجھا؟</span>
              </h1>
              <span className="hidden sm:inline-flex items-center gap-1 text-[11px] font-semibold bg-emerald-50 text-emerald-700 px-2 py-0.5 rounded-full border border-emerald-200/70">
                <ShieldCheck className="w-3.5 h-3.5" /> BNF 84 Verified
              </span>
            </div>
            <p className={`text-xs text-slate-500 ${isUrdu ? 'urdu-text text-sm' : ''}`}>
              {currentLang === 'en' && 'Make your prescription make sense • Patient-first clarity'}
              {currentLang === 'ur-Latn' && 'Nuskha samjhein, be-fikri se sehat mand rahein'}
              {currentLang === 'ur' && 'نسخہ سمجھیں، بے فکری سے صحت مند رہیں'}
            </p>
          </div>
        </div>

        {/* Language Switcher */}
        <div className="flex items-center gap-1.5 bg-slate-100/90 p-1 rounded-xl border border-slate-200/70">
          <Globe className="w-4 h-4 text-slate-400 ml-1.5 hidden sm:block" />
          <button
            onClick={() => onLanguageChange('en')}
            className={`px-3 py-1.5 text-xs font-medium rounded-lg transition-all ${
              currentLang === 'en'
                ? 'bg-white text-indigo-600 shadow-sm font-semibold'
                : 'text-slate-600 hover:text-slate-900 hover:bg-white/50'
            }`}
          >
            English
          </button>
          <button
            onClick={() => onLanguageChange('ur-Latn')}
            className={`px-3 py-1.5 text-xs font-medium rounded-lg transition-all ${
              currentLang === 'ur-Latn'
                ? 'bg-white text-indigo-600 shadow-sm font-semibold'
                : 'text-slate-600 hover:text-slate-900 hover:bg-white/50'
            }`}
          >
            Roman Urdu
          </button>
          <button
            onClick={() => onLanguageChange('ur')}
            className={`px-3 py-1.5 text-xs font-medium rounded-lg transition-all font-urdu ${
              currentLang === 'ur'
                ? 'bg-white text-indigo-600 shadow-sm font-semibold'
                : 'text-slate-600 hover:text-slate-900 hover:bg-white/50'
            }`}
          >
            اردو
          </button>
        </div>
      </div>
    </header>
  );
};
