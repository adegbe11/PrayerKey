import type { Metadata } from 'next';
import EditorApp from '@/components/editor/EditorApp';

export const metadata: Metadata = {
  title: 'Editor — Booksane',
  description: 'Format your manuscript into a beautiful book. Preview as paperback or eBook. Export PDF and EPUB.',
};

export default function EditorPage() {
  return <EditorApp />;
}
