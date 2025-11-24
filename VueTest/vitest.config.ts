import { fileURLToPath } from 'node:url'
import { mergeConfig, defineConfig, configDefaults } from 'vitest/config'
import viteConfig from './vite.config'

export default mergeConfig(
  viteConfig,
  defineConfig({
    test: {
      environment: 'jsdom',
      exclude: [...configDefaults.exclude, 'e2e/**'],
      root: fileURLToPath(new URL('./', import.meta.url)),
    },
    server: {
      proxy: {
        '/api': {
        // 目標是您的 Spring Boot 伺服器
          target: 'http://localhost:8080', 
          // 允許跨來源
          changeOrigin: true, 
          // 注意：這裡不需要 rewrite，因為您的後端 API 
          // (例如 /api/users/me 和 /api/auth/login) 
          // 已經包含了 /api 前綴
        },
          // (*** 必須要有這一段 ***)
          '/twse': {
            target: 'https://openapi.twse.com.tw',
            changeOrigin: true,
            rewrite: (path) => path.replace(/^\/twse/, '')
          }
      },
    host: "0.0.0.0",
    port: 5000,
      hmr: {
        clientPort: 443,
      },
    },
    preview: {
      host: "0.0.0.0",
      port: 5000,
    },
  }),
)
