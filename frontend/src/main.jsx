import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import ProtectedRoute from './routes/ProtectedRoute'
import DashboardLayout from './layouts/DashboardLayout'
import HomePage from './pages/public/HomePage'
import LoginPage from './pages/auth/LoginPage'
import ForgotPasswordPage from './pages/auth/ForgotPasswordPage'
import ResetPasswordPage from './pages/auth/ResetPasswordPage'
import ForbiddenPage from './pages/ForbiddenPage'
import DashboardStub from './pages/dashboards/DashboardStub'
import DepartmentsPage from './pages/admin/DepartmentsPage'
import AcademicYearsPage from './pages/admin/AcademicYearsPage'
import CoursesPage from './pages/admin/CoursesPage'
import SubjectsPage from './pages/admin/SubjectsPage'
import FacultyPage from './pages/admin/FacultyPage'
import StudentsPage from './pages/admin/StudentsPage'
import MarkAttendancePage from './pages/faculty/MarkAttendancePage'
import AttendanceViewPage from './pages/shared/AttendanceViewPage'
import ParentAttendancePage from './pages/shared/ParentAttendancePage'
import { ToastProvider } from './context/ToastContext'
import './styles.css'

const ADMIN_NAV = [
  { to: '/admin/dashboard', label: 'Dashboard', icon: '📊' },
  { to: '/admin/students', label: 'Students', icon: '🎓' },
  { to: '/admin/faculty', label: 'Faculty', icon: '👩‍🏫' },
  { to: '/admin/departments', label: 'Departments', icon: '🏛️' },
  { to: '/admin/courses', label: 'Courses', icon: '📚' },
  { to: '/admin/subjects', label: 'Subjects', icon: '📖' },
  { to: '/admin/academic-years', label: 'Academic Years', icon: '📅' },
  { to: '/admin/settings', label: 'Settings', icon: '⚙️' },
]
const FACULTY_NAV = [
  { to: '/faculty/dashboard', label: 'Dashboard', icon: '📊' },
  { to: '/faculty/students', label: 'Students', icon: '🎓' },
  { to: '/faculty/attendance', label: 'Attendance', icon: '🗓️' },
  { to: '/faculty/results', label: 'Results', icon: '🏆' },
]
const STUDENT_NAV = [
  { to: '/student/dashboard', label: 'Dashboard', icon: '📊' },
  { to: '/student/profile', label: 'Profile', icon: '👤' },
  { to: '/student/attendance', label: 'Attendance', icon: '🗓️' },
  { to: '/student/results', label: 'Results', icon: '🏆' },
  { to: '/student/fees', label: 'Fees', icon: '💳' },
]
const PARENT_NAV = [
  { to: '/parent/dashboard', label: 'Dashboard', icon: '📊' },
  { to: '/parent/attendance', label: 'Attendance', icon: '🗓️' },
  { to: '/parent/results', label: 'Results', icon: '🏆' },
  { to: '/parent/fees', label: 'Fees', icon: '💳' },
]

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <AuthProvider>
      <ToastProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/reset-password" element={<ResetPasswordPage />} />
          <Route path="/forbidden" element={<ForbiddenPage />} />

          <Route path="/admin" element={<ProtectedRoute roles={['SUPER_ADMIN', 'ADMIN']}><DashboardLayout nav={ADMIN_NAV} /></ProtectedRoute>}>
              <Route path="dashboard" element={<DashboardStub area="admin" />} />
              <Route path="students" element={<StudentsPage />} />
              <Route path="faculty" element={<FacultyPage />} />
            <Route path="departments" element={<DepartmentsPage />} />
            <Route path="courses" element={<CoursesPage />} />
            <Route path="subjects" element={<SubjectsPage />} />
            <Route path="academic-years" element={<AcademicYearsPage />} />
            <Route path="settings" element={<DashboardStub area="admin settings" />} />
          </Route>

            <Route path="/faculty" element={<ProtectedRoute roles={['FACULTY']}><DashboardLayout nav={FACULTY_NAV} /></ProtectedRoute>}>
              <Route path="dashboard" element={<DashboardStub area="faculty" />} />
              <Route path="students" element={<DashboardStub area="faculty students" />} />
              <Route path="attendance" element={<MarkAttendancePage />} />
              <Route path="results" element={<DashboardStub area="faculty results" />} />
            </Route>

          <Route path="/student" element={<ProtectedRoute roles={['STUDENT']}><DashboardLayout nav={STUDENT_NAV} /></ProtectedRoute>}>
              <Route path="dashboard" element={<DashboardStub area="student" />} />
              <Route path="profile" element={<DashboardStub area="student profile" />} />
              <Route path="attendance" element={<AttendanceViewPage />} />
            <Route path="results" element={<DashboardStub area="student results" />} />
            <Route path="fees" element={<DashboardStub area="student fees" />} />
          </Route>

            <Route path="/parent" element={<ProtectedRoute roles={['PARENT']}><DashboardLayout nav={PARENT_NAV} /></ProtectedRoute>}>
              <Route path="dashboard" element={<DashboardStub area="parent" />} />
              <Route path="attendance" element={<ParentAttendancePage />} />
            <Route path="results" element={<DashboardStub area="parent results" />} />
            <Route path="fees" element={<DashboardStub area="parent fees" />} />
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
      </ToastProvider>
    </AuthProvider>
  </StrictMode>,
)
