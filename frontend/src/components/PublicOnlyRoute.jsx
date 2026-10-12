import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

/** Sign-in and register pages send already signed-in users to the dashboard. */
export default function PublicOnlyRoute() {
  const { user, checking } = useAuth()
  if (!checking && user) return <Navigate to="/dashboard" replace />
  return <Outlet />
}
