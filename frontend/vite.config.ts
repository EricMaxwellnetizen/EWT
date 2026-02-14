import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    react({
      babel: {
        plugins: [['babel-plugin-react-compiler']],
      },
    }),
  ],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        secure: false,
        // Mark proxied requests as XHR so backend can return 401 JSON instead of redirect
        // and keep the connection header. Also strip redirect Location headers to
        // prevent the browser from following backend-initiated external redirects.
        headers: {
          'Connection': 'keep-alive',
          'X-Requested-With': 'XMLHttpRequest'
        },
        onProxyRes: (proxyRes, req, res) => {
          try {
            if (proxyRes && (proxyRes.statusCode === 301 || proxyRes.statusCode === 302) && proxyRes.headers) {
              delete proxyRes.headers.location;
            }
          } catch (e) {
            // ignore
          }
        },
      },
      '/oauth2': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        secure: false,
        headers: {
          'Connection': 'keep-alive',
        },
        bypass: (req) => {
          // Let the frontend handle /oauth2/callback
          if (req.url?.includes('/oauth2/callback')) {
            return req.url;
          }
        },
      },
      '/login': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        secure: false,
        headers: {
          'Connection': 'keep-alive',
        },
      },
    },
  },
})
