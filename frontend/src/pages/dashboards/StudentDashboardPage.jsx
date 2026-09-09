import { useEffect, useState } from 'react'
import dashboardService from '../../services/dashboardService'
import { StatCard, ProgressBar, SectionCard, RecentActivities } from '../../components/DashboardWidgets'
import { Loading, ErrorState, Empty } from '../../components/States'

export default function StudentDashboardPage() {
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    dashboardService.student().then(setData).catch(e => setError(e.response?.data?.message || 'Failed to load dashboard'))
  }, [])

  if (error) return <ErrorState message={error} />
  if (!data) return <Loading label="Loading dashboard…" />

  return (
    <div className="dashboard-page">
      <h1 className="page-title">Welcome, {data.name}</h1>
      <p className="muted">
        {data.courseName}{data.currentSemesterNumber ? ` · Semester ${data.currentSemesterNumber}` : ''}
        {data.academicYearName ? ` · AY ${data.academicYearName}` : ''}
      </p>

      <div className="stat-grid">
        <StatCard
          icon="🗓️"
          label="Overall Attendance"
          value={`${data.overallAttendancePercent}%`}
          tone={data.overallAttendancePercent >= 80 ? 'good' : data.overallAttendancePercent >= 60 ? 'warn' : 'bad'}
        />
        <StatCard icon="📝" label="Pending Assignments" value={data.pendingAssignments} tone="primary" />
        <StatCard icon="📖" label="Upcoming Exams" value={data.upcomingExams} tone="accent" />
        <StatCard
          icon="💰"
          label="Fees Paid"
          value={`${data.feesPaidPercent}%`}
          tone={data.feesPaidPercent >= 100 ? 'good' : 'warn'}
          hint={data.outstandingFees != null ? `Outstanding: ₹${data.outstandingFees}` : null}
        />
      </div>

      <SectionCard title="Attendance by Subject">
        {data.subjectAttendance?.length ? (
          <table className="table">
            <thead>
              <tr><th>Subject</th><th>Total</th><th>Present</th><th>Absent</th><th>Attendance</th></tr>
            </thead>
            <tbody>
              {data.subjectAttendance.map(s => (
                <tr key={s.subjectId}>
                  <td>
                    <div><strong>{s.subjectName}</strong></div>
                    <div className="muted small">{s.subjectCode}</div>
                  </td>
                  <td>{s.total}</td>
                  <td className="text-good">{s.present + s.late}</td>
                  <td className="text-bad">{s.absent}</td>
                  <td style={{ minWidth: 160 }}><ProgressBar percent={s.percent} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : <Empty message="No attendance recorded yet" />}
      </SectionCard>

      <SectionCard title="Recent Attendance">
        <RecentActivities activities={data.recentActivities} />
      </SectionCard>
    </div>
  )
}
