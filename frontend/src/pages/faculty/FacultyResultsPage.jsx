import { useEffect, useState } from 'react'
import { examService } from '../../services/examService'
import resultService from '../../services/resultService'
import { SectionCard } from '../../components/DashboardWidgets'
import { Loading, Empty } from '../../components/States'
import StatusBadge from '../../components/StatusBadge'
import { useToast } from '../../context/ToastContext'

export default function FacultyResultsPage() {
  const toast = useToast()
  const [exams, setExams] = useState([])
  const [examId, setExamId] = useState('')
  const [rows, setRows] = useState([])
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [grid, setGrid] = useState({})

  useEffect(() => { examService.all().then(setExams).catch(() => setExams([])) }, [])

  useEffect(() => {
    if (!examId) { setRows([]); return }
    setLoading(true)
    resultService.examResults(examId)
      .then(setRows)
      .catch(() => setRows([]))
      .finally(() => setLoading(false))
  }, [examId])

  const keyOf = (r) => `${r.studentId}-${r.subjectId}`
  const valueOf = (r, field) => {
    const k = keyOf(r)
    if (grid[k] && grid[k][field] !== undefined) return grid[k][field]
    return r[field] ?? ''
  }
  const setCell = (r, field, value) => {
    const k = keyOf(r)
    setGrid(g => ({ ...g, [k]: { ...(g[k] || {}), [field]: value } }))
  }
  const dirty = (r) => grid[keyOf(r)] !== undefined

  const saveRow = async (r) => {
    const g = grid[keyOf(r)] || {}
    const internal = g.internalMarks !== undefined ? g.internalMarks : r.internalMarks
    const external = g.externalMarks !== undefined ? g.externalMarks : r.externalMarks
    const practical = g.practicalMarks !== undefined ? g.practicalMarks : r.practicalMarks
    try {
      setSaving(true)
      const saved = await resultService.enterMarks(examId, [{
        studentId: r.studentId, subjectId: r.subjectId,
        internalMarks: internal === '' ? null : Number(internal),
        externalMarks: external === '' ? null : Number(external),
        practicalMarks: practical === '' ? null : Number(practical),
        credits: r.credits,
      }])
      setRows(rs => rs.map(x => keyOf(x) === keyOf(r) ? saved[0] : x))
      setGrid(g => { const n = { ...g }; delete n[keyOf(r)]; return n })
      toast.success('Marks saved')
    } catch (e) {
      toast.error(e.response?.data?.message || 'Save failed')
    } finally {
      setSaving(false)
    }
  }

  const publish = async () => {
    try {
      const res = await resultService.publishExam(examId)
      toast.success(res.message)
      setRows(await resultService.examResults(examId))
      setExams(es => es.map(e => String(e.id) === String(examId) ? { ...e, resultPublished: true } : e))
    } catch (e) {
      toast.error(e.response?.data?.message || 'Publish failed')
    }
  }

  const exam = exams.find(e => String(e.id) === String(examId))

  return (
    <div className="dashboard-page">
      <div className="page-head-row">
        <div>
          <h1 className="page-title">Results & Marks Entry</h1>
          <p className="muted">Edit internal/external/practical marks per row and save, then publish to lock the exam results.</p>
        </div>
      </div>

      <SectionCard title="Select exam">
        <div className="form-row">
          <label>Exam</label>
          <select value={examId} onChange={e => setExamId(e.target.value)}>
            <option value="">Select an exam…</option>
            {exams.map(e => (
              <option key={e.id} value={e.id}>
                {e.examName} ({e.examType}){e.resultPublished ? ' — published' : ''}
              </option>
            ))}
          </select>
        </div>
      </SectionCard>

      {examId && exam && (
        <SectionCard
          title={`${exam.examName} — entered results`}
          actions={
            exam.resultPublished
              ? <StatusBadge value="PUBLISHED" />
              : <button type="button" className="btn btn-primary" onClick={publish} disabled={rows.length === 0}>
                  Publish & lock ({rows.length})
                </button>
          }
        >
          {loading ? <Loading /> : rows.length === 0
            ? <Empty message="No marks entered for this exam yet." />
            : (
              <div className="table-wrap">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Student</th><th>Subject</th>
                      <th>Internal</th><th>External</th><th>Practical</th>
                      <th>Total</th><th>Grade</th><th>GP</th><th>Cr</th><th>Status</th><th></th>
                    </tr>
                  </thead>
                  <tbody>
                    {rows.map(r => (
                      <tr key={keyOf(r)}>
                        <td>{r.studentName} <span className="muted small">({r.studentCode})</span></td>
                        <td>{r.subjectCode}</td>
                        <td><input className="cell-input" type="number" min="0" max="50" disabled={r.published}
                          value={valueOf(r, 'internalMarks')} onChange={e => setCell(r, 'internalMarks', e.target.value)} /></td>
                        <td><input className="cell-input" type="number" min="0" max="100" disabled={r.published}
                          value={valueOf(r, 'externalMarks')} onChange={e => setCell(r, 'externalMarks', e.target.value)} /></td>
                        <td><input className="cell-input" type="number" min="0" max="50" disabled={r.published}
                          value={valueOf(r, 'practicalMarks')} onChange={e => setCell(r, 'practicalMarks', e.target.value)} /></td>
                        <td><strong>{r.totalMarks}</strong>/{r.maxMarks}</td>
                        <td>{r.grade}</td>
                        <td>{r.gradePoint}</td>
                        <td>{r.credits}</td>
                        <td>{r.published ? <StatusBadge value="PUBLISHED" /> : <StatusBadge value="DRAFT" />}</td>
                        <td>{!r.published && dirty(r) && (
                          <button type="button" className="btn btn-sm btn-primary" onClick={() => saveRow(r)} disabled={saving}>Save</button>
                        )}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
        </SectionCard>
      )}
      {!examId && !loading && <Empty message="Pick an exam to view and edit its results." />}
    </div>
  )
}
