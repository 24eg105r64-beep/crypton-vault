import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { api, getToken, setToken } from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [checking, setChecking] = useState(Boolean(getToken()))

  // If a token is already saved in this tab, find out who it belongs to.
  useEffect(() => {
    if (!getToken()) return
    api('/api/auth/me')
      .then(setUser)
      .catch(() => {
        setToken(null)
        setUser(null)
      })
      .finally(() => setChecking(false))
  }, [])

  // The API client fires this when the backend says the token is no longer valid.
  useEffect(() => {
    const onExpired = () => setUser(null)
    window.addEventListener('crypton:session-expired', onExpired)
    return () => window.removeEventListener('crypton:session-expired', onExpired)
  }, [])

  const login = useCallback(async (email, password) => {
    const result = await api('/api/auth/login', { method: 'POST', body: { email, password } })
    setToken(result.token)
    setUser(result.user)
    return result.user
  }, [])

  const logout = useCallback(async () => {
    try {
      await api('/api/auth/logout', { method: 'POST' })
    } catch {
      // Signing out locally still works even if the server can't be reached.
    }
    setToken(null)
    setUser(null)
  }, [])

  const value = useMemo(() => ({ user, checking, login, logout }), [user, checking, login, logout])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used inside <AuthProvider>')
  return context
}
