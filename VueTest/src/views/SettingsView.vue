<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/api'
import Button from '@/components/ui/Button.vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

const userData = ref({
  username: '',
  email: '',
})

// 分開控制「初始載入」與「資料更新」的狀態
const isLoading = ref(true) // 用於剛進入頁面時讀取資料
const isUpdating = ref(false) // 用於按下更新按鈕時
const errorMessage = ref('')
const successMessage = ref('') // 新增成功訊息提示

const fetchUserProfile = async () => {
  const response = await api.get('api/users/me')
  return response.data
}

onMounted(async () => {
  try {
    isLoading.value = true
    const data = await fetchUserProfile()
    userData.value.username = data.username
    userData.value.email = data.email
  } catch (error) {
    errorMessage.value = '無法載入個人資料，請稍後再試。'
    console.error(error)
  } finally {
    isLoading.value = false
  }
})

// 實作更新邏輯
const handleUpdate = async () => {
  // 1. 清除舊訊息
  errorMessage.value = ''
  successMessage.value = ''

  // 2. 設定更新狀態為 true (按鈕會變 loading，但表單不會消失)
  isUpdating.value = true

  try {
    // 假設後端更新 API 為 PUT /api/users/me (請依實際情況調整)
    // 注意：這裡傳送 userData.value，後端通常只允許更新 email
    await api.post('api/users/updateEmail', userData.value.email)

    successMessage.value = '資料更新成功！'

    // 更新成功後，可以選擇重新抓取資料確保同步，或直接使用當前值
  } catch (error) {
    console.error(error)
    errorMessage.value = '更新失敗，請檢查網路或資料格式。'
  } finally {
    // 3. 無論成功失敗，都解除更新狀態
    isUpdating.value = false
  }
}

</script>

<template>
  <div class="space-y-6">
    <h1 class="text-2xl font-semibold">個人資料管理</h1>

    <div v-if="isLoading" class="text-center text-muted-foreground py-10">
      <i class="fas fa-spinner fa-spin mr-2"></i> 讀取資料中...
    </div>

    <div v-else class="bg-card p-6 rounded-lg shadow">
      <div
        v-if="errorMessage"
        class="mb-4 bg-red-100 border-l-4 border-red-500 text-red-700 p-4"
        role="alert"
      >
        <p>{{ errorMessage }}</p>
      </div>
      <div
        v-if="successMessage"
        class="mb-4 bg-green-100 border-l-4 border-green-500 text-green-700 p-4"
        role="alert"
      >
        <p>{{ successMessage }}</p>
      </div>

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
              required
              class="mt-1 block w-full px-3 py-2 bg-background border rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>

          <div class="flex justify-end">
            <button
              type="submit"
              :disabled="isUpdating"
              class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed flex items-center"
            >
              <span v-if="isUpdating">
                <svg
                  class="animate-spin -ml-1 mr-2 h-4 w-4 text-white"
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                >
                  <circle
                    class="opacity-25"
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    stroke-width="4"
                  ></circle>
                  <path
                    class="opacity-75"
                    fill="currentColor"
                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                  ></path>
                </svg>
                更新中...
              </span>
              <span v-else>更新資料</span>
            </button>
          </div>
        </div>
      </form>
      <Button
        @click="authStore.logout"
        class="mt-4  hover:bg-red-500 text-white backgroundcolor: bg-red-600"
      >
        登出
      </Button>
    </div>
  </div>
</template>
