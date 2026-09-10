import { useEffect, useState } from 'react'
import api from '../../services/api'
import { Loading, Empty, pickErrorMessage } from '../../components/States'
import StatusBadge from '../../components/StatusBadge'
import { useToast } from '../../context/ToastContext'

/** Student self-service profile — reads GET /api/students/me. */
export default function StudentProfilePage() {
  const toast = useToast()
  const [me, setMe] = useState(null)
  const [error, setError] = useState('')
  const [pwOpen, setPwOpen] = useState(false)
  const [pw, setPw] = useState({ currentPassword: '', newPassword: '', confirm: '' })
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    api.get('/students/me')
      .then(({ data }) => setMe(data))
      .catch((e) => setError(pickErrorMessage(e, 'Failed to load profile')))
  }, [])

  const changePassword = async (e) => {
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
        currentPassword: pw.currentPassword,
        newPassword: pw.newPassword,
      })
      toast.success('Password changed successfully')
      setPw({ currentPassword: '', newPassword: '', confirm: '' })
      setPwOpen(false)
    } catch (err) {
      toast.error(err.response?.data?.message || 'Password change failed')
    } finally {
      setSaving(false)
    }
  }

  if (error) return <div className="form-error">{error}</div>
  if (!me) return <Loading label="Loading profile…" />

  const rows = [
    ['Roll Number', me.studentId],
    ['Full Name', me.fullName],
    ['Email', me.email],
    ['Phone', me.phone || '—'],
    ['Date of Birth', me.dateOfBirth || '—'],
    ['Gender', me.gender || '—'],
    ['Course', me.courseName],
    ['Department', me.departmentName],
    ['Semester', me.semesterName],
    ['Academic Year', me.academicYearName],
    ['Address', [me.addressLine1, me.city, me.state, me.pincode].filter(Boolean).join(', ') || '—'],
    ['Status', <StatusBadge key="s" value={me.status} />],
  ]

  return (
    <div>
      <div className="page-head" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '1rem', flexWrap: 'wrap' }}>
        <div>
          <h1>My Profile</h1>
          <p className="muted">Your enrollment record as held by the college.</p>
        </div>
        <button className="btn btn-outline" style={{ borderColor: 'var(--primary)', color: 'var(--primary)' }}
                onClick={() => setPwOpen((v) => !v)}>
          Change password
        </button>
      </div>

      {pwOpen && (
        <form onSubmit={changePassword} className="section-card" style={{ maxWidth: 420, margin: '0 0 1.2rem' }}>
          <h3>Change password</h3>
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
            {saving ? 'Saving…' : 'Save new password'}
          </button>
        </form>
      )}

      <div className="table-wrap">
        <table className="data-table">
          <tbody>
            {rows.map(([k, v]) => (
              <tr key={k}>
                <th scope="row" style={{ textAlign: 'left', width: 180, color: 'var(--muted)' }}>{k}</th>
                <td>{v}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {me.guardians?.length > 0 && (
        <>
          <h2 style={{ marginTop: '1.6rem' }}>Guardians</h2>
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr><th>Name</th><th>Relation</th><th>Phone</th><th>Email</th><th>Occupation</th></tr>
              </thead>
              <tbody>
                {me.guardians.map((g) => (
                  <tr key={g.id}>
                    <td>{g.name}</td><td>{g.relation}</td><td>{g.phone || '—'}</td>
                    <td>{g.email || '—'}</td><td>{g.occupation || '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  )
}
