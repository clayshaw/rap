<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue' // 1. 匯入
import Card from '@/components/ui/Card.vue' //
import CardHeader from '@/components/ui/CardHeader.vue' //
import CardTitle from '@/components/ui/CardTitle.vue' //
import CardContent from '@/components/ui/CardContent.vue' //
import CardFooter from '@/components/ui/CardFooter.vue' //

import { Send } from 'lucide-vue-next' // (你需要 lucide-vue-next)
import api from '@/api' // 匯入 API 客戶端
import { AxiosError } from 'axios'
import '@/assets/index.css' //

// 2. 定義訊息的型別
interface Message {
  role: 'user' | 'model' // 'user' 是你, 'model' 是 AI
  content: string
}

// 3. 建立響應式狀態
const messages = ref<Message[]>([]) // 存放所有聊天訊息
const inputMessage = ref('') // 綁定到輸入框
const isLoading = ref(false) // 控制是否在 "思考中..."
const error = ref('') // 顯示錯誤

// 處理傳送訊息的函式
const handleSend = async () => {
  const userMessage = inputMessage.value.trim()
  if (!userMessage || isLoading.value) return

  isLoading.value = true
  error.value = ''

  // 1. (*** 關鍵 ***) 建立 "包含新訊息" 的完整歷史
  const fullHistory = [...messages.value, { role: 'user', content: userMessage }]

  // 2. 立刻將使用者的訊息加入 "畫面"
  messages.value.push({ role: 'user', content: userMessage })

  inputMessage.value = ''
  scrollToBottom()

  try {
    // 3. (*** 關鍵 ***)
    //    呼叫後端 API，傳送 "完整" 的歷史紀錄
    //    這必須符合 GeminiChatRequest.java 的格式 { "messages": [...] }
    const response = await api.post('/api/gemini/chat', {
      messages: fullHistory, // <-- 傳送完整歷史
    })

    // 4. 成功後，將 AI 的回覆加入列表
    messages.value.push({ role: 'model', content: response.data })
  } catch (err) {
    if (err instanceof AxiosError) {
      error.value = `錯誤: ${err.response?.data || err.message}`
    } else {
      error.value = '發生未知錯誤'
    }
    // ... (錯誤處理)
  } finally {
    isLoading.value = false
    scrollToBottom()
  }
}

// 自動滾動
const chatContainer = ref<HTMLElement | null>(null)

const scrollToBottom = () => {
  // nextTick 確保 DOM 已經更新完畢
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

// --- 6. 載入時給一條歡迎訊息 ---
onMounted(() => {
  messages.value.push({
    role: 'model',
    content: '你好！我是 Gemini，有什麼可以幫助你的嗎？',
  })
})
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-xl font-bold tracking-tight">Gemini Chat</h1>
      <p class="text-muted-foreground">與你的 AI 助理聊天</p>
    </div>

    <Card class="flex flex-col h-[70vh]">
      <CardHeader> <CardTitle>聊天室</CardTitle> </CardHeader>

      <CardContent
        ref="chatContainer"
        class="flex-1 overflow-y-auto space-y-4 p-4 border-t border-b"
      >
        <div
          v-for="(msg, index) in messages"
          :key="index"
          class="flex"
          :class="{ 'justify-end': msg.role === 'user', 'justify-start': msg.role === 'model' }"
        >
          <div
            class="max-w-xs lg:max-w-md px-4 py-2 rounded-lg shadow-md"
            :class="{
              'bg-blue-600 text-white': msg.role === 'user',
              'bg-gray-100 text-gray-800': msg.role === 'model',
            }"
          >
            {{ msg.content }}
          </div>
        </div>

        <div v-if="isLoading" class="flex justify-start">
          <div class="max-w-xs px-4 py-2 rounded-lg shadow-md bg-gray-100 text-gray-800">
            <span class="animate-pulse">思考中...</span>
          </div>
        </div>
      </CardContent>

      <CardFooter class="pt-6">
        <div class="flex items-center w-full space-x-2">
          <input
            v-model="inputMessage"
            @keyup.enter="handleSend"
            :disabled="isLoading"
            type="text"
            placeholder="輸入你的訊息..."
            class="flex-1 p-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            style="border-radius: 1rem"
          />
          <button
            @click="handleSend"
            :disabled="isLoading"
            class="p-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-gray-400"
          >
            <Send class="h-5 w-5" />
          </button>
        </div>
      </CardFooter>
    </Card>
  </div>
</template>

<style scoped lang="postcss">
/* (你可以加入額外的 @apply 樣式，但 Tailwind class 應該足夠了) */
</style>
