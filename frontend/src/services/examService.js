import api from './api'

export const examService = {
  all: () => api.get('/exams').then(r => r.data),
  one: (id) => api.get(`/exams/${id}`).then(r => r.data),
  studentUpcoming: () => api.get('/exams/student/upcoming').then(r => r.data),
  create: (data) => api.post('/exams', data).then(r => r.data),
  update: (id, data) => api.put(`/exams/${id}`, data).then(r => r.data),
  setStatus: (id, status) => api.put(`/exams/${id}/status`, null, { params: { status } }).then(r => r.data),
  addSchedule: (id, data) => api.post(`/exams/${id}/schedules`, data).then(r => r.data),
  removeSchedule: (examId, scheduleId) => api.delete(`/exams/${examId}/schedules/${scheduleId}`).then(r => r.data),
}

export default examService
