import api from './api'

// ---- attendance ----
export const bulkMarkAttendance = (data) => api.post('/attendance', data)
export const getAttendanceForSubjectDate = (subjectId, date) =>
  api.get(`/attendance/subject/${subjectId}/date/${date}`)
export const getStudentAttendance = (studentId) => api.get(`/attendance/student/${studentId}`)
export const getStudentAttendanceSummary = (studentId) =>
  api.get(`/attendance/student/${studentId}/summary`)
export const getStudentAttendanceOverall = (studentId) =>
  api.get(`/attendance/student/${studentId}/overall`)
export const getMyAttendance = () => api.get('/student/attendance/me')
export const getMyAttendanceSummary = () => api.get('/student/attendance/my-summary')
