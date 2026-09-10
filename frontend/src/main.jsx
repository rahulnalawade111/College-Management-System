import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import ProtectedRoute from './routes/ProtectedRoute'
import DashboardLayout from './layouts/DashboardLayout'
import HomePage from './pages/public/HomePage'
import AboutPage from './pages/public/AboutPage'
import AcademicsPage from './pages/public/AcademicsPage'
import PublicDepartmentsPage from './pages/public/PublicDepartmentsPage'
import PublicCoursesPage from './pages/public/CoursesPage'
import AdmissionsPage from './pages/public/AdmissionsPage'
import PublicFacultyPage from './pages/public/PublicFacultyPage'
import EventsPage from './pages/public/EventsPage'
import NoticesPage from './pages/public/NoticesPage'
import GalleryPage from './pages/public/GalleryPage'
import ContactPage from './pages/public/ContactPage'
import PublicLayout from './layouts/PublicLayout'
import LoginPage from './pages/auth/LoginPage'
import ForgotPasswordPage from './pages/auth/ForgotPasswordPage'
import ResetPasswordPage from './pages/auth/ResetPasswordPage'
import ForbiddenPage from './pages/ForbiddenPage'
import DashboardStub from './pages/dashboards/DashboardStub'
import AdminDashboardPage from './pages/dashboards/AdminDashboardPage'
import FacultyDashboardPage from './pages/dashboards/FacultyDashboardPage'
import StudentDashboardPage from './pages/dashboards/StudentDashboardPage'
import ParentDashboardPage from './pages/dashboards/ParentDashboardPage'
import FacultyAssignmentsPage from './pages/faculty/FacultyAssignmentsPage'
import StudentAssignmentsPage from './pages/student/StudentAssignmentsPage'
import ExamsAdminPage from './pages/admin/ExamsAdminPage'
import StudentExamsPage from './pages/student/StudentExamsPage'
import FacultyResultsPage from './pages/faculty/FacultyResultsPage'
import StudentResultsPage from './pages/student/StudentResultsPage'
import ParentResultsPage from './pages/shared/ParentResultsPage'
import FeesAdminPage from './pages/admin/FeesAdminPage'
import WebsiteContentPage from './pages/admin/WebsiteContentPage'
import FeeViewPage from './pages/shared/FeeViewPage'
import feeService from './services/feeService'
const feeServiceMine = () => feeService.mine()
const feeServiceMyChild = () => feeService.myChild()
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
  { to: '/admin/exams', label: 'Exams', icon: '📝' },
  { to: '/admin/fees', label: 'Fees', icon: '💳' },
  { to: '/admin/website', label: 'Website', icon: '🌐' },
  { to: '/admin/settings', label: 'Settings', icon: '⚙️' },
]
const FACULTY_NAV = [
  { to: '/faculty/dashboard', label: 'Dashboard', icon: '📊' },
  { to: '/faculty/students', label: 'Students', icon: '🎓' },
  { to: '/faculty/attendance', label: 'Attendance', icon: '🗓️' },
  { to: '/faculty/assignments', label: 'Assignments', icon: '📝' },
  { to: '/faculty/results', label: 'Results', icon: '🏆' },
]
const STUDENT_NAV = [
  { to: '/student/dashboard', label: 'Dashboard', icon: '📊' },
  { to: '/student/profile', label: 'Profile', icon: '👤' },
  { to: '/student/attendance', label: 'Attendance', icon: '🗓️' },
  { to: '/student/assignments', label: 'Assignments', icon: '📝' },
  { to: '/student/exams', label: 'Exams', icon: '📅' },
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
          <Route element={<PublicLayout />}>
            <Route path="/" element={<HomePage />} />
            <Route path="/about" element={<AboutPage />} />
            <Route path="/academics" element={<AcademicsPage />} />
            <Route path="/departments" element={<PublicDepartmentsPage />} />
            <Route path="/courses" element={<PublicCoursesPage />} />
            <Route path="/admissions" element={<AdmissionsPage />} />
            <Route path="/faculty" element={<PublicFacultyPage />} />
            <Route path="/events" element={<EventsPage />} />
            <Route path="/notices" element={<NoticesPage />} />
            <Route path="/gallery" element={<GalleryPage />} />
            <Route path="/contact" element={<ContactPage />} />
          </Route>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/reset-password" element={<ResetPasswordPage />} />
          <Route path="/forbidden" element={<ForbiddenPage />} />

          <Route path="/admin" element={<ProtectedRoute roles={['SUPER_ADMIN', 'ADMIN']}><DashboardLayout nav={ADMIN_NAV} /></ProtectedRoute>}>
              <Route path="dashboard" element={<AdminDashboardPage />} />
              <Route path="students" element={<StudentsPage />} />
              <Route path="faculty" element={<FacultyPage />} />
            <Route path="departments" element={<DepartmentsPage />} />
            <Route path="courses" element={<CoursesPage />} />
            <Route path="subjects" element={<SubjectsPage />} />
            <Route path="academic-years" element={<AcademicYearsPage />} />
            <Route path="exams" element={<ExamsAdminPage />} />
            <Route path="website" element={<WebsiteContentPage />} />
            <Route path="settings" element={<DashboardStub area="admin settings" />} />
          </Route>

            <Route path="/faculty" element={<ProtectedRoute roles={['FACULTY']}><DashboardLayout nav={FACULTY_NAV} /></ProtectedRoute>}>
              <Route path="dashboard" element={<FacultyDashboardPage />} />
              <Route path="students" element={<DashboardStub area="faculty students" />} />
              <Route path="attendance" element={<MarkAttendancePage />} />
              <Route path="assignments" element={<FacultyAssignmentsPage />} />
              <Route path="results" element={<DashboardStub area="faculty results" />} />
            </Route>

          <Route path="/student" element={<ProtectedRoute roles={['STUDENT']}><DashboardLayout nav={STUDENT_NAV} /></ProtectedRoute>}>
              <Route path="dashboard" element={<StudentDashboardPage />} />
              <Route path="profile" element={<DashboardStub area="student profile" />} />
              <Route path="attendance" element={<AttendanceViewPage />} />
              <Route path="assignments" element={<StudentAssignmentsPage />} />
              <Route path="exams" element={<StudentExamsPage />} />
            <Route path="results" element={<StudentResultsPage />} />
            <Route path="fees" element={<FeeViewPage title="My Fees" dataLoader={() => feeServiceMine()} />} />
          </Route>

            <Route path="/parent" element={<ProtectedRoute roles={['PARENT']}><DashboardLayout nav={PARENT_NAV} /></ProtectedRoute>}>
              <Route path="dashboard" element={<ParentDashboardPage />} />
              <Route path="attendance" element={<ParentAttendancePage />} />
            <Route path="results" element={<ParentResultsPage />} />
            <Route path="fees" element={<FeeViewPage title="Child's Fees" dataLoader={() => feeServiceMyChild()} />} />
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
      </ToastProvider>
    </AuthProvider>
  </StrictMode>,
)
