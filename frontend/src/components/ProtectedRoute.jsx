import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { Loading } from './PageState'

/** Only signed-in users can see the pages inside this route. */
export default function ProtectedRoute() {
  const { user, checking } = useAuth()
  const location = useLocation()

  if (checking) return <Loading />
  if (!user) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }
  return <Outlet />
}
