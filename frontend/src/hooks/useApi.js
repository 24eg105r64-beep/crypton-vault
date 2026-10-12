import { useCallback, useEffect, useState } from 'react'
import { api } from '../api/client'

/** Loads JSON from the backend and keeps track of loading / error state. */
export function useApi(path) {
  const [data, setData] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  const reload = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      setData(await api(path))
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [path])

  useEffect(() => {
    reload()
  }, [reload])

  return { data, error, loading, reload }
}
