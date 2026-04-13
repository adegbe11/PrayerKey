import type { ColorIndex } from './colors'

export type { ColorIndex }

export interface TravelerProfile {
  id: string
  name: string
  age: number
  photos: string[]
  location: string
  destinations: string[]
  travelDates: string
  travelStyle: TravelStyle[]
  travelGoal: TravelGoal
  bio: string
  interests: string[]
  budget: BudgetTier
  languages: string[]
  height?: string
  education?: string
  job?: string
  company?: string
  verified: VerifiedLevel
  anthem?: { title: string; artist: string }
  reputationScore?: number
  reputationCount?: number
  distance?: string
}

export type TravelStyle =
  | 'adventure' | 'beach' | 'culture' | 'content'
  | 'party' | 'luxury' | 'budget' | 'wellness'

export type TravelGoal = 'friends' | 'group' | 'open'

export type BudgetTier = 'budget' | 'midrange' | 'comfortable' | 'luxury'

export type VerifiedLevel = 0 | 1 | 2 | 3 | 4

export interface Match {
  id: string
  profile: TravelerProfile
  matchedAt: Date
  lastMessage?: Message
  unread?: number
}

export interface Message {
  id: string
  senderId: string
  text?: string
  emoji?: string
  gif?: string
  timestamp: Date
  status: 'sent' | 'delivered' | 'read'
  reactions?: { emoji: string; userId: string }[]
}

export interface Conversation {
  matchId: string
  profile: TravelerProfile
  messages: Message[]
  unread: number
  matchedAt: Date
}

export interface TripBoard {
  id: string
  host: TravelerProfile
  destination: string
  coverImage: string
  dates: string
  spotsTotal: number
  spotsFilled: number
  budget: BudgetTier
  vibe: string[]
  description: string
  applicants?: TravelerProfile[]
}

export interface OnboardingState {
  step: number
  name: string
  dob: { day: string; month: string; year: string }
  gender: string
  showGender: boolean
  lookingFor: string[]
  location: string
  radius: number
  travelStyles: TravelStyle[]
  destinations: string[]
  travelDates: { start: string; end: string }[]
  budget: BudgetTier
  travelGoal: TravelGoal
  interests: string[]
  photos: string[]
  bio: string
  spotifyAnthem?: { title: string; artist: string }
  isPremium: boolean
}
