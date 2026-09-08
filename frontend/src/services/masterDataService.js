import api from './api'

// ---- Departments ----
export const departmentService = {
  list: () => api.get('/departments').then((r) => r.data),
  create: (data) => api.post('/departments', data).then((r) => r.data),
  update: (id, data) => api.put(`/departments/${id}`, data).then((r) => r.data),
  remove: (id) => api.delete(`/departments/${id}`),
}

// ---- Academic years ----
export const academicYearService = {
  list: () => api.get('/academic-years').then((r) => r.data),
  active: () => api.get('/academic-years/active').then((r) => r.data),
  create: (data) => api.post('/academic-years', data).then((r) => r.data),
  update: (id, data) => api.put(`/academic-years/${id}`, data).then((r) => r.data),
  activate: (id) => api.patch(`/academic-years/${id}/activate`).then((r) => r.data),
  remove: (id) => api.delete(`/academic-years/${id}`),
}

// ---- Semesters ----
export const semesterService = {
  list: (academicYearId) =>
    api.get('/semesters', { params: academicYearId ? { academicYearId } : {} }).then((r) => r.data),
  create: (data) => api.post('/semesters', data).then((r) => r.data),
  update: (id, data) => api.put(`/semesters/${id}`, data).then((r) => r.data),
  remove: (id) => api.delete(`/semesters/${id}`),
}

// ---- Courses ----
export const courseService = {
  list: (params) => api.get('/courses', { params }).then((r) => r.data),
  get: (id) => api.get(`/courses/${id}`).then((r) => r.data),
  create: (data) => api.post('/courses', data).then((r) => r.data),
  update: (id, data) => api.put(`/courses/${id}`, data).then((r) => r.data),
  remove: (id) => api.delete(`/courses/${id}`),
}

// ---- Subjects ----
export const subjectService = {
  list: (params) => api.get('/subjects', { params }).then((r) => r.data),
  create: (data) => api.post('/subjects', data).then((r) => r.data),
  update: (id, data) => api.put(`/subjects/${id}`, data).then((r) => r.data),
  remove: (id) => api.delete(`/subjects/${id}`),
}
