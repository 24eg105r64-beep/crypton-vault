import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

/** Top navigation for signed-in pages (same as the original pages). */
export default function AppNav() {
  const { logout } = useAuth()
  const navigate = useNavigate()

  const handleSignOut = async (event) => {
    event.preventDefault()
    await logout()
    navigate('/login?logout', { replace: true })
  }

  return (
    <nav>
      <Link className="brand" to="/dashboard">Crypton</Link>
      <Link to="/dashboard">Dashboard</Link>
      <Link to="/vaults">My vaults</Link>
      <form onSubmit={handleSignOut}>
        <button type="submit">Sign out</button>
      </form>
    </nav>
  )
}
