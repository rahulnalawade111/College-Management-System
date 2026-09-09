import { useEffect, useState } from 'react'
import { assignmentService, submissionService } from '../../services/assignmentService'
import { StatCard, SectionCard } from '../../components/DashboardWidgets'
import { Loading, ErrorState, Empty } from '../../components/States'
import StatusBadge from '../../components/StatusBadge'
import { useToast } from '../../context/ToastContext'
import Modal from '../../components/Modal'

const EMPTY_FORM = {
  title: '', description: '', assignmentType: 'HOMEWORK',
  maxMarks: 100, dueDate: '',
}

function fmtDue(d) {
  if (!d) return 'No due date'
  return new Date(d).toLocaleString('en-GB', {
    day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit',
  })
}

export default function FacultyAssignmentsPage() {
  const toast = useToast()
  const [assignments, setAssignments] = useState(null)
  const [error, setError] = useState(null)
  const [showCreate, setShowCreate] = useState(false)
  const [form, setForm] = useState(EMPTY_FORM)
  const [saving, setSaving] = useState(false)
  const [grading, setGrading] = useState(null) // assignment open for grading
  const [submissions, setSubmissions] = useState([])
  const [gradeForm, setGradeForm] = useState({ id: null, marks: '', feedback: '' })

  const load = () => assignmentService.facultyMine().then(setAssignments)
    .catch(e => setError(e.response?.data?.message || 'Failed to load assignments'))

  useEffect(() => { load() }, [])

  async function openGrading(a) {
    setGrading(a)
    try {
      const subs = await submissionService.forAssignment(a.id)
      setSubmissions(subs)
    } catch (e) {
      toast.error(e.response?.data?.message || 'Failed to load submissions')
    }
  }

  async function saveGrade(e) {
    e.preventDefault()
    try {
      await submissionService.grade(gradeForm.id, {
        marksObtained: Number(gradeForm.marks),
        feedback: gradeForm.feedback,
      })
      toast.success('Submission graded')
      setGradeForm({ id: null, marks: '', feedback: '' })
      if (grading) setSubmissions(await submissionService.forAssignment(grading.id))
      load()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to save grade')
    }
  }

  async function toggleStatus(a) {
    try {
      await assignmentService.setStatus(a.id, a.status === 'ACTIVE' ? 'CLOSED' : 'ACTIVE')
      toast.success(a.status === 'ACTIVE' ? 'Assignment closed' : 'Assignment reopened')
      load()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to update status')
    }
  }

  async function createAssignment(e) {
    e.preventDefault()
    setSaving(true)
    try {
      await assignmentService.create({
        ...form,
        maxMarks: Number(form.maxMarks),
        dueDate: form.dueDate || null,
      })
      toast.success('Assignment created')
      setShowCreate(false)
      setForm(EMPTY_FORM)
      load()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to create assignment')
    } finally {
      setSaving(false)
    }
  }

  if (error) return <ErrorState message={error} />
  if (!assignments) return <Loading label="Loading assignments…" />

  const active = assignments.filter(a => a.status === 'ACTIVE')
  const ungraded = submissions.filter(s => s.status !== 'GRADED').length

  return (
    <div className="dashboard-page">
      <div className="page-head-row">
        <h1 className="page-title">Assignments</h1>
        <button type="button" className="btn btn-primary" onClick={() => setShowCreate(true)}>
          + New Assignment
        </button>
      </div>

      <div className="stat-grid">
        <StatCard icon="📚" label="Total Assignments" value={assignments.length} tone="primary" />
        <StatCard icon="🟢" label="Active" value={active.length} tone="good" />
        <StatCard icon="🔴" label="Closed" value={assignments.length - active.length} tone="bad" />
        <StatCard icon="📝" label="Ungraded (open)" value={ungraded} tone="warn" />
      </div>

      <SectionCard title="My Assignments">
        {assignments.length ? (
          <table className="table">
            <thead>
              <tr><th>Title</th><th>Subject</th><th>Type</th><th>Max</th><th>Due</th><th>Status</th><th>Actions</th></tr>
            </thead>
            <tbody>
              {assignments.map(a => (
                <tr key={a.id}>
                  <td><strong>{a.title}</strong></td>
                  <td className="small">{a.subjectCode} — {a.subjectName}</td>
                  <td className="small">{a.assignmentType}</td>
                  <td>{a.maxMarks}</td>
                  <td className="small">{fmtDue(a.dueDate)}</td>
                  <td><StatusBadge value={a.status} /></td>
                  <td>
                    <div className="btn-row">
                      <button type="button" className="btn btn-sm btn-ghost" onClick={() => openGrading(a)}>Grade</button>
                      <button type="button" className="btn btn-sm btn-ghost" onClick={() => toggleStatus(a)}>
                        {a.status === 'ACTIVE' ? 'Close' : 'Reopen'}
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : <Empty message="No assignments yet. Create your first one!" />}
      </SectionCard>

      {showCreate && (
        <Modal title="New Assignment" onClose={() => setShowCreate(false)}>
          <form onSubmit={createAssignment} className="form-grid">
            <label>Title *
              <input required maxLength={150} value={form.title}
                onChange={e => setForm({ ...form, title: e.target.value })} />
            </label>
            <label>Description
              <textarea rows={3} value={form.description}
                onChange={e => setForm({ ...form, description: e.target.value })} />
            </label>
            <div className="form-row-2">
              <label>Type
                <select value={form.assignmentType}
                  onChange={e => setForm({ ...form, assignmentType: e.target.value })}>
                  <option>HOMEWORK</option><option>LAB</option>
                  <option>PROJECT</option><option>REPORT</option>
                </select>
              </label>
              <label>Max marks
                <input type="number" min="1" required value={form.maxMarks}
                  onChange={e => setForm({ ...form, maxMarks: e.target.value })} />
              </label>
            </div>
            <label>Due date
              <input type="datetime-local" value={form.dueDate}
                onChange={e => setForm({ ...form, dueDate: e.target.value })} />
            </label>
            <div className="btn-row end">
              <button type="button" className="btn btn-ghost" onClick={() => setShowCreate(false)}>Cancel</button>
              <button type="submit" className="btn btn-primary" disabled={saving}>
                {saving ? 'Saving…' : 'Create'}
              </button>
            </div>
          </form>
        </Modal>
      )}

      {grading && (
        <Modal title={`Submissions — ${grading.title}`} onClose={() => { setGrading(null); setSubmissions([]) }}>
          {submissions.length ? (
            <div className="grading-list">
              {submissions.map(s => (
                <div key={s.id} className="grading-item">
                  <div className="grading-head">
                    <strong>{s.studentName}</strong>
                    <span className="muted small">{s.studentCode}</span>
                    <StatusBadge value={s.status} />
                    <span className="muted small">{fmtDue(s.submittedAt)}</span>
                  </div>
                  {s.submissionText && <p className="small grading-text">{s.submissionText}</p>}
                  {s.status === 'GRADED' ? (
                    <p className="small"><strong>Grade:</strong> {s.marksObtained} / {s.maxMarks}
                      {s.feedback && <span className="muted"> — {s.feedback}</span>}</p>
                  ) : (
                    <form className="form-row-2 grade-form" onSubmit={saveGrade}>
                      <input type="number" step="0.5" min="0" max={grading.maxMarks}
                        placeholder={`Marks / ${grading.maxMarks}`} required
                        value={gradeForm.id === s.id ? gradeForm.marks : ''}
                        onChange={e => setGradeForm({ id: s.id, marks: e.target.value, feedback: gradeForm.feedback })} />
                      <input placeholder="Feedback"
                        value={gradeForm.id === s.id ? gradeForm.feedback : ''}
                        onChange={e => setGradeForm({ id: s.id, marks: gradeForm.marks, feedback: e.target.value })} />
                      <button type="submit" className="btn btn-sm btn-primary">Save grade</button>
                    </form>
                  )}
                </div>
              ))}
            </div>
          ) : <Empty message="No submissions yet for this assignment" />}
        </Modal>
      )}
    </div>
  )
}
