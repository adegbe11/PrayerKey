import { NextRequest, NextResponse } from 'next/server'

export async function POST(req: NextRequest) {
  const { imageBase64 } = await req.json()
  const token = process.env.REPLICATE_API_TOKEN

  if (!token) {
    return NextResponse.json({ clientEnhance: true })
  }

  try {
    const startRes = await fetch('https://api.replicate.com/v1/predictions', {
      method: 'POST',
      headers: {
        Authorization: `Token ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        version: '42fed1c4974146d4d2414e2be2c5277c7fcf05fcc3a73abf41610695738c1d7b',
        input: { image: imageBase64, scale: 4, face_enhance: true },
      }),
    })

    let prediction = await startRes.json()
    let attempts = 0

    while (prediction.status !== 'succeeded' && prediction.status !== 'failed' && attempts < 30) {
      await new Promise(r => setTimeout(r, 2000))
      const pollRes = await fetch(
        `https://api.replicate.com/v1/predictions/${prediction.id}`,
        { headers: { Authorization: `Token ${token}` } }
      )
      prediction = await pollRes.json()
      attempts++
    }

    if (prediction.status === 'succeeded') {
      return NextResponse.json({ enhancedUrl: prediction.output })
    }
    return NextResponse.json({ clientEnhance: true })
  } catch {
    return NextResponse.json({ clientEnhance: true })
  }
}
