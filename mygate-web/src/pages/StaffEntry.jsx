import { useState } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { db } from '../firebase'
import { collection, query, where, getDocs, limit } from 'firebase/firestore'
import { markLogin, markLogout } from '../managers/staffManager'

const GS = 'Google Sans,Roboto,sans-serif'

export default function StaffEntry() {
  const [phone, setPhone]     = useState('')
  const [staff, setStaff]     = useState(null)
  const [staffId, setStaffId] = useState('')
  const [loading, setLoading] = useState(false)
  const [searching, setSearching] = useState(false)
  const [msg, setMsg]         = useState(null)   // { text, type: 'success'|'error' }
  const navigate = useNavigate()
  const { state } = useLocation()

  async function handleSearch(e) {
    e.preventDefault()
    setSearching(true); setStaff(null); setMsg(null)
    try {
      const q    = query(collection(db, 'staff'), where('phone', '==', phone), where('isActive', '==', true), limit(1))
      const snap = await getDocs(q)
      if (snap.empty) { setMsg({ text: 'No staff found with this phone number', type:'error' }); return }
      const doc  = snap.docs[0]
      setStaff(doc.data())
      setStaffId(doc.id)
    } catch(err) { setMsg({ text: err.message, type:'error' }) }
    finally { setSearching(false) }
  }

  async function handleLogin() {
    setLoading(true); setMsg(null)
    try {
      await markLogin(staffId)
      setStaff(s => ({ ...s, isLoggedIn: true }))
      setMsg({ text: `${staff.name} logged in successfully ✓`, type:'success' })
    } catch(err) { setMsg({ text: err.message, type:'error' }) }
    finally { setLoading(false) }
  }

  async function handleLogout() {
    setLoading(true); setMsg(null)
    try {
      await markLogout(staffId)
      setStaff(s => ({ ...s, isLoggedIn: false }))
      setMsg({ text: `${staff.name} logged out successfully ✓`, type:'success' })
    } catch(err) { setMsg({ text: err.message, type:'error' }) }
    finally { setLoading(false) }
  }

  const inp = { width:'100%', padding:'13px 14px', fontSize:15, border:'1px solid #dadce0', borderRadius:8, outline:'none', boxSizing:'border-box', fontFamily:GS, color:'#202124' }
  const lbl = { display:'block', fontSize:12, fontWeight:500, color:'#5f6368', marginBottom:6, textTransform:'uppercase', letterSpacing:'.06em' }

  return (
    <div style={{ minHeight:'100vh', background:'#f8f9fa', fontFamily:GS }}>

      {/* Top bar */}
      <div style={{ background:'#fff', borderBottom:'1px solid #e0e0e0', padding:'0 20px', height:60, display:'flex', alignItems:'center', gap:14, position:'sticky', top:0, zIndex:10 }}>
        <span onClick={() => navigate('/home', { state })} style={{ cursor:'pointer', color:'#5f6368', fontSize:22 }}>←</span>
        <span style={{ fontSize:18, fontWeight:500, color:'#202124' }}>Staff Entry</span>
      </div>

      <div style={{ maxWidth:540, margin:'0 auto', padding:'32px 16px' }}>

        {/* Search card */}
        <div style={{ background:'#fff', borderRadius:16, border:'1px solid #e0e0e0', padding:24, marginBottom:16 }}>
          <h3 style={{ fontSize:16, fontWeight:500, color:'#202124', margin:'0 0 20px' }}>Search staff by phone</h3>
          <form onSubmit={handleSearch}>
            <label style={lbl}>Phone number</label>
            <div style={{ display:'flex', gap:10 }}>
              <input style={{ ...inp, flex:1 }} type="tel" placeholder="+91 99999 99999" value={phone} onChange={e => setPhone(e.target.value)} required />
              <button type="submit" disabled={searching} style={{ padding:'13px 20px', borderRadius:8, border:'none', background:'#1a73e8', color:'#fff', fontSize:14, fontWeight:500, cursor:'pointer', fontFamily:GS, flexShrink:0 }}>
                {searching ? '...' : 'Search'}
              </button>
            </div>
          </form>
        </div>

        {/* Message */}
        {msg && (
          <div style={{ background: msg.type === 'success' ? '#e6f4ea' : '#fce8e6', color: msg.type === 'success' ? '#137333' : '#c5221f', borderRadius:10, padding:'12px 16px', fontSize:14, marginBottom:16, border: `1px solid ${msg.type === 'success' ? '#b7dfbf' : '#f28b82'}` }}>
            {msg.type === 'success' ? '✅' : '⚠️'} {msg.text}
          </div>
        )}

        {/* Staff card */}
        {staff && (
          <div style={{ background:'#fff', borderRadius:16, border:'1px solid #e0e0e0', padding:24 }}>

            {/* Staff info */}
            <div style={{ display:'flex', alignItems:'center', gap:14, marginBottom:20, paddingBottom:20, borderBottom:'1px solid #f1f3f4' }}>
              <div style={{ width:56, height:56, borderRadius:'50%', background: staff.isLoggedIn ? '#e6f4ea' : '#f1f3f4', display:'flex', alignItems:'center', justifyContent:'center', fontSize:26, flexShrink:0 }}>👤</div>
              <div style={{ flex:1 }}>
                <div style={{ fontSize:18, fontWeight:500, color:'#202124' }}>{staff.name}</div>
                <div style={{ fontSize:13, color:'#5f6368', marginTop:2 }}>{staff.role}</div>
                <div style={{ fontSize:12, color:'#9aa0a6', marginTop:2 }}>⏰ {staff.shiftStart} – {staff.shiftEnd}</div>
              </div>
              <span style={{ background: staff.isLoggedIn ? '#e6f4ea' : '#f1f3f4', color: staff.isLoggedIn ? '#137333' : '#5f6368', borderRadius:12, padding:'5px 12px', fontSize:13, fontWeight:500 }}>
                {staff.isLoggedIn ? '● Inside' : '○ Outside'}
              </span>
            </div>

            {/* Info rows */}
            {[
              { label:'Flat', value: `${staff.flatNo}, Tower ${staff.tower}` },
              { label:'Phone', value: staff.phone },
              { label:'Status', value: staff.isLoggedIn ? 'Currently inside premises' : 'Currently outside premises' },
            ].map(row => (
              <div key={row.label} style={{ display:'flex', justifyContent:'space-between', padding:'10px 0', borderBottom:'1px solid #f1f3f4', fontSize:14 }}>
                <span style={{ color:'#5f6368' }}>{row.label}</span>
                <span style={{ color:'#202124', fontWeight:500 }}>{row.value}</span>
              </div>
            ))}

            {/* Action buttons */}
            <div style={{ display:'flex', gap:12, marginTop:20 }}>
              <button
                onClick={handleLogin}
                disabled={loading || staff.isLoggedIn}
                style={{ flex:1, padding:14, borderRadius:10, border:'none', background: staff.isLoggedIn ? '#e8eaed' : '#34a853', color: staff.isLoggedIn ? '#9aa0a6' : '#fff', fontSize:15, fontWeight:500, cursor: staff.isLoggedIn ? 'not-allowed' : 'pointer', fontFamily:GS }}>
                {loading ? '...' : '✓ Mark Login'}
              </button>
              <button
                onClick={handleLogout}
                disabled={loading || !staff.isLoggedIn}
                style={{ flex:1, padding:14, borderRadius:10, border:'none', background: !staff.isLoggedIn ? '#e8eaed' : '#ea4335', color: !staff.isLoggedIn ? '#9aa0a6' : '#fff', fontSize:15, fontWeight:500, cursor: !staff.isLoggedIn ? 'not-allowed' : 'pointer', fontFamily:GS }}>
                {loading ? '...' : '✕ Mark Logout'}
              </button>
            </div>

            <button onClick={() => { setStaff(null); setPhone(''); setMsg(null) }}
              style={{ width:'100%', marginTop:12, padding:11, borderRadius:8, border:'1px solid #dadce0', background:'#fff', color:'#5f6368', fontSize:14, cursor:'pointer', fontFamily:GS }}>
              Search another staff
            </button>
          </div>
        )}
      </div>
    </div>
  )
}