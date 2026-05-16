import { useLocation, useNavigate } from 'react-router-dom'
import { logoutUser } from '../managers/authManager'

const menuItems = {
  resident: [
    { icon:'👥', label:'Staff Dashboard',  desc:'Manage your household staff',       path:'/staff',    color:'#e8f0fe', border:'#c5d5f5', iconBg:'#1a73e8' },
    { icon:'🙋', label:'Visitors',          desc:'Pre-approve incoming visitors',     path:'/visitors', color:'#e6f4ea', border:'#b7dfbf', iconBg:'#34a853' },
    { icon:'📦', label:'Deliveries',        desc:'Track incoming packages',           path:'/deliveries', color:'#fef7e0', border:'#f5d97e', iconBg:'#fbbc04' },
    { icon:'🔔', label:'Alerts',            desc:'Community notifications',           path:'/alerts',   color:'#fce8e6', border:'#f5b8b4', iconBg:'#ea4335' },
  ],
  guard: [
    { icon:'🚪', label:'Visitor Arrival',  desc:'Mark visitors as arrived',          path:'/visitor-arrival', color:'#e8f0fe', border:'#c5d5f5', iconBg:'#1a73e8' },
    { icon:'💂', label:'Staff Entry',      desc:'Log staff login and logout',        path:'/staff-entry',     color:'#e6f4ea', border:'#b7dfbf', iconBg:'#34a853' },
    { icon:'📦', label:'Log Delivery',     desc:'Record incoming deliveries',        path:'/delivery',        color:'#fef7e0', border:'#f5d97e', iconBg:'#fbbc04' },
    { icon:'🔔', label:'Alerts',           desc:'View community alerts',             path:'/alerts',          color:'#fce8e6', border:'#f5b8b4', iconBg:'#ea4335' },
  ],
  admin: [
    { icon:'👥', label:'Staff Dashboard',  desc:'View all staff',                    path:'/staff',           color:'#e8f0fe', border:'#c5d5f5', iconBg:'#1a73e8' },
    { icon:'🙋', label:'Visitors',          desc:'All visitor approvals',             path:'/visitors',        color:'#e6f4ea', border:'#b7dfbf', iconBg:'#34a853' },
    { icon:'🚪', label:'Visitor Arrival',  desc:'Mark visitors as arrived',          path:'/visitor-arrival', color:'#fef7e0', border:'#f5d97e', iconBg:'#fbbc04' },
    { icon:'📦', label:'Deliveries',       desc:'All deliveries',                    path:'/deliveries',      color:'#fce8e6', border:'#f5b8b4', iconBg:'#ea4335' },
  ]
}

const roleChip = {
  resident: { bg:'#e8f0fe', color:'#1a73e8' },
  guard:    { bg:'#e6f4ea', color:'#137333' },
  admin:    { bg:'#fef7e0', color:'#b06000' },
}

export default function HomePage() {
  const { state } = useLocation()
  const navigate  = useNavigate()
  const role      = state?.role ?? 'resident'
  const name      = state?.name ?? 'User'
  const items     = menuItems[role] ?? menuItems.resident
  const chip      = roleChip[role] ?? roleChip.resident

  async function handleLogout() {
    await logoutUser()
    navigate('/')
  }

  return (
    <div style={{ minHeight:'100vh', background:'#f8f9fa', fontFamily:'Google Sans,Roboto,sans-serif' }}>

      {/* Top App Bar */}
      <div style={{ background:'#fff', borderBottom:'1px solid #e0e0e0', padding:'0 24px', height:64, display:'flex', alignItems:'center', justifyContent:'space-between', position:'sticky', top:0, zIndex:10 }}>
        <div style={{ display:'flex', alignItems:'center', gap:12 }}>
          <div style={{ width:32, height:32, borderRadius:8, background:'#1a73e8', display:'flex', alignItems:'center', justifyContent:'center', fontSize:18 }}>🏠</div>
          <span style={{ fontSize:20, fontWeight:500, color:'#202124' }}>Mygate</span>
        </div>
        <div style={{ display:'flex', alignItems:'center', gap:16 }}>
          <div style={{ width:36, height:36, borderRadius:'50%', background:'#1a73e8', display:'flex', alignItems:'center', justifyContent:'center', color:'#fff', fontWeight:600, fontSize:15, cursor:'pointer' }} title={name}>
            {name.charAt(0).toUpperCase()}
          </div>
        </div>
      </div>

      <div style={{ maxWidth:680, margin:'0 auto', padding:'32px 16px' }}>

        {/* Welcome card */}
        <div style={{ background:'#fff', borderRadius:16, border:'1px solid #e0e0e0', padding:24, marginBottom:28, display:'flex', alignItems:'center', gap:16 }}>
          <div style={{ width:56, height:56, borderRadius:'50%', background:'#e8f0fe', display:'flex', alignItems:'center', justifyContent:'center', fontSize:26, flexShrink:0 }}>
            {role === 'guard' ? '💂' : role === 'admin' ? '⚙️' : '🏠'}
          </div>
          <div style={{ flex:1 }}>
            <p style={{ fontSize:13, color:'#5f6368', margin:'0 0 2px' }}>Welcome back</p>
            <h2 style={{ fontSize:20, fontWeight:500, color:'#202124', margin:'0 0 8px' }}>{name}</h2>
            <span style={{ background: chip.bg, color: chip.color, borderRadius:12, padding:'3px 12px', fontSize:12, fontWeight:500 }}>
              {role.charAt(0).toUpperCase()+role.slice(1)}
            </span>
          </div>
          <button onClick={handleLogout} style={{ background:'none', border:'1px solid #dadce0', borderRadius:20, padding:'8px 18px', fontSize:14, color:'#5f6368', cursor:'pointer', fontFamily:'Google Sans,Roboto,sans-serif' }}>
            Sign out
          </button>
        </div>

        {/* Section label */}
        <p style={{ fontSize:13, fontWeight:500, color:'#5f6368', marginBottom:14, textTransform:'uppercase', letterSpacing:'.08em' }}>
          Quick actions
        </p>

        {/* Menu grid */}
        <div style={{ display:'grid', gridTemplateColumns:'1fr 1fr', gap:14 }}>
          {items.map(item => (
            <div key={item.path}
              onClick={() => navigate(item.path, { state })}
              style={{ background:'#fff', borderRadius:16, border:'1px solid #e0e0e0', padding:20, cursor:'pointer', transition:'box-shadow .2s, border-color .2s' }}
              onMouseEnter={e => { e.currentTarget.style.boxShadow='0 2px 12px rgba(0,0,0,0.1)'; e.currentTarget.style.borderColor='#c5d5f5' }}
              onMouseLeave={e => { e.currentTarget.style.boxShadow='none'; e.currentTarget.style.borderColor='#e0e0e0' }}
            >
              <div style={{ width:44, height:44, borderRadius:12, background:item.color, display:'flex', alignItems:'center', justifyContent:'center', fontSize:22, marginBottom:14 }}>
                {item.icon}
              </div>
              <div style={{ fontSize:15, fontWeight:500, color:'#202124', marginBottom:4 }}>{item.label}</div>
              <div style={{ fontSize:13, color:'#5f6368', lineHeight:1.5 }}>{item.desc}</div>
            </div>
          ))}
        </div>

        {/* Footer */}
        <p style={{ textAlign:'center', fontSize:12, color:'#9aa0a6', marginTop:40 }}>
          Mygate Society Management · Powered by Firebase
        </p>
      </div>
    </div>
  )
}