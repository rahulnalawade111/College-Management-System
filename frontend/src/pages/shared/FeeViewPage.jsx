import { useEffect, useState } from 'react'
import feeService from '../../services/feeService'
import { StatCard, SectionCard } from '../../components/DashboardWidgets'
import { Loading, Empty } from '../../components/States'
import StatusBadge from '../../components/StatusBadge'

const money = (v) => `₹${Number(v ?? 0).toLocaleString('en-IN')}`

/** Shared read-only fee view — used by STUDENT (/fees/me) and PARENT (/fees/my-child). */
export default function FeeViewPage({ title = 'My Fees', dataLoader = () => feeService.mine() }) {
  const [rows, setRows] = useState([])
  const [summary, setSummary] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([dataLoader(), feeService.summary()])
      .then(([r, s]) => { setRows(r); setSummary(s) })
      .catch((e) => setError(e.response?.data?.message || 'Could not load fees'))
      .finally(() => setLoading(false))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  if (error) return <div className="form-error">{error}</div>

  return (
    <div className="dashboard-page">
      <h1 className="page-title">{title}</h1>
      <p className="muted">Your fee records and outstanding balance. Payments are collected at the admin office.</p>

      {summary && (
        <div className="stat-grid">
          <StatCard icon="🧾" label="Total billed" value={money(summary.totalBilled)} />
          <StatCard icon="💰" label="Paid" value={money(summary.totalCollected)} tone="success" />
          <StatCard icon="⏳" label="Outstanding" value={money(summary.totalOutstanding)} tone="warning" />
        </div>
      )}

      <SectionCard title="Fee records">
        {loading ? <Loading /> : rows.length === 0 ? <Empty message="No fee records yet." /> : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Title</th><th>Type</th><th>Amount</th><th>Paid</th>
                  <th>Balance</th><th>Due date</th><th>Status</th>
                </tr>
              </thead>
              <tbody>
                {rows.map(f => (
                  <tr key={f.id}>
                    <td>{f.title}</td>
                    <td>{f.feeType}</td>
                    <td>{money(f.amount)}</td>
                    <td>{money(f.paidAmount)}</td>
                    <td>{money(f.balance)}</td>
                    <td>{f.dueDate}</td>
                    <td><StatusBadge value={f.status} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </SectionCard>
    </div>
  )
}
