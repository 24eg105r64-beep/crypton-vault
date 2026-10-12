import { useState } from 'react'
import { Link, useLocation, useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { useTitle } from '../hooks/useTitle'
import Message from '../components/Message'

export default function Login() {
  useTitle('Login - Crypton')
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [params] = useSearchParams()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      await login(email, password)
      navigate(location.state?.from || '/dashboard', { replace: true })
    } catch (err) {
      setError(err.message)
      setSubmitting(false)
    }
  }

  return (
    <>
      <nav>
        <Link className="brand" to="/">Crypton</Link>
        <Link className="btn" to="/register">Create a vault</Link>
      </nav>

      <section className="hero split">
        <div>
          <span className="eyebrow">Welcome back</span>
          <h1>Sign<br />in.</h1>
          <p className="lead">Your vault is exactly as you left it.</p>
        </div>
        <div className="panel">
          {params.has('logout') && <Message>You’ve been signed out. See you soon.</Message>}
          {params.has('registered') && <Message>Your vault is ready. Sign in to open it.</Message>}
          {location.state?.from && !error && <Message>Please sign in to continue.</Message>}
          <Message type="error">{error}</Message>

          <form onSubmit={handleSubmit}>
            <label htmlFor="username">Email</label>
            <input
              type="email"
              id="username"
              autoComplete="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              autoFocus
            />
            <label htmlFor="password" style={{ marginTop: '1rem' }}>Password</label>
            <input
              type="password"
              id="password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
            <button type="submit" style={{ marginTop: '1.5rem' }} disabled={submitting}>
              {submitting ? 'Signing in…' : 'Sign in'}
            </button>
          </form>
          <p style={{ marginTop: '2rem' }}>
            <small>New here? <Link to="/register">Create your vault</Link></small>
          </p>
        </div>
      </section>
    </>
  )
}
