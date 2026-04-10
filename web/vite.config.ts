import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      // 本地联调方案 A: 将 /api 开头的请求代理到后端服务
      '/api': {
        target: 'http://localhost:8080', // 后端 Spring Boot 默认端口
        changeOrigin: true,
        secure: false,
        // 如有需要，可以重写路径，但通常不需要
        // rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
