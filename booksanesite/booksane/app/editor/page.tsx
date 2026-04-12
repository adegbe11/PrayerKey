'use client';
import { useState, useCallback } from 'react';
import WelcomePage from '@/components/editor/WelcomePage';
import EditorApp from '@/components/editor/EditorApp';
import type { RecentBook } from '@/types';

type EditorMode = 'welcome' | 'editing';

export default function EditorPage() {
  const [mode, setMode] = useState<EditorMode>('welcome');
  const [initialText, setInitialText] = useState('');
  const [initialFilename, setInitialFilename] = useState('');

  const handleNewBook = useCallback(() => {
    setInitialText('');
    setInitialFilename('');
    setMode('editing');
  }, []);

  const handleImportText = useCallback((text: string, filename: string) => {
    setInitialText(text);
    setInitialFilename(filename);
    setMode('editing');
  }, []);

  const handleOpenRecent = useCallback((book: RecentBook) => {
    setInitialText(book.rawText);
    setInitialFilename(book.title);
    setMode('editing');
  }, []);

  if (mode === 'welcome') {
    return (
      <WelcomePage
        onNewBook={handleNewBook}
        onImportText={handleImportText}
        onOpenRecent={handleOpenRecent}
      />
    );
  }

  return (
    <EditorApp
      initialText={initialText}
      initialFilename={initialFilename}
      onGoHome={() => setMode('welcome')}
    />
  );
}
