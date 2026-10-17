import React from 'react';
import { PrescriptionReview, Language } from '../types';
import { BookmarkCheck, Trash2, ExternalLink, Calendar, ShieldCheck, AlertCircle } from 'lucide-react';

interface SavedPrescriptionsProps {
  reviews: PrescriptionReview[];
  onLoadReview: (review: PrescriptionReview) => void;
  onDeleteReview: (id: string) => void;
  onClearAll: () => void;
  currentLang: Language;
}

export const SavedPrescriptions: React.FC<SavedPrescriptionsProps> = ({
  reviews,
  onLoadReview,
  onDeleteReview,
  onClearAll,
  currentLang,
}) => {
  const isUrdu = currentLang === 'ur';

  if (reviews.length === 0) {
    return (
      <div className="glass-card rounded-2xl p-8 text-center max-w-md mx-auto border border-slate-200">
        <div className="w-12 h-12 rounded-full bg-slate-100 text-slate-400 flex items-center justify-center mx-auto mb-3">
          <BookmarkCheck className="w-6 h-6" />
        </div>
        <h3 className="text-sm font-bold text-slate-800">No Saved Prescriptions</h3>
        <p className="text-xs text-slate-500 mt-1">
          Prescriptions analyzed or confirmed in the Understand tab can be saved locally on this device.
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between pb-2">
        <h2 className="text-base font-bold text-slate-900 flex items-center gap-2">
          <BookmarkCheck className="w-5 h-5 text-indigo-600" />
          Saved Prescription Reviews ({reviews.length})
        </h2>
        <button
          onClick={onClearAll}
          className="text-xs text-rose-600 hover:text-rose-700 hover:bg-rose-50 px-2.5 py-1 rounded-lg border border-rose-200/60"
        >
          Clear All
        </button>
      </div>

      <div className="space-y-3">
        {reviews.map(review => {
          const hasFlags = review.findings.some(f => f.status === 'FLAG');
          const hasUncert = review.findings.some(f => f.status === 'UNABLE_TO_CHECK');

          return (
            <div
              key={review.id}
              className="glass-card rounded-xl p-4 border border-slate-200/90 shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-3 hover:border-indigo-200 transition-all"
            >
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <h3 className="text-sm font-bold text-slate-900">{review.title}</h3>
                  {hasUncert ? (
                    <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-rose-100 text-rose-700">
                      Uncertainty Flagged
                    </span>
                  ) : hasFlags ? (
                    <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-amber-100 text-amber-700">
                      Cautions Active
                    </span>
                  ) : (
                    <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-emerald-100 text-emerald-700">
                      No Issues
                    </span>
                  )}
                </div>

                <div className="flex flex-wrap items-center gap-2 text-xs text-slate-500">
                  <span className="flex items-center gap-1">
                    <Calendar className="w-3.5 h-3.5" />
                    {new Date(review.timestamp).toLocaleDateString()}
                  </span>
                  <span>•</span>
                  <span>{review.items.length} medicines</span>
                  {review.isSignedOff && (
                    <>
                      <span>•</span>
                      <span className="text-emerald-600 font-semibold flex items-center gap-0.5">
                        <ShieldCheck className="w-3 h-3" /> Signed off by {review.signedOffBy}
                      </span>
                    </>
                  )}
                </div>

                <div className="flex flex-wrap gap-1.5 mt-2">
                  {review.items.map(item => (
                    <span
                      key={item.id}
                      className="text-[11px] px-2 py-0.5 rounded bg-slate-100 text-slate-700 font-medium"
                    >
                      {item.drugName} {item.strength}
                    </span>
                  ))}
                </div>
              </div>

              <div className="flex items-center gap-2 self-end sm:self-auto shrink-0">
                <button
                  onClick={() => onLoadReview(review)}
                  className="px-3 py-1.5 text-xs font-semibold bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg shadow-sm"
                >
                  View Details
                </button>
                <button
                  onClick={() => onDeleteReview(review.id)}
                  className="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
