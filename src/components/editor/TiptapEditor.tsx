'use client';

import { useEditor, EditorContent } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import Underline from '@tiptap/extension-underline';
import Placeholder from '@tiptap/extension-placeholder';
import Typography from '@tiptap/extension-typography';
import { useEffect, useRef } from 'react';

// ─────────────────────────────────────────
//  TOOLBAR BUTTON
// ─────────────────────────────────────────

function TBtn({
  label,
  title,
  onClick,
  active = false,
  wide = false,
  style: extraStyle,
}: {
  label: string;
  title: string;
  onClick: () => void;
  active?: boolean;
  wide?: boolean;
  style?: React.CSSProperties;
}) {
  return (
    <button
      title={title}
      onMouseDown={(e) => {
        // Prevent editor losing focus on toolbar click
        e.preventDefault();
        onClick();
      }}
      style={{
        height: 28,
        minWidth: wide ? 60 : 28,
        padding: '0 8px',
        border: `1px solid ${active ? 'rgba(0,0,0,0.28)' : 'rgba(0,0,0,0.12)'}`,
        borderRadius: 3,
        background: active ? 'rgba(0,0,0,0.08)' : 'transparent',
        cursor: 'pointer',
        fontSize: 12,
        fontWeight: 600,
        color: active ? '#000' : '#555',
        transition: 'all 0.1s',
        flexShrink: 0,
        letterSpacing: wide ? '0.06em' : 'normal',
        ...extraStyle,
      }}
      onMouseEnter={(e) => {
        if (!active) (e.currentTarget as HTMLElement).style.background = 'rgba(0,0,0,0.05)';
      }}
      onMouseLeave={(e) => {
        if (!active) (e.currentTarget as HTMLElement).style.background = 'transparent';
      }}
    >
      {label}
    </button>
  );
}

// ─────────────────────────────────────────
//  TIPTAP EDITOR
// ─────────────────────────────────────────

interface TiptapEditorProps {
  content: string;
  chapterId: string;
  chapterTitle: string;
  chapterNumber: number;
  onChange: (html: string) => void;
}

export default function TiptapEditor({
  content,
  chapterId,
  chapterTitle,
  chapterNumber,
  onChange,
}: TiptapEditorProps) {
  const debounceRef   = useRef<ReturnType<typeof setTimeout> | null>(null);
  const prevIdRef     = useRef(chapterId);
  const onChangeRef   = useRef(onChange);
  onChangeRef.current = onChange;

  const editor = useEditor({
    extensions: [
      StarterKit.configure({
        // We only need paragraph, bold, italic, hardBreak, history, text.
        // Disable things that don't belong in a book editor.
        heading:      false,
        blockquote:   false,
        codeBlock:    false,
        code:         false,
        bulletList:   false,
        orderedList:  false,
        listItem:     false,
        horizontalRule: false, // We handle scene breaks as styled paragraphs
      }),
      Underline,
      Placeholder.configure({
        placeholder:      'Begin writing your chapter here…',
        emptyEditorClass: 'tiptap-empty',
      }),
      Typography.configure({
        // Smart typography as the author types
        openDoubleQuote:  '\u201C',
        closeDoubleQuote: '\u201D',
        openSingleQuote:  '\u2018',
        closeSingleQuote: '\u2019',
      }),
    ],
    content: content || '<p></p>',
    immediatelyRender: false, // prevents Next.js SSR hydration mismatch
    editorProps: {
      attributes: {
        class:      'tiptap-content',
        spellcheck: 'true',
      },
    },
    onUpdate({ editor }) {
      if (debounceRef.current) clearTimeout(debounceRef.current);
      debounceRef.current = setTimeout(() => {
        onChangeRef.current(editor.getHTML());
      }, 350);
    },
  });

  // When user navigates to a different chapter, load that chapter's content
  useEffect(() => {
    if (!editor || chapterId === prevIdRef.current) return;
    prevIdRef.current = chapterId;
    // Flush any pending save for the old chapter first
    if (debounceRef.current) {
      clearTimeout(debounceRef.current);
      onChangeRef.current(editor.getHTML());
    }
    editor.commands.setContent(content || '<p></p>', false);
    // Scroll editor to top
    editor.commands.focus('start');
  // content intentionally excluded — only trigger on chapter id change
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [chapterId, editor]);

  // Flush pending save on unmount
  useEffect(() => {
    return () => {
      if (debounceRef.current) {
        clearTimeout(debounceRef.current);
        if (editor) onChangeRef.current(editor.getHTML());
      }
    };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [editor]);

  // Insert a scene break as a styled paragraph (compatible with paginator)
  const insertSceneBreak = () => {
    if (!editor) return;
    editor
      .chain()
      .focus()
      .insertContent('<p class="section-break">* * *</p>')
      .run();
  };

  if (!editor) {
    return (
      <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <div style={{ fontSize: 12, color: '#bbb' }}>Loading editor…</div>
      </div>
    );
  }

  const displayTitle =
    chapterTitle && !/^chapter\s+\d+$/i.test(chapterTitle.trim())
      ? chapterTitle
      : `Chapter ${chapterNumber}`;

  return (
    <>
      {/* ── Header + Toolbar ──────────────────────────── */}
      <div
        style={{
          borderBottom: '1px solid rgba(0,0,0,0.07)',
          padding: '16px 40px 0',
          flexShrink: 0,
          background: '#fafaf8',
        }}
      >
        <div
          style={{
            fontSize: 22,
            fontFamily: 'Georgia, serif',
            fontWeight: 700,
            color: '#1a1a1a',
            marginBottom: 12,
          }}
        >
          {displayTitle}
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: 4, paddingBottom: 10 }}>
          {/* Format buttons */}
          <TBtn
            label="B"
            title="Bold (Cmd+B)"
            active={editor.isActive('bold')}
            onClick={() => editor.chain().focus().toggleBold().run()}
            style={{ fontWeight: 900, fontFamily: 'Georgia, serif' }}
          />
          <TBtn
            label="I"
            title="Italic (Cmd+I)"
            active={editor.isActive('italic')}
            onClick={() => editor.chain().focus().toggleItalic().run()}
            style={{ fontStyle: 'italic', fontFamily: 'Georgia, serif' }}
          />
          <TBtn
            label="U"
            title="Underline (Cmd+U)"
            active={editor.isActive('underline')}
            onClick={() => editor.chain().focus().toggleUnderline().run()}
            style={{ textDecoration: 'underline' }}
          />

          <div style={{ width: 1, height: 18, background: 'rgba(0,0,0,0.1)', margin: '0 4px' }} />

          <TBtn
            label="* * *"
            title="Insert scene break"
            onClick={insertSceneBreak}
            wide
          />

          <div style={{ flex: 1 }} />

          {/* Keyboard hint */}
          <span style={{ fontSize: 10, color: '#ccc', userSelect: 'none' }}>
            Cmd+B · Cmd+I · Cmd+Z
          </span>
        </div>
      </div>

      {/* ── Editor Area ───────────────────────────────── */}
      <div
        style={{
          flex: 1,
          overflowY: 'auto',
          padding: '28px 40px 60px',
          background: '#fafaf8',
        }}
        // Clicking the container focuses the editor at the end
        onClick={() => editor.commands.focus()}
      >
        <EditorContent editor={editor} />
      </div>
    </>
  );
}
