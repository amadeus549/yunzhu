import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [uni()],
  // 开发期 H5（npm run dev:h5）把 /resident 代理到后端；
  // 生产 H5 部署在 nginx 同源 /m/ 下天然同源，无需代理
  server: {
    proxy: {
      '/resident': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
