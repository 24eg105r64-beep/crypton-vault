import { Link } from 'react-router-dom'
import { useTitle } from '../hooks/useTitle'

export default function NotFound() {
  useTitle('Not found - Crypton')
  return (
    <>
      <nav>
        <Link className="brand" to="/">Crypton</Link>
      </nav>
      <section className="hero">
        <span className="eyebrow">Nothing here</span>
        <h1>Lost<br />the key.</h1>
        <p className="lead">This page doesn’t exist. Let’s get you back somewhere familiar.</p>
        <p style={{ marginTop: '2rem' }}><Link className="btn" to="/">Go home</Link></p>
      </section>
    </>
  )
}
