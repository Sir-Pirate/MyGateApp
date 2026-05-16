import { useEffect, useState } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { logDelivery, getPendingDeliveries } from '../managers/deliveryManager'

const GS = 'Google Sans,Roboto,sans-serif'

export default function DeliveryLog() {
  const [deliveries, setDeliveries] = useState([])
  const [showAdd, setShowAdd]       = useState(false)
  const [form, setForm]             = useState({ courierName:'', courierPhone:'', flatNumber:'' })
  const [adding, setAdding]         = useState(false)
  const [msg, setMsg]               = useState(null)
  const navigate = useNavigate()
  const { state } = useLocation()
  const up = (k,v) => setForm(f => ({ ...f, [k]:v }))

  const load = () => getPendingDeliveries().then(setDeliveries)
  useEffect(() => { load() }, [])

  async function handleLog(e) {
    e.preventDefault()
    setAdding(true); setMsg(null)
    try {
      await logDelivery(form)
      setForm({ courierName:'', courierPhone:'', flatNumber:'' })
      setShowAdd(false)
      setMsg({ text: 'Delivery logged successfully ✓', type:'success' })
      load()
    } catch(err) { setMsg({ text: err.message, type:'error' }) }
    finally { setAdding(false) }
  }

  const inp = { width:'100%', padding:'13px 14px', fontSize:15, border:'1px solid #dadce0', borderRadius:8, outline:'none', boxSizing:'border-box', fontFamily:GS, color:'#202124', marginBottom:16 }
  const lbl = { display:'block', fontSize:12, fontWeight:500, color:'#5f6368', marginBottom:6, textTransform:'uppercase', letterSpacing:'.06em' }

  return (
    <div style={{ minHeight:'100vh', background:'#f8f9fa', fontFamily:GS }}>

      {/* Top bar */}
      <div style={{ background:'#fff', borderBottom:'1px solid #e0e0e0', padding:'0 20px', height:60, display:'flex', alignItems:'center', gap:14, position:'sticky', top:0, zIndex:10 }}>
        <span onClick={() => navigate('/home', { state })} style={{ cursor:'pointer', color:'#5f6368', fontSize:22 }}>←</span>
        <span style={{ fontSize:18, fontWeight:500, color:'#202124', flex:1 }}>Delivery Log</span>
        <button onClick={() => setShowAdd(true)} style={{ background:'#1a73e8', color:'#fff', border:'none', borderRadius:20, padding:'8px 18px', fontSize:13, fontWeight:500, cursor:'pointer', fontFamily:GS }}>
          + Log delivery
        </button>
      </div>

      <div style={{ maxWidth:580, margin:'0 auto', padding:'24px 16px' }}>

        {/* Summary */}
        <div style={{ background:'#fff', borderRadius:14, border:'1px solid #e0e0e0', padding:'16px 20px', marginBottom:16, display:'flex', alignItems:'center', gap:14 }}>
          <div style={{ width:44, height:44, borderRadius:12, background:'#fef7e0', display:'flex', alignItems:'center', justifyContent:'center', fontSize:22 }}>📦</div>
          <div>
            <div style={{ fontSize:22, fontWeight:500, color:'#fbbc04' }}>{deliveries.length}</div>
            <div style={{ fontSize:13, color:'#5f6368' }}>Pending deliveries awaiting pickup</div>
          </div>
        </div>

        {/* Message */}
        {msg && (
          <div style={{ background: msg.type === 'success' ? '#e6f4ea' : '#fce8e6', color: msg.type === 'success' ? '#137333' : '#c5221f', borderRadius:10, padding:'12px 16px', fontSize:14, marginBottom:16, border:`1px solid ${msg.type === 'success' ? '#b7dfbf' : '#f28b82'}` }}>
            {msg.type === 'success' ? '✅' : '⚠️'} {msg.text}
          </div>
        )}

        {/* Log delivery form */}
        {showAdd && (
          <div style={{ background:'#fff', borderRadius:16, border:'1px solid #e0e0e0', padding:24, marginBottom:16 }}>
            <h3 style={{ fontSize:16, fontWeight:500, color:'#202124', margin:'0 0 20px' }}>Log new delivery</h3>
            <form onSubmit={handleLog}>
              <label style={lbl}>Courier / delivery person name</label>
              <input style={inp} placeholder="e.g. Swiggy Instamart" value={form.courierName} onChange={e => up('courierName',e.target.value)} required />

              <label style={lbl}>Courier phone</label>
              <input style={inp} type="tel" placeholder="+91 99999 99999" value={form.courierPhone} onChange={e => up('courierPhone',e.target.value)} required />

              <label style={lbl}>Destination flat number</label>
              <input style={inp} placeholder="e.g. A-101" value={form.flatNumber} onChange={e => up('flatNumber',e.target.value)} required />

              <div style={{ display:'flex', gap:10 }}>
                <button type="button" onClick={() => setShowAdd(false)} style={{ flex:1, padding:12, borderRadius:8, border:'1px solid #dadce0', background:'#fff', color:'#5f6368', fontSize:14, cursor:'pointer', fontFamily:GS }}>Cancel</button>
                <button type="submit" disabled={adding} style={{ flex:1, padding:12, borderRadius:8, border:'none', background:'#1a73e8', color:'#fff', fontSize:14, fontWeight:500, cursor:'pointer', fontFamily:GS, opacity: adding ? .7 : 1 }}>
                  {adding ? 'Logging...' : 'Log delivery'}
                </button>
              </div>
            </form>
          </div>
        )}

        {/* Pending list */}
        <p style={{ fontSize:13, fontWeight:500, color:'#5f6368', textTransform:'uppercase', letterSpacing:'.08em', marginBottom:12 }}>Pending pickups</p>

        {deliveries.length === 0 && (
          <div style={{ textAlign:'center', padding:'48px 24px', background:'#fff', borderRadius:16, border:'1px solid #e0e0e0' }}>
            <div style={{ fontSize:40, marginBottom:12 }}>📭</div>
            <p style={{ color:'#5f6368' }}>No pending deliveries right now</p>
          </div>
        )}

        {deliveries.map(d => (
          <div key={d.id} style={{ background:'#fff', borderRadius:14, border:'1px solid #e0e0e0', padding:'16px 20px', marginBottom:10 }}>
            <div style={{ display:'flex', alignItems:'center', gap:12, marginBottom:12 }}>
              <div style={{ width:44, height:44, borderRadius:12, background:'#fef7e0', display:'flex', alignItems:'center', justifyContent:'center', fontSize:20, flexShrink:0 }}>📦</div>
              <div style={{ flex:1 }}>
                <div style={{ fontSize:15, fontWeight:500, color:'#202124' }}>{d.courierName}</div>
                <div style={{ fontSize:13, color:'#5f6368' }}>{d.courierPhone}</div>
              </div>
              <span style={{ background:'#fef7e0', color:'#b06000', borderRadius:12, padding:'4px 10px', fontSize:12, fontWeight:500 }}>Pending</span>
            </div>
            <div style={{ display:'flex', justifyContent:'space-between', fontSize:13, color:'#5f6368', paddingTop:10, borderTop:'1px solid #f1f3f4' }}>
              <span>📍 Flat {d.flatNumber}</span>
              <span>🕐 {new Date(d.loggedAt).toLocaleTimeString('en-IN', { hour:'2-digit', minute:'2-digit' })}</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}