import { useEffect, useState } from 'react'
import dashboardService from '../../services/dashboardService'
import { StatCard, ProgressBar, SectionCard, RecentActivities } from '../../components/DashboardWidgets'
import { Loading, ErrorState, Empty } from '../../components/States'

function ChildCard({ child }) {
  return (
    <div className="child-card">
      <div className="child-head">
        <div className="avatar">{child.name?.[0]?.toUpperCase()}</div>
        <div>
          <strong>{child.name}</strong>
          <div className="muted small">
            {child.studentCode} · {child.courseName}
            {child.currentSemesterNumber ? ` · Sem ${child.currentSemesterNumber}` : ''}
          </div>
        </div>
      </div>

      <div className="stat-grid stat-grid-2">
        <StatCard
          icon="🗓️"
          label="Overall Attendance"
          value={`${child.overallAttendancePercent}%`}
          tone={child.overallAttendancePercent >= 80 ? 'good' : child.overallAttendancePercent >= 60 ? 'warn' : 'bad'}
        />
        <StatCard
          icon="💰"
          label="Fees Paid"
          value={`${child.feesPaidPercent}%`}
          tone={child.feesPaidPercent >= 100 ? 'good' : 'warn'}
          hint={child.outstandingFees != null ? `Outstanding: ₹${child.outstandingFees}` : null}
        />
      </div>

      <h4 className="sub-heading">Attendance by Subject</h4>
      {child.subjectAttendance?.length ? (
        <table className="table">
          <thead>
            <tr><th>Subject</th><th>Total</th><th>Present</th><th>Absent</th><th>Attendance</th></tr>
          </thead>
          <tbody>
            {child.subjectAttendance.map(s => (
              <tr key={s.subjectId}>
                <td>
                  <div><strong>{s.subjectName}</strong></div>
                  <div className="muted small">{s.subjectCode}</div>
                </td>
                <td>{s.total}</td>
                <td className="text-good">{s.present + s.late}</td>
                <td className="text-bad">{s.absent}</td>
                <td style={{ minWidth: 150 }}><ProgressBar percent={s.percent} /></td>
              </tr>
            ))}
          </tbody>
        </table>
      ) : <Empty message="No attendance recorded yet" />}

      <h4 className="sub-heading">Recent Attendance</h4>
      <RecentActivities activities={child.recentActivities} />
    </div>
  )
}

export default function ParentDashboardPage() {
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    dashboardService.parent().then(setData).catch(e => setError(e.response?.data?.message || 'Failed to load dashboard'))
  }, [])

  if (error) return <ErrorState message={error} />
  if (!data) return <Loading label="Loading dashboard…" />

  return (
    <div className="dashboard-page">
      <h1 className="page-title">Welcome, {data.parentName}</h1>
      <p className="muted">Linked children: {data.children.length}</p>

      {data.children.length
        ? data.children.map(c => (
            <SectionCard key={c.studentId} className="mb">
              <ChildCard child={c} />
            </SectionCard>
          ))
        : <Empty message="No child linked to this account yet" />}
    </div>
  )
}
