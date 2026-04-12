'use client';

import { useCallback, useState, useEffect } from 'react';
import { useDropzone } from 'react-dropzone';
import { motion, AnimatePresence } from 'framer-motion';
import { Upload, FileText, X, AlertCircle, CheckCircle2 } from 'lucide-react';
import type { BookData } from '@/types';

interface InputPanelProps {
  rawText: string;
  onFormat: (text: string) => void;
  isProcessing: boolean;
  bookData: BookData | null;
}

type UploadStatus = 'idle' | 'uploading' | 'success' | 'error';

export default function InputPanel({ rawText, onFormat, isProcessing, bookData }: InputPanelProps) {
  const [text, setText] = useState(rawText);

  // Sync textarea when parent sets rawText (e.g. demo text loaded on mount)
  useEffect(() => {
    setText(rawText);
  }, [rawText]);
  const [uploadStatus, setUploadStatus] = useState<UploadStatus>('idle');
  const [uploadedFileName, setUploadedFileName] = useState('');
  const [uploadError, setUploadError] = useState('');

  // ─── FILE UPLOAD ───────────────────────

  const onDrop = useCallback(async (acceptedFiles: File[]) => {
    const file = acceptedFiles[0];
    if (!file) return;

    setUploadStatus('uploading');
    setUploadedFileName(file.name);
    setUploadError('');

    try {
      let extractedText = '';

      if (file.type === 'text/plain' || file.name.endsWith('.txt')) {
        extractedText = await file.text();
      } else if (
        file.type === 'application/vnd.openxmlformats-officedocument.wordprocessingml.document' ||
        file.name.endsWith('.docx')
      ) {
        // Use FormData to send to API
        const formData = new FormData();
        formData.append('file', file);
        const res = await fetch('/api/extract', { method: 'POST', body: formData });
        if (!res.ok) throw new Error('Failed to parse document');
        const data = await res.json();
        extractedText = data.text;
      } else {
        // Try to read as plain text
        extractedText = await file.text();
      }

      if (!extractedText.trim()) {
        throw new Error('No readable text found in this file');
      }

      setText(extractedText);
      setUploadStatus('success');
      onFormat(extractedText);
    } catch (err) {
      setUploadStatus('error');
      setUploadError(err instanceof Error ? err.message : 'Upload failed');
    }
  }, [onFormat]);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'text/plain': ['.txt'],
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document': ['.docx'],
      'application/pdf': ['.pdf'],
    },
    maxFiles: 1,
  });

  // ─── TEXT INPUT ───────────────────────

  const handleTextChange = (value: string) => {
    setText(value);
  };

  const handleFormat = () => {
    if (text.trim()) onFormat(text);
  };

  const clearUpload = () => {
    setUploadStatus('idle');
    setUploadedFileName('');
    setUploadError('');
  };

  const wordCount = text.trim().split(/\s+/).filter(Boolean).length;

  return (
    <div className="flex flex-col h-full overflow-hidden">
      {/* Upload zone */}
      <div
        {...getRootProps()}
        className="mx-3 mt-3 rounded-xl flex flex-col items-center justify-center gap-2 cursor-pointer transition-all duration-200"
        style={{
          padding: '14px',
          border: isDragActive
            ? '1.5px dashed var(--accent)'
            : '1.5px dashed rgba(255,255,255,0.12)',
          background: isDragActive
            ? 'rgba(124,58,237,0.08)'
            : 'rgba(255,255,255,0.02)',
          minHeight: '80px',
        }}
      >
        <input {...getInputProps()} />

        <AnimatePresence mode="wait">
          {uploadStatus === 'uploading' ? (
            <motion.div
              key="uploading"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              className="flex items-center gap-2 text-xs"
              style={{ color: 'var(--accent-light)' }}
            >
              <div className="w-3 h-3 rounded-full border-2 border-accent-light border-t-transparent animate-spin" />
              Parsing {uploadedFileName}…
            </motion.div>
          ) : uploadStatus === 'success' ? (
            <motion.div
              key="success"
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              className="flex items-center gap-2 text-xs"
            >
              <CheckCircle2 size={13} color="#34C759" />
              <span style={{ color: '#34C759' }}>{uploadedFileName}</span>
              <button
                onClick={(e) => { e.stopPropagation(); clearUpload(); }}
                className="ml-1"
                style={{ color: 'var(--text-muted)' }}
              >
                <X size={11} />
              </button>
            </motion.div>
          ) : uploadStatus === 'error' ? (
            <motion.div
              key="error"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              className="flex items-center gap-2 text-xs"
            >
              <AlertCircle size={13} color="#FF3B30" />
              <span style={{ color: '#FF3B30' }}>{uploadError}</span>
            </motion.div>
          ) : (
            <motion.div
              key="idle"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              className="flex flex-col items-center gap-1 text-center"
            >
              <div className="flex items-center gap-2">
                <Upload size={13} color="var(--text-muted)" />
                <span className="text-xs" style={{ color: 'var(--text-muted)' }}>
                  {isDragActive ? 'Drop your file here' : 'Drop file or click to upload'}
                </span>
              </div>
              <span className="text-xs" style={{ color: 'var(--text-muted)', opacity: 0.5, fontSize: '10px' }}>
                .docx · .txt · .pdf
              </span>
            </motion.div>
          )}
        </AnimatePresence>
      </div>

      {/* Divider */}
      <div className="flex items-center gap-2 mx-3 my-2">
        <div className="flex-1 h-px" style={{ background: 'rgba(255,255,255,0.06)' }} />
        <span className="text-xs" style={{ color: 'var(--text-muted)', opacity: 0.5 }}>or paste below</span>
        <div className="flex-1 h-px" style={{ background: 'rgba(255,255,255,0.06)' }} />
      </div>

      {/* Text area */}
      <div className="flex-1 overflow-hidden mx-3 mb-3 flex flex-col gap-2" style={{ minHeight: 0 }}>
        <textarea
          value={text}
          onChange={(e) => handleTextChange(e.target.value)}
          placeholder="Paste your manuscript here…

Include chapter headings like:
Chapter 1
or
Chapter One: The Beginning

Booksane will auto-detect your structure."
          className="w-full h-full rounded-xl resize-none text-xs leading-relaxed focus:outline-none transition-all"
          style={{
            flex: 1,
            background: 'rgba(255,255,255,0.03)',
            border: '1px solid rgba(255,255,255,0.08)',
            color: 'var(--text-secondary)',
            padding: '12px',
            fontFamily: 'var(--font-book)',
            fontSize: '12px',
          }}
        />

        {/* Word count + Format button */}
        <div className="flex items-center justify-between">
          <span className="text-xs" style={{ color: 'var(--text-muted)', opacity: 0.6 }}>
            {wordCount > 0 ? `${wordCount.toLocaleString()} words` : ''}
          </span>
          <button
            onClick={handleFormat}
            disabled={isProcessing || !text.trim()}
            className="btn btn-primary text-xs py-1.5 px-3"
            style={{
              opacity: isProcessing || !text.trim() ? 0.4 : 1,
              borderRadius: '8px',
            }}
          >
            Format →
          </button>
        </div>
      </div>

      {/* Stats strip */}
      {bookData && (
        <div
          className="mx-3 mb-3 p-3 rounded-xl"
          style={{
            background: 'rgba(124,58,237,0.06)',
            border: '1px solid rgba(124,58,237,0.12)',
          }}
        >
          <div className="grid grid-cols-3 gap-2 text-center">
            {[
              { label: 'Words', value: bookData.metadata.wordCount.toLocaleString() },
              { label: 'Pages', value: `~${bookData.metadata.estimatedPages}` },
              { label: 'Chapters', value: bookData.chapters.length },
            ].map((stat) => (
              <div key={stat.label}>
                <div className="text-sm font-semibold" style={{ color: 'var(--text-primary)' }}>
                  {stat.value}
                </div>
                <div className="text-xs" style={{ color: 'var(--text-muted)', fontSize: '10px' }}>
                  {stat.label}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
