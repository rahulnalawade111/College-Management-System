import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Dev proxy → Spring Boot on :8080
export default defineConfig({
  plugins: [react()],
  server: {
    host: true,
    port: 5173,
    strictPort: true,
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
})
