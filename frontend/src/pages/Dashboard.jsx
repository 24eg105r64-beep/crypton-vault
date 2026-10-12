import { Link } from 'react-router-dom'
import AppNav from '../components/AppNav'
import CoinBadge from '../components/CoinBadge'
import { Loading, LoadError } from '../components/PageState'
import { useApi } from '../hooks/useApi'
import { useTitle } from '../hooks/useTitle'
import { activityLabel, dayTime, monthYear } from '../utils'

export default function Dashboard() {
  useTitle('Dashboard - Crypton')
  const { data, error, loading, reload } = useApi('/api/dashboard')

  return (
    <>
      <AppNav />
      {loading && !data && <Loading />}
      {error && <LoadError error={error} onRetry={reload} />}
      {data && (
        <>
          <section className="hero split">
            <div>
              <span className="eyebrow">Welcome, {data.user.name}</span>
              <h1>Your digital<br />vault.</h1>
            </div>
            <div>
              <p className="lead">Everything you hold, kept quietly in one place and visible only to you.</p>
              <p style={{ marginTop: '2rem' }}><Link className="btn" to="/vaults">Open my vaults</Link></p>
            </div>
          </section>

          <h2>Holdings</h2>
          {Object.keys(data.totals).length === 0 ? (
            <p>Nothing here yet. <Link to="/vaults">Create a vault</Link> and add your first coin.</p>
          ) : (
            <div className="coins">
              {Object.entries(data.totals).map(([code, amount]) => (
                <div className="coin" key={code}>
                  <CoinBadge code={code} />
                  <span className="name">{code}</span>
                  <span className="amount">{amount}</span>
                </div>
              ))}
            </div>
          )}

          <h2>Vaults</h2>
          <section className="split">
            <div>
              <span className="figure">{data.vaults.length}</span>
              <span className="eyebrow">{data.vaults.length === 1 ? 'Vault secured' : 'Vaults secured'}</span>
            </div>
            <div>
              <ul className="vault-list">
                {data.vaults.map((vault) => (
                  <li key={vault.id}>
                    <Link to={`/vaults/${vault.id}`}>{vault.name}</Link>
                    <small>{monthYear(vault.createdAt)}</small>
                  </li>
                ))}
              </ul>
              <p style={{ marginTop: '1.5rem' }}><Link className="btn-ghost" to="/vaults">Create a new vault</Link></p>
            </div>
          </section>

          <h2>Recent activity</h2>
          {data.recentActivity.length === 0 && <p>No activity yet.</p>}
          <ul>
            {data.recentActivity.map((log, index) => (
              <li key={`${log.timestamp}-${index}`}>
                <span>{activityLabel(log.action)}</span>
                <small style={{ marginLeft: 'auto' }}>{dayTime(log.timestamp)}</small>
              </li>
            ))}
          </ul>
        </>
      )}
    </>
  )
}
