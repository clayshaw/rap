

<script setup lang="ts">
import { ref, computed } from 'vue'
import Card from '@/components/ui/Card.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/Button.vue'
import { onMounted } from 'vue';
import api from '@/api';
import { AxiosError } from 'axios';
import '@/assets/index.css'
import stockData from '@/assets/stock.json' // 引入 stock.json

// 股票資料型別
interface Stock {
  industry_category: string;
  id: string;
  name: string;
}

interface PortfolioItem {
  id?: number;
  stockSymbol: string;
  quantity: number;
  purchasePrice: number;
  createdAt?: string;
}

const historyList = ref<PortfolioItem[]>([]); 
const loadingHistory = ref(false);
const loading = ref(false);
const error = ref('');

const form = ref<Omit<PortfolioItem, 'id' | 'createdAt'>>({
  stockSymbol: '',
  quantity: 0,
  purchasePrice: 0
});

// 搜尋相關狀態
const searchQuery = ref('');
const showDropdown = ref(false);
const stocks = ref<Stock[]>(stockData as Stock[]); // 載入股票資料

// 過濾搜尋結果
const filteredStocks = computed(() => {
  if (!searchQuery.value) return [];
  
  const query = searchQuery.value.toLowerCase();
  return stocks.value
    .filter(stock => 
      stock.id.toLowerCase().includes(query) || 
      stock.name.toLowerCase().includes(query)
    )
    .slice(0, 30); // 限制顯示 10 筆
});

// 選擇股票（點擊後直接選擇，不需要按 Enter）
const selectStock = (stock: Stock) => {
  // 如果 id 長度小於 4，前面補 0
  const paddedId = stock.id.padStart(4, '0');
  form.value.stockSymbol = paddedId;
  searchQuery.value = `${paddedId} - ${stock.name}`;
  showDropdown.value = false;
  
  // 自動聚焦到下一個輸入框（數量）
  const quantityInput = document.querySelector('input[placeholder="數量 (股)"]') as HTMLInputElement;
  if (quantityInput) {
    setTimeout(() => quantityInput.focus(), 100);
  }
};

// 處理輸入變化
const handleSearchInput = () => {
  showDropdown.value = searchQuery.value.length > 0;
};

// 點擊外部關閉下拉選單
const handleClickOutside = (event: FocusEvent) => {
  // 檢查是否點擊到下拉選單內，如果是就不關閉
  const target = event.relatedTarget as HTMLElement;
  if (target && target.closest('.stock-results-list')) {
    return;
  }
  setTimeout(() => {
    showDropdown.value = false;
  }, 200);
};

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

const addHolding = async () => {
  loading.value = true;
  error.value = '';
  try {
    await api.post<PortfolioItem>('/api/portfolio', {
      stockSymbol: form.value.stockSymbol,
      quantity: form.value.quantity,
      purchasePrice: form.value.purchasePrice
    });
    
    await fetchHistory(); 

    // 清空表單
    form.value.stockSymbol = '';
    form.value.quantity = 0;
    form.value.purchasePrice = 0;
    searchQuery.value = '';

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

const removeHolding = async (item: PortfolioItem) => {
  loading.value = true;
  error.value = '';
  try {
    await api.delete(`/api/portfolio/remove`, { data: { createdAt: item.createdAt } });
    await fetchHistory(); 
    // console.log('刪除股票尚未實作');
  } catch (err) {
    console.error('刪除持股失敗:', err);
    error.value = '刪除持股時發生錯誤。';
  } finally {
    loading.value = false;
  }
};

const formatDateTime = (isoString: string) => {
  if (!isoString) return 'N/A';
  const date = new Date(isoString);
  return date.toLocaleString('zh-TW');
};

// 根據股票代號查找股票名稱
const getStockName = (stockSymbol: string) => {
  const stock = stocks.value.find(s => s.id === stockSymbol || s.id.padStart(4, '0') === stockSymbol);
  return stock ? stock.name : '';
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
      <CardContent style="overflow: visible;">
        <div class="space-y-2" style="overflow: visible;">
          <form @submit.prevent="addHolding" class="holding-form" style="overflow: visible;">
          <div class="form-row" style="overflow: visible;">
            <!-- 股票搜尋輸入框 -->
            <div class="stock-search-container">
              <input 
                v-model="searchQuery"
                @input="handleSearchInput"
                @blur="handleClickOutside"
                placeholder="搜尋股票代號或名稱" 
                style="border: 2px solid #ccc;outline: none;border-radius: 6px;margin-right: 7px;padding: 8px 12px;width: 100%;" 
                required 
                autocomplete="off"
              />
              
              <!-- 下拉選單 -->
              <ul v-if="showDropdown && filteredStocks.length > 0" class="stock-results-list">
                <li 
                  v-for="stock in filteredStocks" 
                  :key="stock.id"
                  @click="selectStock(stock)"
                  class="stock-result-item"
                >
                  <strong>{{ stock.id }}</strong> - {{ stock.name }}
                  <span class="stock-category">{{ stock.industry_category }}</span>
                </li>
              </ul>
              
              <!-- 無結果提示 -->
              <div v-if="showDropdown && searchQuery && filteredStocks.length === 0" class="no-results">
                找不到相關股票
              </div>
            </div>
            
            <input v-model.number="form.quantity" placeholder="數量 (股)" style="border: 2px solid #ccc;outline: none;border-radius: 6px;margin-right: 7px;padding: 8px 12px;" required />
            <input v-model.number="form.purchasePrice"  placeholder="買入價格" style="border: 2px solid #ccc;outline: none;border-radius: 6px;margin-right: 7px;padding: 8px 12px;" required />
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
          </div>

          <div
            v-for="item in historyList"
            :key="item.id"
            class="flex flex-col md:grid md:grid-cols-5 gap-2 md:gap-5 px-5 py-3 hover:bg-accent rounded-md"
          >
            <div class="flex justify-between md:block">
              <span class="text-sm md:hidden font-medium text-muted-foreground">Invoice</span>
              <div class="font-medium">
                <div>{{ item.stockSymbol }}</div>
                <div class="text-sm text-muted-foreground">{{ getStockName(item.stockSymbol) }}</div>
              </div>
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
            <Button variant="outline"  @click="removeHolding(item)">Remove</Button>
          </div>
        </div>
      </CardContent>
    </Card>
  </div>
</template>

<style scoped>
.stock-search-container {
  position: relative;
  width: 250px;
  font-family: Arial, sans-serif;
  z-index: 10; /* 確保在上層 */
}

.stock-results-list {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background-color: white;
  border: 2px solid #ccc;
  border-top: none;
  border-radius: 0 0 6px 6px;
  list-style: none;
  margin: 0;
  padding: 0;
  max-height: 250px;
  overflow-y: auto;
  z-index: 9999; /* 提高 z-index 確保在最上層 */
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.stock-result-item {
  padding: 10px 12px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

.stock-result-item:hover {
  background-color: #f5f5f5;
}

.stock-result-item:last-child {
  border-bottom: none;
}

.stock-category {
  float: right;
  font-size: 12px;
  color: #666;
  background: #e8e8e8;
  padding: 2px 8px;
  border-radius: 4px;
}

.no-results {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background-color: white;
  border: 2px solid #ccc;
  border-top: none;
  border-radius: 0 0 6px 6px;
  padding: 12px;
  color: #999;
  text-align: center;
  z-index: 9999; /* 同樣提高 z-index */
}

.form-row {
  display: flex;
  gap: 8px;
  align-items: flex-start;
}

.holding-form {
  width: 100%;
}

.error-message {
  color: #dc2626;
  font-size: 14px;
  margin-top: 8px;
}
</style>
