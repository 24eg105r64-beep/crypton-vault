import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import { api } from '../api/client'
import AppNav from '../components/AppNav'
import CoinBadge from '../components/CoinBadge'
import Message from '../components/Message'
import { Loading, LoadError } from '../components/PageState'
import { useApi } from '../hooks/useApi'
import { useTitle } from '../hooks/useTitle'

export default function VaultDetail() {
  const { vaultId } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const { data, error: loadError, loading, reload } = useApi(`/api/vaults/${vaultId}`)

  const [coinType, setCoinType] = useState('BTC')
  const [newName, setNewName] = useState('')
  const [success, setSuccess] = useState(location.state?.success || '')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  useTitle(data ? `${data.vault.name} - Crypton` : 'Vault - Crypton')

  useEffect(() => {
    if (data) setNewName(data.vault.name)
  }, [data])

  const run = async (action, successMessage) => {
    setError('')
    setSuccess('')
    setBusy(true)
    try {
      await action()
      setSuccess(successMessage)
      await reload()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  const handleAddCoin = (event) => {
    event.preventDefault()
    run(
      () => api(`/api/vaults/${vaultId}/assets`, { method: 'POST', body: { coinType } }),
      `${coinType} added to your vault.`,
    )
  }

  const handleRename = (event) => {
    event.preventDefault()
    if (!newName.trim()) {
      setError('Please give your vault a name.')
      return
    }
    run(() => api(`/api/vaults/${vaultId}`, { method: 'PUT', body: { name: newName.trim() } }), 'Vault renamed.')
  }

  const handleDelete = async (event) => {
    event.preventDefault()
    if (!window.confirm('Delete this vault and every coin and record inside it? This cannot be undone.')) return
    setError('')
    setBusy(true)
    try {
      await api(`/api/vaults/${vaultId}`, { method: 'DELETE' })
      navigate('/vaults', { replace: true, state: { success: 'Vault deleted.' } })
    } catch (err) {
      setError(err.message)
      setBusy(false)
    }
  }

  return (
    <>
      <AppNav />
      {loading && !data && <Loading text="Opening this vault…" />}
      {loadError && !data && (
        <>
          <LoadError error={loadError} onRetry={reload} />
          <p><Link to="/vaults">← All vaults</Link></p>
        </>
      )}
      {data && (
        <>
          <section className="hero split">
            <div>
              <span className="eyebrow">
                <Link to="/vaults" style={{ border: 'none', color: 'inherit' }}>← All vaults</Link>
              </span>
              <h1>{data.vault.name}.</h1>
            </div>
            <div>
              <p className="lead">Add a coin to this vault. Each one gets its own placeholder wallet address.</p>
              <form onSubmit={handleAddCoin} style={{ marginTop: '2rem' }}>
                <label htmlFor="coinType">Choose a coin</label>
                <select id="coinType" value={coinType} onChange={(e) => setCoinType(e.target.value)}>
                  {data.coinTypes.map((coin) => (
                    <option key={coin} value={coin}>{coin}</option>
                  ))}
                </select>
                <button type="submit" disabled={busy}>Add coin</button>
              </form>
            </div>
          </section>

          <Message>{success}</Message>
          <Message type="error">{error}</Message>

          <h2>Assets</h2>
          {data.assets.length === 0 && <p className="lead">This vault is empty for now. Add your first coin above.</p>}
          {data.assets.map((asset) => (
            <div className="coin" key={asset.id}>
              <CoinBadge code={asset.coinType} />
              <div>
                <span className="name">{asset.coinType}</span><br />
                <Link to={`/assets/${asset.id}`} style={{ fontSize: '.875rem' }}>Deposit or withdraw →</Link>
              </div>
              <span className="amount">{asset.balance}</span>
            </div>
          ))}

          <h2>Vault settings</h2>
          <form onSubmit={handleRename}>
            <label htmlFor="newName">Rename</label>
            <input
              type="text"
              id="newName"
              maxLength={100}
              value={newName}
              onChange={(e) => setNewName(e.target.value)}
              required
            />
            <button type="submit" disabled={busy}>Save</button>
          </form>
          <form className="quiet-danger" onSubmit={handleDelete} style={{ marginTop: '2rem' }}>
            <button type="submit" disabled={busy}>Delete this vault</button>
          </form>
        </>
      )}
    </>
  )
}
