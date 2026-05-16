import { useState } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { db } from '../firebase'
import { collection, query, where, getDocs, updateDoc, doc } from 'firebase/firestore'

const GS = 'Google Sans,Roboto,sans-serif'

export default function VisitorArrival() {
  const [phone, setPhone]       = useState('')
  const [visitors, setVisitors] = useState([])
  const [searching, setSearching] = useState(false)
  const [msg, setMsg]           = useState(null)
  const navigate = useNavigate()
  const { state } = useLocation()

  async function handleSearch(e) {
    e.preventDefault()
    setSearching(true); setVisitors([]); setMsg(null)
    try {
      const q    = query(collection(db, 'visitors'), where('phone', '==', phone), where('status', '==', 'approved'))
      const snap = await getDocs(q)
      if (snap.empty) {
        setMsg({ text: 'No approved visitors found with this phone number', type:'error' })
      } else {
        setVisitors(snap.docs.map(d => ({ id: d.id, ...d.data() })))
      }
    } catch(err) { setMsg({ text: err.message, type:'error' }) }
    finally { setSearching(false) }
  }

  async function handleArrived(visitorId, name) {
    try {
      await updateDoc(doc(db, 'visitors', visitorId), { status:'arrived', arrivedAt: Date.now() })
      setVisitors(vs => vs.filter(v => v.id !== visitorId))
      setMsg({ text: `${name} marked as arrived ✓`, type:'success' })
    } catch(err) { setMsg({ text: err.message, type:'error' }) }
  }

  const inp = { width:'100%', padding:'13px 14px', fontSize:15, border:'1px solid #dadce0', borderRadius:8, outline:'none', boxSizing:'border-box', fontFamily:GS, color:'#202124' }

  return (
    <div style={{ minHeight:'100vh', background:'#f8f9fa', fontFamily:GS }}>

      {/* Top bar */}
      <div style={{ background:'#fff', borderBottom:'1px solid #e0e0e0', padding:'0 20px', height:60, display:'flex', alignItems:'center', gap:14, position:'sticky', top:0, zIndex:10 }}>
        <span onClick={() => navigate('/home', { state })} style={{ cursor:'pointer', color:'#5f6368', fontSize:22 }}>←</span>
        <span style={{ fontSize:18, fontWeight:500, color:'#202124' }}>Visitor Arrival</span>
      </div>

      <div style={{ maxWidth:540, margin:'0 auto', padding:'32px 16px' }}>

        {/* Search */}
        <div style={{ background:'#fff', borderRadius:16, border:'1px solid #e0e0e0', padding:24, marginBottom:16 }}>
          <h3 style={{ fontSize:16, fontWeight:500, color:'#202124', margin:'0 0 6px' }}>Search visitor by phone</h3>
          <p style={{ fontSize:13, color:'#5f6368', margin:'0 0 20px' }}>Enter the visitor's phone number to find their pre-approval</p>
          <form onSubmit={handleSearch}>
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
          <div style={{ background: msg.type === 'success' ? '#e6f4ea' : '#fce8e6', color: msg.type === 'success' ? '#137333' : '#c5221f', borderRadius:10, padding:'12px 16px', fontSize:14, marginBottom:16, border:`1px solid ${msg.type === 'success' ? '#b7dfbf' : '#f28b82'}` }}>
            {msg.type === 'success' ? '✅' : '⚠️'} {msg.text}
          </div>
        )}

        {/* Visitor cards */}
        {visitors.map(v => (
          <div key={v.id} style={{ background:'#fff', borderRadius:16, border:'1px solid #e0e0e0', padding:20, marginBottom:12 }}>
            <div style={{ display:'flex', alignItems:'center', gap:12, marginBottom:16 }}>
              <div style={{ width:48, height:48, borderRadius:'50%', background:'#e8f0fe', display:'flex', alignItems:'center', justifyContent:'center', fontSize:22 }}>🙋</div>
              <div style={{ flex:1 }}>
                <div style={{ fontSize:16, fontWeight:500, color:'#202124' }}>{v.name}</div>
                <div style={{ fontSize:13, color:'#5f6368' }}>{v.phone}</div>
              </div>
              <span style={{ background:'#e8f0fe', color:'#1a73e8', borderRadius:12, padding:'4px 10px', fontSize:12, fontWeight:500 }}>Pre-approved</span>
            </div>

            {[
              { label:'Approved by', value: v.residentName || 'Resident' },
              { label:'Note',        value: v.note || '—' },
              { label:'Approved at', value: new Date(v.approvedAt).toLocaleString('en-IN', { day:'numeric', month:'short', hour:'2-digit', minute:'2-digit' }) },
            ].map(row => (
              <div key={row.label} style={{ display:'flex', justifyContent:'space-between', padding:'8px 0', borderBottom:'1px solid #f1f3f4', fontSize:13 }}>
                <span style={{ color:'#5f6368' }}>{row.label}</span>
                <span style={{ color:'#202124', fontWeight:500 }}>{row.value}</span>
              </div>
            ))}

            <button onClick={() => handleArrived(v.id, v.name)}
              style={{ width:'100%', marginTop:16, padding:13, borderRadius:10, border:'none', background:'#34a853', color:'#fff', fontSize:15, fontWeight:500, cursor:'pointer', fontFamily:GS }}>
              ✓ Mark as Arrived
            </button>
          </div>
        ))}

        {/* Empty state */}
        {!searching && visitors.length === 0 && !msg && (
          <div style={{ textAlign:'center', padding:'48px 24px', background:'#fff', borderRadius:16, border:'1px solid #e0e0e0' }}>
            <div style={{ fontSize:40, marginBottom:12 }}>🚪</div>
            <p style={{ color:'#5f6368', fontSize:15 }}>Search for a visitor's phone number to verify their entry</p>
          </div>
        )}
      </div>
    </div>
  )
}