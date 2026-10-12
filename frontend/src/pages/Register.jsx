import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api } from '../api/client'
import { useTitle } from '../hooks/useTitle'
import Message from '../components/Message'

const EMPTY = { name: '', email: '', phone: '', password: '', confirmPassword: '' }

export default function Register() {
  useTitle('Register - Crypton')
  const navigate = useNavigate()
  const [form, setForm] = useState(EMPTY)
  const [fieldErrors, setFieldErrors] = useState({})
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const update = (field) => (event) => setForm({ ...form, [field]: event.target.value })

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError('')
    setFieldErrors({})

    if (form.password !== form.confirmPassword) {
      setFieldErrors({ confirmPassword: 'Passwords do not match' })
      return
    }

    setSubmitting(true)
    try {
      await api('/api/auth/register', { method: 'POST', body: form })
      navigate('/login?registered', { replace: true })
    } catch (err) {
      setFieldErrors(err.errors || {})
      if (!err.errors || Object.keys(err.errors).length === 0) setError(err.message)
      setSubmitting(false)
    }
  }

  const fieldError = (field) =>
    fieldErrors[field] ? <small className="field-error">{fieldErrors[field]}</small> : null

  return (
    <>
      <nav>
        <Link className="brand" to="/">Crypton</Link>
        <Link to="/login">Sign in</Link>
      </nav>

      <section className="hero split">
        <div>
          <span className="eyebrow">Create your vault</span>
          <h1>Begin.</h1>
          <p className="lead">It takes a minute. Your vaults stay private to your account.</p>
        </div>
        <div className="panel">
          <Message type="error">{error}</Message>
          <form onSubmit={handleSubmit} noValidate>
            <label htmlFor="name">Your name</label>
            <input type="text" id="name" autoComplete="name" value={form.name} onChange={update('name')} />
            {fieldError('name')}

            <label htmlFor="email" style={{ marginTop: '1rem' }}>Email</label>
            <input type="email" id="email" autoComplete="email" value={form.email} onChange={update('email')} />
            {fieldError('email')}

            <label htmlFor="phone" style={{ marginTop: '1rem' }}>Phone</label>
            <input
              type="text"
              id="phone"
              inputMode="numeric"
              autoComplete="tel"
              placeholder="10 digits"
              value={form.phone}
              onChange={update('phone')}
            />
            {fieldError('phone')}

            <label htmlFor="password" style={{ marginTop: '1rem' }}>Password</label>
            <input
              type="password"
              id="password"
              autoComplete="new-password"
              placeholder="At least 8 characters"
              value={form.password}
              onChange={update('password')}
            />
            {fieldError('password')}

            <label htmlFor="confirmPassword" style={{ marginTop: '1rem' }}>Confirm password</label>
            <input
              type="password"
              id="confirmPassword"
              autoComplete="new-password"
              value={form.confirmPassword}
              onChange={update('confirmPassword')}
            />
            {fieldError('confirmPassword')}

            <button type="submit" style={{ marginTop: '1.5rem' }} disabled={submitting}>
              {submitting ? 'Creating…' : 'Create my vault'}
            </button>
          </form>
          <p style={{ marginTop: '2rem' }}>
            <small>Already have a vault? <Link to="/login">Sign in</Link></small>
          </p>
        </div>
      </section>
    </>
  )
}
