import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../../services/api'

export default function ResetPasswordPage() {
  const navigate = useNavigate()
  const [token, setToken] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState(null)

  async function handleSubmit(e) {
    e.preventDefault()
    setError(null)
    if (newPassword.length < 6) return setError('Password must be at least 6 characters')
    if (newPassword !== confirm) return setError('Passwords do not match')
    setBusy(true)
    try {
      await api.post('/auth/reset-password', { token: token.trim(), newPassword })
      navigate('/login', { state: { reset: true } })
    } catch (err) {
      setError(err.response?.data?.message || 'Reset failed')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1>Set a new password</h1>
        {error && <div className="alert alert-danger">{error}</div>}
        <form onSubmit={handleSubmit} noValidate>
          <div className="form-field">
            <label htmlFor="token">Reset token</label>
            <input id="token" value={token} onChange={(e) => setToken(e.target.value)} placeholder="Paste token" required />
          </div>
          <div className="form-field">
            <label htmlFor="newPassword">New password</label>
            <input id="newPassword" type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} required />
          </div>
          <div className="form-field">
            <label htmlFor="confirm">Confirm password</label>
            <input id="confirm" type="password" value={confirm} onChange={(e) => setConfirm(e.target.value)} required />
          </div>
          <button type="submit" className="btn btn-primary btn-block" disabled={busy}>
            {busy ? 'Resetting…' : 'Reset password'}
          </button>
        </form>
        <p className="muted small center"><Link to="/forgot-password">Request a new token</Link></p>
      </div>
    </div>
  )
}
