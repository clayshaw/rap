import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/api'; // <-- 1. 匯入客製化 API 客戶端
import router from '@/router'; // 2. 匯入 router

export const useAuthStore = defineStore('auth', () => {
  
  const token = ref<string | null>(localStorage.getItem('authToken'));
  const isAuthenticated = computed(() => !!token.value);

  // *** (新增) ***
  // App 啟動時，如果 localStorage 有 token，就先設定一次標頭
  if (token.value) {
    api.defaults.headers.common['Authorization'] = `Bearer ${token.value}`;
  }

  function login(newToken: string) {
    token.value = newToken;
    localStorage.setItem('authToken', newToken);
    
    // 3. (*** 新增 ***) 登入時，"立刻" 更新 axios 的預設標頭
    api.defaults.headers.common['Authorization'] = `Bearer ${newToken}`;
    
    // (*** 新增 ***) 登入後自動跳轉
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
    localStorage.removeItem('authToken');
    
    // 4. (*** 新增 ***) 登出時，"立刻" 移除 axios 的預設標頭
    delete api.defaults.headers.common['Authorization'];
    
    // (*** 新增 ***) 登出後，跳轉到登入頁
    router.push('/login');
  }

  return { token, isAuthenticated, login, logout }
})