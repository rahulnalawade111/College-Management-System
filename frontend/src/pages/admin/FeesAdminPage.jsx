import { useEffect, useState } from 'react'
import feeService from '../../services/feeService'
import { listStudents } from '../../services/peopleService'
import { StatCard, SectionCard } from '../../components/DashboardWidgets'
import { Loading, Empty } from '../../components/States'
import StatusBadge from '../../components/StatusBadge'
import Modal from '../../components/Modal'
import { useToast } from '../../context/ToastContext'

const money = (v) => `₹${Number(v ?? 0).toLocaleString('en-IN')}`

export default function FeesAdminPage() {
  const toast = useToast()
  const [rows, setRows] = useState([])
  const [pageInfo, setPageInfo] = useState({ totalElements: 0, totalPages: 1, number: 0 })
  const [summary, setSummary] = useState(null)
  const [loading, setLoading] = useState(true)
  const [students, setStudents] = useState([])
  const [filterStudent, setFilterStudent] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [payFee, setPayFee] = useState(null)
  const [payAmount, setPayAmount] = useState('')
  const [payMethod, setPayMethod] = useState('CASH')
  const [form, setForm] = useState({ studentId: '', title: '', feeType: 'TUITION', amount: '', dueDate: '' })

  const load = (studentId = filterStudent) => {
    setLoading(true)
    Promise.all([
      feeService.list(studentId || undefined),
      feeService.summary(studentId || undefined),
    ])
      .then(([page, s]) => { setRows(page.content); setPageInfo(page); setSummary(s) })
      .catch((e) => toast.error(e.response?.data?.message || 'Could not load fees'))
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])
  useEffect(() => {
    studentService.all().then(setStudents).catch(() => setStudents([]))
  }, [])

  const submitForm = async (e) => {
    e.preventDefault()
    try {
      await feeService.create({
        studentId: Number(form.studentId),
        title: form.title,
        feeType: form.feeType,
        amount: Number(form.amount),
        dueDate: form.dueDate,
      })
      toast.success('Fee created')
      setShowForm(false)
      setForm({ studentId: '', title: '', feeType: 'TUITION', amount: '', dueDate: '' })
      load()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Create failed')
    }
  }

  const submitPayment = async (e) => {
    e.preventDefault()
    try {
      await feeService.pay(payFee.id, { amount: Number(payAmount), paymentMethod: payMethod })
      toast.success('Payment recorded')
      setPayFee(null); setPayAmount('')
      load()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Payment failed')
    }
  }

  const removeFee = async (id) => {
    try {
      await feeService.remove(id)
      toast.success('Fee deleted')
      load()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Delete failed')
    }
  }

  return (
    <div className="dashboard-page">
      <div className="page-head-row">
        <div>
          <h1 className="page-title">Fees</h1>
          <p className="muted">Fee ledger — create fee records and collect payments.</p>
        </div>
        <button type="button" className="btn btn-primary" onClick={() => setShowForm(true)}>+ New Fee</button>
      </div>

      {summary && (
        <div className="stat-grid">
          <StatCard icon="🧾" label="Total billed" value={money(summary.totalBilled)} />
          <StatCard icon="💰" label="Collected" value={money(summary.totalCollected)} tone="success" />
          <StatCard icon="⏳" label="Outstanding" value={money(summary.totalOutstanding)} tone="warning" />
          <StatCard icon="📄" label="Fee records" value={pageInfo.totalElements} />
        </div>
      )}

      <SectionCard title="All fees" actions={
        <select value={filterStudent} onChange={e => { setFilterStudent(e.target.value); load(e.target.value) }}>
          <option value="">All students</option>
          {students.map(s => <option key={s.id} value={s.id}>{s.studentId} — {s.firstName} {s.lastName}</option>)}
        </select>
      }>
        {loading ? <Loading /> : rows.length === 0 ? <Empty message="No fee records." /> : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Student</th><th>Title</th><th>Type</th>
                  <th>Amount</th><th>Paid</th><th>Balance</th><th>Due</th><th>Status</th><th></th>
                </tr>
              </thead>
              <tbody>
                {rows.map(f => (
                  <tr key={f.id}>
                    <td>{f.studentName} <span className="muted small">({f.studentCode})</span></td>
                    <td>{f.title}</td>
                    <td>{f.feeType}</td>
                    <td>{money(f.amount)}</td>
                    <td>{money(f.paidAmount)}</td>
                    <td>{money(f.balance)}</td>
                    <td>{f.dueDate}</td>
                    <td><StatusBadge value={f.status} /></td>
                    <td>
                      <div className="btn-row">
                        {f.status !== 'PAID' && (
                          <button type="button" className="btn btn-sm btn-primary"
                            onClick={() => { setPayFee(f); setPayAmount(f.balance) }}>Collect</button>
                        )}
                        <button type="button" className="btn btn-sm btn-ghost" onClick={() => removeFee(f.id)}>Delete</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </SectionCard>

      {showForm && (
        <Modal title="New fee" onClose={() => setShowForm(false)}>
          <form onSubmit={submitForm}>
            <div className="form-row">
              <label>Student
                <select required value={form.studentId} onChange={e => setForm({ ...form, studentId: e.target.value })}>
                  <option value="">Select…</option>
                  {students.map(s => <option key={s.id} value={s.id}>{s.studentId} — {s.firstName} {s.lastName}</option>)}
                </select>
              </label>
              <label>Title
                <input required maxLength={150} value={form.title}
                  onChange={e => setForm({ ...form, title: e.target.value })} />
              </label>
            </div>
            <div className="form-row">
              <label>Type
                <select value={form.feeType} onChange={e => setForm({ ...form, feeType: e.target.value })}>
                  {['TUITION', 'EXAM', 'HOSTEL', 'LIBRARY', 'TRANSPORT', 'MISC'].map(t => <option key={t}>{t}</option>)}
                </select>
              </label>
              <label>Amount (₹)
                <input required type="number" min="1" step="0.01" value={form.amount}
                  onChange={e => setForm({ ...form, amount: e.target.value })} />
              </label>
            </div>
            <div className="form-row">
              <label>Due date
                <input required type="date" value={form.dueDate}
                  onChange={e => setForm({ ...form, dueDate: e.target.value })} />
              </label>
            </div>
            <button type="submit" className="btn btn-primary">Create fee</button>
          </form>
        </Modal>
      )}

      {payFee && (
        <Modal title={`Collect payment — ${payFee.title}`} onClose={() => setPayFee(null)}>
          <form onSubmit={submitPayment}>
            <p className="muted">Outstanding balance: <strong>{money(payFee.balance)}</strong></p>
            <div className="form-row">
              <label>Amount (₹)
                <input required type="number" min="0.01" step="0.01" value={payAmount}
                  onChange={e => setPayAmount(e.target.value)} />
              </label>
              <label>Method
                <select value={payMethod} onChange={e => setPayMethod(e.target.value)}>
                  {['CASH', 'UPI', 'BANK_TRANSFER', 'CARD', 'CHEQUE'].map(m => <option key={m}>{m}</option>)}
                </select>
              </label>
            </div>
            <button type="submit" className="btn btn-primary">Record payment</button>
          </form>
        </Modal>
      )}
    </div>
  )
}
