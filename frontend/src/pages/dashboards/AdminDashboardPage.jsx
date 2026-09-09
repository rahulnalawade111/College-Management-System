import { useEffect, useState } from 'react'
import dashboardService from '../../services/dashboardService'
import { StatCard, ProgressBar, SectionCard, RecentActivities } from '../../components/DashboardWidgets'
import { Loading, ErrorState, Empty } from '../../components/States'
import {
  ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip, CartesianGrid,
  LineChart, Line, PieChart, Pie, Cell, Legend,
} from 'recharts'

const PIE_COLORS = ['#123B6D', '#1E5AA8', '#F4B942', '#4CAF50', '#9C27B0', '#E91E63']

export default function AdminDashboardPage() {
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    dashboardService.admin().then(setData).catch(e => setError(e.response?.data?.message || 'Failed to load dashboard'))
  }, [])

  if (error) return <ErrorState message={error} />
  if (!data) return <Loading label="Loading dashboard…" />

  return (
    <div className="dashboard-page">
      <h1 className="page-title">Overview</h1>

      <div className="stat-grid">
        <StatCard icon="🎓" label="Total Students" value={data.totalStudents} tone="primary" />
        <StatCard icon="👨‍🏫" label="Faculty" value={data.totalFaculty} tone="info" />
        <StatCard icon="🏛️" label="Departments" value={data.totalDepartments} tone="accent" />
        <StatCard icon="📚" label="Courses" value={data.totalCourses} hint={`${data.activeCourses} active`} tone="secondary" />
        <StatCard icon="✨" label="New Admissions (month)" value={data.newAdmissionsThisMonth} tone="good" />
        <StatCard
          icon="🗓️"
          label="Today's Attendance"
          value={data.todayAttendancePercent != null ? `${data.todayAttendancePercent}%` : '—'}
          tone={data.todayAttendancePercent >= 75 ? 'good' : 'warn'}
        />
      </div>

      <div className="grid-2">
        <SectionCard title="Enrollment by Department">
          {data.enrollmentByDepartment?.length ? (
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={data.enrollmentByDepartment}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e3e8f0" />
                <XAxis dataKey="departmentName" tick={{ fontSize: 11 }} interval={0} angle={-12} textAnchor="end" height={55} />
                <YAxis allowDecimals={false} />
                <Tooltip />
                <Bar dataKey="maleStudents" name="Male" stackId="s" fill="#1E5AA8" radius={[0, 0, 0, 0]} />
                <Bar dataKey="femaleStudents" name="Female" stackId="s" fill="#F4B942" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          ) : <Empty message="No enrollment data yet" />}
        </SectionCard>

        <SectionCard title="Monthly Admissions (last 6 months)">
          <ResponsiveContainer width="100%" height={280}>
            <LineChart data={data.admissionsByMonth}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e3e8f0" />
              <XAxis dataKey="month" />
              <YAxis allowDecimals={false} />
              <Tooltip />
              <Line type="monotone" dataKey="admissions" name="Admissions" stroke="#123B6D" strokeWidth={2.5} dot={{ r: 4 }} />
            </LineChart>
          </ResponsiveContainer>
        </SectionCard>

        <SectionCard title="Attendance Trend (14 days)">
          {data.attendanceTrend?.length ? (
            <ResponsiveContainer width="100%" height={260}>
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
          ) : <Empty message="No attendance recorded yet" />}
        </SectionCard>

        <SectionCard title="Students Below 75% Attendance">
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
          ) : <Empty message="All students are above 75% 🎉" />}
        </SectionCard>
      </div>

      <SectionCard title="Recent Activities">
        <RecentActivities activities={data.recentActivities} />
      </SectionCard>
    </div>
  )
}
