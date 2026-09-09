import { useEffect, useState } from 'react'
import examService from '../../services/examService'
import { StatCard, SectionCard } from '../../components/DashboardWidgets'
import { Loading, ErrorState, Empty } from '../../components/States'
import StatusBadge from '../../components/StatusBadge'

function fmtDate(d) {
  return d ? new Date(d).toLocaleDateString('en-GB', { weekday: 'short', day: 'numeric', month: 'short' }) : '—'
}
function fmtTime(t) {
  return t ? t.slice(0, 5) : '—'
}

export default function StudentExamsPage() {
  const [exams, setExams] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    examService.studentUpcoming().then(setExams)
      .catch(e => setError(e.response?.data?.message || 'Failed to load exam schedule'))
  }, [])

  if (error) return <ErrorState message={error} />
  if (!exams) return <Loading label="Loading exam schedule…" />

  const totalPapers = exams.reduce((n, e) => n + e.schedule.length, 0)
  const firstExam = exams[0]

  return (
    <div className="dashboard-page">
      <h1 className="page-title">Exam Schedule</h1>

      <div className="stat-grid">
        <StatCard icon="📅" label="Upcoming Exams" value={exams.length} tone="primary" />
        <StatCard icon="📝" label="My Papers" value={totalPapers} tone="info" />
        <StatCard icon="⏰" label="First Exam"
          value={firstExam ? fmtDate(firstExam.schedule[0]?.examDate || firstExam.startDate) : '—'} tone="warn" />
      </div>

      {exams.length ? exams.map(e => (
        <SectionCard key={e.id}
          title={`${e.examName} · ${e.examType}`}
          actions={<StatusBadge value={e.status} />}>
          <p className="muted small">
            {e.academicYearName}{e.semesterNumber ? ` · Semester ${e.semesterNumber}` : ''}
            {' · '}{fmtDate(e.startDate)} → {fmtDate(e.endDate)}
          </p>
          <table className="table">
            <thead>
              <tr><th>Date</th><th>Time</th><th>Subject</th><th>Room</th><th>Max marks</th></tr>
            </thead>
            <tbody>
              {e.schedule.map(s => (
                <tr key={s.id}>
                  <td><strong>{fmtDate(s.examDate)}</strong></td>
                  <td className="small">{fmtTime(s.startTime)} – {fmtTime(s.endTime)}</td>
                  <td><strong>{s.subjectCode}</strong> <span className="muted small">{s.subjectName}</span></td>
                  <td className="small">{s.room || 'TBA'}</td>
                  <td>{s.maxMarks}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </SectionCard>
      )) : <Empty message="No upcoming exams scheduled for your course yet" />}
    </div>
  )
}
