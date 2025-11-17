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
