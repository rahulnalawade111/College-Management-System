import { useEffect, useState } from 'react'
import dashboardService from '../../services/dashboardService'
import { StatCard, ProgressBar, SectionCard, RecentActivities } from '../../components/DashboardWidgets'
import { Loading, ErrorState, Empty } from '../../components/States'
import {
  ResponsiveContainer, LineChart, Line, XAxis, YAxis, Tooltip, CartesianGrid, Legend,
} from 'recharts'

export default function FacultyDashboardPage() {
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    dashboardService.faculty().then(setData).catch(e => setError(e.response?.data?.message || 'Failed to load dashboard'))
  }, [])

  if (error) return <ErrorState message={error} />
  if (!data) return <Loading label="Loading dashboard…" />

  return (
    <div className="dashboard-page">
      <h1 className="page-title">Welcome, {data.name}</h1>
      <p className="muted">{data.designation} · {data.departmentName}</p>

      <div className="stat-grid">
        <StatCard icon="📚" label="Subjects Taught" value={data.totalSubjects} tone="primary" />
        <StatCard icon="👥" label="Students" value={data.totalStudents} tone="info" />
        <StatCard
          icon="🗓️"
          label="Overall Attendance"
          value={`${data.overallAttendancePercent}%`}
          tone={data.overallAttendancePercent >= 75 ? 'good' : 'warn'}
        />
        <StatCard icon="📝" label="Pending Grading" value={data.pendingGrading} tone="accent" />
      </div>

      <div className="grid-2">
        <SectionCard title="My Subjects">
          {data.subjectLoad?.length ? (
            <table className="table">
              <thead>
                <tr><th>Subject</th><th>Course</th><th>Students</th><th>Attendance</th></tr>
              </thead>
              <tbody>
                {data.subjectLoad.map(s => (
                  <tr key={s.subjectId}>
                    <td>
                      <div><strong>{s.subjectName}</strong></div>
                      <div className="muted small">{s.subjectCode}</div>
                    </td>
                    <td className="small">{s.courseName}</td>
                    <td>{s.students}</td>
                    <td style={{ minWidth: 150 }}><ProgressBar percent={s.attendancePercent} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : <Empty message="No subjects assigned yet" />}
        </SectionCard>

        <SectionCard title="Attendance Trend (14 days)">
          {data.attendanceTrend?.length ? (
            <ResponsiveContainer width="100%" height={280}>
              <LineChart data={data.attendanceTrend}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e3e8f0" />
                <XAxis dataKey="date" tick={{ fontSize: 10 }} />
                <YAxis domain={[0, 100]} />
                <Tooltip formatter={(v, n) => (n === 'Attendance %' ? `${v}%` : v)} />
                <Legend />
                <Line type="monotone" dataKey="presentPercent" name="Attendance %" stroke="#4CAF50" strokeWidth={2.5} dot={false} />
                <Line type="monotone" dataKey="sessions" name="Sessions" stroke="#F4B942" strokeWidth={1.5} dot={false} />
              </LineChart>
            </ResponsiveContainer>
          ) : <Empty message="No attendance marked in the last 14 days" />}
        </SectionCard>
      </div>

      <SectionCard title="Students Below 75% in My Subjects">
        {data.lowAttendanceStudents?.length ? (
          <table className="table">
            <thead>
              <tr><th>Student</th><th>Course</th><th>Attendance</th></tr>
            </thead>
            <tbody>
              {data.lowAttendanceStudents.map(s => (
                <tr key={s.studentId}>
                  <td>
                    <div><strong>{s.name}</strong></div>
                    <div className="muted small">{s.studentCode}</div>
                  </td>
                  <td className="small">{s.courseName}</td>
                  <td style={{ minWidth: 160 }}><ProgressBar percent={s.attendancePercent} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : <Empty message="All students above 75% 🎉" />}
      </SectionCard>
    </div>
  )
}
