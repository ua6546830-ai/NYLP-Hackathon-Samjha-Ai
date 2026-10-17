import React, { useRef, useState, useEffect } from 'react';
import { Camera, Upload, X, RefreshCw, CheckCircle, AlertCircle } from 'lucide-react';
import { Language } from '../types';

interface CameraModalProps {
  isOpen: boolean;
  onClose: () => void;
  onImageCaptured: (imageDataUrl: string) => void;
  currentLang: Language;
}

export const CameraModal: React.FC<CameraModalProps> = ({
  isOpen,
  onClose,
  onImageCaptured,
  currentLang,
}) => {
  const videoRef = useRef<HTMLVideoElement>(null);
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const [stream, setStream] = useState<MediaStream | null>(null);
  const [cameraError, setCameraError] = useState<string | null>(null);
  const [capturedImage, setCapturedImage] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen) {
      startCamera();
    } else {
      stopCamera();
      setCapturedImage(null);
      setCameraError(null);
    }
    return () => {
      stopCamera();
    };
  }, [isOpen]);

  const startCamera = async () => {
    try {
      setCameraError(null);
      const mediaStream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'environment', width: { ideal: 1280 }, height: { ideal: 720 } },
      });
      setStream(mediaStream);
      if (videoRef.current) {
        videoRef.current.srcObject = mediaStream;
      }
    } catch (err: any) {
      console.warn('Camera access denied or unavailable:', err);
      setCameraError('Camera access not granted. You can still upload a photo below.');
    }
  };

  const stopCamera = () => {
    if (stream) {
      stream.getTracks().forEach(track => track.stop());
      setStream(null);
    }
  };

  const captureFrame = () => {
    if (!videoRef.current || !canvasRef.current) return;
    const video = videoRef.current;
    const canvas = canvasRef.current;
    canvas.width = video.videoWidth || 640;
    canvas.height = video.videoHeight || 480;
    const ctx = canvas.getContext('2d');
    if (ctx) {
      ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
      const dataUrl = canvas.toDataURL('image/jpeg', 0.85);
      setCapturedImage(dataUrl);
      stopCamera();
    }
  };

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = () => {
        if (typeof reader.result === 'string') {
          setCapturedImage(reader.result);
          stopCamera();
        }
      };
      reader.readAsDataURL(file);
    }
  };

  const confirmCapture = () => {
    if (capturedImage) {
      onImageCaptured(capturedImage);
      onClose();
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-in fade-in">
      <div className="glass-card-elevated rounded-2xl w-full max-w-lg overflow-hidden border border-slate-200 shadow-2xl">
        {/* Modal Header */}
        <div className="flex items-center justify-between px-5 py-3.5 border-b border-slate-200/80 bg-slate-50/50">
          <div className="flex items-center gap-2">
            <Camera className="w-5 h-5 text-indigo-600" />
            <h3 className="font-bold text-sm text-slate-800">
              {currentLang === 'en' && 'Capture Prescription Label'}
              {currentLang === 'ur-Latn' && 'Nuskhe ki Tasweer lein'}
              {currentLang === 'ur' && 'نسخے کی تصویر لیں'}
            </h3>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-slate-400 hover:text-slate-700 hover:bg-slate-200/60"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Viewfinder or Preview */}
        <div className="relative bg-slate-950 aspect-[4/3] flex items-center justify-center overflow-hidden">
          {capturedImage ? (
            <img src={capturedImage} alt="Prescription" className="w-full h-full object-contain" />
          ) : stream ? (
            <>
              <video
                ref={videoRef}
                autoPlay
                playsInline
                muted
                className="w-full h-full object-cover"
              />
              {/* Reticle Guide */}
              <div className="absolute inset-8 border-2 border-dashed border-indigo-400/80 rounded-2xl pointer-events-none flex flex-col items-center justify-between p-3">
                <span className="text-[11px] text-white bg-slate-900/70 px-2.5 py-0.5 rounded-full backdrop-blur-sm font-medium">
                  Align prescription label inside guide
                </span>
                <span className="text-[10px] text-white/80">Ensure good lighting</span>
              </div>
            </>
          ) : (
            <div className="p-6 text-center text-slate-400 max-w-xs">
              <Camera className="w-12 h-12 mx-auto mb-2 opacity-40 text-indigo-300" />
              <p className="text-xs mb-3">
                {cameraError || 'Initializing camera stream...'}
              </p>
            </div>
          )}
          <canvas ref={canvasRef} className="hidden" />
        </div>

        {/* Modal Footer Controls */}
        <div className="p-4 bg-white flex items-center justify-between gap-3 border-t border-slate-100">
          <label className="cursor-pointer flex items-center gap-1.5 text-xs font-medium text-slate-600 hover:text-indigo-600 hover:bg-slate-100 px-3 py-2 rounded-xl border border-slate-200">
            <Upload className="w-4 h-4" />
            <span>Upload Photo</span>
            <input
              type="file"
              accept="image/*"
              onChange={handleFileUpload}
              className="hidden"
            />
          </label>

          {capturedImage ? (
            <div className="flex items-center gap-2">
              <button
                onClick={() => {
                  setCapturedImage(null);
                  startCamera();
                }}
                className="flex items-center gap-1 text-xs px-3 py-2 text-slate-600 hover:bg-slate-100 rounded-xl"
              >
                <RefreshCw className="w-3.5 h-3.5" /> Retake
              </button>
              <button
                onClick={confirmCapture}
                className="flex items-center gap-1.5 text-xs font-semibold px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl shadow-md"
              >
                <CheckCircle className="w-4 h-4" /> Analyze Prescription
              </button>
            </div>
          ) : (
            <button
              onClick={captureFrame}
              disabled={!stream}
              className="flex items-center gap-2 text-xs font-bold px-5 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl shadow-md disabled:opacity-50"
            >
              <Camera className="w-4 h-4" /> Snap Photo
            </button>
          )}
        </div>
      </div>
    </div>
  );
};
