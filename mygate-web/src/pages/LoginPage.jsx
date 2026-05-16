import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { loginUser } from '../managers/authManager'

export default function LoginPage() {
  const [email, setEmail]     = useState('')
  const [password, setPass]   = useState('')
  const [error, setError]     = useState('')
  const [loading, setLoading] = useState(false)
  const [showPass, setShow]   = useState(false)
  const navigate = useNavigate()

  async function handleLogin(e) {
    e.preventDefault()
    setLoading(true)
    try {
      const user = await loginUser(email, password)
      navigate('/home', { state: { role: user.role, name: user.name } })
    } catch { setError('Wrong email or password') }
    finally { setLoading(false) }
  }

  return (
    <div style={{ minHeight:'100vh', background:'#f8f9fa', display:'flex', alignItems:'center', justifyContent:'center', fontFamily:'Google Sans,Roboto,sans-serif' }}>
      <div style={{ background:'#fff', borderRadius:28, padding:'48px 40px', width:'100%', maxWidth:420, boxShadow:'0 2px 10px rgba(0,0,0,0.08)', border:'1px solid #dadce0' }}>

        {/* App branding — no Google logo */}
        <div style={{ textAlign:'center', marginBottom:32 }}>
          <div style={{ width:56, height:56, borderRadius:16, background:'#e8f0fe', display:'flex', alignItems:'center', justifyContent:'center', margin:'0 auto 16px', fontSize:28 }}>
            🏠
          </div>
          <h1 style={{ fontSize:24, fontWeight:400, color:'#202124', margin:'0 0 8px' }}>Sign in</h1>
          <p style={{ fontSize:16, color:'#5f6368', margin:0 }}>to continue to Mygate</p>
        </div>

        <form onSubmit={handleLogin}>
          {/* Email field */}
          <div style={{ position:'relative', marginBottom:20 }}>
            <label style={{
              position:'absolute', left:14,
              top: email ? 6 : 16,
              fontSize: email ? 12 : 16,
              color: email ? '#1a73e8' : '#5f6368',
              transition:'all .15s', pointerEvents:'none',
              fontFamily:'Google Sans,Roboto,sans-serif'
            }}>
              Email or phone
            </label>
            <input
              type="email" value={email}
              onChange={e => setEmail(e.target.value)}
              required
              onFocus={e => e.target.previousSibling.style.color='#1a73e8'}
              onBlur={e => e.target.previousSibling.style.color= email ? '#1a73e8' : '#5f6368'}
              style={{ width:'100%', padding:'20px 14px 8px', fontSize:16, border:'1px solid #dadce0', borderRadius:4, outline:'none', boxSizing:'border-box', fontFamily:'Google Sans,Roboto,sans-serif', color:'#202124', background:'transparent' }}
            />
          </div>

          {/* Password field */}
          <div style={{ position:'relative', marginBottom:28 }}>
            <label style={{
              position:'absolute', left:14,
              top: password ? 6 : 16,
              fontSize: password ? 12 : 16,
              color: password ? '#1a73e8' : '#5f6368',
              transition:'all .15s', pointerEvents:'none',
              fontFamily:'Google Sans,Roboto,sans-serif'
            }}>
              Password
            </label>
            <input
              type={showPass ? 'text' : 'password'} value={password}
              onChange={e => setPass(e.target.value)}
              required
              style={{ width:'100%', padding:'20px 44px 8px 14px', fontSize:16, border:'1px solid #dadce0', borderRadius:4, outline:'none', boxSizing:'border-box', fontFamily:'Google Sans,Roboto,sans-serif', color:'#202124', background:'transparent' }}
            />
            <span onClick={() => setShow(!showPass)} style={{ position:'absolute', right:12, top:'50%', transform:'translateY(-50%)', cursor:'pointer', color:'#5f6368', fontSize:18 }}>
              {showPass ? '🙈' : '👁️'}
            </span>
          </div>

          {error && (
            <div style={{ background:'#fce8e6', color:'#c5221f', borderRadius:4, padding:'10px 14px', fontSize:14, marginBottom:20, border:'1px solid #f28b82' }}>
              {error}
            </div>
          )}

          <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
            <span onClick={() => navigate('/register')} style={{ color:'#1a73e8', fontSize:14, fontWeight:500, cursor:'pointer' }}>
              Create account
            </span>
            <button type="submit" disabled={loading} style={{ background: loading ? '#8ab4f8' : '#1a73e8', color:'#fff', border:'none', borderRadius:4, padding:'10px 24px', fontSize:14, fontWeight:500, cursor: loading ? 'not-allowed' : 'pointer', fontFamily:'Google Sans,Roboto,sans-serif' }}>
              {loading ? 'Signing in...' : 'Next'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}