import { storage } from './firebase'
import { ref, uploadString, getDownloadURL } from 'firebase/storage'

/**
 * Upload a base64 data URL to Firebase Storage.
 * Returns the public download URL.
 *
 * @param dataUrl   - base64 data URL (e.g. from canvas or FileReader)
 * @param userId    - user's UID (used to scope the storage path)
 * @param slot      - photo slot index 0–8
 */
export async function uploadPhoto(
  dataUrl: string,
  userId: string,
  slot: number
): Promise<string> {
  const path = `users/${userId}/photos/slot_${slot}_${Date.now()}.jpg`
  const storageRef = ref(storage, path)

  // Strip the data URL prefix so Firebase gets raw base64
  await uploadString(storageRef, dataUrl, 'data_url')

  const downloadUrl = await getDownloadURL(storageRef)
  return downloadUrl
}

/**
 * Upload a profile cover / hero photo.
 */
export async function uploadCoverPhoto(
  dataUrl: string,
  userId: string
): Promise<string> {
  const path = `users/${userId}/cover_${Date.now()}.jpg`
  const storageRef = ref(storage, path)
  await uploadString(storageRef, dataUrl, 'data_url')
  return getDownloadURL(storageRef)
}
