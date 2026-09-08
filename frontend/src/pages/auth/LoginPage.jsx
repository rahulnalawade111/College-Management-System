import { useState } from 'react'
import { useNavigate, Link, useLocation } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { homeForRole } from '../../services/authService'

const DEMO_ACCOUNTS = [
  ['admin', 'Admin@123', 'SUPER_ADMIN'],
  ['admin2', 'Admin@123', 'ADMIN'],
  ['faculty', 'Faculty@123', 'FACULTY'],
  ['student', 'Student@123', 'STUDENT'],
  ['parent', 'Parent@123', 'PARENT'],
]

export default function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const [form, setForm] = useState({ username: '', password: '' })
  const [showPassword, setShowPassword] = useState(false)
  const [remember, setRemember] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState(null)
  const [fieldErrors, setFieldErrors] = useState({})

  function validate() {
    const errors = {}
    if (!form.username.trim()) errors.username = 'Username or email is required'
    if (!form.password) errors.password = 'Password is required'
    return errors
  }

  async function handleSubmit(e) {
    e.preventDefault()
    const errors = validate()
    setFieldErrors(errors)
    if (Object.keys(errors).length) return

    setBusy(true)
    setError(null)
    try {
      const user = await login(form.username.trim(), form.password)
      navigate(location.state?.from || homeForRole(user.role), { replace: true })
    } catch (err) {
      const msg = err.response?.data?.message
      setError(msg || 'Login failed. Please try again.')
    } finally {
      setBusy(false)
    }
  }

  function fillDemo(username, password) {
    setForm({ username, password })
    setError(null)
    setFieldErrors({})
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-brand">
          <span className="auth-logo">🎓</span>
          <h1>ABC College of Higher Education</h1>
          <p>Student Management System</p>
        </div>

        {error && <div className="alert alert-danger" role="alert">{error}</div>}

        <form onSubmit={handleSubmit} noValidate>
          <div className="form-field">
            <label htmlFor="username">Username / Email</label>
            <input
              id="username"
              type="text"
              autoComplete="username"
              value={form.username}
              onChange={(e) => setForm({ ...form, username: e.target.value })}
              placeholder="e.g. admin"
            />
            {fieldErrors.username && <span className="field-error">{fieldErrors.username}</span>}
          </div>

          <div className="form-field">
            <label htmlFor="password">Password</label>
            <div className="password-wrap">
              <input
                id="password"
                type={showPassword ? 'text' : 'password'}
                autoComplete="current-password"
                value={form.password}
                onChange={(e) => setForm({ ...form, password: e.target.value })}
                placeholder="••••••••"
              />
              <button
                type="button"
                className="password-toggle"
                onClick={() => setShowPassword((v) => !v)}
                aria-label={showPassword ? 'Hide password' : 'Show password'}
              >
                {showPassword ? '🙈' : '👁️'}
              </button>
            </div>
            {fieldErrors.password && <span className="field-error">{fieldErrors.password}</span>}
          </div>

          <div className="auth-row">
            <label className="remember">
              <input type="checkbox" checked={remember} onChange={(e) => setRemember(e.target.checked)} />
              Remember me
            </label>
            <Link to="/forgot-password" className="forgot-link">Forgot password?</Link>
          </div>

          <button type="submit" className="btn btn-primary btn-block" disabled={busy}>
            {busy ? 'Signing in…' : 'Sign In'}
          </button>
        </form>

        <div className="demo-box">
          <p className="demo-title">Demo accounts (development only)</p>
          <div className="demo-grid">
            {DEMO_ACCOUNTS.map(([u, p, role]) => (
              <button key={u} type="button" className="demo-chip" onClick={() => fillDemo(u, p)} title={`Fill ${u}`}>
                <strong>{u}</strong>
                <span>{role}</span>
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}
