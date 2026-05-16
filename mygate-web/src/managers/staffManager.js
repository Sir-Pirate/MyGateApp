import { db, auth } from '../firebase'
import {
  collection, doc, getDoc, addDoc, setDoc, getDocs,
  query, where, orderBy, limit, updateDoc, onSnapshot
} from 'firebase/firestore'

export async function addStaff({ name, phone, role, shiftStart, shiftEnd }) {
  const uid      = auth.currentUser.uid
  const userSnap = await getDoc(doc(db, 'users', uid))
  const { flatNo, tower, email } = userSnap.data()
  const docId = `${phone}_${uid}`

  const existing = await getDoc(doc(db, 'staff', docId))
  if (existing.exists()) throw new Error('Staff already exists')

  await setDoc(doc(db, 'staff', docId), {
    name, phone, role, shiftStart, shiftEnd,
    residentId: uid, residentName: email.split('@')[0],
    flatNo, tower, isActive: true, isLoggedIn: false,
    createdAt: Date.now()
  })
}

export async function markLogin(staffId) {
  const staffDoc = await getDoc(doc(db, 'staff', staffId))
  if (staffDoc.data().isLoggedIn) throw new Error('Already logged in')
  const now = Date.now()
  await addDoc(collection(db, 'staff_logs'), {
    staffId, residentId: staffDoc.data().residentId,
    loginTime: now, logoutTime: 0, isActive: true, createdAt: now
  })
  await updateDoc(doc(db, 'staff', staffId), { isLoggedIn: true })
}

export async function markLogout(staffId) {
  const now  = Date.now()
  const q    = query(collection(db, 'staff_logs'),
                     where('staffId',  '==', staffId),
                     where('isActive', '==', true), limit(1))
  const snap = await getDocs(q)
  if (snap.empty) throw new Error('No active login')

  const logDoc    = snap.docs[0]
  const duration  = Math.floor((now - logDoc.data().loginTime) / 60000)
  const status    = duration >= 60 ? 'present' : 'partial'

  await updateDoc(logDoc.ref, { logoutTime: now, durationMinutes: duration, status, isActive: false })
  await updateDoc(doc(db, 'staff', staffId), { isLoggedIn: false })
}

export async function getStaffLogs(staffId) {
  const q    = query(collection(db, 'staff_logs'),
                     where('staffId', '==', staffId),
                     orderBy('createdAt', 'desc'))
  const snap = await getDocs(q)
  return snap.docs.map(d => ({ id: d.id, ...d.data() }))
}

export function subscribeStaffDashboard(residentId, callback) {
  const q = query(collection(db, 'staff'),
                  where('residentId', '==', residentId),
                  where('isActive',   '==', true))
  return onSnapshot(q, snap => {
    const staff = snap.docs.map(d => ({ id: d.id, ...d.data() }))
    callback(staff)
  })
}