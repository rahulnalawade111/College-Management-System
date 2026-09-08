/** Reusable states for data-driven pages: loading / empty / error. */
export function Loading({ label = 'Loading…' }) {
  return <div className="state-box state-loading"><span className="spinner" /> {label}</div>
}

export function Empty({ message = 'No records found.', hint }) {
  return (
    <div className="state-box state-empty">
      <span className="state-icon">📭</span>
      <p>{message}</p>
      {hint && <p className="muted small">{hint}</p>}
    </div>
  )
}

export function ErrorState({ message = 'Failed to load.', onRetry }) {
  return (
    <div className="state-box state-error">
      <span className="state-icon">⚠️</span>
      <p>{message}</p>
      {onRetry && <button type="button" className="btn btn-ghost" onClick={onRetry}>Retry</button>}
    </div>
  )
}

export function pickErrorMessage(err, fallback = 'Something went wrong') {
  const data = err?.response?.data
  if (!data) return fallback
  if (typeof data === 'string') return data
  if (data.message) return data.message
  if (data.errors && Object.keys(data.errors).length) {
    return Object.entries(data.errors).map(([f, m]) => `${f}: ${m}`).join('; ')
  }
  return fallback
}
