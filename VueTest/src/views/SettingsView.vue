<script setup lang="ts">
import { ref, onMounted } from 'vue'
// 步驟 2.1 建立的 API 函式
import  api  from '@/api' 

// 定義用戶資料的結構 (符合後端 User 實體)
const userData = ref({
  username: '',
  email: ''
})

const isLoading = ref(true)
const errorMessage = ref('')

const fetchUserProfile = async () => {
  try {
    const response = await api.get('api/users/me')
    console.log('Fetched user profile:', response)
    // response.data 將會是後端傳來的 User 物件
    // { id, username, email, createdAt, ... }
    return response.data
  } catch (error) {
    console.error('Error fetching user profile:', error)
    throw error
  }
}

// onMounted 時呼叫 API 獲取真實的用戶資料
onMounted(async () => {
  try {
    isLoading.value = true
    errorMessage.value = ''
    
    // 呼叫 API
    const data = await fetchUserProfile()
    
    // 更新 VUE 畫面上的資料
    userData.value.username = data.username
    userData.value.email = data.email


  } catch (error) {
    errorMessage.value = '無法載入個人資料，請稍後再試。'
    console.error(error)
  } finally {
    isLoading.value = false
  }
})

const handleUpdate = () => {
  
  alert('更新的 API 尚未實作')
}

</script>

<template>
  <div class="space-y-6">
    <h1 class="text-2xl font-semibold">個人資料管理</h1>

    <div v-if="isLoading" class="text-center text-muted-foreground">
      載入中...
    </div>

    <div v-else-if="errorMessage" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4" role="alert">
      <p>{{ errorMessage }}</p>
    </div>

    <div v-else class="bg-card p-6 rounded-lg shadow">
      <form @submit.prevent="handleUpdate">
        <div class="space-y-4">
          <div>
            <label for="username" class="block text-sm font-medium text-muted-foreground">
              使用者名稱 (Username)
            </label>
            <input 
              type="text" 
              id="username"
              v-model="userData.username"
              disabled
              class="mt-1 block w-full px-3 py-2 bg-background border rounded-md shadow-sm opacity-70 cursor-not-allowed"
            />
            <p class="mt-1 text-xs text-muted-foreground">使用者名稱目前不開放修改。</p>
          </div>

          <div>
            <label for="email" class="block text-sm font-medium text-muted-foreground">
              電子郵件 (Email)
            </label>
            <input 
              type="email" 
              id="email"
              v-model="userData.email"
              class="mt-1 block w-full px-3 py-2 bg-background border rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>

          <div class="flex justify-end">
            <button 
              type="submit"
              class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
            >
              更新資料
            </button>
          </div>
        </div>
      </form>
    </div>
  </div>
</template>