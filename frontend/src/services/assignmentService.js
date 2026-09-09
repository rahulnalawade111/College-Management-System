import api from './api'

export const assignmentService = {
  facultyMine: () => api.get('/assignments/faculty/mine').then(r => r.data),
  studentMine: () => api.get('/assignments/student/mine').then(r => r.data),
  bySubject: (subjectId) => api.get('/assignments', { params: { subjectId } }).then(r => r.data),
  create: (data) => api.post('/assignments', data).then(r => r.data),
  update: (id, data) => api.put(`/assignments/${id}`, data).then(r => r.data),
  setStatus: (id, status) => api.put(`/assignments/${id}/status`, null, { params: { status } }).then(r => r.data),
}

export const submissionService = {
  forAssignment: (assignmentId) => api.get(`/submissions/assignment/${assignmentId}`).then(r => r.data),
  studentMine: () => api.get('/submissions/student/mine').then(r => r.data),
  submit: (assignmentId, data) => api.post(`/submissions/assignment/${assignmentId}`, data).then(r => r.data),
  grade: (submissionId, data) => api.put(`/submissions/${submissionId}/grade`, data).then(r => r.data),
  forStudent: (studentId) => api.get(`/submissions/student/${studentId}`).then(r => r.data),
}

export default { assignmentService, submissionService }
