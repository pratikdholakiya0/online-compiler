import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Proxies /api requests to the Spring Boot backend during local dev,
// so the browser never needs CORS config on the backend.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
