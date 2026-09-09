import { useEffect, useState } from 'react'
import { examService } from '../../services/examService'
import resultService from '../../services/resultService'
import { StatCard, SectionCard, ProgressBar } from '../../components/DashboardWidgets'
import { Loading, Empty } from '../../components/States'
import StatusBadge from '../../components/StatusBadge'
import { useToast } from '../../context/ToastContext'

export default function StudentResultsPage() {
  const toast = useToast()
  const [exams, setExams] = useState([])
  const [examId, setExamId] = useState('')
  const [marksheet, setMarksheet] = useState(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    examService.all().then(setExams).catch(() => setExams([]))
  }, [])

  useEffect(() => {
    if (!examId) { setMarksheet(null); return }
    setLoading(true)
    resultService.myMarksheet(examId)
      .then(setMarksheet)
      .catch((e) => { setMarksheet(null); toast.error(e.response?.data?.message || 'No results published for this exam yet') })
      .finally(() => setLoading(false))
  }, [examId])

  return (
    <div className="dashboard-page">
      <h1 className="page-title">My Results</h1>
      <p className="muted">Your published marks, SGPA and CGPA per examination.</p>

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
        <>
          <div className="stat-grid">
            <StatCard icon="🎯" label="SGPA" value={marksheet.sgpa} />
            <StatCard icon="📈" label="CGPA" value={marksheet.cgpa} />
            <StatCard icon="📚" label="Subjects" value={marksheet.subjects.length} />
            <StatCard icon="🧾" label="Exam" value={marksheet.examName} />
          </div>

          <SectionCard title={`Marksheet — ${marksheet.studentName} (${marksheet.studentCode})${marksheet.courseName ? ' · ' + marksheet.courseName : ''}`}>
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
                      <td>
                        {s.grade}{' '}
                        <StatusBadge value={s.grade === 'F' ? 'FAILED' : 'PUBLISHED'} />
                      </td>
                      <td>{s.gradePoint}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </SectionCard>
        </>
      )}
      {!loading && !marksheet && examId && <Empty message="No published results for this exam yet." />}
      {!examId && <Empty message="Pick an exam to see your marksheet." />}
    </div>
  )
}
