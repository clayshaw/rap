import axios, { AxiosError } from 'axios'; // 匯入

import { useAuthStore } from '@/stores/auth'; // 匯入 Pinia Store
import router from '@/router'; // 匯入 Vue Router




//建立一個客製化的 axios 實例
const api = axios.create({
  baseURL: 'http://10.244.208.177:8080', // 你的後端 API 基礎 URL
  timeout: 10000, // 請求超時
});

// 建立「請求攔截器」
//    在 "每一次" API 請求 "發送出去之前" 執行
api.interceptors.request.use(
  (config) => {
    // 取得 Pinia store
    // 注意：我們不能在這裡用 useAuthStore()，因為 Pinia 可能還沒初始化
    // 我們將在 store 檔案中動態設定標頭
    const token = localStorage.getItem('authToken'); // 直接從 localStorage 讀取

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config; // 回傳設定好的 config
  },
  (error) => {
    // 請求錯誤的處理
    return Promise.reject(error);
  }
);

// 建立「回應攔截器」
//    在 "收到" API 回應 "之後" 執行
api.interceptors.response.use(
  (response) => {
    // 成功的回應 (2xx 狀態碼)，直接回傳
    return response;
  },
  (error: AxiosError) => { // 明確指定 error 型別
    // 失敗的回應 (4xx, 5xx 狀態碼)
    if (error.response && error.response.status === 403) {
      // 如果是 401 (未授權)，代表 Token 失效或被竄改
      // 我們可以自動執行 "登出"
      const authStore = useAuthStore();
      authStore.logout();

      // 重新導向到登入頁面
      // (並附上一個 'redirect' 參數，讓登入後能跳回來)
      console.log('連線逾時，請重新登入');
      router.push({
        path: '/login',
        query: { redirect: router.currentRoute.value.fullPath }
      });
    }
    return Promise.reject(error);
  }
);



// 匯出這個客製化的實例
export default api;