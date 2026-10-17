import React, { useState } from 'react';
import { DoseScheduleItem, Language } from '../types';
import { Sun, CloudSun, Moon, Sunset, Bell, BellRing, Lock, Clock, Utensils } from 'lucide-react';

interface TimelineGridProps {
  schedule: DoseScheduleItem[];
  isLocked: boolean;
  currentLang: Language;
}

export const TimelineGrid: React.FC<TimelineGridProps> = ({ schedule, isLocked, currentLang }) => {
  const [remindersSet, setRemindersSet] = useState(false);
  const isUrdu = currentLang === 'ur';

  const timeSlots = [
    {
      slot: 'Morning',
      labelEn: 'Morning (Breakfast)',
      labelUrLatn: 'Subah (Nashta)',
      labelUr: 'صبح (ناشتہ)',
      time: '08:00 AM',
      icon: Sun,
      color: 'text-amber-500 bg-amber-50 border-amber-200/80',
    },
    {
      slot: 'Afternoon',
      labelEn: 'Afternoon (Lunch)',
      labelUrLatn: 'Dopehar (Khana)',
      labelUr: 'دوپہر (کھانا)',
      time: '02:00 PM',
      icon: CloudSun,
      color: 'text-sky-500 bg-sky-50 border-sky-200/80',
    },
    {
      slot: 'Evening',
      labelEn: 'Evening (Dinner)',
      labelUrLatn: 'Shaam / Raat (Khana)',
      labelUr: 'شام / رات (کھانا)',
      time: '08:00 PM',
      icon: Sunset,
      color: 'text-indigo-500 bg-indigo-50 border-indigo-200/80',
    },
    {
      slot: 'Bedtime',
      labelEn: 'Night (Bedtime)',
      labelUrLatn: 'Soney se pehle',
      labelUr: 'رات (سونے سے قبل)',
      time: '10:30 PM',
      icon: Moon,
      color: 'text-purple-500 bg-purple-50 border-purple-200/80',
    },
  ];

  const handleToggleReminders = () => {
    if ('Notification' in window && Notification.permission !== 'granted') {
      Notification.requestPermission();
    }
    setRemindersSet(!remindersSet);
  };

  return (
    <div className="glass-card rounded-2xl p-4 sm:p-6 mb-6 border border-slate-200/90 shadow-sm">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-5 pb-3 border-b border-slate-200/70">
        <div>
          <h2 className="text-base sm:text-lg font-bold text-slate-900 flex items-center gap-2">
            <Clock className="w-5 h-5 text-indigo-600" />
            {currentLang === 'en' && 'Visual 24-Hour Dosing Timeline'}
            {currentLang === 'ur-Latn' && '24-Ghantay ka Dawa Schedule'}
            {currentLang === 'ur' && 'چوبیس گھنٹے کا روزانہ شیڈول اور اوقات'}
          </h2>
          <p className="text-xs text-slate-500 mt-0.5">
            {currentLang === 'en' && 'Meal-timed medication administration schedule'}
            {currentLang === 'ur-Latn' && 'Khanay ke auqaat ke mutabiq dawai lene ka waqt'}
            {currentLang === 'ur' && 'کھانے کے اوقات کے مطابق ادویات کے درست شیڈول'}
          </p>
        </div>

        {!isLocked && (
          <button
            onClick={handleToggleReminders}
            className={`flex items-center gap-1.5 text-xs font-semibold px-3 py-1.5 rounded-lg border transition-all ${
              remindersSet
                ? 'bg-emerald-50 text-emerald-700 border-emerald-300'
                : 'bg-indigo-50 hover:bg-indigo-100/80 text-indigo-700 border-indigo-200'
            }`}
          >
            {remindersSet ? (
              <>
                <BellRing className="w-3.5 h-3.5 text-emerald-600" /> Reminders Active
              </>
            ) : (
              <>
                <Bell className="w-3.5 h-3.5" /> Set Dose Alarms
              </>
            )}
          </button>
        )}
      </div>

      {isLocked ? (
        <div className="p-6 rounded-xl bg-amber-50/70 border border-amber-200 text-center flex flex-col items-center justify-center">
          <div className="w-12 h-12 rounded-full bg-amber-100 text-amber-700 flex items-center justify-center mb-3">
            <Lock className="w-6 h-6" />
          </div>
          <h3 className="text-sm font-bold text-amber-900">
            {currentLang === 'en' && 'Timeline Locked for Patient Safety'}
            {currentLang === 'ur-Latn' && 'Schedule Hifazat ki Khatir Band Hai'}
            {currentLang === 'ur' && 'مریض کے تحفظ کے لیے شیڈول مقفل ہے'}
          </h3>
          <p className="text-xs text-amber-800/90 max-w-md mt-1 leading-relaxed">
            {currentLang === 'en' && 'Please confirm all medicine strengths and frequencies in the checklist above to unlock your daily timeline.'}
            {currentLang === 'ur-Latn' && 'Upar di gayi fehrist mein har dawai ki tasdeeq karein taakay waqt ka timeline khul sakay.'}
            {currentLang === 'ur' && 'براہ کرم اوپر دی گئی فہرست میں تمام ادویات کی خوراک اور طاقت کی تصدیق کریں تاکہ شیڈول جاری ہو سکے۔'}
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {timeSlots.map(slot => {
            const SlotIcon = slot.icon;
            const slotItems = schedule.filter(s => s.timeSlot === slot.slot);
            const slotLabel = currentLang === 'en' ? slot.labelEn : currentLang === 'ur-Latn' ? slot.labelUrLatn : slot.labelUr;

            return (
              <div
                key={slot.slot}
                className="bg-white/80 rounded-xl p-4 border border-slate-200/80 flex flex-col justify-between shadow-sm"
              >
                <div>
                  <div className="flex items-center justify-between pb-2.5 mb-2.5 border-b border-slate-100">
                    <div className="flex items-center gap-2">
                      <div className={`p-1.5 rounded-lg border ${slot.color}`}>
                        <SlotIcon className="w-4 h-4" />
                      </div>
                      <div>
                        <div className={`text-xs font-bold text-slate-800 ${isUrdu ? 'font-urdu' : ''}`}>
                          {slotLabel}
                        </div>
                        <div className="text-[10px] text-slate-400 font-medium">
                          {slot.time}
                        </div>
                      </div>
                    </div>
                  </div>

                  <div className="space-y-2 mt-2">
                    {slotItems.length === 0 ? (
                      <p className="text-[11px] text-slate-400 italic py-2 text-center">
                        No medicines scheduled
                      </p>
                    ) : (
                      slotItems.map(item => (
                        <div
                          key={item.id}
                          className="p-2 rounded-lg bg-slate-50 border border-slate-200/60 text-xs"
                        >
                          <div className="font-semibold text-slate-900 truncate">
                            {item.drugName}
                          </div>
                          <div className="flex items-center gap-1 text-[11px] text-indigo-600 font-medium mt-0.5">
                            <Utensils className="w-3 h-3 text-indigo-500" />
                            <span className={isUrdu ? 'font-urdu' : ''}>
                              {currentLang === 'en' ? item.foodTiming : currentLang === 'ur-Latn' ? item.foodTimingUrLatn : item.foodTimingUr}
                            </span>
                          </div>
                        </div>
                      ))
                    )}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
