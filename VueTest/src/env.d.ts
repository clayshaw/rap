/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  
  // 透過這個 "eslint-disable-next-line" 註解, 
  // 我們告訴 ESLint 忽略下一行的 "any" 和 "empty-object" 規則
  // eslint-disable-next-line @typescript-eslint/no-explicit-any, @typescript-eslint/no-empty-object-type
  const component: DefineComponent<{}, {}, any>
  export default component
}