import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { api } from '../api/client'
import AppNav from '../components/AppNav'
import Message from '../components/Message'
import { Loading, LoadError } from '../components/PageState'
import { useApi } from '../hooks/useApi'
import { useTitle } from '../hooks/useTitle'
import { monthYear } from '../utils'

export default function Vaults() {
  useTitle('My Vaults - Crypton')
  const navigate = useNavigate()
  const location = useLocation()
  const { data: vaults, error: loadError, loading, reload } = useApi('/api/vaults')
  const [name, setName] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const handleCreate = async (event) => {
    event.preventDefault()
    setError('')
    if (!name.trim()) {
      setError('Please give your vault a name.')
      return
    }
    setSubmitting(true)
    try {
      const vault = await api('/api/vaults', { method: 'POST', body: { name: name.trim() } })
      navigate(`/vaults/${vault.id}`, { state: { success: `Your vault "${vault.name}" is ready.` } })
    } catch (err) {
      setError(err.message)
      setSubmitting(false)
    }
  }

  const count = vaults ? vaults.length : 0

  return (
    <>
      <AppNav />

      <section className="hero split">
        <div>
          <span className="eyebrow">Your collection</span>
          <h1>Vaults.</h1>
        </div>
        <div>
          <p className="lead">Group your assets the way you think about them: savings, long-term, everyday.</p>
          <form onSubmit={handleCreate} style={{ marginTop: '2rem' }}>
            <label htmlFor="vaultName">Name a new vault</label>
            <input
              type="text"
              id="vaultName"
              placeholder="e.g. Long-term savings"
              maxLength={100}
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
            <button type="submit" disabled={submitting}>{submitting ? 'Creating…' : 'Create'}</button>
          </form>
        </div>
      </section>

      {!error && <Message>{location.state?.success}</Message>}
      <Message type="error">{error}</Message>

      {loading && !vaults && <Loading text="Loading your vaults…" />}
      {loadError && <LoadError error={loadError} onRetry={reload} />}
      {vaults && (
        <>
          <h2>{count} {count === 1 ? 'vault' : 'vaults'}</h2>
          {count === 0 && <p className="lead">No vaults yet. Name your first one above. It only takes a moment.</p>}
          <ul className="vault-list">
            {vaults.map((vault) => (
              <li key={vault.id}>
                <Link to={`/vaults/${vault.id}`}>{vault.name}</Link>
                <small>Since {monthYear(vault.createdAt)}  →</small>
              </li>
            ))}
          </ul>
        </>
      )}
    </>
  )
}
