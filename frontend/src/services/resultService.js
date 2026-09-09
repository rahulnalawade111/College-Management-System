import api from './api'

const listExams = () => api.get('/exams').then(r => r.data)
const examResults = (examId, studentId) =>
  api.get(`/results/exam/${examId}`, { params: studentId ? { studentId } : {} }).then(r => r.data)
const enterMarks = (examId, entries) => api.post('/results', entries, { params: { examId } }).then(r => r.data)
const publishExam = (examId) => api.post(`/results/${examId}/publish`).then(r => r.data)
const unpublishExam = (examId) => api.post(`/results/${examId}/unpublish`).then(r => r.data)
const myResults = (examId) => api.get('/results/me', { params: examId ? { examId } : {} }).then(r => r.data)
const myMarksheet = (examId) => api.get('/results/me/marksheet', { params: { examId } }).then(r => r.data)
const studentMarksheet = (studentId, examId) =>
  api.get(`/results/student/${studentId}/marksheet`, { params: { examId } }).then(r => r.data)

export default { listExams, examResults, enterMarks, publishExam, unpublishExam, myResults, myMarksheet, studentMarksheet }
