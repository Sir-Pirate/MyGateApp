import { useEffect, useState } from 'react'
import { useParams, useNavigate, useLocation } from 'react-router-dom'
import { db } from '../firebase'
import { getDoc, doc } from 'firebase/firestore'
import { getStaffLogs } from '../managers/staffManager'

const GS = 'Google Sans,Roboto,sans-serif'

export default function StaffHistory() {
  const { staffId }       = useParams()
  const [staff, setStaff] = useState(null)
  const [logs,  setLogs]  = useState([])
  const navigate          = useNavigate()
  const { state }         = useLocation()

  useEffect(() => {
    getDoc(doc(db, 'staff', staffId)).then(d => setStaff(d.data()))
    getStaffLogs(staffId).then(setLogs)
  }, [staffId])

  const fmt = ts => ts ? new Date(ts).toLocaleString('en-IN', { day:'numeric', month:'short', hour:'2-digit', minute:'2-digit' }) : '—'

  const totalMins  = logs.reduce((s,l) => s + (l.durationMinutes ?? 0), 0)
  const presentDays = logs.filter(l => l.status === 'present').length

  return (
    <div style={{ minHeight:'100vh', background:'#f8f9fa', fontFamily:GS }}>

      {/* Top bar */}
      <div style={{ background:'#fff', borderBottom:'1px solid #e0e0e0', padding:'0 20px', height:60, display:'flex', alignItems:'center', gap:14, position:'sticky', top:0, zIndex:10 }}>
        <span onClick={() => navigate('/staff', { state })} style={{ cursor:'pointer', color:'#5f6368', fontSize:22 }}>←</span>
        <span style={{ fontSize:18, fontWeight:500, color:'#202124' }}>Attendance History</span>
      </div>

      <div style={{ maxWidth:640, margin:'0 auto', padding:'24px 16px' }}>

        {/* Staff info card */}
        {staff && (
          <div style={{ background:'#fff', borderRadius:16, border:'1px solid #e0e0e0', padding:20, marginBottom:20, display:'flex', alignItems:'center', gap:14 }}>
            <div style={{ width:52, height:52, borderRadius:'50%', background:'#e8f0fe', display:'flex', alignItems:'center', justifyContent:'center', fontSize:24 }}>👤</div>
            <div style={{ flex:1 }}>
              <div style={{ fontSize:17, fontWeight:500, color:'#202124' }}>{staff.name}</div>
              <div style={{ fontSize:13, color:'#5f6368', marginTop:2 }}>{staff.role} · {staff.shiftStart} – {staff.shiftEnd}</div>
            </div>
            <span style={{ background: staff.isLoggedIn ? '#e6f4ea' : '#f1f3f4', color: staff.isLoggedIn ? '#137333' : '#5f6368', borderRadius:12, padding:'5px 12px', fontSize:12, fontWeight:500 }}>
              {staff.isLoggedIn ? '● Inside' : '○ Outside'}
            </span>
          </div>
        )}

        {/* Summary tiles */}
        <div style={{ display:'grid', gridTemplateColumns:'1fr 1fr 1fr', gap:10, marginBottom:20 }}>
          {[
            { label:'Total logs',    value: logs.length,    color:'#1a73e8' },
            { label:'Present days',  value: presentDays,    color:'#34a853' },
            { label:'Total minutes', value: `${totalMins}m`,color:'#ea4335' },
          ].map(t => (
            <div key={t.label} style={{ background:'#fff', borderRadius:14, border:'1px solid #e0e0e0', padding:'14px 10px', textAlign:'center' }}>
              <div style={{ fontSize:22, fontWeight:500, color:t.color }}>{t.value}</div>
              <div style={{ fontSize:11, color:'#5f6368', marginTop:2 }}>{t.label}</div>
            </div>
          ))}
        </div>

        {/* Log list */}
        <p style={{ fontSize:13, fontWeight:500, color:'#5f6368', textTransform:'uppercase', letterSpacing:'.08em', marginBottom:12 }}>Log history</p>

        {logs.length === 0 && (
          <div style={{ textAlign:'center', padding:'48px 24px', background:'#fff', borderRadius:16, border:'1px solid #e0e0e0' }}>
            <div style={{ fontSize:36, marginBottom:10 }}>📋</div>
            <p style={{ color:'#5f6368' }}>No attendance logs yet</p>
          </div>
        )}

        {logs.map((log, i) => (
          <div key={i} style={{ background:'#fff', borderRadius:14, border:'1px solid #e0e0e0', padding:'16px 20px', marginBottom:10 }}>
            <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:12 }}>
              <span style={{ fontSize:13, color:'#5f6368', fontWeight:500 }}>Entry #{logs.length - i}</span>
              <span style={{ background: log.status === 'present' ? '#e6f4ea' : '#fef7e0', color: log.status === 'present' ? '#137333' : '#b06000', borderRadius:12, padding:'3px 10px', fontSize:12, fontWeight:500, textTransform:'capitalize' }}>
                {log.status ?? 'partial'}
              </span>
            </div>
            <div style={{ display:'grid', gridTemplateColumns:'1fr 1fr', gap:8 }}>
              <div style={{ background:'#f8f9fa', borderRadius:8, padding:'10px 12px' }}>
                <div style={{ fontSize:11, color:'#9aa0a6', marginBottom:2 }}>LOGIN</div>
                <div style={{ fontSize:13, fontWeight:500, color:'#202124' }}>{fmt(log.loginTime)}</div>
              </div>
              <div style={{ background:'#f8f9fa', borderRadius:8, padding:'10px 12px' }}>
                <div style={{ fontSize:11, color:'#9aa0a6', marginBottom:2 }}>LOGOUT</div>
                <div style={{ fontSize:13, fontWeight:500, color:'#202124' }}>{fmt(log.logoutTime)}</div>
              </div>
            </div>
            {log.durationMinutes > 0 && (
              <div style={{ marginTop:10, fontSize:13, color:'#5f6368' }}>
                ⏱ Duration: <strong style={{ color:'#202124' }}>{log.durationMinutes} minutes</strong>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}