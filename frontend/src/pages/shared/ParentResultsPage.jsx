import { useEffect, useState } from 'react'
import api from '../../services/api'
import { examService } from '../../services/examService'
import resultService from '../../services/resultService'
import { SectionCard } from '../../components/DashboardWidgets'
import { Loading, Empty } from '../../components/States'
import StatusBadge from '../../components/StatusBadge'
import { pickErrorMessage } from '../../components/States'
import { useToast } from '../../context/ToastContext'

/** Parent's view of the linked child's published results. */
export default function ParentResultsPage() {
  const toast = useToast()
  const [child, setChild] = useState(null)
  const [childError, setChildError] = useState('')
  const [exams, setExams] = useState([])
  const [examId, setExamId] = useState('')
  const [marksheet, setMarksheet] = useState(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    api.get('/parent/me/child')
      .then(({ data }) => setChild(data))
      .catch((e) => setChildError(pickErrorMessage(e, 'Could not load child record')))
    examService.all().then(setExams).catch(() => setExams([]))
  }, [])

  useEffect(() => {
    if (!child || !examId) { setMarksheet(null); return }
    setLoading(true)
    resultService.studentMarksheet(child.id, examId)
      .then(setMarksheet)
      .catch((e) => { setMarksheet(null); toast.error(e.response?.data?.message || 'No results published for this exam yet') })
      .finally(() => setLoading(false))
  }, [child, examId])

  if (childError) return <div className="form-error">{childError}</div>
  if (!child) return <div className="state-box"><span className="spinner" /> Loading…</div>

  return (
    <div className="dashboard-page">
      <h1 className="page-title">Results — {child.fullName}</h1>
      <p className="muted">Your child's published marks, SGPA and CGPA per examination.</p>

      <SectionCard title="Select examination">
        <div className="form-row">
          <label>Exam</label>
          <select value={examId} onChange={e => setExamId(e.target.value)}>
            <option value="">Select an exam…</option>
            {exams.map(e => <option key={e.id} value={e.id}>{e.examName} ({e.examType})</option>)}
          </select>
        </div>
      </SectionCard>

      {loading && <Loading />}
      {!loading && marksheet && (
        <SectionCard title={`${marksheet.examName} — SGPA ${marksheet.sgpa} · CGPA ${marksheet.cgpa}`}>
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Code</th><th>Subject</th><th>Cr</th>
                  <th>Internal</th><th>External</th><th>Practical</th>
                  <th>Total</th><th>Grade</th><th>GP</th>
                </tr>
              </thead>
              <tbody>
                {marksheet.subjects.map(s => (
                  <tr key={s.subjectCode}>
                    <td>{s.subjectCode}</td>
                    <td>{s.subjectName}</td>
                    <td>{s.credits}</td>
                    <td>{s.internalMarks ?? '—'}</td>
                    <td>{s.externalMarks ?? '—'}</td>
                    <td>{s.practicalMarks ?? '—'}</td>
                    <td><strong>{s.totalMarks}</strong>/{s.maxMarks}</td>
                    <td>{s.grade} <StatusBadge value={s.grade === 'F' ? 'FAILED' : 'PUBLISHED'} /></td>
                    <td>{s.gradePoint}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </SectionCard>
      )}
      {!loading && !marksheet && examId && <Empty message="No published results for this exam yet." />}
      {!examId && <Empty message="Pick an exam to see the marksheet." />}
    </div>
  )
}
