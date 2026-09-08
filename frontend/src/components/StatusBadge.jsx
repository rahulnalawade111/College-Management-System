export default function StatusBadge({ value }) {
  const cls = String(value || '').toLowerCase().replace('_', '-')
  return <span className={`badge badge-${cls}`}>{format(value)}</span>
}

function format(value) {
  if (!value) return '—'
  return String(value)
    .split('_')
    .map((w) => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase())
    .join(' ')
}
