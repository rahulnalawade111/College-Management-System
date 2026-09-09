import { useEffect, useState } from 'react'
import { assignmentService, submissionService } from '../../services/assignmentService'
import { StatCard, SectionCard } from '../../components/DashboardWidgets'
import { Loading, ErrorState, Empty } from '../../components/States'
import StatusBadge from '../../components/StatusBadge'
import { useToast } from '../../context/ToastContext'
import Modal from '../../components/Modal'

function fmtDue(d) {
  if (!d) return 'No due date'
  return new Date(d).toLocaleString('en-GB', {
    day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit',
  })
}

export default function StudentAssignmentsPage() {
  const toast = useToast()
  const [assignments, setAssignments] = useState(null)
  const [submissions, setSubmissions] = useState([])
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(null) // assignment being submitted
  const [form, setForm] = useState({ submissionText: '', fileUrl: '' })
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    Promise.all([assignmentService.studentMine(), submissionService.studentMine()])
      .then(([a, s]) => { setAssignments(a); setSubmissions(s) })
      .catch(e => setError(e.response?.data?.message || 'Failed to load assignments'))
  }, [])

  const submissionFor = (id) => submissions.find(s => s.assignmentId === id)

  async function handleSubmit(e) {
    e.preventDefault()
    setSaving(true)
    try {
      const res = await submissionService.submit(submitting.id, form)
      toast.success(res.status === 'LATE'
        ? 'Submitted — recorded as LATE (past due date)'
        : 'Assignment submitted')
      setSubmissions(await submissionService.studentMine())
      setSubmitting(null)
      setForm({ submissionText: '', fileUrl: '' })
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to submit')
    } finally {
      setSaving(false)
    }
  }

  if (error) return <ErrorState message={error} />
  if (!assignments) return <Loading label="Loading assignments…" />

  const pending = assignments.filter(a => a.status === 'ACTIVE' && !submissionFor(a.id))
  const graded = submissions.filter(s => s.status === 'GRADED')

  return (
    <div className="dashboard-page">
      <h1 className="page-title">Assignments</h1>

      <div className="stat-grid">
        <StatCard icon="📚" label="Total" value={assignments.length} tone="primary" />
        <StatCard icon="⏳" label="Pending" value={pending.length} tone="warn" />
        <StatCard icon="✅" label="Submitted" value={submissions.length} tone="info" />
        <StatCard icon="🎯" label="Graded" value={graded.length} tone="good" />
      </div>

      <SectionCard title="My Assignments">
        {assignments.length ? (
          <table className="table">
            <thead>
              <tr><th>Title</th><th>Subject</th><th>Max</th><th>Due</th><th>My status</th><th>Action</th></tr>
            </thead>
            <tbody>
              {assignments.map(a => {
                const sub = submissionFor(a.id)
                return (
                  <tr key={a.id}>
                    <td>
                      <div><strong>{a.title}</strong></div>
                      {a.description && <div className="muted small clamp-2">{a.description}</div>}
                    </td>
                    <td className="small">{a.subjectCode}</td>
                    <td>{a.maxMarks}</td>
                    <td className="small">{fmtDue(a.dueDate)}</td>
                    <td>
                      {sub
                        ? <><StatusBadge value={sub.status} />
                            {sub.status === 'GRADED' &&
                              <div className="small"><strong>{sub.marksObtained}</strong>/{a.maxMarks}</div>}
                          </>
                        : <StatusBadge value={a.status === 'ACTIVE' ? 'PENDING' : 'CLOSED'} />}
                    </td>
                    <td>
                      {!sub && a.status === 'ACTIVE' && (
                        <button type="button" className="btn btn-sm btn-primary"
                          onClick={() => setSubmitting(a)}>Submit</button>
                      )}
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        ) : <Empty message="No assignments yet" />}
      </SectionCard>

      {submitting && (
        <Modal title={`Submit — ${submitting.title}`} onClose={() => setSubmitting(null)}>
          <form onSubmit={handleSubmit} className="form-grid">
            <p className="muted small">Due: {fmtDue(submitting.dueDate)}. Submissions after the due
              date are marked <strong>LATE</strong> automatically.</p>
            <label>Your answer
              <textarea rows={5} required={!form.fileUrl} value={form.submissionText}
                placeholder="Type your submission…"
                onChange={e => setForm({ ...form, submissionText: e.target.value })} />
            </label>
            <label>File link (optional)
              <input value={form.fileUrl} placeholder="https://…"
                onChange={e => setForm({ ...form, fileUrl: e.target.value })} />
            </label>
            <div className="btn-row end">
              <button type="button" className="btn btn-ghost" onClick={() => setSubmitting(null)}>Cancel</button>
              <button type="submit" className="btn btn-primary" disabled={saving}>
                {saving ? 'Submitting…' : 'Submit'}
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
