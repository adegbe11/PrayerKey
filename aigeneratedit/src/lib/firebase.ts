import { initializeApp, getApps } from 'firebase/app'
import { getStorage } from 'firebase/storage'
import {
  initializeAuth,
  getAuth,
  indexedDBLocalPersistence,
  browserLocalPersistence,
} from 'firebase/auth'
import { getFirestore } from 'firebase/firestore'

const firebaseConfig = {
  apiKey:            process.env.NEXT_PUBLIC_FIREBASE_API_KEY!,
  authDomain:        process.env.NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN!,
  projectId:         process.env.NEXT_PUBLIC_FIREBASE_PROJECT_ID!,
  storageBucket:     process.env.NEXT_PUBLIC_FIREBASE_STORAGE_BUCKET!,
  messagingSenderId: process.env.NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID!,
  appId:             process.env.NEXT_PUBLIC_FIREBASE_APP_ID!,
  measurementId:     process.env.NEXT_PUBLIC_FIREBASE_MEASUREMENT_ID,
}

// Prevent re-initialising on hot reload
const app = getApps().length ? getApps()[0] : initializeApp(firebaseConfig)

// Use indexedDB persistence — required for Capacitor WebView (sessionStorage is partitioned/unavailable)
function createAuth() {
  try {
    return initializeAuth(app, {
      persistence: [indexedDBLocalPersistence, browserLocalPersistence],
    })
  } catch {
    // Already initialized — getAuth() returns the existing instance
    return getAuth(app)
  }
}

export const storage = getStorage(app)
export const auth    = createAuth()
export const db      = getFirestore(app)
export default app
