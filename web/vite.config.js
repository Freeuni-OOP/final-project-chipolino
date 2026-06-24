import fs from 'fs';
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

const isHttps = fs.existsSync('./localhost-key.pem') && fs.existsSync('./localhost.pem');

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: 'http://roadreport-backend:8080',
        changeOrigin: true,
        secure: false
      }
    },
    ...(isHttps ? {
      https: {
        key: fs.readFileSync('./localhost-key.pem'),
        cert: fs.readFileSync('./localhost.pem'),
      }
    } : {})
  },
});