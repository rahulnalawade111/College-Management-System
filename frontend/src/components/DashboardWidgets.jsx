export function StatCard({ icon, label, value, tone = 'primary', hint }) {
  return (
    <div className={`stat-card stat-${tone}`}>
      <div className="stat-icon">{icon}</div>
      <div className="stat-body">
        <span className="stat-value">{value ?? '—'}</span>
        <span className="stat-label">{label}</span>
        {hint && <span className="stat-hint muted small">{hint}</span>}
      </div>
    </div>
  )
}

export function ProgressBar({ percent, showLabel = true }) {
  const p = Math.max(0, Math.min(100, Number(percent) || 0))
  const tone = p >= 80 ? 'good' : p >= 60 ? 'warn' : 'bad'
  return (
    <div className={`progress-wrap progress-${tone}`} title={`${p}%`}>
      <div className="progress-track">
        <div className="progress-fill" style={{ width: `${p}%` }} />
      </div>
      {showLabel && <span className={`progress-text text-${tone}`}>{p}%</span>}
    </div>
  )
}

export function SectionCard({ title, actions, children, className = '' }) {
  return (
    <section className={`card section-card ${className}`}>
      {(title || actions) && (
        <div className="section-card-head">
          <h3>{title}</h3>
          {actions}
        </div>
      )}
      {children}
    </section>
  )
}

export function RecentActivities({ activities }) {
  if (!activities?.length) {
    return <p className="muted small pad-sm">No recent activity.</p>
  }
  const ICONS = {
    attendance: '🗓️',
    admission: '🎓',
    faculty: '👨‍🏫',
    exam: '📝',
    fee: '💰',
  }
  return (
    <ul className="activity-feed">
      {activities.map((a, i) => (
        <li key={i} className="activity-item">
          <span className="activity-icon">{ICONS[a.type] || '📌'}</span>
          <div>
            <div className="activity-message">{a.message}</div>
            <div className="muted small">{a.actor} · {a.time}</div>
          </div>
        </li>
      ))}
    </ul>
  )
}
