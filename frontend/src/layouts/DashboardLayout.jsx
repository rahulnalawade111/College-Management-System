import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const ROLE_LABEL = {
  SUPER_ADMIN: 'Super Admin',
  ADMIN: 'Administrator',
  FACULTY: 'Faculty',
  STUDENT: 'Student',
  PARENT: 'Parent',
}

export default function DashboardLayout({ nav }) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="app-shell">
      <input type="checkbox" id="sidebar-toggle" className="sidebar-toggle-input" />
      <aside className="sidebar">
        <div className="sidebar-brand">
          <span className="sidebar-logo">🎓</span>
          <div>
            <strong>ABC College</strong>
            <span className="muted small">ERP Portal</span>
          </div>
        </div>
        <nav className="sidebar-nav">
          {nav.map(({ to, label, icon }) => (
            <NavLink key={to} to={to} className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
              <span className="nav-icon">{icon}</span> {label}
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-footer">
          <div className="user-chip">
            <div className="avatar">{user?.username?.[0]?.toUpperCase() ?? '?'}</div>
            <div>
              <strong>{user?.username}</strong>
              <span className="muted small">{ROLE_LABEL[user?.role] || user?.role}</span>
            </div>
          </div>
          <button type="button" className="btn btn-ghost btn-block" onClick={handleLogout}>
            Sign out
          </button>
        </div>
      </aside>

      <div className="main-col">
        <header className="topbar">
          <label htmlFor="sidebar-toggle" className="hamburger" aria-label="Toggle menu">☰</label>
          <span className="topbar-title">{ROLE_LABEL[user?.role] || 'Dashboard'}</span>
        </header>
        <main className="content">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
