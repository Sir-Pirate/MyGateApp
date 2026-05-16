import { auth, db } from '../firebase'
import {
  createUserWithEmailAndPassword,
  signInWithEmailAndPassword,
  signOut
} from 'firebase/auth'
import { doc, setDoc, getDoc, collection, query, where, getDocs } from 'firebase/firestore'

export async function registerUser({ email, password, name, phone, role, flatNo, tower }) {
  const q = query(collection(db, 'users'), where('phone', '==', phone))
  const snap = await getDocs(q)
  if (!snap.empty) throw new Error('Phone number already registered')

  const result = await createUserWithEmailAndPassword(auth, email, password)
  const userId = result.user.uid

  await setDoc(doc(db, 'users', userId), {
    name, email, phone, role, userId, flatNo, tower
  })
  return { userId, role }
}

export async function loginUser(email, password) {
  const result = await signInWithEmailAndPassword(auth, email, password)
  const userDoc = await getDoc(doc(db, 'users', result.user.uid))
  return userDoc.data()
}

export const logoutUser = () => signOut(auth)