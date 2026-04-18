'use client';

/**
 * Minimal Groq chat-completions client.
 * Groq's API is OpenAI-compatible and CORS-enabled, so we call directly from the browser.
 * User supplies their own API key (stored locally) — no server keys, no cost to us.
 */

export const GROQ_MODELS = [
  { id: 'llama-3.3-70b-versatile', label: 'Llama 3.3 70B (recommended)' },
  { id: 'llama-3.1-8b-instant',    label: 'Llama 3.1 8B (fastest)' },
  { id: 'openai/gpt-oss-120b',     label: 'GPT-OSS 120B (reasoning)' },
  { id: 'openai/gpt-oss-20b',      label: 'GPT-OSS 20B (reasoning)' },
  { id: 'deepseek-r1-distill-llama-70b', label: 'DeepSeek R1 Distill 70B (reasoning)' },
  { id: 'gemma2-9b-it',            label: 'Gemma 2 9B' },
] as const;

export type GroqModelId = typeof GROQ_MODELS[number]['id'];
export const DEFAULT_GROQ_MODEL: GroqModelId = 'llama-3.3-70b-versatile';

function isReasoningModel(model: string): boolean {
  return /^openai\/gpt-oss|deepseek-r1|r1-distill/i.test(model);
}

export interface GroqMessage {
  role: 'system' | 'user' | 'assistant';
  content: string;
}

export interface GroqChatOptions {
  apiKey: string;
  model?: string;
  messages: GroqMessage[];
  temperature?: number;
  maxTokens?: number;
  responseFormat?: 'text' | 'json_object';
}

const ENDPOINT = 'https://api.groq.com/openai/v1/chat/completions';

export class GroqError extends Error {
  status: number;
  constructor(msg: string, status: number) {
    super(msg);
    this.status = status;
  }
}

export async function groqChat(opts: GroqChatOptions): Promise<string> {
  if (!opts.apiKey) throw new GroqError('Groq API key required', 401);

  const model = opts.model ?? DEFAULT_GROQ_MODEL;
  const reasoning = isReasoningModel(model);
  // Reasoning models spend tokens on hidden CoT before answering.
  // Triple the budget for them and ask for "low" effort so content still fits.
  const maxTokens = (opts.maxTokens ?? 800) * (reasoning ? 3 : 1);

  const res = await fetch(ENDPOINT, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${opts.apiKey}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      model,
      messages: opts.messages,
      temperature: opts.temperature ?? 0.7,
      max_tokens: maxTokens,
      ...(reasoning ? { reasoning_effort: 'low' } : {}),
      ...(opts.responseFormat === 'json_object'
        ? { response_format: { type: 'json_object' } }
        : {}),
    }),
  });

  if (!res.ok) {
    let errMsg = `Groq API ${res.status}`;
    try {
      const j = await res.json();
      errMsg = j?.error?.message || errMsg;
    } catch { /* noop */ }
    throw new GroqError(errMsg, res.status);
  }

  const data = await res.json();
  const content = data?.choices?.[0]?.message?.content;
  if (typeof content !== 'string') throw new GroqError('Empty Groq response', 502);
  return content.trim();
}

/**
 * Validate a key by pinging Groq's /models endpoint.
 */
export async function validateGroqKey(apiKey: string): Promise<boolean> {
  if (!apiKey?.trim()) return false;
  try {
    const res = await fetch('https://api.groq.com/openai/v1/models', {
      headers: { 'Authorization': `Bearer ${apiKey}` },
    });
    return res.ok;
  } catch {
    return false;
  }
}
