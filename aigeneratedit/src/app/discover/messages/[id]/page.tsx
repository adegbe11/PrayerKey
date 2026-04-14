import ChatPageClient from './_client'

export function generateStaticParams() {
  return ['m1', 'm2', 'm3'].map(id => ({ id }))
}

export default function ChatPage() {
  return <ChatPageClient />
}
