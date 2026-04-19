'use client';

import { useEditor, EditorContent } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import Underline from '@tiptap/extension-underline';
import Placeholder from '@tiptap/extension-placeholder';
import Typography from '@tiptap/extension-typography';
import TextAlign from '@tiptap/extension-text-align';
import Subscript from '@tiptap/extension-subscript';
import Superscript from '@tiptap/extension-superscript';
import CharacterCount from '@tiptap/extension-character-count';
import { useEffect, useRef, useState } from 'react';

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
  disabled = false,
}: {
  label: string | React.ReactNode;
  title: string;
  onClick: () => void;
  active?: boolean;
  wide?: boolean;
  style?: React.CSSProperties;
  disabled?: boolean;
}) {
  return (
    <button
      title={title}
      disabled={disabled}
      onMouseDown={(e) => {
        e.preventDefault();
        if (!disabled) onClick();
      }}
      style={{
        height: 28,
        minWidth: wide ? 52 : 28,
        padding: '0 6px',
        border: `1px solid ${active ? 'rgba(0,0,0,0.30)' : 'rgba(0,0,0,0.12)'}`,
        borderRadius: 3,
        background: active ? 'rgba(0,0,0,0.09)' : 'transparent',
        cursor: disabled ? 'default' : 'pointer',
        fontSize: 12,
        fontWeight: 600,
        color: disabled ? '#ccc' : active ? '#000' : '#555',
        transition: 'all 0.1s',
        flexShrink: 0,
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 2,
        ...extraStyle,
      }}
      onMouseEnter={(e) => {
        if (!active && !disabled) (e.currentTarget as HTMLElement).style.background = 'rgba(0,0,0,0.05)';
      }}
      onMouseLeave={(e) => {
        if (!active && !disabled) (e.currentTarget as HTMLElement).style.background = 'transparent';
      }}
    >
      {label}
    </button>
  );
}

// Divider between toolbar groups
function Divider() {
  return <div style={{ width: 1, height: 18, background: 'rgba(0,0,0,0.10)', margin: '0 2px', flexShrink: 0 }} />;
}

// ─────────────────────────────────────────
//  SVG ICONS (inline, no deps)
// ─────────────────────────────────────────
const Icon = {
  AlignLeft:    () => <svg width="13" height="13" viewBox="0 0 13 13"><line x1="1" y1="2.5" x2="12" y2="2.5" stroke="currentColor" strokeWidth="1.5"/><line x1="1" y1="5.5" x2="9"  y2="5.5" stroke="currentColor" strokeWidth="1.5"/><line x1="1" y1="8.5" x2="12" y2="8.5" stroke="currentColor" strokeWidth="1.5"/><line x1="1" y1="11.5" x2="7" y2="11.5" stroke="currentColor" strokeWidth="1.5"/></svg>,
  AlignCenter:  () => <svg width="13" height="13" viewBox="0 0 13 13"><line x1="1" y1="2.5" x2="12" y2="2.5" stroke="currentColor" strokeWidth="1.5"/><line x1="3" y1="5.5" x2="10" y2="5.5" stroke="currentColor" strokeWidth="1.5"/><line x1="1" y1="8.5" x2="12" y2="8.5" stroke="currentColor" strokeWidth="1.5"/><line x1="3" y1="11.5" x2="10" y2="11.5" stroke="currentColor" strokeWidth="1.5"/></svg>,
  AlignRight:   () => <svg width="13" height="13" viewBox="0 0 13 13"><line x1="1" y1="2.5" x2="12" y2="2.5" stroke="currentColor" strokeWidth="1.5"/><line x1="4" y1="5.5" x2="12" y2="5.5" stroke="currentColor" strokeWidth="1.5"/><line x1="1" y1="8.5" x2="12" y2="8.5" stroke="currentColor" strokeWidth="1.5"/><line x1="6" y1="11.5" x2="12" y2="11.5" stroke="currentColor" strokeWidth="1.5"/></svg>,
  AlignJustify: () => <svg width="13" height="13" viewBox="0 0 13 13"><line x1="1" y1="2.5" x2="12" y2="2.5" stroke="currentColor" strokeWidth="1.5"/><line x1="1" y1="5.5" x2="12" y2="5.5" stroke="currentColor" strokeWidth="1.5"/><line x1="1" y1="8.5" x2="12" y2="8.5" stroke="currentColor" strokeWidth="1.5"/><line x1="1" y1="11.5" x2="12" y2="11.5" stroke="currentColor" strokeWidth="1.5"/></svg>,
  Undo:         () => <svg width="13" height="13" viewBox="0 0 13 13"><path d="M2 6.5C2 4 4 2 6.5 2c2 0 3.8 1.2 4.5 3" stroke="currentColor" strokeWidth="1.4" fill="none"/><polyline points="2,2 2,6.5 6.5,6.5" stroke="currentColor" strokeWidth="1.4" fill="none"/></svg>,
  Redo:         () => <svg width="13" height="13" viewBox="0 0 13 13"><path d="M11 6.5C11 4 9 2 6.5 2c-2 0-3.8 1.2-4.5 3" stroke="currentColor" strokeWidth="1.4" fill="none"/><polyline points="11,2 11,6.5 6.5,6.5" stroke="currentColor" strokeWidth="1.4" fill="none"/></svg>,
};

// ─────────────────────────────────────────
//  HEADING SELECT
// ─────────────────────────────────────────
function HeadingSelect({ editor }: { editor: ReturnType<typeof useEditor> }) {
  if (!editor) return null;

  const value =
    editor.isActive('heading', { level: 1 }) ? '1' :
    editor.isActive('heading', { level: 2 }) ? '2' :
    editor.isActive('heading', { level: 3 }) ? '3' : '0';

  return (
    <select
      title="Paragraph style"
      value={value}
      onMouseDown={(e) => e.stopPropagation()}
      onChange={(e) => {
        const v = e.target.value;
        if (v === '0') editor.chain().focus().setParagraph().run();
        else editor.chain().focus().toggleHeading({ level: parseInt(v) as 1|2|3 }).run();
      }}
      style={{
        height: 28,
        padding: '0 6px',
        border: '1px solid rgba(0,0,0,0.12)',
        borderRadius: 3,
        background: 'transparent',
        fontSize: 12,
        color: '#555',
        cursor: 'pointer',
        outline: 'none',
        fontFamily: 'Georgia, serif',
        minWidth: 110,
        flexShrink: 0,
      }}
    >
      <option value="0">Body Text</option>
      <option value="1">Chapter Title</option>
      <option value="2">Section Heading</option>
      <option value="3">Sub-heading</option>
    </select>
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

  // Live word count from CharacterCount extension
  const [wordCount, setWordCount] = useState(0);

  const editor = useEditor({
    extensions: [
      StarterKit.configure({
        // Enable heading for Chapter Title / Section Heading / Sub-heading
        heading: { levels: [1, 2, 3] },
        // Disable things that don't belong in a book
        blockquote:    false,
        codeBlock:     false,
        code:          false,
        bulletList:    false,
        orderedList:   false,
        listItem:      false,
        horizontalRule: false,
      }),
      Underline,
      Subscript,
      Superscript,
      TextAlign.configure({
        types: ['heading', 'paragraph'],
        defaultAlignment: 'left',
      }),
      CharacterCount,
      Placeholder.configure({
        placeholder:      'Begin writing your chapter here…',
        emptyEditorClass: 'tiptap-empty',
      }),
      Typography.configure({
        openDoubleQuote:  '\u201C',
        closeDoubleQuote: '\u201D',
        openSingleQuote:  '\u2018',
        closeSingleQuote: '\u2019',
      }),
    ],
    content: content || '<p></p>',
    immediatelyRender: false,
    editorProps: {
      attributes: {
        class:      'tiptap-content',
        spellcheck: 'true',
      },
    },
    onUpdate({ editor }) {
      // Update word count
      setWordCount(editor.storage.characterCount.words());
      // Debounced save
      if (debounceRef.current) clearTimeout(debounceRef.current);
      debounceRef.current = setTimeout(() => {
        onChangeRef.current(editor.getHTML());
      }, 350);
    },
  });

  // Init word count on mount
  useEffect(() => {
    if (editor) setWordCount(editor.storage.characterCount.words());
  }, [editor]);

  // Switch chapter: flush old, load new
  useEffect(() => {
    if (!editor || chapterId === prevIdRef.current) return;
    prevIdRef.current = chapterId;
    if (debounceRef.current) {
      clearTimeout(debounceRef.current);
      onChangeRef.current(editor.getHTML());
    }
    editor.commands.setContent(content || '<p></p>', false);
    editor.commands.focus('start');
    setWordCount(editor.storage.characterCount.words());
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [chapterId, editor]);

  // Flush on unmount
  useEffect(() => {
    return () => {
      if (debounceRef.current) {
        clearTimeout(debounceRef.current);
        if (editor) onChangeRef.current(editor.getHTML());
      }
    };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [editor]);

  const insertSceneBreak = () => {
    if (!editor) return;
    editor.chain().focus().insertContent('<p class="section-break">* * *</p>').run();
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

        {/* ── Toolbar row 1: style + format ── */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 3, flexWrap: 'wrap', paddingBottom: 6 }}>

          {/* Paragraph style */}
          <HeadingSelect editor={editor} />

          <Divider />

          {/* Bold / Italic / Underline / Strike */}
          <TBtn label="B" title="Bold (Ctrl+B)" active={editor.isActive('bold')}
            onClick={() => editor.chain().focus().toggleBold().run()}
            style={{ fontWeight: 900, fontFamily: 'Georgia, serif' }} />
          <TBtn label="I" title="Italic (Ctrl+I)" active={editor.isActive('italic')}
            onClick={() => editor.chain().focus().toggleItalic().run()}
            style={{ fontStyle: 'italic', fontFamily: 'Georgia, serif' }} />
          <TBtn label="U" title="Underline (Ctrl+U)" active={editor.isActive('underline')}
            onClick={() => editor.chain().focus().toggleUnderline().run()}
            style={{ textDecoration: 'underline' }} />
          <TBtn label="S" title="Strikethrough (Ctrl+Shift+S)" active={editor.isActive('strike')}
            onClick={() => editor.chain().focus().toggleStrike().run()}
            style={{ textDecoration: 'line-through' }} />

          <Divider />

          {/* Superscript / Subscript */}
          <TBtn label="x²" title="Superscript" active={editor.isActive('superscript')}
            onClick={() => editor.chain().focus().toggleSuperscript().run()}
            style={{ fontSize: 11 }} />
          <TBtn label="x₂" title="Subscript" active={editor.isActive('subscript')}
            onClick={() => editor.chain().focus().toggleSubscript().run()}
            style={{ fontSize: 11 }} />

          <Divider />

          {/* Text alignment */}
          <TBtn label={<Icon.AlignLeft />}    title="Align left"    active={editor.isActive({ textAlign: 'left' })}    onClick={() => editor.chain().focus().setTextAlign('left').run()} />
          <TBtn label={<Icon.AlignCenter />}  title="Align center"  active={editor.isActive({ textAlign: 'center' })}  onClick={() => editor.chain().focus().setTextAlign('center').run()} />
          <TBtn label={<Icon.AlignRight />}   title="Align right"   active={editor.isActive({ textAlign: 'right' })}   onClick={() => editor.chain().focus().setTextAlign('right').run()} />
          <TBtn label={<Icon.AlignJustify />} title="Justify (book standard)" active={editor.isActive({ textAlign: 'justify' })} onClick={() => editor.chain().focus().setTextAlign('justify').run()} />

          <Divider />

          {/* Scene break */}
          <TBtn label="* * *" title="Insert scene break" onClick={insertSceneBreak} wide />

          <div style={{ flex: 1 }} />

          {/* Undo / Redo */}
          <TBtn label={<Icon.Undo />} title="Undo (Ctrl+Z)"       onClick={() => editor.chain().focus().undo().run()} disabled={!editor.can().undo()} />
          <TBtn label={<Icon.Redo />} title="Redo (Ctrl+Shift+Z)" onClick={() => editor.chain().focus().redo().run()} disabled={!editor.can().redo()} />
        </div>

        {/* ── Status bar ── */}
        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: 16,
          paddingBottom: 8,
          fontSize: 10,
          color: '#bbb',
          userSelect: 'none',
        }}>
          <span>{wordCount.toLocaleString()} words</span>
          <span style={{ marginLeft: 'auto' }}>Ctrl+B · Ctrl+I · Ctrl+U · Ctrl+Z</span>
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
        onClick={() => editor.commands.focus()}
      >
        <EditorContent editor={editor} />
      </div>
    </>
  );
}
