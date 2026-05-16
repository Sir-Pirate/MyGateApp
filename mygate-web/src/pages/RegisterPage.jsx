import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { registerUser } from '../managers/authManager'

const GS = 'Google Sans,Roboto,sans-serif'

const styles = {
  page:  { minHeight:'100vh', background:'#f8f9fa', display:'flex', alignItems:'center', justifyContent:'center', fontFamily:GS, padding:16 },
  card:  { background:'#fff', borderRadius:28, padding:'40px 36px', width:'100%', maxWidth:460, boxShadow:'0 2px 10px rgba(0,0,0,0.08)', border:'1px solid #dadce0' },
  label: { display:'block', fontSize:12, fontWeight:500, color:'#5f6368', marginBottom:6, textTransform:'uppercase', letterSpacing:'.06em' },
  inp:   { width:'100%', padding:'13px 14px', fontSize:15, border:'1px solid #dadce0', borderRadius:8, outline:'none', boxSizing:'border-box', fontFamily:GS, color:'#202124', background:'#fff', marginBottom:18 },
  btn:   { width:'100%', padding:14, borderRadius:8, border:'none', background:'#1a73e8', color:'#fff', fontSize:15, fontWeight:500, cursor:'pointer', fontFamily:GS, marginTop:4 },
  err:   { background:'#fce8e6', color:'#c5221f', borderRadius:8, padding:'10px 14px', fontSize:13, marginBottom:16, border:'1px solid #f28b82' },
  step:  (active) => ({ width:28, height:28, borderRadius:'50%', background: active ? '#1a73e8' : '#e8eaed', color: active ? '#fff' : '#5f6368', display:'flex', alignItems:'center', justifyContent:'center', fontSize:13, fontWeight:600, flexShrink:0 }),
  line:  { flex:1, height:2, background:'#e8eaed', margin:'0 8px' },
  lineA: { flex:1, height:2, background:'#1a73e8', margin:'0 8px' },
}

const ROLES = [
  { id:'resident', label:'Resident',  icon:'🏠', desc:'Flat owner or tenant' },
  { id:'guard',    label:'Guard',     icon:'💂', desc:'Security personnel' },
  { id:'admin',    label:'Admin',     icon:'⚙️', desc:'Society manager' },
]

export default function RegisterPage() {
  const [step, setStep]       = useState(1)   // 1 = role, 2 = details, 3 = done
  const [form, setForm]       = useState({ name:'', email:'', password:'', phone:'', role:'', flatNo:'', tower:'' })
  const [error, setError]     = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const up = (k,v) => setForm(f => ({ ...f, [k]:v }))

  async function handleSubmit(e) {
    e.preventDefault()
    if (!form.name || !form.email || !form.password || !form.phone) {
      setError('Please fill in all fields'); return
    }
    if (form.role === 'resident' && (!form.flatNo || !form.tower)) {
      setError('Please enter your flat number and tower'); return
    }
    setLoading(true)
    setError('')
    try {
      await registerUser(form)
      setStep(3)
    } catch(err) {
      setError(err.message)
    } finally { setLoading(false) }
  }

  // Step indicator
  const StepBar = () => (
    <div style={{ display:'flex', alignItems:'center', marginBottom:32 }}>
      <div style={styles.step(step >= 1)}>1</div>
      <div style={step >= 2 ? styles.lineA : styles.line}/>
      <div style={styles.step(step >= 2)}>2</div>
      <div style={step >= 3 ? styles.lineA : styles.line}/>
      <div style={styles.step(step >= 3)}>3</div>
    </div>
  )

  // ── Step 1: Pick role ──
  if (step === 1) return (
    <div style={styles.page}>
      <div style={styles.card}>
        <div style={{ textAlign:'center', marginBottom:28 }}>
          <div style={{ width:52, height:52, borderRadius:14, background:'#e8f0fe', display:'flex', alignItems:'center', justifyContent:'center', margin:'0 auto 14px', fontSize:26 }}>🏠</div>
          <h1 style={{ fontSize:22, fontWeight:400, color:'#202124', margin:'0 0 6px' }}>Create your account</h1>
          <p style={{ fontSize:14, color:'#5f6368', margin:0 }}>Step 1 of 2 — Choose your role</p>
        </div>
        <StepBar />
        <div style={{ display:'flex', flexDirection:'column', gap:12, marginBottom:24 }}>
          {ROLES.map(r => (
            <div key={r.id} onClick={() => { up('role', r.id); setStep(2) }}
              style={{ display:'flex', alignItems:'center', gap:16, padding:'16px 18px', borderRadius:12, border: form.role === r.id ? '2px solid #1a73e8' : '1px solid #dadce0', background: form.role === r.id ? '#e8f0fe' : '#fff', cursor:'pointer', transition:'all .15s' }}
              onMouseEnter={e => e.currentTarget.style.borderColor='#1a73e8'}
              onMouseLeave={e => e.currentTarget.style.borderColor= form.role===r.id ? '#1a73e8' : '#dadce0'}
            >
              <div style={{ width:44, height:44, borderRadius:12, background:'#f8f9fa', display:'flex', alignItems:'center', justifyContent:'center', fontSize:22, flexShrink:0 }}>{r.icon}</div>
              <div>
                <div style={{ fontSize:15, fontWeight:500, color:'#202124' }}>{r.label}</div>
                <div style={{ fontSize:13, color:'#5f6368' }}>{r.desc}</div>
              </div>
              <div style={{ marginLeft:'auto', color:'#1a73e8', fontSize:18 }}>›</div>
            </div>
          ))}
        </div>
        <div style={{ textAlign:'center' }}>
          <span onClick={() => navigate('/')} style={{ color:'#1a73e8', fontSize:14, fontWeight:500, cursor:'pointer' }}>Already have an account? Sign in</span>
        </div>
      </div>
    </div>
  )

  // ── Step 2: Fill details ──
  if (step === 2) return (
    <div style={styles.page}>
      <div style={styles.card}>
        <div style={{ textAlign:'center', marginBottom:28 }}>
          <div style={{ width:52, height:52, borderRadius:14, background:'#e8f0fe', display:'flex', alignItems:'center', justifyContent:'center', margin:'0 auto 14px', fontSize:26 }}>
            {ROLES.find(r=>r.id===form.role)?.icon}
          </div>
          <h1 style={{ fontSize:22, fontWeight:400, color:'#202124', margin:'0 0 6px' }}>Your details</h1>
          <p style={{ fontSize:14, color:'#5f6368', margin:0 }}>Step 2 of 2 — Fill in your information</p>
        </div>
        <StepBar />

        <form onSubmit={handleSubmit}>
          <label style={styles.label}>Full name</label>
          <input style={styles.inp} placeholder="John Doe" value={form.name} onChange={e => up('name', e.target.value)} required />

          <label style={styles.label}>Email address</label>
          <input style={styles.inp} type="email" placeholder="john@email.com" value={form.email} onChange={e => up('email', e.target.value)} required />

          <label style={styles.label}>Password</label>
          <input style={styles.inp} type="password" placeholder="Minimum 6 characters" value={form.password} onChange={e => up('password', e.target.value)} required />

          <label style={styles.label}>Phone number</label>
          <input style={styles.inp} type="tel" placeholder="+91 99999 99999" value={form.phone} onChange={e => up('phone', e.target.value)} required />

          {form.role === 'resident' && (
            <div style={{ display:'grid', gridTemplateColumns:'1fr 1fr', gap:12 }}>
              <div>
                <label style={styles.label}>Flat no.</label>
                <input style={styles.inp} placeholder="A-101" value={form.flatNo} onChange={e => up('flatNo', e.target.value)} required />
              </div>
              <div>
                <label style={styles.label}>Tower</label>
                <input style={styles.inp} placeholder="A" value={form.tower} onChange={e => up('tower', e.target.value)} required />
              </div>
            </div>
          )}

          {error && <div style={styles.err}>⚠️ {error}</div>}

          <button type="submit" disabled={loading} style={{ ...styles.btn, opacity: loading ? .7 : 1 }}>
            {loading ? 'Creating account...' : 'Create account'}
          </button>

          <button type="button" onClick={() => { setStep(1); setError('') }} style={{ width:'100%', padding:12, borderRadius:8, border:'1px solid #dadce0', background:'#fff', color:'#5f6368', fontSize:14, cursor:'pointer', fontFamily:GS, marginTop:10 }}>
            ← Back
          </button>
        </form>
      </div>
    </div>
  )

  // ── Step 3: Success ──
  return (
    <div style={styles.page}>
      <div style={{ ...styles.card, textAlign:'center' }}>
        <div style={{ width:72, height:72, borderRadius:'50%', background:'#e6f4ea', display:'flex', alignItems:'center', justifyContent:'center', margin:'0 auto 20px', fontSize:36 }}>✅</div>
        <h1 style={{ fontSize:22, fontWeight:400, color:'#202124', margin:'0 0 10px' }}>Account created!</h1>
        <p style={{ fontSize:15, color:'#5f6368', marginBottom:32, lineHeight:1.6 }}>
          Welcome to Mygate, <strong>{form.name}</strong>.<br/>You can now sign in with your email and password.
        </p>
        <button onClick={() => navigate('/')} style={styles.btn}>Go to Sign in</button>
      </div>
    </div>
  )
}