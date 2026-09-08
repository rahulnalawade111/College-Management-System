import { useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../../services/api'

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [busy, setBusy] = useState(false)
  const [done, setDone] = useState(null)
  const [error, setError] = useState(null)

  async function handleSubmit(e) {
    e.preventDefault()
    if (!email.trim()) return
    setBusy(true)
    setError(null)
    try {
      const { data } = await api.post('/auth/forgot-password', { email: email.trim() })
      setDone(data)
    } catch (err) {
      setError(err.response?.data?.message || 'Something went wrong')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1>Reset your password</h1>
        <p className="muted">Enter your account email; we'll issue a reset token.</p>

        {error && <div className="alert alert-danger">{error}</div>}

        {done ? (
          <div className="alert alert-success">
            <p>{done.message}</p>
            {done.resetToken && (
              <>
                <p className="muted small">Dev mode token (email is mocked):</p>
                <code className="reset-token">{done.resetToken}</code>
              </>
            )}
          </div>
        ) : (
          <form onSubmit={handleSubmit} noValidate>
            <div className="form-field">
              <label htmlFor="email">Email</label>
              <input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@college.edu"
                required
              />
            </div>
            <button type="submit" className="btn btn-primary btn-block" disabled={busy}>
              {busy ? 'Sending…' : 'Send reset token'}
            </button>
          </form>
        )}

        <p className="muted small center">
          <Link to="/reset-password">I have a token</Link> · <Link to="/login">Back to login</Link>
        </p>
      </div>
    </div>
  )
}
