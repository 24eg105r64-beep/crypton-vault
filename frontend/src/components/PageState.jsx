import Message from './Message'

/** Shown while a page's data is loading, or if it failed to load. */
export function Loading({ text = 'Opening your vault…' }) {
  return <p className="loading lead">{text}</p>
}

export function LoadError({ error, onRetry }) {
  return (
    <>
      <Message type="error">{error}</Message>
      {onRetry && (
        <p>
          <button type="button" onClick={onRetry}>Try again</button>
        </p>
      )}
    </>
  )
}
