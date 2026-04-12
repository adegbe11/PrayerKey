'use client';

import { motion } from 'framer-motion';
import { templates } from '@/lib/templates';

interface TemplatePanelProps {
  selectedId: string;
  onSelect: (id: string) => void;
}

export default function TemplatePanel({ selectedId, onSelect }: TemplatePanelProps) {
  return (
    <div className="flex flex-col h-full overflow-hidden">
      {/* Header */}
      <div
        className="px-3 py-3 border-b shrink-0"
        style={{ borderColor: 'rgba(255,255,255,0.07)' }}
      >
        <p className="text-xs font-semibold uppercase tracking-widest" style={{ color: 'var(--text-muted)', letterSpacing: '0.12em' }}>
          Templates
        </p>
      </div>

      {/* Template list */}
      <div className="flex-1 overflow-y-auto p-2">
        {templates.map((t, i) => {
          const isSelected = t.id === selectedId;

          return (
            <motion.button
              key={t.id}
              initial={{ opacity: 0, x: -10 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.3, delay: i * 0.04 }}
              onClick={() => onSelect(t.id)}
              className="w-full text-left flex items-center gap-3 p-2.5 rounded-xl mb-1 transition-all duration-200"
              style={{
                background: isSelected ? 'rgba(124,58,237,0.12)' : 'transparent',
                border: `1px solid ${isSelected ? 'rgba(124,58,237,0.25)' : 'transparent'}`,
              }}
            >
              {/* Mini color swatch */}
              <div
                className="shrink-0 rounded-md overflow-hidden"
                style={{
                  width: '32px',
                  height: '44px',
                  background: t.paperColor,
                  border: `1px solid rgba(0,0,0,0.1)`,
                  position: 'relative',
                }}
              >
                {/* Mini text lines */}
                <div style={{ padding: '4px 3px' }}>
                  <div
                    style={{
                      height: '2px',
                      background: t.headingColor,
                      marginBottom: '3px',
                      borderRadius: '1px',
                      opacity: 0.8,
                    }}
                  />
                  {[0.6, 0.5, 0.55, 0.5].map((op, j) => (
                    <div
                      key={j}
                      style={{
                        height: '1.5px',
                        background: t.inkColor,
                        marginBottom: '2px',
                        borderRadius: '1px',
                        opacity: op,
                        width: `${70 + j * 5}%`,
                      }}
                    />
                  ))}
                </div>
              </div>

              {/* Name + category */}
              <div className="min-w-0">
                <div
                  className="text-xs font-medium truncate"
                  style={{ color: isSelected ? 'var(--accent-light)' : 'var(--text-primary)' }}
                >
                  {t.name}
                </div>
                <div
                  className="text-xs capitalize mt-0.5"
                  style={{ color: 'var(--text-muted)', fontSize: '10px' }}
                >
                  {t.category}
                </div>
              </div>

              {/* Active indicator */}
              {isSelected && (
                <div
                  className="ml-auto shrink-0 w-1.5 h-1.5 rounded-full"
                  style={{ background: 'var(--accent-light)' }}
                />
              )}
            </motion.button>
          );
        })}
      </div>

      {/* Bottom info */}
      <div
        className="p-3 border-t shrink-0"
        style={{ borderColor: 'rgba(255,255,255,0.07)' }}
      >
        <p className="text-xs text-center" style={{ color: 'var(--text-muted)', opacity: 0.5, fontSize: '10px' }}>
          Switching templates is instant
        </p>
      </div>
    </div>
  );
}
