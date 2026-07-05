import fs from 'fs';
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
    server: {
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                secure: false
            }
        }
    }
})

//     https: fs.existsSync('./localhost-key.pem') && fs.existsSync('./localhost.pem')
//         ? {
//           key: fs.readFileSync('./localhost-key.pem'),
//           cert: fs.readFileSync('./localhost.pem'),
//         } : false
//   },