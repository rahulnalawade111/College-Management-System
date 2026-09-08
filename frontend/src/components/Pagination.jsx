export default function Pagination({ page, totalPages, totalElements, onPageChange, size, onSizeChange }) {
  if (totalPages == null) return null
  const from = totalElements === 0 ? 0 : page * (size || 10) + 1
  const to = Math.min((page + 1) * (size || 10), totalElements ?? 0)

  return (
    <div className="pagination">
      <span className="muted small">
        {totalElements != null ? `${from}–${to} of ${totalElements}` : `Page ${page + 1} of ${totalPages}`}
      </span>
      <div className="page-controls">
        <button type="button" className="btn btn-ghost btn-sm" disabled={page === 0} onClick={() => onPageChange(page - 1)}>
          ← Prev
        </button>
        <span className="page-current">{page + 1} / {totalPages || 1}</span>
        <button type="button" className="btn btn-ghost btn-sm" disabled={page >= totalPages - 1} onClick={() => onPageChange(page + 1)}>
          Next →
        </button>
        {onSizeChange && (
          <select
            className="page-size"
            value={size}
            onChange={(e) => onSizeChange(Number(e.target.value))}
            aria-label="Rows per page"
          >
            {[10, 20, 50].map((n) => <option key={n} value={n}>{n} / page</option>)}
          </select>
        )}
      </div>
    </div>
  )
}
