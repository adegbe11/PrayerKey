'use client';

// Minimal storage module — just Groq key/model persistence.
// Multi-project storage can be added separately later.

const GROQ_KEY = 'booksane:groqKey:v1';
const GROQ_MODEL_KEY = 'booksane:groqModel:v1';

export function loadGroqKey(): string {
  if (typeof window === 'undefined') return '';
  return localStorage.getItem(GROQ_KEY) || '';
}

export function saveGroqKey(key: string): void {
  if (typeof window === 'undefined') return;
  if (key) localStorage.setItem(GROQ_KEY, key);
  else localStorage.removeItem(GROQ_KEY);
}

export function loadGroqModel(): string {
  if (typeof window === 'undefined') return '';
  return localStorage.getItem(GROQ_MODEL_KEY) || '';
}

export function saveGroqModel(model: string): void {
  if (typeof window === 'undefined') return;
  if (model) localStorage.setItem(GROQ_MODEL_KEY, model);
  else localStorage.removeItem(GROQ_MODEL_KEY);
}
