import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { auth } from './firebase'
import { useEffect, useState } from 'react'
import LoginPage      from './pages/LoginPage'
import RegisterPage   from './pages/RegisterPage'
import HomePage       from './pages/HomePage'
import StaffDashboard from './pages/StaffDashboard'
import StaffHistory   from './pages/StaffHistory'
import VisitorApprove from './pages/VisitorApprove'
import StaffEntry     from './pages/StaffEntry'
import VisitorArrival from './pages/VisitorArrival'
import DeliveryLog    from './pages/DeliveryLog'

function ProtectedRoute({ children }) {
  const [user, setUser] = useState(undefined)
  useEffect(() => {
    const unsub = auth.onAuthStateChanged(u => setUser(u))
    return unsub
  }, [])
  if (user === undefined) return (
    <div style={{ minHeight:'100vh', display:'flex', alignItems:'center', justifyContent:'center', fontFamily:'Google Sans,Roboto,sans-serif', color:'#5f6368' }}>
      Loading...
    </div>
  )
  if (!user) return <Navigate to="/" replace />
  return children
}

const P = ({ children }) => <ProtectedRoute>{children}</ProtectedRoute>

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public */}
        <Route path="/"         element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        {/* Resident routes */}
        <Route path="/home"     element={<P><HomePage /></P>} />
        <Route path="/staff"    element={<P><StaffDashboard /></P>} />
        <Route path="/staff/:staffId/history" element={<P><StaffHistory /></P>} />
        <Route path="/visitors" element={<P><VisitorApprove /></P>} />

        {/* Guard routes */}
        <Route path="/staff-entry"     element={<P><StaffEntry /></P>} />
        <Route path="/visitor-arrival" element={<P><VisitorArrival /></P>} />
        <Route path="/delivery"        element={<P><DeliveryLog /></P>} />

        {/* Fallback */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  )
}