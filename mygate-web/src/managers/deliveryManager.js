import { db, auth } from '../firebase'
import { collection, addDoc, getDocs, updateDoc, doc, query, where, orderBy } from 'firebase/firestore'

// Guard logs a new delivery
export async function logDelivery({ courierName, courierPhone, flatNumber }) {
  const guard = auth.currentUser
  if (!guard) throw new Error('Not logged in')
  return addDoc(collection(db, 'deliveries'), {
    courierName, courierPhone, flatNumber,
    residentId: '', residentEmail: '',
    status: 'pending',
    loggedAt: Date.now(),
    pickedUpAt: 0,
    guardId: guard.uid
  })
}

// Guard sees all pending deliveries
export async function getPendingDeliveries() {
  const q    = query(collection(db, 'deliveries'), where('status', '==', 'pending'), orderBy('loggedAt', 'desc'))
  const snap = await getDocs(q)
  return snap.docs.map(d => ({ id: d.id, ...d.data() }))
}

// Resident confirms pickup
export async function confirmPickup(deliveryId) {
  await updateDoc(doc(db, 'deliveries', deliveryId), {
    status: 'pickedup', pickedUpAt: Date.now()
  })
}

// Get deliveries for a flat (resident view)
export async function getMyDeliveries(flatNumber) {
  const q    = query(collection(db, 'deliveries'), where('flatNumber', '==', flatNumber), orderBy('loggedAt', 'desc'))
  const snap = await getDocs(q)
  return snap.docs.map(d => ({ id: d.id, ...d.data() }))
}