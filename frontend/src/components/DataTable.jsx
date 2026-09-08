import { Loading, Empty, ErrorState } from './States'

/**
 * Generic data table with sorting + optional empty state.
 * columns: [{ key, label, sortable?, render?(row) }]
 */
export default function DataTable({
  columns,
  rows,
  loading,
  error,
  onRetry,
  emptyMessage,
  emptyHint,
  sort,
  onSortChange,
  rowKey = (r) => r.id,
}) {
  if (loading) return <Loading />
  if (error) return <ErrorState message={error} onRetry={onRetry} />
  if (!rows?.length) return <Empty message={emptyMessage} hint={emptyHint} />

  function handleSort(col) {
    if (!col.sortable || !onSortChange) return
    onSortChange({
      field: col.key,
      dir: sort?.field === col.key && sort.dir === 'asc' ? 'desc' : 'asc',
    })
  }

  return (
    <div className="table-wrap">
      <table className="data-table">
        <thead>
          <tr>
            {columns.map((col) => (
              <th
                key={col.key}
                className={col.sortable ? 'sortable' : ''}
                onClick={() => handleSort(col)}
                aria-sort={sort?.field === col.key ? (sort.dir === 'asc' ? 'ascending' : 'descending') : undefined}
              >
                {col.label}
                {col.sortable && <span className="sort-indicator">
                  {sort?.field === col.key ? (sort.dir === 'asc' ? ' ▲' : ' ▼') : ' ↕'}
                </span>}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={rowKey(row)}>
              {columns.map((col) => (
                <td key={col.key} data-label={col.label}>
                  {col.render ? col.render(row) : row[col.key]}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
