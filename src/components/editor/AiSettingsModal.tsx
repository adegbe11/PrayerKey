'use client';

import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { X, Zap, ExternalLink, CheckCircle2, AlertCircle, Loader2 } from 'lucide-react';
import { loadGroqKey, saveGroqKey, loadGroqModel, saveGroqModel } from '@/lib/storage';
import { GROQ_MODELS, DEFAULT_GROQ_MODEL, validateGroqKey } from '@/lib/groq';

interface Props {
  open: boolean;
  onClose: () => void;
  onSaved?: () => void;
}

export default function AiSettingsModal({ open, onClose, onSaved }: Props) {
  const [key, setKey] = useState('');
  const [model, setModel] = useState<string>(DEFAULT_GROQ_MODEL);
  const [check, setCheck] = useState<'idle' | 'checking' | 'ok' | 'bad'>('idle');
  const [touched, setTouched] = useState(false);

  useEffect(() => {
    if (!open) return;
    setKey(loadGroqKey());
    setModel(loadGroqModel() || DEFAULT_GROQ_MODEL);
    setCheck('idle');
    setTouched(false);
  }, [open]);

  const handleCheck = async () => {
    if (!key.trim()) {
      setCheck('bad');
      return;
    }
    setCheck('checking');
    const ok = await validateGroqKey(key.trim());
    setCheck(ok ? 'ok' : 'bad');
  };

  const handleSave = () => {
    saveGroqKey(key.trim());
    saveGroqModel(model);
    onSaved?.();
    onClose();
  };

  const handleClear = () => {
    setKey('');
    saveGroqKey('');
    setTouched(true);
    setCheck('idle');
  };

  if (!open) return null;

  return (
    <>
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        className="fixed inset-0 z-[60]"
        style={{
          background: 'rgba(5,3,12,0.45)',
          backdropFilter: 'blur(24px) saturate(160%)',
          WebkitBackdropFilter: 'blur(24px) saturate(160%)',
        }}
        onClick={onClose}
      />
      <motion.div
        initial={{ opacity: 0, scale: 0.96, y: 10 }}
        animate={{ opacity: 1, scale: 1, y: 0 }}
        exit={{ opacity: 0, scale: 0.96, y: 10 }}
        transition={{ duration: 0.2, ease: [0.16, 1, 0.3, 1] }}
        className="fixed inset-0 z-[61] flex items-center justify-center p-4 pointer-events-none"
      >
        <div
          className="glass-strong pointer-events-auto w-full max-w-md overflow-hidden"
          style={{ borderRadius: '20px' }}
          onClick={(e) => e.stopPropagation()}
        >
          <div
            className="flex items-center justify-between px-5 py-4"
            style={{ borderBottom: '1px solid rgba(255,255,255,0.07)' }}
          >
            <div className="flex items-center gap-2">
              <div
                className="w-7 h-7 flex items-center justify-center"
                style={{
                  background: 'linear-gradient(135deg,#F97316 0%,#EAB308 100%)',
                  borderRadius: '8px',
                  color: '#FFF',
                }}
              >
                <Zap size={14} fill="#FFF" />
              </div>
              <div>
                <h2 className="text-sm font-semibold" style={{ color: 'var(--text-primary)' }}>
                  AI settings
                </h2>
                <p className="text-xs" style={{ color: 'var(--text-muted)', fontSize: '10px' }}>
                  Powered by Groq · free & fast
                </p>
              </div>
            </div>
            <button
              onClick={onClose}
              className="w-8 h-8 rounded-lg flex items-center justify-center"
              style={{ background: 'rgba(255,255,255,0.05)', color: 'var(--text-muted)' }}
            >
              <X size={14} />
            </button>
          </div>

          <div className="p-5 flex flex-col gap-4">
            <div
              className="glass-card p-3"
              style={{ padding: '10px 12px' }}
            >
              <p className="text-xs" style={{ color: 'var(--text-secondary)', lineHeight: 1.5 }}>
                Paste your free Groq API key. Booksane calls Groq directly from your browser —
                nothing is sent to our servers. Get one at{' '}
                <a
                  href="https://console.groq.com/keys"
                  target="_blank"
                  rel="noopener noreferrer"
                  style={{ color: '#A78BFA', textDecoration: 'underline', display: 'inline-flex', alignItems: 'center', gap: '2px' }}
                >
                  console.groq.com/keys
                  <ExternalLink size={9} />
                </a>
                .
              </p>
            </div>

            <label className="flex flex-col gap-1.5">
              <span className="font-semibold uppercase tracking-widest" style={{ fontSize: '9px', color: 'var(--text-muted)', letterSpacing: '0.12em' }}>
                Groq API Key
              </span>
              <div className="flex gap-2">
                <input
                  type="password"
                  value={key}
                  onChange={(e) => { setKey(e.target.value); setTouched(true); setCheck('idle'); }}
                  placeholder="gsk_..."
                  className="flex-1 focus:outline-none"
                  style={{
                    background: 'rgba(255,255,255,0.04)',
                    border: '1px solid rgba(255,255,255,0.1)',
                    color: 'var(--text-primary)',
                    padding: '8px 10px',
                    borderRadius: '8px',
                    fontSize: '12px',
                    fontFamily: 'monospace',
                  }}
                />
                <button
                  onClick={handleCheck}
                  disabled={!key.trim() || check === 'checking'}
                  className="text-xs font-semibold shrink-0"
                  style={{
                    padding: '0 12px',
                    background: 'rgba(124,58,237,0.12)',
                    border: '1px solid rgba(124,58,237,0.35)',
                    color: '#C4B5FD',
                    borderRadius: '8px',
                    opacity: !key.trim() || check === 'checking' ? 0.5 : 1,
                    display: 'flex',
                    alignItems: 'center',
                    gap: '4px',
                  }}
                >
                  {check === 'checking' ? <Loader2 size={11} className="animate-spin" /> : null}
                  Test
                </button>
              </div>
              {check === 'ok' && (
                <span className="text-xs flex items-center gap-1" style={{ color: '#4ADE80', fontSize: '11px' }}>
                  <CheckCircle2 size={11} />
                  Key is valid
                </span>
              )}
              {check === 'bad' && (
                <span className="text-xs flex items-center gap-1" style={{ color: '#FCA5A5', fontSize: '11px' }}>
                  <AlertCircle size={11} />
                  Invalid key — double-check it
                </span>
              )}
            </label>

            <label className="flex flex-col gap-1.5">
              <span className="font-semibold uppercase tracking-widest" style={{ fontSize: '9px', color: 'var(--text-muted)', letterSpacing: '0.12em' }}>
                Model
              </span>
              <select
                value={model}
                onChange={(e) => { setModel(e.target.value); setTouched(true); }}
                className="focus:outline-none"
                style={{
                  background: 'rgba(255,255,255,0.04)',
                  border: '1px solid rgba(255,255,255,0.1)',
                  color: 'var(--text-primary)',
                  padding: '8px 10px',
                  borderRadius: '8px',
                  fontSize: '12px',
                  appearance: 'none',
                }}
              >
                {GROQ_MODELS.map((m) => (
                  <option key={m.id} value={m.id}>{m.label}</option>
                ))}
              </select>
            </label>

            <div className="flex items-center gap-2 pt-2">
              {key.trim() && (
                <button
                  onClick={handleClear}
                  className="text-xs"
                  style={{ color: 'var(--text-muted)', padding: '8px 10px' }}
                >
                  Remove key
                </button>
              )}
              <div className="flex-1" />
              <button
                onClick={onClose}
                className="text-xs font-semibold"
                style={{
                  padding: '8px 14px',
                  borderRadius: '8px',
                  color: 'var(--text-secondary)',
                  background: 'rgba(255,255,255,0.04)',
                  border: '1px solid rgba(255,255,255,0.08)',
                }}
              >
                Cancel
              </button>
              <button
                onClick={handleSave}
                disabled={!touched}
                className="text-xs font-black"
                style={{
                  padding: '8px 14px',
                  borderRadius: '8px',
                  background: '#FFE500',
                  color: '#000',
                  border: '2px solid #000',
                  boxShadow: '2px 2px 0 #000',
                  opacity: touched ? 1 : 0.5,
                }}
              >
                Save
              </button>
            </div>
          </div>
        </div>
      </motion.div>
    </>
  );
}
