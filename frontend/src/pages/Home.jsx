import { useRef } from 'react'
import { Link } from 'react-router-dom'
import { useParallax } from '../hooks/useParallax'
import { useTitle } from '../hooks/useTitle'
import { useAuth } from '../auth/AuthContext'

export default function Home() {
  useTitle('Crypton | Your keys. Your rules.')
  const pageRef = useRef(null)
  useParallax(pageRef)
  const { user } = useAuth()

  return (
    <div ref={pageRef}>
      <nav>
        <Link className="brand" to="/">Crypton</Link>
        {user ? (
          <Link className="btn" to="/dashboard">Open dashboard</Link>
        ) : (
          <>
            <Link to="/login">Sign in</Link>
            <Link className="btn" to="/register">Get started</Link>
          </>
        )}
      </nav>

      <section className="collage">
        <figure className="pic" data-depth="35" style={{ top: 0, left: '1%', width: 300, rotate: '-4deg' }}>
          <img src="/images/billboard.jpg" alt="You belong here" />
        </figure>
        <figure className="pic" data-depth="-25" style={{ top: 40, left: '40%', width: 190, rotate: '2deg' }}>
          <img src="/images/normal.jpg" alt="Your normal day is someone's dream" />
        </figure>
        <figure className="pic" data-depth="50" style={{ top: 70, right: '3%', width: 230, rotate: '5deg' }}>
          <img src="/images/star.jpg" alt="Work until you don't have to introduce yourself" />
        </figure>
        <p className="phrase" data-depth="20" style={{ top: 330, right: '6%' }}>“HODL, but make it calm.”</p>

        <div className="center" data-depth="-10">
          <h1>Your keys.<br />Your rules.</h1>
          <p className="lead">A calm, private home for your crypto.</p>
          <p style={{ marginTop: '2rem' }}><a className="btn" href="#start">Take a look ↓</a></p>
        </div>

        <p className="phrase" data-depth="-20" style={{ top: 720, left: '30%' }}>“Less panic. More patience.”</p>
        <figure className="pic" data-depth="-45" style={{ top: 690, left: 0, width: 240, rotate: '3deg' }}>
          <img src="/images/pink.jpg" alt="Busy being that girl" />
        </figure>
        <figure className="pic" data-depth="30" style={{ top: 820, left: '44%', width: 220, rotate: '-2deg' }}>
          <img src="/images/dream.jpg" alt="Follow that dream" />
        </figure>
        <figure className="pic" data-depth="-35" style={{ top: 700, right: '2%', width: 200, rotate: '-3deg' }}>
          <img src="/images/building.jpg" alt="To make it, you have to believe you can" />
        </figure>
        <p className="phrase" data-depth="15" style={{ top: 1130, left: '4%' }}>“Locked tighter than your group chat.”</p>
        <p className="phrase" data-depth="-15" style={{ top: 1150, right: '5%' }}>“Sleep well. Your coins are fine.”</p>
      </section>

      <section className="doodles">
        <div className="doodle" data-depth="20" style={{ rotate: '-3deg' }}>
          <svg viewBox="0 0 72 72"><circle className="blob" cx="40" cy="38" r="22" /><circle cx="22" cy="32" r="10" /><path d="M32 32h26M50 32v8M56 32v6" /></svg>
          <p>Yours. Not ours.</p>
        </div>
        <div className="doodle" data-depth="-25" style={{ rotate: '2deg' }}>
          <svg viewBox="0 0 72 72"><circle className="blob" cx="32" cy="42" r="22" /><rect x="18" y="30" width="36" height="28" rx="6" /><path d="M26 30v-8a10 10 0 0 1 20 0v8" /><circle cx="36" cy="44" r="3" /></svg>
          <p>Locked by default.</p>
        </div>
        <div className="doodle" data-depth="15" style={{ rotate: '-2deg' }}>
          <svg viewBox="0 0 72 72"><circle className="blob" cx="42" cy="40" r="22" /><ellipse cx="36" cy="22" rx="16" ry="6" /><path d="M20 22v8c0 3.3 7.2 6 16 6s16-2.7 16-6v-8M20 30v8c0 3.3 7.2 6 16 6s16-2.7 16-6v-8M20 38v8c0 3.3 7.2 6 16 6s16-2.7 16-6v-8" /></svg>
          <p>Stack quietly.</p>
        </div>
        <div className="doodle" data-depth="-20" style={{ rotate: '3deg' }}>
          <svg viewBox="0 0 72 72"><circle className="blob" cx="30" cy="40" r="22" /><path d="M44 16a18 18 0 1 0 12 30A16 16 0 0 1 44 16z" /><path d="M58 14v6M55 17h6M62 30v4M60 32h4" /></svg>
          <p>Sleep well tonight.</p>
        </div>
        <div className="doodle" data-depth="25" style={{ rotate: '-2deg' }}>
          <svg viewBox="0 0 72 72"><circle className="blob" cx="38" cy="36" r="22" /><path d="M36 58V34M26 58h20" /><path d="M36 38c0-8 6-14 16-14 0 9-6 14-16 14z" /><path d="M36 44c0-7-5-12-13-12 0 8 5 12 13 12z" /></svg>
          <p>Patience is a strategy.</p>
        </div>
        <div className="doodle" data-depth="-15" style={{ rotate: '2deg' }}>
          <svg viewBox="0 0 72 72"><circle className="blob" cx="34" cy="40" r="22" /><path d="M18 40c1-12 8-20 18-20s17 8 18 20M24 44c0-8 5-14 12-14s12 6 12 14M29 52c-1-2-1-5-1-8 0-5 3-9 8-9s8 4 8 9v6M36 44v10" /></svg>
          <p>Only you get in.</p>
        </div>
      </section>

      <section className="hero split" id="start" style={{ marginTop: 'clamp(5rem,12vw,10rem)' }}>
        <div>
          <span className="eyebrow">Private digital vault</span>
          <h1>Your digital<br />vault.</h1>
        </div>
        <div>
          <p className="lead">Group your coins into vaults and keep a clear record of every deposit and withdrawal, all in one calm place.</p>
          <p style={{ marginTop: '2rem' }}><Link className="btn" to="/register">Open your vault</Link></p>
        </div>
      </section>

      <h2>Built on quiet principles</h2>
      <section className="principles">
        <div>
          <span className="icon-badge"><svg viewBox="0 0 24 24"><path d="M12 3l7 3v5c0 4.5-3 8-7 10-4-2-7-5.5-7-10V6l7-3z" /></svg></span>
          <span className="num">01</span><h3>Careful sign-in</h3>
          <p><small>Passwords are stored as secure hashes, and repeated wrong attempts pause the account for 15 minutes.</small></p>
        </div>
        <div>
          <span className="icon-badge"><svg viewBox="0 0 24 24"><rect x="5" y="11" width="14" height="9" rx="2" /><path d="M8 11V8a4 4 0 0 1 8 0v3" /></svg></span>
          <span className="num">02</span><h3>Only yours</h3>
          <p><small>Every vault and coin belongs to your account alone. Nobody else can open, change or delete it.</small></p>
        </div>
        <div>
          <span className="icon-badge"><svg viewBox="0 0 24 24"><path d="M7 4h10v16H7z" /><path d="M10 8h4M10 12h4M10 16h2" /></svg></span>
          <span className="num">03</span><h3>Every action recorded</h3>
          <p><small>A private audit trail only you can see, so you always know what happened, and when.</small></p>
        </div>
      </section>

      <h2>Begin</h2>
      <section>
        <p className="display">Quietly<br />secure.</p>
        <Link className="btn" to="/register">Create your vault</Link>
        <Link to="/login" style={{ marginLeft: '1.5rem' }}>I already have one</Link>
      </section>

      <footer>
        <span>Crypton</span>
        <span>Your crypto, vaulted on your own terms</span>
      </footer>
    </div>
  )
}
