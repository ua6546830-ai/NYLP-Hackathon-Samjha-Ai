import React from 'react';
import { BookOpen, Stethoscope, BookmarkCheck, Shield, Sparkles } from 'lucide-react';
import { Language } from '../types';

export type ActiveTab = 'UNDERSTAND' | 'PHARMACIST' | 'SAVED' | 'ACCOUNT';

interface NavigationProps {
  activeTab: ActiveTab;
  onTabChange: (tab: ActiveTab) => void;
  savedCount: number;
  currentLang: Language;
}

export const Navigation: React.FC<NavigationProps> = ({
  activeTab,
  onTabChange,
  savedCount,
  currentLang,
}) => {
  const isUrdu = currentLang === 'ur';

  const tabs: { id: ActiveTab; labelEn: string; labelUrLatn: string; labelUr: string; icon: any }[] = [
    {
      id: 'UNDERSTAND',
      labelEn: 'Understand',
      labelUrLatn: 'Nuskha Samjhein',
      labelUr: 'نسخہ سمجھیں',
      icon: BookOpen,
    },
    {
      id: 'PHARMACIST',
      labelEn: 'Pharmacist Review',
      labelUrLatn: 'Pharmacist Review',
      labelUr: 'فارماسسٹ جائزہ',
      icon: Stethoscope,
    },
    {
      id: 'SAVED',
      labelEn: 'Saved Records',
      labelUrLatn: 'Mehfooz Nuskhe',
      labelUr: 'محفوظ نسخہ جات',
      icon: BookmarkCheck,
    },
    {
      id: 'ACCOUNT',
      labelEn: 'Privacy & Lock',
      labelUrLatn: 'Privacy & Lock',
      labelUr: 'پرائیویسی و لاک',
      icon: Shield,
    },
  ];

  return (
    <nav className="glass-card mb-6 p-1.5 rounded-2xl flex items-center justify-between gap-1 overflow-x-auto max-w-4xl mx-auto border border-slate-200/80 shadow-sm">
      {tabs.map(tab => {
        const Icon = tab.icon;
        const isActive = activeTab === tab.id;
        const label = currentLang === 'en' ? tab.labelEn : currentLang === 'ur-Latn' ? tab.labelUrLatn : tab.labelUr;

        return (
          <button
            key={tab.id}
            onClick={() => onTabChange(tab.id)}
            className={`flex-1 min-w-[100px] flex items-center justify-center gap-2 py-2.5 px-3 rounded-xl font-medium text-xs sm:text-sm transition-all ${
              isActive
                ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/25 font-semibold'
                : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100/70'
            } ${isUrdu ? 'font-urdu' : ''}`}
          >
            <Icon className="w-4 h-4 shrink-0" />
            <span className="truncate">{label}</span>
            {tab.id === 'SAVED' && savedCount > 0 && (
              <span className={`text-[10px] px-1.5 py-0.2 rounded-full font-bold ${
                isActive ? 'bg-indigo-800 text-white' : 'bg-slate-200 text-slate-700'
              }`}>
                {savedCount}
              </span>
            )}
          </button>
        );
      })}
    </nav>
  );
};
