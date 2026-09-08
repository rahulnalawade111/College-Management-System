import { createContext, useContext, useEffect, useState } from 'react'
import { getToken, getStoredUser, login as loginApi, logout as logoutApi } from '../services/authService'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(getStoredUser())
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(!getToken()) // no token → not logged in; token validity enforced by API 401s
    if (getToken() && !getStoredUser()) setUser(null)
    setLoading(false)
  }, [])

  async function login(username, password) {
    const data = await loginApi(username, password)
    setUser(data.user)
    return data.user
  }

  function logout() {
    logoutApi()
    setUser(null)
  }

  const value = { user, loading, login, logout }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider')
  return ctx
}
