import { useEffect, useState } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { approveVisitor, revokeVisitor, getMyVisitors } from '../managers/visitorManager'

const GS = 'Google Sans,Roboto,sans-serif'

const statusStyle = {
  approved: { bg:'#e8f0fe', color:'#1a73e8' },
  arrived:  { bg:'#e6f4ea', color:'#137333' },
  revoked:  { bg:'#fce8e6', color:'#c5221f' },
}

export default function VisitorApprove() {
  const [visitors, setVisitors] = useState([])
  const [showAdd, setShowAdd]   = useState(false)
  const [form, setForm]         = useState({ name:'', phone:'', note:'' })
  const [adding, setAdding]     = useState(false)
  const [error, setError]       = useState('')
  const [filter, setFilter]     = useState('all')
  const navigate = useNavigate()
  const { state } = useLocation()
  const up = (k,v) => setForm(f => ({ ...f, [k]:v }))

  const load = () => getMyVisitors().then(setVisitors)
  useEffect(() => { load() }, [])

  async function handleApprove(e) {
    e.preventDefault()
    setAdding(true); setError('')
    try {
      await approveVisitor(form)
      setForm({ name:'', phone:'', note:'' })
      setShowAdd(false)
      load()
    } catch(err) { setError(err.message) }
    finally { setAdding(false) }
  }

  const filtered = filter === 'all' ? visitors : visitors.filter(v => v.status === filter)
  const inp = { width:'100%', padding:'12px 14px', fontSize:15, border:'1px solid #dadce0', borderRadius:8, outline:'none', boxSizing:'border-box', fontFamily:GS, color:'#202124', marginBottom:16 }
  const lbl = { display:'block', fontSize:12, fontWeight:500, color:'#5f6368', marginBottom:6, textTransform:'uppercase', letterSpacing:'.06em' }

  return (
    <div style={{ minHeight:'100vh', background:'#f8f9fa', fontFamily:GS }}>

      {/* Top bar */}
      <div style={{ background:'#fff', borderBottom:'1px solid #e0e0e0', padding:'0 20px', height:60, display:'flex', alignItems:'center', gap:14, position:'sticky', top:0, zIndex:10 }}>
        <span onClick={() => navigate('/home', { state })} style={{ cursor:'pointer', color:'#5f6368', fontSize:22 }}>←</span>
        <span style={{ fontSize:18, fontWeight:500, color:'#202124', flex:1 }}>Visitors</span>
        <button onClick={() => setShowAdd(true)} style={{ background:'#1a73e8', color:'#fff', border:'none', borderRadius:20, padding:'8px 18px', fontSize:13, fontWeight:500, cursor:'pointer', fontFamily:GS }}>
          + Approve visitor
        </button>
      </div>

      <div style={{ maxWidth:640, margin:'0 auto', padding:'24px 16px' }}>

        {/* Summary tiles */}
        <div style={{ display:'grid', gridTemplateColumns:'repeat(3,1fr)', gap:10, marginBottom:20 }}>
          {[
            { label:'Total',    value: visitors.length,                              color:'#1a73e8' },
            { label:'Arrived',  value: visitors.filter(v=>v.status==='arrived').length, color:'#34a853' },
            { label:'Pending',  value: visitors.filter(v=>v.status==='approved').length,color:'#fbbc04' },
          ].map(t => (
            <div key={t.label} style={{ background:'#fff', borderRadius:14, border:'1px solid #e0e0e0', padding:'14px 10px', textAlign:'center' }}>
              <div style={{ fontSize:24, fontWeight:500, color:t.color }}>{t.value}</div>
              <div style={{ fontSize:12, color:'#5f6368', marginTop:2 }}>{t.label}</div>
            </div>
          ))}
        </div>

        {/* Add visitor form */}
        {showAdd && (
          <div style={{ background:'#fff', borderRadius:16, border:'1px solid #e0e0e0', padding:20, marginBottom:16 }}>
            <h3 style={{ fontSize:16, fontWeight:500, color:'#202124', margin:'0 0 20px' }}>Pre-approve a visitor</h3>
            <form onSubmit={handleApprove}>
              <label style={lbl}>Visitor name</label>
              <input style={inp} placeholder="e.g. Rahul Sharma" value={form.name} onChange={e => up('name',e.target.value)} required />

              <label style={lbl}>Phone number</label>
              <input style={inp} type="tel" placeholder="+91 99999 99999" value={form.phone} onChange={e => up('phone',e.target.value)} required />

              <label style={lbl}>Note (optional)</label>
              <input style={inp} placeholder="e.g. Friend visiting for dinner" value={form.note} onChange={e => up('note',e.target.value)} />

              {error && <div style={{ background:'#fce8e6', color:'#c5221f', borderRadius:8, padding:'10px 14px', fontSize:13, marginBottom:14 }}>⚠️ {error}</div>}

              <div style={{ display:'flex', gap:10 }}>
                <button type="button" onClick={() => { setShowAdd(false); setError('') }} style={{ flex:1, padding:12, borderRadius:8, border:'1px solid #dadce0', background:'#fff', color:'#5f6368', fontSize:14, cursor:'pointer', fontFamily:GS }}>Cancel</button>
                <button type="submit" disabled={adding} style={{ flex:1, padding:12, borderRadius:8, border:'none', background:'#1a73e8', color:'#fff', fontSize:14, fontWeight:500, cursor:'pointer', fontFamily:GS, opacity: adding ? .7 : 1 }}>
                  {adding ? 'Approving...' : 'Approve'}
                </button>
              </div>
            </form>
          </div>
        )}

        {/* Filter chips */}
        <div style={{ display:'flex', gap:8, marginBottom:16, overflowX:'auto' }}>
          {['all','approved','arrived','revoked'].map(f => (
            <button key={f} onClick={() => setFilter(f)} style={{ padding:'7px 16px', borderRadius:20, border: filter===f ? '2px solid #1a73e8' : '1px solid #dadce0', background: filter===f ? '#e8f0fe' : '#fff', color: filter===f ? '#1a73e8' : '#5f6368', fontSize:13, fontWeight: filter===f ? 600 : 400, cursor:'pointer', fontFamily:GS, whiteSpace:'nowrap', flexShrink:0 }}>
              {f.charAt(0).toUpperCase()+f.slice(1)}
            </button>
          ))}
        </div>

        {/* Visitor list */}
        {filtered.length === 0 && (
          <div style={{ textAlign:'center', padding:'48px 24px', background:'#fff', borderRadius:16, border:'1px solid #e0e0e0' }}>
            <div style={{ fontSize:36, marginBottom:10 }}>🙋</div>
            <p style={{ color:'#5f6368' }}>No visitors {filter !== 'all' ? `with status "${filter}"` : 'yet'}</p>
          </div>
        )}

        {filtered.map(v => {
          const s = statusStyle[v.status] ?? statusStyle.approved
          return (
            <div key={v.id} style={{ background:'#fff', borderRadius:14, border:'1px solid #e0e0e0', padding:'16px 20px', marginBottom:10 }}>
              <div style={{ display:'flex', alignItems:'center', gap:12 }}>
                <div style={{ width:44, height:44, borderRadius:'50%', background:'#e8f0fe', display:'flex', alignItems:'center', justifyContent:'center', fontSize:20, flexShrink:0 }}>🙋</div>
                <div style={{ flex:1 }}>
                  <div style={{ fontSize:15, fontWeight:500, color:'#202124' }}>{v.name}</div>
                  <div style={{ fontSize:13, color:'#5f6368' }}>{v.phone}</div>
                  {v.note && <div style={{ fontSize:12, color:'#9aa0a6', marginTop:2 }}>📝 {v.note}</div>}
                </div>
                <div style={{ textAlign:'right' }}>
                  <span style={{ background:s.bg, color:s.color, borderRadius:12, padding:'4px 10px', fontSize:12, fontWeight:500, textTransform:'capitalize' }}>{v.status}</span>
                  {v.status === 'approved' && (
                    <div style={{ marginTop:8 }}>
                      <button onClick={() => revokeVisitor(v.id).then(load)} style={{ background:'#fce8e6', color:'#c5221f', border:'none', borderRadius:8, padding:'5px 12px', fontSize:12, cursor:'pointer', fontFamily:GS }}>Revoke</button>
                    </div>
                  )}
                </div>
              </div>
              <div style={{ marginTop:10, paddingTop:10, borderTop:'1px solid #f1f3f4', fontSize:12, color:'#9aa0a6' }}>
                Approved {new Date(v.approvedAt).toLocaleDateString('en-IN', { day:'numeric', month:'short', hour:'2-digit', minute:'2-digit' })}
                {v.arrivedAt > 0 && ` · Arrived ${new Date(v.arrivedAt).toLocaleTimeString('en-IN', { hour:'2-digit', minute:'2-digit' })}`}
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}