import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

// During local development, requests to /api are forwarded to Spring Boot,
// so the React app and the backend behave like one website.
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  return {
    plugins: [react()],
    server: {
      port: 5173,
      proxy: {
        '/api': {
          target: env.VITE_DEV_BACKEND_URL || 'http://localhost:8080',
          changeOrigin: true,
        },
      },
    },
  }
})
