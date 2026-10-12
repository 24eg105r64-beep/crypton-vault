// Central place for every request to the Spring Boot backend.
//
// VITE_API_BASE_URL is the backend's address (set on Render).
// When it's empty (local development), requests go to /api on the same
// address and Vite forwards them to http://localhost:8080.

const BASE_URL = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/+$/, '')
const TOKEN_KEY = 'crypton.token'

export class ApiError extends Error {
  constructor(message, status, errors = {}) {
    super(message)
    this.status = status
    this.errors = errors
  }
}

// The sign-in token lives in sessionStorage: it is cleared when the tab closes.
export function getToken() {
  try {
    return sessionStorage.getItem(TOKEN_KEY)
  } catch {
    return null
  }
}

export function setToken(token) {
  try {
    if (token) sessionStorage.setItem(TOKEN_KEY, token)
    else sessionStorage.removeItem(TOKEN_KEY)
  } catch {
    // Storage unavailable (private mode). The session just won't survive a refresh.
  }
}

export async function api(path, { method = 'GET', body } = {}) {
  const headers = { Accept: 'application/json' }
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  const token = getToken()
  if (token) headers.Authorization = `Bearer ${token}`

  let response
  try {
    response = await fetch(`${BASE_URL}${path}`, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    })
  } catch {
    throw new ApiError(
      "We can't reach Crypton right now. If it has been idle, the server can take up to a minute to wake up. Please try again.",
      0,
    )
  }

  if (response.status === 204) return null

  let data = null
  const text = await response.text()
  if (text) {
    try {
      data = JSON.parse(text)
    } catch {
      data = null
    }
  }

  if (!response.ok) {
    if (response.status === 401 && token) {
      setToken(null)
      window.dispatchEvent(new Event('crypton:session-expired'))
    }
    const message = (data && data.message) || 'Something went wrong. Please try again.'
    throw new ApiError(message, response.status, (data && data.errors) || {})
  }

  return data
}
