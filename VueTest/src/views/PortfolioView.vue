<script setup lang="ts">
import { ref } from 'vue'
import Card from '@/components/ui/Card.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/Button.vue'
import { onMounted } from 'vue';
import api from '@/api'; // 1. 匯入 API 客戶端
import { AxiosError } from 'axios';
import '@/assets/index.css' 







//    * 我們只需要 PortfolioItem (單筆交易) 的型別
interface PortfolioItem {
  id?: number;
  stockSymbol: string;
  quantity: number;
  purchasePrice: number;
  createdAt?: string; // 交易時間
}





// 2. 定義狀態變數 (State) - 加上型別 
// 修正點 1：明確指定 allStocks 是 Stock 陣列
// 修正點 2：明確指定 selectedStock 是 Stock 或 null
const historyList = ref<PortfolioItem[]>([]); 
const loadingHistory = ref(false);
const loading = ref(false); // "新增中" 的 loading
const error = ref(''); // 表單錯誤

const form = ref<Omit<PortfolioItem, 'id' | 'createdAt'>>({
  stockSymbol: '',
  quantity: 0,
  purchasePrice: 0
});

//    * 只抓取交易歷史 
const fetchHistory = async () => {
  loadingHistory.value = true;
  try {
    const response = await api.get<PortfolioItem[]>('/api/portfolio/history');
    historyList.value = response.data;
  } catch (err) {
    console.error('取得交易歷史失敗:', err);
    error.value = '無法載入交易歷史。';
  } finally {
    loadingHistory.value = false;
  }
};

// 6. (*** 簡化 ***) 新增持股
const addHolding = async () => {
  loading.value = true;
  error.value = '';
  try {
    await api.post<PortfolioItem>('/api/portfolio', {
      stockSymbol: form.value.stockSymbol,
      quantity: form.value.quantity,
      purchasePrice: form.value.purchasePrice
    });
    
    // 新增成功後，我們只需要 "重新整理歷史表"
    await fetchHistory(); 

    // (清空表單，保持不變)
    form.value.stockSymbol = '';
    form.value.quantity = 0;
    form.value.purchasePrice = 0;


  } catch (err) {
    console.error('新增持股失敗:', err);
    if (err instanceof AxiosError && err.response) {
      error.value = `新增失敗: ${err.response.data.message || '請檢查輸入'}`;
    } else {
      error.value = '新增持股時發生錯誤。';
    }
  } finally {
    loading.value = false;
  }
};

// (格式化時間函式，保持不變)
const formatDateTime = (isoString: string) => {
  if (!isoString) return 'N/A';
  const date = new Date(isoString);
  return date.toLocaleString('zh-TW'); // 格式化為台灣本地時間
};








onMounted(() => {
  fetchHistory();
});




</script>

<template>
  <div>
    <div class="mb-8">
      <h1 class="text-3xl font-bold mb-2">持有股票</h1>
      <p class="text-muted-foreground">Manage your portfolio information</p>
    </div>
   <Card class="mt-6">
      <CardHeader>
        <CardTitle>新增持股</CardTitle>
      </CardHeader>
      <CardContent>
        <div class="space-y-2">
          <form @submit.prevent="addHolding" class="holding-form">
          <div class="form-row">
            <input v-model.number="form.stockSymbol" placeholder="股票代號" style="border: 2px solid #ccc;outline: none;border-radius: 6px;margin-right: 7px;" required />
            <input v-model.number="form.quantity" placeholder="數量 (股)" style="border: 2px solid #ccc;outline: none;border-radius: 6px;margin-right: 7px;" required />
            <input v-model.number="form.purchasePrice"  placeholder="買入價格" style="border: 2px solid #ccc;outline: none;border-radius: 6px;margin-right: 7px;" required />
            <Button size="sm"  type="submit" :disabled="loading">
              新增持股
            </Button>

          </div>
          <p v-if="error" class="error-message">{{ error }}</p>
        </form>

        </div>
      </CardContent>
    </Card>

    

    <Card class="mt-6">
      <CardHeader>
        <CardTitle>History</CardTitle>
      </CardHeader>
      <CardContent>
        <div class="space-y-2">
          
          <div class="hidden md:grid grid-cols-5 gap-5 px-5 py-2 text-sm font-medium text-muted-foreground border-b">
            <div>portfolio_name</div>
            <div>Date</div>
            <div>Amount</div>
            <div>price</div>
            <!-- <div>Status</div> -->
            
          </div>

          <div
            v-for="item in historyList"
            :key="item.id"
            class="flex flex-col md:grid md:grid-cols-5 gap-2 md:gap-5 px-5 py-3 hover:bg-accent rounded-md"
          >
            <div class="flex justify-between md:block">
              <span class="text-sm md:hidden font-medium text-muted-foreground">Invoice</span>
              <span class="font-medium">{{ item.stockSymbol }}</span>
            </div>
            <div class="flex justify-between md:block">
              <span class="text-sm md:hidden font-medium text-muted-foreground">Date</span>
              <span>{{ formatDateTime(item.createdAt!) }}</span>
            </div>
            <div class="flex justify-between md:block">
              <span class="text-sm md:hidden font-medium text-muted-foreground">Amount</span>
              <span>${{ item.quantity }} </span>
            </div>
            <div class="flex justify-between md:block">
              <span class="text-sm md:hidden font-medium text-muted-foreground">price</span>
              <span>${{ item.purchasePrice }}</span>
            </div>
            <!-- <div class="flex justify-between md:block items-center">
              <span class="text-sm md:hidden font-medium text-muted-foreground">Status</span>
              <div class="flex items-center gap-2">
                <span class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800">
                  {{ portfolio_name.status }}
                </span>
              </div>
            </div> -->
          </div>
        </div>
      </CardContent>
    </Card>
  </div>
</template>


<style scoped>
/* 6. 加上一點基本樣式 */
.stock-search-container {
  position: relative; /* 關鍵：讓 ul 可以定位在 input 下方 */
  width: 300px;
  font-family: Arial, sans-serif;
}

#stock-search {
  width: 100%;
  padding: 8px 12px;
  font-size: 16px;
  box-sizing: border-box; /* 確保 padding 不會讓寬度超出去 */
}

.stock-results-list {
  position: absolute;
  top: 100%; /* 顯示在 input 的正下方 */
  left: 0;
  right: 0;
  background-color: white;
  border: 1px solid #000000;
  border-top: none;
  list-style: none;
  margin: 0;
  padding: 0;
  max-height: 100px; /* 加上最大高度和捲軸 */
  overflow-y: auto;
  z-index: 1000; /* 確保在最上層 */
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}

.stock-result-item {
  padding: 8px 12px;
  cursor: pointer;
}

.stock-result-item:hover {
  background-color: #f0f0f0;
}
</style>