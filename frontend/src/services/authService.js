import api from './api'

export async function login(username, password) {
  const { data } = await api.post('/auth/login', { username, password })
  localStorage.setItem('sms_token', data.token)
  localStorage.setItem('sms_user', JSON.stringify(data.user))
  return data
}

export async function fetchMe() {
  const { data } = await api.get('/auth/me')
  return data.user
}

export function logout() {
  localStorage.removeItem('sms_token')
  localStorage.removeItem('sms_user')
}

export function getStoredUser() {
  const raw = localStorage.getItem('sms_user')
  return raw ? JSON.parse(raw) : null
}

export function getToken() {
  return localStorage.getItem('sms_token')
}

export function homeForRole(role) {
  switch (role) {
    case 'SUPER_ADMIN':
    case 'ADMIN':
      return '/admin/dashboard'
    case 'FACULTY':
      return '/faculty/dashboard'
    case 'STUDENT':
      return '/student/dashboard'
    case 'PARENT':
      return '/parent/dashboard'
    default:
      return '/login'
  }
}
