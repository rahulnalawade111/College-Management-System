import api from './api'

// ---- reference data ----
export const listDepartments = () => api.get('/departments')
export const listAcademicYears = () => api.get('/academic-years')
export const listSemesters = (academicYearId) =>
  api.get('/semesters', { params: academicYearId ? { academicYearId } : {} })
export const listCourses = (params) => api.get('/courses', { params })
export const listSubjects = (params) => api.get('/subjects', { params })

// ---- faculty ----
export const listFaculty = (params) => api.get('/faculty', { params })
export const getFaculty = (id) => api.get(`/faculty/${id}`)
export const createFaculty = (data) => api.post('/faculty', data)
export const updateFaculty = (id, data) => api.put(`/faculty/${id}`, data)
export const deleteFaculty = (id) => api.delete(`/faculty/${id}`)

// ---- students ----
export const listStudents = (params) => api.get('/students', { params })
export const getStudent = (id) => api.get(`/students/${id}`)
export const createStudent = (data) => api.post('/students', data)
export const updateStudent = (id, data) => api.put(`/students/${id}`, data)
export const updateStudentStatus = (id, status) =>
  api.patch(`/students/${id}/status`, { status })
export const deleteStudent = (id) => api.delete(`/students/${id}`)
