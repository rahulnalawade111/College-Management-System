import api from './api'

export const dashboardService = {
  admin: () => api.get('/dashboard/admin').then(r => r.data),
  faculty: () => api.get('/dashboard/faculty').then(r => r.data),
  student: () => api.get('/dashboard/student').then(r => r.data),
  parent: () => api.get('/dashboard/parent').then(r => r.data),
}

export default dashboardService
