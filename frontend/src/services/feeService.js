import api from './api'

const list = (studentId, page = 0, size = 50) =>
  api.get('/fees', { params: { studentId, page, size } }).then(r => r.data)
const mine = () => api.get('/fees/me').then(r => r.data)
const myChild = () => api.get('/fees/my-child').then(r => r.data)
const summary = (studentId) => api.get('/fees/summary', { params: studentId ? { studentId } : {} }).then(r => r.data)
const create = (data) => api.post('/fees', data).then(r => r.data)
const update = (id, data) => api.put(`/fees/${id}`, data).then(r => r.data)
const remove = (id) => api.delete(`/fees/${id}`).then(r => r.data)
const pay = (id, data) => api.post(`/fees/${id}/payments`, data).then(r => r.data)

export default { list, mine, myChild, summary, create, update, remove, pay }
