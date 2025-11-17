import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
        path: '/', // 當使用者在根目錄時
        redirect: '/login' // 自動幫他跳轉到 /login 頁面
      },
      {
        path: '/login', // 網址
        name: 'login', // 路由名稱
        // 告訴 Router 要載入哪個組件
        component: () => import('@/views/LoginView.vue'),
        meta: {
          fullscreen: true  // 我們給它一個 "fullscreen" 的標記
        }
      },
      {
        path: '/protected',
        name: 'protected',
        component: () => import('@/views/ProtectedView.vue')
      },
      {
      path: '/',
      component: MainLayout,
      
      children: [
        {
          path: '/dashboard',
          name: 'dashboard',
          component: () => import('@/views/DashboardView.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: '/portfolio',
          name: 'Portfolio',
          component: () => import('@/views/PortfolioView.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: '/chat',
          name: 'chat',
          component: () => import('@/views/ChatView.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: '/settings',
          name: 'settings',
          component: () => import('@/views/SettingsView.vue'), // 指向新的組件
          meta: { requiresAuth: true } // 同樣設定需要登入
        }
      ]
    }
]
})




export default router
