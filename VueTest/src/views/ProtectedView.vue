<template>
  <div>
    <h1>受保護的頁面</h1>
    <button @click="fetchProtectedData">取得後端資料</button>
    <p v-if="message">來自後端的訊息： {{ message }}</p>
    <p v-if="error" style="color: red">錯誤： {{ error }}</p>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import api from '@/api' // <-- 1. 使用我們的中央 API 客戶端

const message = ref('')
const error = ref('')

const fetchProtectedData = async () => {
  message.value = ''
  error.value = ''
  try {
    // 2. 呼叫受保護的 API (不需要手動加 Token)
    const response = await api.get('/api/test/hello')
    message.value = response.data
  } catch (err) {
    console.error(err)
    error.value = '無法取得資料，請確認您已登入且有權限存取此資源。'
  }
}
</script>
