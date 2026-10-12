/** The calm success / error banner used across the site. */
export default function Message({ type = 'success', children }) {
  if (!children) return null
  return (
    <p className={`msg msg-${type}`} role={type === 'error' ? 'alert' : 'status'}>
      {children}
    </p>
  )
}
