/**
 * Nomapal — Firestore database layer
 * Collections:
 *   users/{uid}                     — user profile
 *   swipes/{uid}/sent/{targetUid}   — swipe actions
 *   matches/{matchId}               — match documents
 *   messages/{matchId}/msgs/{msgId} — chat messages
 */

import {
  doc, getDoc, setDoc, updateDoc, deleteDoc,
  collection, query, where, getDocs, addDoc,
  onSnapshot, orderBy, limit, serverTimestamp,
  writeBatch, Timestamp,
  type DocumentData,
} from 'firebase/firestore'
import { db } from './firebase'

/* ─── Types ──────────────────────────────────────────────────────────────── */

export interface UserProfile {
  uid: string
  name: string
  age: number
  dob: string
  gender: string
  showGender: boolean
  lookingFor: string[]
  location: string
  radius: number
  destinations: string[]
  travelDates: string
  travelStyle: string[]
  budget: string
  travelGoal: string
  interests: string[]
  photos: string[]          // Firebase Storage URLs
  bio: string
  job?: string
  education?: string
  languages?: string[]
  height?: string
  anthem?: { title: string; artist: string }
  verified: number
  reputationScore?: number
  reputationCount?: number
  createdAt: Timestamp | null
  updatedAt: Timestamp | null
  // Lifestyle
  pronouns?: string
  zodiac?: string
  familyPlans?: string
  pets?: string
  drinking?: string
  smoking?: string
  workout?: string
  diet?: string
  loveLanguage?: string
  communicationStyle?: string
  relationshipGoal?: string
  prompts?: { question: string; answer: string }[]
  // Geolocation (stored for distance queries)
  lat?: number
  lng?: number
}

export type SwipeAction = 'like' | 'nope' | 'superlike' | 'dreamcrew'

export interface MatchDoc {
  id: string
  users: [string, string]         // [uid1, uid2]
  profiles: [UserProfile, UserProfile]
  createdAt: Timestamp | null
  lastMessage?: {
    text: string
    senderId: string
    timestamp: Timestamp | null
    status: string
  }
  unreadCounts: Record<string, number>  // { uid: count }
}

export interface MessageDoc {
  id: string
  senderId: string
  text: string
  timestamp: Timestamp | null
  status: 'sent' | 'delivered' | 'read'
}

/* ─── Users ──────────────────────────────────────────────────────────────── */

export async function saveUserProfile(uid: string, data: Partial<UserProfile>): Promise<void> {
  const ref = doc(db, 'users', uid)
  const existing = await getDoc(ref)
  if (existing.exists()) {
    await updateDoc(ref, { ...data, updatedAt: serverTimestamp() })
  } else {
    await setDoc(ref, {
      ...data,
      uid,
      verified: 1,
      reputationScore: null,
      reputationCount: 0,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  }
}

export async function getUserProfile(uid: string): Promise<UserProfile | null> {
  const snap = await getDoc(doc(db, 'users', uid))
  if (!snap.exists()) return null
  return snap.data() as UserProfile
}

/**
 * Fetch profiles to show in the swipe deck.
 * Returns up to `n` users the current user hasn't swiped on yet.
 */
export async function getDiscoverProfiles(currentUid: string, n = 20): Promise<UserProfile[]> {
  // Get all UIDs the current user has already swiped
  const swipedSnap = await getDocs(collection(db, 'swipes', currentUid, 'sent'))
  const swipedUids = new Set(swipedSnap.docs.map(d => d.id))
  swipedUids.add(currentUid) // never show yourself

  // Get all users (simple scan — works for MVP, add geo index later)
  const usersSnap = await getDocs(
    query(collection(db, 'users'), limit(100))
  )

  const profiles: UserProfile[] = []
  for (const d of usersSnap.docs) {
    if (swipedUids.has(d.id)) continue
    const data = d.data() as UserProfile
    if (data.photos?.length) profiles.push(data)
    if (profiles.length >= n) break
  }
  return profiles
}

/* ─── Swipes ─────────────────────────────────────────────────────────────── */

/**
 * Record a swipe and check for mutual match.
 * Returns the match ID if a match was created, null otherwise.
 */
export async function recordSwipe(
  fromUid: string,
  toUid: string,
  action: SwipeAction,
  fromProfile: UserProfile,
  toProfile: UserProfile,
): Promise<string | null> {
  const batch = writeBatch(db)

  // Save swipe
  const swipeRef = doc(db, 'swipes', fromUid, 'sent', toUid)
  batch.set(swipeRef, {
    action,
    timestamp: serverTimestamp(),
  })
  await batch.commit()

  // Only check for match on like/superlike
  if (action !== 'like' && action !== 'superlike') return null

  // Check if the other user already liked back
  const theirSwipeRef = doc(db, 'swipes', toUid, 'sent', fromUid)
  const theirSwipe = await getDoc(theirSwipeRef)

  if (theirSwipe.exists()) {
    const theirAction = theirSwipe.data()?.action as SwipeAction
    if (theirAction === 'like' || theirAction === 'superlike') {
      // It's a match!
      const matchId = [fromUid, toUid].sort().join('_')
      const matchRef = doc(db, 'matches', matchId)
      const existing = await getDoc(matchRef)
      if (!existing.exists()) {
        await setDoc(matchRef, {
          id: matchId,
          users: [fromUid, toUid],
          profiles: [fromProfile, toProfile],
          createdAt: serverTimestamp(),
          lastMessage: null,
          unreadCounts: { [fromUid]: 0, [toUid]: 0 },
        })
      }
      return matchId
    }
  }

  return null
}

/* ─── Matches ────────────────────────────────────────────────────────────── */

export async function getUserMatches(uid: string): Promise<MatchDoc[]> {
  const snap = await getDocs(
    query(
      collection(db, 'matches'),
      where('users', 'array-contains', uid),
      orderBy('createdAt', 'desc'),
    )
  )
  return snap.docs.map(d => ({ id: d.id, ...d.data() } as MatchDoc))
}

export function subscribeToMatches(uid: string, cb: (matches: MatchDoc[]) => void) {
  return onSnapshot(
    query(
      collection(db, 'matches'),
      where('users', 'array-contains', uid),
      orderBy('createdAt', 'desc'),
    ),
    snap => cb(snap.docs.map(d => ({ id: d.id, ...d.data() } as MatchDoc)))
  )
}

export async function getMatch(matchId: string): Promise<MatchDoc | null> {
  const snap = await getDoc(doc(db, 'matches', matchId))
  if (!snap.exists()) return null
  return { id: snap.id, ...snap.data() } as MatchDoc
}

/* ─── Messages ───────────────────────────────────────────────────────────── */

export async function sendMessage(
  matchId: string,
  senderId: string,
  text: string,
  otherUid: string,
): Promise<void> {
  const msgsRef = collection(db, 'messages', matchId, 'msgs')
  const msgRef = await addDoc(msgsRef, {
    senderId,
    text: text.trim(),
    timestamp: serverTimestamp(),
    status: 'sent',
  })

  // Update match document: lastMessage + unread count for recipient
  const matchRef = doc(db, 'matches', matchId)
  await updateDoc(matchRef, {
    lastMessage: {
      text: text.trim(),
      senderId,
      timestamp: serverTimestamp(),
      status: 'sent',
    },
    [`unreadCounts.${otherUid}`]: (await getDoc(matchRef)).data()?.unreadCounts?.[otherUid] + 1 || 1,
    [`unreadCounts.${senderId}`]: 0,
  })

  // Mark as delivered (simulate — in production use Cloud Functions)
  setTimeout(async () => {
    try {
      await updateDoc(doc(db, 'messages', matchId, 'msgs', msgRef.id), { status: 'delivered' })
    } catch { /* ignore */ }
  }, 800)
}

export function subscribeToMessages(
  matchId: string,
  cb: (msgs: MessageDoc[]) => void
) {
  return onSnapshot(
    query(
      collection(db, 'messages', matchId, 'msgs'),
      orderBy('timestamp', 'asc'),
    ),
    snap => cb(snap.docs.map(d => ({ id: d.id, ...d.data() } as MessageDoc)))
  )
}

export async function markMessagesRead(matchId: string, uid: string): Promise<void> {
  // Reset unread count for this user
  try {
    await updateDoc(doc(db, 'matches', matchId), {
      [`unreadCounts.${uid}`]: 0,
    })
    // Mark all messages sent by other user as read
    const snap = await getDocs(
      query(
        collection(db, 'messages', matchId, 'msgs'),
        where('senderId', '!=', uid),
        where('status', '!=', 'read'),
      )
    )
    const batch = writeBatch(db)
    snap.docs.forEach(d => batch.update(d.ref, { status: 'read' }))
    await batch.commit()
  } catch { /* ignore */ }
}

/* ─── Dream Crew (saved profiles) ───────────────────────────────────────── */

export async function saveToDreamCrew(uid: string, targetUid: string): Promise<void> {
  await setDoc(doc(db, 'dreamcrew', uid, 'saved', targetUid), {
    savedAt: serverTimestamp(),
  })
}

export async function getDreamCrew(uid: string): Promise<string[]> {
  const snap = await getDocs(collection(db, 'dreamcrew', uid, 'saved'))
  return snap.docs.map(d => d.id)
}
