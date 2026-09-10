import { useEffect, useState } from 'react'
import useSeo from './useSeo'
import publicService from '../../services/publicService'

export default function NoticesPage() {
  useSeo('Notices', 'Official notices and announcements from ABC College of Higher Education.')
  const [notices, setNotices] = useState(null)
  const [error, setError] = useState(false)

  useEffect(() => {
    let active = true
    publicService.notices()
      .then((d) => active && setNotices(d))
      .catch(() => active && setError(true))
    return () => { active = false }
  }, [])

  return (
    <div className="page">
      <h1>Notices</h1>
      {error && <p className="alert alert-danger">Could not load notices right now — please try again shortly.</p>}
      {notices === null && !error && <p className="muted">Loading notices…</p>}
      {notices !== null && notices.length === 0 && !error && (
        <p className="muted">No notices published yet.</p>
      )}
      <ul className="notice-list">
        {(notices || []).map((n) => (
          <li key={n.id} className="notice-item">
            <strong>{n.title}</strong>
            {n.body && <p className="small">{n.body}</p>}
            {n.publishedAt && (
              <time className="muted small" dateTime={n.publishedAt}>
                Published {new Date(n.publishedAt).toLocaleDateString()}
              </time>
            )}
          </li>
        ))}
      </ul>
    </div>
  )
}
