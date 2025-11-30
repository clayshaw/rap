import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueJsx(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    host: '0.0.0.0',
    port: 5173, // 如果你想固定用 5000 port 可以加這行，預設通常是 5173
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/twse': {
        target: 'https://openapi.twse.com.tw',
        changeOrigin: true,
        // 這一行非常重要，必須把 /twse 拿掉，不然證交所會看不懂路徑
        rewrite: (path) => path.replace(/^\/twse/, '') 
      }
    }
  }
})