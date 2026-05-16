import { db, auth } from '../firebase'
import { collection, addDoc, updateDoc, doc, getDocs, query, where, orderBy } from 'firebase/firestore'

export async function approveVisitor({ name, phone, note }) {
  const user = auth.currentUser
  return addDoc(collection(db, 'visitors'), {
    name, phone, note, status: 'approved',
    residentId: user.uid,
    approvedAt: Date.now(), arrivedAt: 0, revokedAt: 0
  })
}

export async function revokeVisitor(visitorId) {
  await updateDoc(doc(db, 'visitors', visitorId), {
    status: 'revoked', revokedAt: Date.now()
  })
}

export async function markArrived(visitorId) {
  await updateDoc(doc(db, 'visitors', visitorId), {
    status: 'arrived', arrivedAt: Date.now()
  })
}

export async function getMyVisitors() {
  const uid  = auth.currentUser.uid
  const q    = query(collection(db, 'visitors'),
                     where('residentId', '==', uid),
                     orderBy('approvedAt', 'desc'))
  const snap = await getDocs(q)
  return snap.docs.map(d => ({ id: d.id, ...d.data() }))
}