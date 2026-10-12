import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api } from '../api/client'
import AppNav from '../components/AppNav'
import Message from '../components/Message'
import { Loading, LoadError } from '../components/PageState'
import { useApi } from '../hooks/useApi'
import { useTitle } from '../hooks/useTitle'
import { coinName, fullDateTime } from '../utils'

const STATUS_LABELS = { SUCCESS: 'Completed', FAILED: 'Didn’t go through', PENDING: 'Pending' }

export default function AssetDetail() {
  const { assetId } = useParams()
  const { data, error: loadError, loading, reload } = useApi(`/api/assets/${assetId}`)

  const [depositAmount, setDepositAmount] = useState('')
  const [withdrawAmount, setWithdrawAmount] = useState('')
  const [success, setSuccess] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const code = data?.asset.coinType
  useTitle(code ? `${code} - Crypton` : 'Coin - Crypton')

  const submit = async (kind, amount, clear) => {
    setError('')
    setSuccess('')
    setBusy(true)
    try {
      await api(`/api/assets/${assetId}/${kind}`, { method: 'POST', body: { amount: amount.trim() } })
      setSuccess(kind === 'deposit'
        ? 'Deposit complete. Your balance has been updated.'
        : 'Withdrawal complete. Your balance has been updated.')
      clear('')
    } catch (err) {
      setError(err.message)
    } finally {
      // Reload either way: a failed withdrawal is still recorded in the history.
      await reload()
      setBusy(false)
    }
  }

  return (
    <>
      <AppNav />
      {loading && !data && <Loading text="Opening this coin…" />}
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
                <Link to={`/vaults/${data.vault.id}`} style={{ border: 'none', color: 'inherit' }}>
                  ← {data.vault.name}
                </Link>
              </span>
              <span
                className="name"
                style={{ display: 'block', letterSpacing: '.16em', textTransform: 'uppercase', fontSize: '.75rem', marginBottom: '1rem' }}
              >
                {coinName(code)} balance
              </span>
              <span className="figure">
                <span>{data.asset.balance}</span> <small>{code}</small>
              </span>
              <p style={{ marginTop: '1.5rem' }}>
                <small>Wallet address</small><br /><code>{data.asset.walletAddress}</code>
              </p>
            </div>
            <div className="panel">
              <form onSubmit={(e) => { e.preventDefault(); submit('deposit', depositAmount, setDepositAmount) }}>
                <label htmlFor="dep">Add funds</label>
                <input
                  type="text"
                  id="dep"
                  placeholder="Amount, e.g. 0.5"
                  inputMode="decimal"
                  value={depositAmount}
                  onChange={(e) => setDepositAmount(e.target.value)}
                  required
                />
                <button type="submit" disabled={busy}>Deposit</button>
              </form>
              <form
                onSubmit={(e) => { e.preventDefault(); submit('withdraw', withdrawAmount, setWithdrawAmount) }}
                style={{ marginTop: '2rem' }}
              >
                <label htmlFor="wd">Move funds out</label>
                <input
                  type="text"
                  id="wd"
                  placeholder="Amount, e.g. 0.1"
                  inputMode="decimal"
                  value={withdrawAmount}
                  onChange={(e) => setWithdrawAmount(e.target.value)}
                  required
                />
                <button type="submit" disabled={busy}>Withdraw</button>
              </form>
            </div>
          </section>

          <Message>{success}</Message>
          <Message type="error">{error}</Message>

          <h2>History</h2>
          {data.transactions.length === 0 ? (
            <p className="lead">No activity yet. Your deposits and withdrawals will appear here.</p>
          ) : (
            <table>
              <thead>
                <tr><th>Date</th><th>Activity</th><th>Amount</th><th>Status</th></tr>
              </thead>
              <tbody>
                {data.transactions.map((txn) => (
                  <tr key={txn.id}>
                    <td>{fullDateTime(txn.timestamp)}</td>
                    <td>{txn.type === 'DEPOSIT' ? 'Deposit' : 'Withdrawal'}</td>
                    <td>{txn.type === 'DEPOSIT' ? '+ ' : '− '}{txn.amount}</td>
                    <td>{STATUS_LABELS[txn.status] || txn.status}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}
    </>
  )
}
