import { Route, Routes, useLocation } from 'react-router-dom'
import ProtectedRoute from './components/ProtectedRoute'
import PublicOnlyRoute from './components/PublicOnlyRoute'
import Home from './pages/Home'
import Login from './pages/Login'
import Register from './pages/Register'
import Dashboard from './pages/Dashboard'
import Vaults from './pages/Vaults'
import VaultDetail from './pages/VaultDetail'
import AssetDetail from './pages/AssetDetail'
import NotFound from './pages/NotFound'

export default function App() {
  const location = useLocation()

  return (
    // The key replays the gentle entrance animation on every page change.
    <div className="route" key={location.pathname}>
      <Routes>
        <Route path="/" element={<Home />} />

        <Route element={<PublicOnlyRoute />}>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
        </Route>

        <Route element={<ProtectedRoute />}>
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/vaults" element={<Vaults />} />
          <Route path="/vaults/:vaultId" element={<VaultDetail />} />
          <Route path="/assets/:assetId" element={<AssetDetail />} />
        </Route>

        <Route path="*" element={<NotFound />} />
      </Routes>
    </div>
  )
}
