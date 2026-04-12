import { NextRequest, NextResponse } from 'next/server';

export async function POST(req: NextRequest) {
  try {
    const formData = await req.formData();
    const file = formData.get('file') as File | null;

    if (!file) {
      return NextResponse.json({ error: 'No file provided' }, { status: 400 });
    }

    const name = file.name.toLowerCase();
    let text = '';

    if (name.endsWith('.txt')) {
      text = await file.text();
    } else if (name.endsWith('.docx')) {
      try {
        const mammoth = await import('mammoth');
        const buffer = Buffer.from(await file.arrayBuffer());
        const result = await mammoth.extractRawText({ buffer });
        text = result.value;
      } catch {
        // Fallback: try reading as text
        text = await file.text();
      }
    } else if (name.endsWith('.pdf')) {
      // Basic: read as text (won't work well for binary PDFs, but OK for text-based)
      // For real PDF parsing you'd use pdf-parse but it has Node.js dependencies
      text = await file.text();
      // Clean non-printable chars from PDF text extraction
      text = text.replace(/[\x00-\x08\x0B\x0C\x0E-\x1F\x7F-\x9F]/g, ' ');
    } else {
      text = await file.text();
    }

    if (!text.trim()) {
      return NextResponse.json(
        { error: 'No readable text found. Try uploading a .txt or .docx file.' },
        { status: 422 }
      );
    }

    return NextResponse.json({ text });
  } catch (err) {
    console.error('Extract error:', err);
    return NextResponse.json({ error: 'Failed to process file' }, { status: 500 });
  }
}
