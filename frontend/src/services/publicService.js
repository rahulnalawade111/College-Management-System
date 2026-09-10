import api from './api'

// Public (unauthenticated) endpoints for the college website
export const publicService = {
  notices: () => api.get('/public/notices').then((r) => r.data),
  events: () => api.get('/public/events').then((r) => r.data),
  departments: () => api.get('/departments').then((r) => r.data),
  submitContact: (payload) => api.post('/contact', payload).then((r) => r.data),
}

export default publicService
