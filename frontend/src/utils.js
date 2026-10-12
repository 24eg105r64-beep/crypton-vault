const SYMBOLS = { BTC: '₿', ETH: 'Ξ', SOL: '◎', USDT: '₮' }
const NAMES = { BTC: 'Bitcoin', ETH: 'Ethereum', SOL: 'Solana', USDT: 'Tether' }
const MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']

export const coinSymbol = (code) => SYMBOLS[code] || '•'
export const coinName = (code) => NAMES[code] || code

// The backend sends dates like "2026-10-12T14:39:24.123" (server time).
function parts(value) {
  const match = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})/.exec(value || '')
  if (!match) return null
  const [, year, month, day, hour, minute] = match
  return { year, month: MONTHS[Number(month) - 1], day, hour, minute }
}

/** "Oct 2026" */
export function monthYear(value) {
  const p = parts(value)
  return p ? `${p.month} ${p.year}` : ''
}

/** "12 Oct, 14:39" */
export function dayTime(value) {
  const p = parts(value)
  return p ? `${p.day} ${p.month}, ${p.hour}:${p.minute}` : ''
}

/** "12 Oct 2026, 14:39" */
export function fullDateTime(value) {
  const p = parts(value)
  return p ? `${p.day} ${p.month} ${p.year}, ${p.hour}:${p.minute}` : ''
}

/** "VAULT_CREATED" -> "Vault created" */
export function activityLabel(action) {
  const text = String(action || '').replace(/_/g, ' ').toLowerCase()
  return text.charAt(0).toUpperCase() + text.slice(1)
}
