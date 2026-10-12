import { coinSymbol } from '../utils'

export default function CoinBadge({ code }) {
  return (
    <span className="icon-badge" aria-hidden="true">
      <span>{coinSymbol(code)}</span>
    </span>
  )
}
