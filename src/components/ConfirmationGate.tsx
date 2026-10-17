import React, { useState } from 'react';
import { PrescriptionItem, Language } from '../types';
import { CheckCircle2, AlertTriangle, Edit3, Lock, Unlock, HelpCircle, Check, X } from 'lucide-react';

interface ConfirmationGateProps {
  items: PrescriptionItem[];
  onConfirmItem: (id: string) => void;
  onUpdateItem: (updatedItem: PrescriptionItem) => void;
  onEscalateToPharmacist: () => void;
  currentLang: Language;
}

export const ConfirmationGate: React.FC<ConfirmationGateProps> = ({
  items,
  onConfirmItem,
  onUpdateItem,
  onEscalateToPharmacist,
  currentLang,
}) => {
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editForm, setEditForm] = useState<Partial<PrescriptionItem>>({});

  const isUrdu = currentLang === 'ur';
  const hasUnconfirmed = items.some(i => !i.isConfirmed || i.hasUncertainty);

  const startEdit = (item: PrescriptionItem) => {
    setEditingId(item.id);
    setEditForm({ ...item });
  };

  const saveEdit = () => {
    if (!editingId) return;
    const original = items.find(i => i.id === editingId);
    if (!original) return;

    const updated: PrescriptionItem = {
      ...original,
      ...editForm,
      isConfirmed: true,
      hasUncertainty: false,
      uncertaintyNotes: undefined,
    } as PrescriptionItem;

    onUpdateItem(updated);
    setEditingId(null);
  };

  return (
    <div className="glass-card rounded-2xl p-4 sm:p-6 mb-6 border border-slate-200/90 shadow-sm">
      {/* Header Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-4 pb-3 border-b border-slate-200/70">
        <div>
          <div className="flex items-center gap-2">
            <h2 className="text-base sm:text-lg font-bold text-slate-900">
              {currentLang === 'en' && 'Human-in-the-Loop Confirmation Gate'}
              {currentLang === 'ur-Latn' && 'Dawaiyon ki Tasdeeqi Fehrist'}
              {currentLang === 'ur' && 'ادویات کی تصدیقی فہرست اور پڑتال'}
            </h2>
            {hasUnconfirmed ? (
              <span className="inline-flex items-center gap-1 text-xs font-semibold bg-amber-50 text-amber-700 px-2.5 py-0.5 rounded-full border border-amber-200">
                <Lock className="w-3.5 h-3.5" /> Schedule Locked
              </span>
            ) : (
              <span className="inline-flex items-center gap-1 text-xs font-semibold bg-emerald-50 text-emerald-700 px-2.5 py-0.5 rounded-full border border-emerald-200">
                <Unlock className="w-3.5 h-3.5" /> All Verified
              </span>
            )}
          </div>
          <p className="text-xs text-slate-500 mt-1">
            {currentLang === 'en' && 'Verify extracted fields one-by-one. Missing or unclear parameters are never guessed.'}
            {currentLang === 'ur-Latn' && 'Har dawai ki taaqat aur waqt ki tasdeeq karein. Koi ghalat andaaza nahi lagaya jayega.'}
            {currentLang === 'ur' && 'ہر دوا کے نام، طاقت اور وقت کی تصدیق کریں۔ کوئی فرضی اندازہ نہیں لگایا جاتا۔'}
          </p>
        </div>

        {hasUnconfirmed && (
          <button
            onClick={onEscalateToPharmacist}
            className="self-start sm:self-auto text-xs font-semibold text-indigo-600 hover:text-indigo-700 bg-indigo-50 hover:bg-indigo-100/70 px-3 py-1.5 rounded-lg border border-indigo-200/60 transition-colors"
          >
            {currentLang === 'en' && 'Ask a Pharmacist →'}
            {currentLang === 'ur-Latn' && 'Pharmacist se rabta karein →'}
            {currentLang === 'ur' && 'فارماسسٹ سے مدد لیں ←'}
          </button>
        )}
      </div>

      {/* Items List */}
      <div className="space-y-3">
        {items.map((item, index) => {
          const isEditing = editingId === item.id;
          const instruction = currentLang === 'en' ? item.instructionsEn : currentLang === 'ur-Latn' ? item.instructionsUrLatn : item.instructionsUr;

          if (isEditing) {
            return (
              <div key={item.id} className="p-4 rounded-xl bg-indigo-50/60 border border-indigo-200 space-y-3">
                <div className="font-semibold text-xs text-indigo-900">
                  Editing: {item.drugName}
                </div>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs">
                  <div>
                    <label className="block text-slate-600 font-medium mb-1">Drug Name</label>
                    <input
                      type="text"
                      value={editForm.drugName || ''}
                      onChange={e => setEditForm({ ...editForm, drugName: e.target.value })}
                      className="w-full px-3 py-1.5 bg-white rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    />
                  </div>
                  <div>
                    <label className="block text-slate-600 font-medium mb-1">Strength (e.g. 500mg)</label>
                    <input
                      type="text"
                      value={editForm.strength || ''}
                      onChange={e => setEditForm({ ...editForm, strength: e.target.value })}
                      className="w-full px-3 py-1.5 bg-white rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    />
                  </div>
                  <div>
                    <label className="block text-slate-600 font-medium mb-1">Dose Amount (e.g. 1 tab)</label>
                    <input
                      type="text"
                      value={editForm.doseAmount || ''}
                      onChange={e => setEditForm({ ...editForm, doseAmount: e.target.value })}
                      className="w-full px-3 py-1.5 bg-white rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    />
                  </div>
                  <div>
                    <label className="block text-slate-600 font-medium mb-1">Frequency (OD, BD, TDS)</label>
                    <input
                      type="text"
                      value={editForm.frequency || ''}
                      onChange={e => setEditForm({ ...editForm, frequency: e.target.value })}
                      className="w-full px-3 py-1.5 bg-white rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    />
                  </div>
                </div>
                <div className="flex justify-end gap-2 pt-2">
                  <button
                    onClick={() => setEditingId(null)}
                    className="px-3 py-1.5 text-xs text-slate-600 hover:bg-slate-200/50 rounded-lg flex items-center gap-1"
                  >
                    <X className="w-3.5 h-3.5" /> Cancel
                  </button>
                  <button
                    onClick={saveEdit}
                    className="px-4 py-1.5 text-xs font-semibold bg-indigo-600 text-white hover:bg-indigo-700 rounded-lg shadow-sm flex items-center gap-1"
                  >
                    <Check className="w-3.5 h-3.5" /> Save & Confirm
                  </button>
                </div>
              </div>
            );
          }

          return (
            <div
              key={item.id}
              className={`p-3.5 sm:p-4 rounded-xl border transition-all ${
                item.hasUncertainty
                  ? 'bg-rose-50/50 border-rose-200/80'
                  : item.isConfirmed
                  ? 'bg-white/90 border-slate-200/90'
                  : 'bg-amber-50/40 border-amber-200/80'
              }`}
            >
              <div className="flex items-start justify-between gap-3">
                <div className="flex items-start gap-2.5">
                  <span className="w-5 h-5 rounded-full bg-slate-100 text-slate-600 text-[11px] font-bold flex items-center justify-center shrink-0 mt-0.5">
                    {index + 1}
                  </span>
                  <div>
                    <div className="flex flex-wrap items-center gap-2">
                      <h3 className="text-sm font-bold text-slate-900">
                        {item.drugName}
                      </h3>
                      <span className="text-xs px-2 py-0.5 rounded-md bg-slate-100 text-slate-700 font-medium">
                        {item.strength}
                      </span>
                      <span className="text-xs px-2 py-0.5 rounded-md bg-indigo-50 text-indigo-700 font-medium">
                        {item.frequency}
                      </span>
                      <span className="text-[11px] text-slate-400 italic">
                        {item.genericName}
                      </span>
                    </div>

                    <p className={`text-xs text-slate-600 mt-1.5 leading-relaxed ${isUrdu ? 'urdu-text text-sm' : ''}`}>
                      {instruction}
                    </p>

                    {item.hasUncertainty && item.uncertaintyNotes && (
                      <div className="mt-2 text-xs flex items-start gap-1.5 text-rose-700 bg-rose-100/70 p-2 rounded-lg font-medium">
                        <AlertTriangle className="w-4 h-4 shrink-0 text-rose-600 mt-0.5" />
                        <span>{item.uncertaintyNotes}</span>
                      </div>
                    )}
                  </div>
                </div>

                {/* Actions */}
                <div className="flex items-center gap-1.5 shrink-0">
                  <button
                    onClick={() => startEdit(item)}
                    className="p-1.5 text-slate-400 hover:text-slate-700 hover:bg-slate-100 rounded-lg transition-colors"
                    title="Edit fields"
                  >
                    <Edit3 className="w-4 h-4" />
                  </button>
                  {item.isConfirmed && !item.hasUncertainty ? (
                    <span className="inline-flex items-center gap-1 text-xs font-semibold text-emerald-700 bg-emerald-50 px-2.5 py-1 rounded-lg border border-emerald-200">
                      <CheckCircle2 className="w-4 h-4 text-emerald-600" /> Confirmed
                    </span>
                  ) : (
                    <button
                      onClick={() => onConfirmItem(item.id)}
                      className="px-3 py-1 text-xs font-semibold bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg shadow-sm transition-all"
                    >
                      Confirm
                    </button>
                  )}
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
