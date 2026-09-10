import { useCallback, useEffect, useState } from 'react'
import api from '../../services/api'
import { useAuth } from '../../context/AuthContext'
import { Loading, ErrorState, pickErrorMessage } from '../../components/States'
import StatusBadge from '../../components/StatusBadge'
import { useToast } from '../../context/ToastContext'

/** Admin: user & role management (enable/disable, reset password) + own password change. */
export default function AdminSettingsPage() {
  const toast = useToast()
  const { user } = useAuth()
  const [users, setUsers] = useState(null)
  const [error, setError] = useState(false)
  const [search, setSearch] = useState('')
  const [resetting, setResetting] = useState(null) // user row
  const [newPw, setNewPw] = useState('')

  // own password change
  const [pw, setPw] = useState({ currentPassword: '', newPassword: '', confirm: '' })
  const [saving, setSaving] = useState(false)

  const load = useCallback(() => {
    setError(false)
    api.get('/admin/users')
      .then(({ data }) => setUsers(data))
      .catch(() => setError(true))
  }, [])

  useEffect(() => { load() }, [load])

  const setEnabled = async (u, enabled) => {
    try {
      await api.patch(`/admin/users/${u.id}/enabled`, null, { params: { enabled } })
      toast.success(`${u.username} ${enabled ? 'enabled' : 'disabled'}`)
      load()
    } catch (e) {
      toast.error(pickErrorMessage(e, 'Update failed'))
    }
  }

  const resetPassword = async (e) => {
    e.preventDefault()
    if (newPw.length < 8) {
      toast.error('Password must be at least 8 characters')
      return
    }
    try {
      await api.post(`/admin/users/${resetting.id}/reset-password`, null, { params: { newPassword: newPw } })
      toast.success(`Password reset for ${resetting.username}`)
      setResetting(null); setNewPw('')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Reset failed')
    }
  }

  const changeOwnPassword = async (e) => {
    e.preventDefault()
    if (pw.newPassword !== pw.confirm) {
      toast.error('New passwords do not match')
      return
    }
    if (pw.newPassword.length < 8) {
      toast.error('New password must be at least 8 characters')
      return
    }
    setSaving(true)
    try {
      await api.post('/auth/change-password', {
        currentPassword: pw.currentPassword, newPassword: pw.newPassword,
      })
      toast.success('Your password was changed')
      setPw({ currentPassword: '', newPassword: '', confirm: '' })
    } catch (err) {
      toast.error(err.response?.data?.message || 'Password change failed')
    } finally {
      setSaving(false)
    }
  }

  const visible = (users || []).filter((u) =>
    !search ||
    u.username.toLowerCase().includes(search.toLowerCase()) ||
    u.email?.toLowerCase().includes(search.toLowerCase()) ||
    u.role.toLowerCase().includes(search.toLowerCase()))

  return (
    <div>
      <h1>Settings</h1>
      <p className="muted small">Manage user accounts and your own credentials.</p>

      <div className="section-card" style={{ maxWidth: 460, margin: '1.2rem 0' }}>
        <h3>Change my password ({user?.username})</h3>
        <form onSubmit={changeOwnPassword}>
          <div className="form-row">
            <label>Current password</label>
            <input type="password" required value={pw.currentPassword}
                   onChange={(e) => setPw({ ...pw, currentPassword: e.target.value })} />
          </div>
          <div className="form-row">
            <label>New password (min 8 chars)</label>
            <input type="password" required minLength={8} value={pw.newPassword}
                   onChange={(e) => setPw({ ...pw, newPassword: e.target.value })} />
          </div>
          <div className="form-row">
            <label>Confirm new password</label>
            <input type="password" required value={pw.confirm}
                   onChange={(e) => setPw({ ...pw, confirm: e.target.value })} />
          </div>
          <button className="btn btn-primary" disabled={saving} type="submit">
            {saving ? 'Saving…' : 'Change password'}
          </button>
        </form>
      </div>

      <h2>User Accounts</h2>
      <input
        placeholder="Search users…"
        value={search}
        onChange={(e) => setSearch(e.target.value)}
        aria-label="Search users"
        style={{ maxWidth: 300, margin: '.6rem 0' }}
      />

      {error && <ErrorState onRetry={load} />}
      {users === null && !error && <Loading />}
      {users !== null && visible.length === 0 && !error && <p className="muted">No users match.</p>}

      {visible.length > 0 && (
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr><th>Username</th><th>Email</th><th>Role</th><th>Status</th><th>Actions</th></tr>
            </thead>
            <tbody>
              {visible.map((u) => (
                <tr key={u.id}>
                  <td>{u.username}</td>
                  <td>{u.email}</td>
                  <td>{u.role}</td>
                  <td><StatusBadge value={u.enabled ? 'ACTIVE' : 'INACTIVE'} /></td>
                  <td>
                    <span className="row-actions">
                      <button className="btn btn-ghost" onClick={() => setResetting(u)}>Reset password</button>
                      {u.id !== user?.id && (
                        <button className="btn btn-ghost"
                                onClick={() => setEnabled(u, !u.enabled)}>
                          {u.enabled ? 'Disable' : 'Enable'}
                        </button>
                      )}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {resetting && (
        <div className="section-card" style={{ maxWidth: 420, marginTop: '1rem', borderLeft: '4px solid var(--accent)' }}>
          <h3>Reset password — {resetting.username}</h3>
          <form onSubmit={resetPassword}>
            <div className="form-row">
              <label>New password (min 8 chars)</label>
              <input type="text" required minLength={8} value={newPw}
                     onChange={(e) => setNewPw(e.target.value)} />
            </div>
            <div className="row-actions">
              <button type="button" className="btn btn-ghost" onClick={() => setResetting(null)}>Cancel</button>
              <button className="btn btn-primary" type="submit">Reset</button>
            </div>
          </form>
        </div>
      )}
    </div>
  )
}
