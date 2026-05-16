import { initializeApp } from 'firebase/app'
import { getAuth } from 'firebase/auth'
import { getFirestore } from 'firebase/firestore'

const firebaseConfig = {
  apiKey: "AIzaSyDs2YLhZtE0GOySBqiVMGvH9aq6zzWubOI",
  authDomain: "mygate-f5827.firebaseapp.com",
  projectId: "mygate-f5827",
  storageBucket: "mygate-f5827.firebasestorage.app",
  messagingSenderId: "64254586137",
  appId: "1:64254586137:web:303db8f2b4ff3329146988"
};

const app = initializeApp(firebaseConfig)

export const auth = getAuth(app)
export const db   = getFirestore(app)