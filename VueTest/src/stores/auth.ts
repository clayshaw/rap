import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/api'; // <-- 1. 匯入客製化 API 客戶端
import router from '@/router'; // 2. 匯入 router

export const useAuthStore = defineStore('auth', () => {
  
  const token = ref<string | null>(localStorage.getItem('authToken'));
  const isAuthenticated = computed(() => !!token.value);

  // App 啟動時，如果 localStorage 有 token，就先設定一次標頭
  if (token.value) {
    api.defaults.headers.common['Authorization'] = `Bearer ${token.value}`;
  }

  function login(newToken: string) {
    token.value = newToken;
    localStorage.setItem('authToken', newToken);
    
    // 3. 登入時，"立刻" 更新 axios 的預設標頭
    api.defaults.headers.common['Authorization'] = `Bearer ${newToken}`;
    
    // 登入後自動跳轉
    // 檢查路由是否有 'redirect' 參數
    const redirectPath = router.currentRoute.value.query.redirect as string | undefined;
    if (redirectPath) {
      router.push(redirectPath); // 跳回原本想去的頁面
    } else {
      router.push('/dashboard'); // 預設跳到首頁
    }
  }

  function logout() {
    token.value = null;
    // localStorage.removeItem('authToken');
    // localStorage.removeItem('__vue-devtools-frame-state__');
    localStorage.clear(); // 可選：清除其他相關資料
    
    // 4. 登出時，"立刻" 移除 axios 的預設標頭
    delete api.defaults.headers.common['Authorization'];
    
    // 登出後，跳轉到登入頁
    router.push('/');

    
  }

  return { token, isAuthenticated, login, logout }
})