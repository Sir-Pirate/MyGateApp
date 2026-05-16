import { useEffect, useState } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { auth } from '../firebase'
import { subscribeStaffDashboard, addStaff } from '../managers/staffManager'

const GS = 'Google Sans,Roboto,sans-serif'

function TopBar({ title, onBack }) {
  return (
    <div style={{ background:'#fff', borderBottom:'1px solid #e0e0e0', padding:'0 20px', height:60, display:'flex', alignItems:'center', gap:14, position:'sticky', top:0, zIndex:10, fontFamily:GS }}>
      <span onClick={onBack} style={{ cursor:'pointer', color:'#5f6368', fontSize:22, lineHeight:1 }}>←</span>
      <span style={{ fontSize:18, fontWeight:500, color:'#202124' }}>{title}</span>
    </div>
  )
}

function StatCard({ value, label, color }) {
  return (
    <div style={{ background:'#fff', borderRadius:16, border:'1px solid #e0e0e0', padding:'16px 12px', textAlign:'center' }}>
      <div style={{ fontSize:28, fontWeight:500, color, marginBottom:4 }}>{value}</div>
      <div style={{ fontSize:12, color:'#5f6368', fontWeight:500 }}>{label}</div>
    </div>
  )
}

export default function StaffDashboard() {
  const [staffList, setStaffList] = useState([])
  const [showAdd, setShowAdd]     = useState(false)
  const [form, setForm]           = useState({ name:'', phone:'', role:'Maid', shiftStart:'08:00', shiftEnd:'17:00' })
  const [adding, setAdding]       = useState(false)
  const [error, setError]         = useState('')
  const navigate = useNavigate()
  const { state } = useLocation()
  const up = (k,v) => setForm(f => ({ ...f, [k]:v }))

  useEffect(() => {
    const uid = auth.currentUser?.uid
    if (!uid) return
    return subscribeStaffDashboard(uid, setStaffList)
  }, [])

  const totalStaff   = staffList.length
  const insideNow    = staffList.filter(s => s.isLoggedIn).length
  const presentToday = staffList.filter(s => s.loginTime > 0).length
  const totalMins    = staffList.reduce((sum, s) => sum + (s.durationMinutes ?? 0), 0)

  async function handleAddStaff(e) {
    e.preventDefault()
    setAdding(true); setError('')
    try {
      await addStaff(form)
      setShowAdd(false)
      setForm({ name:'', phone:'', role:'Maid', shiftStart:'08:00', shiftEnd:'17:00' })
    } catch(err) { setError(err.message) }
    finally { setAdding(false) }
  }

  const inp = { width:'100%', padding:'12px 14px', fontSize:15, border:'1px solid #dadce0', borderRadius:8, outline:'none', boxSizing:'border-box', fontFamily:GS, color:'#202124', marginBottom:16 }
  const lbl = { display:'block', fontSize:12, fontWeight:500, color:'#5f6368', marginBottom:6, textTransform:'uppercase', letterSpacing:'.06em' }

  return (
    <div style={{ minHeight:'100vh', background:'#f8f9fa', fontFamily:GS }}>
      <TopBar title="Staff Dashboard" onBack={() => navigate('/home', { state })} />

      <div style={{ maxWidth:640, margin:'0 auto', padding:'24px 16px' }}>

        {/* Analytics tiles */}
        <div style={{ display:'grid', gridTemplateColumns:'repeat(4,1fr)', gap:10, marginBottom:24 }}>
          <StatCard value={totalStaff}        label="Total staff"   color="#1a73e8" />
          <StatCard value={insideNow}         label="Inside now"    color="#34a853" />
          <StatCard value={presentToday}      label="Present today" color="#fbbc04" />
          <StatCard value={`${totalMins}m`}   label="Worked today"  color="#ea4335" />
        </div>

        {/* Header row */}
        <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:14 }}>
          <p style={{ fontSize:13, fontWeight:500, color:'#5f6368', textTransform:'uppercase', letterSpacing:'.08em', margin:0 }}>Your staff</p>
          <button onClick={() => setShowAdd(true)} style={{ background:'#1a73e8', color:'#fff', border:'none', borderRadius:20, padding:'8px 18px', fontSize:13, fontWeight:500, cursor:'pointer', fontFamily:GS }}>
            + Add staff
          </button>
        </div>

        {/* Add staff form */}
        {showAdd && (
          <div style={{ background:'#fff', borderRadius:16, border:'1px solid #e0e0e0', padding:20, marginBottom:16 }}>
            <h3 style={{ fontSize:16, fontWeight:500, color:'#202124', margin:'0 0 20px' }}>Add new staff member</h3>
            <form onSubmit={handleAddStaff}>
              <label style={lbl}>Full name</label>
              <input style={inp} placeholder="e.g. Sunita Devi" value={form.name} onChange={e => up('name',e.target.value)} required />

              <label style={lbl}>Phone number</label>
              <input style={inp} placeholder="+91 99999 99999" value={form.phone} onChange={e => up('phone',e.target.value)} required />

              <label style={lbl}>Role</label>
              <select style={inp} value={form.role} onChange={e => up('role',e.target.value)}>
                {['Maid','Cook','Driver','Security','Gardener','Other'].map(r => <option key={r}>{r}</option>)}
              </select>

              <div style={{ display:'grid', gridTemplateColumns:'1fr 1fr', gap:12 }}>
                <div>
                  <label style={lbl}>Shift start</label>
                  <input style={inp} type="time" value={form.shiftStart} onChange={e => up('shiftStart',e.target.value)} />
                </div>
                <div>
                  <label style={lbl}>Shift end</label>
                  <input style={inp} type="time" value={form.shiftEnd} onChange={e => up('shiftEnd',e.target.value)} />
                </div>
              </div>

              {error && <div style={{ background:'#fce8e6', color:'#c5221f', borderRadius:8, padding:'10px 14px', fontSize:13, marginBottom:14 }}>⚠️ {error}</div>}

              <div style={{ display:'flex', gap:10 }}>
                <button type="button" onClick={() => { setShowAdd(false); setError('') }} style={{ flex:1, padding:12, borderRadius:8, border:'1px solid #dadce0', background:'#fff', color:'#5f6368', fontSize:14, cursor:'pointer', fontFamily:GS }}>
                  Cancel
                </button>
                <button type="submit" disabled={adding} style={{ flex:1, padding:12, borderRadius:8, border:'none', background:'#1a73e8', color:'#fff', fontSize:14, fontWeight:500, cursor:'pointer', fontFamily:GS, opacity: adding ? .7 : 1 }}>
                  {adding ? 'Adding...' : 'Add staff'}
                </button>
              </div>
            </form>
          </div>
        )}

        {/* Staff list */}
        {staffList.length === 0 && !showAdd && (
          <div style={{ textAlign:'center', padding:'48px 24px', background:'#fff', borderRadius:16, border:'1px solid #e0e0e0' }}>
            <div style={{ fontSize:40, marginBottom:12 }}>👥</div>
            <p style={{ fontSize:15, color:'#5f6368', margin:'0 0 16px' }}>No staff added yet</p>
            <button onClick={() => setShowAdd(true)} style={{ background:'#1a73e8', color:'#fff', border:'none', borderRadius:20, padding:'10px 24px', fontSize:14, cursor:'pointer', fontFamily:GS }}>Add your first staff member</button>
          </div>
        )}

        {staffList.map(staff => (
          <div key={staff.id}
            onClick={() => navigate(`/staff/${staff.id}/history`, { state })}
            style={{ background:'#fff', borderRadius:16, border:'1px solid #e0e0e0', padding:'16px 20px', marginBottom:10, cursor:'pointer', display:'flex', alignItems:'center', gap:14, transition:'box-shadow .15s' }}
            onMouseEnter={e => e.currentTarget.style.boxShadow='0 2px 12px rgba(0,0,0,0.08)'}
            onMouseLeave={e => e.currentTarget.style.boxShadow='none'}
          >
            <div style={{ width:44, height:44, borderRadius:'50%', background: staff.isLoggedIn ? '#e6f4ea' : '#f1f3f4', display:'flex', alignItems:'center', justifyContent:'center', fontSize:20, flexShrink:0 }}>
              👤
            </div>
            <div style={{ flex:1 }}>
              <div style={{ fontSize:15, fontWeight:500, color:'#202124', marginBottom:2 }}>{staff.name}</div>
              <div style={{ fontSize:13, color:'#5f6368' }}>{staff.role}</div>
            </div>
            <div style={{ textAlign:'right' }}>
              <span style={{ background: staff.isLoggedIn ? '#e6f4ea' : '#f1f3f4', color: staff.isLoggedIn ? '#137333' : '#5f6368', borderRadius:12, padding:'4px 10px', fontSize:12, fontWeight:500 }}>
                {staff.isLoggedIn ? '● Inside' : '○ Outside'}
              </span>
              {staff.durationMinutes > 0 && (
                <div style={{ fontSize:11, color:'#9aa0a6', marginTop:4 }}>{staff.durationMinutes} min today</div>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}