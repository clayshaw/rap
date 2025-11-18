<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import StockChart from '@/components/StockChart.vue';
import stocksData from '@/assets/stock.json';
import Card from '@/components/ui/Card.vue';
import CardContent from '@/components/ui/CardContent.vue';
import CardHeader from '@/components/ui/CardHeader.vue';

// 定義股票資料的型別
interface Stock {
  id: string;
  name: string;
  industry_category: string;
}

// 狀態
const searchQuery = ref('');
const selectedStock = ref<Stock | null>(null);
const currentSymbol = ref('2330'); // 預設台積電
const currentMarket = ref('TWSE');

// 判斷市場別
const getMarket = (stock: Stock) => {
  if (stock.industry_category.includes('上櫃') || stock.id === 'TPEx') {
    return 'TPEX';
  }
  return 'TWSE';
};

// 搜尋過濾
const filteredStocks = computed(() => {
  const query = searchQuery.value.toLowerCase();
  let result = stocksData as Stock[];
  
  if (query) {
    result = result.filter(s => 
      s.id.includes(query) || 
      s.name.includes(query) ||
      s.industry_category.includes(query)
    );
  }
  // 限制顯示筆數，優化效能
  return result.slice(0, 100);
});

// 點擊股票
const selectStock = (stock: Stock) => {
  selectedStock.value = stock;
  currentSymbol.value = stock.id;
  currentMarket.value = getMarket(stock);
};

// 初始化
onMounted(() => {
  const initial = (stocksData as Stock[]).find(s => s.id === '2330');
  if (initial) selectStock(initial);
});
</script>

<template>
  <Card class="flex h-[calc(100vh-4rem)] bg-gray-100">
    
    <div class="w-1/3 md:w-1/4 bg-white border-r border-gray-200 flex flex-col">
      <div class="p-4 border-b border-gray-200">
        <h2 class="text-xl font-bold mb-4 text-gray-800">股票列表</h2>
        <input 
          v-model="searchQuery" 
          type="text" 
          placeholder="搜尋代號或名稱..." 
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>

      <div class="flex-1 overflow-y-auto p-2 space-y-2">
        <div 
          v-for="stock in filteredStocks" 
          :key="stock.id"
          @click="selectStock(stock)"
          class="p-3 rounded-lg cursor-pointer transition-colors duration-200 flex justify-between items-center"
          :class="currentSymbol === stock.id ? 'bg-blue-100 border-l-4 border-blue-500' : 'hover:bg-gray-50'"
        >
          <div>
            <div class="font-bold text-gray-900">{{ stock.name }}</div>
            <div class="text-sm text-gray-500">{{ stock.id }} | {{ stock.industry_category }}</div>
          </div>
          <span v-if="currentSymbol === stock.id" class="text-blue-600 font-bold">&gt;</span>
        </div>
        
        <div v-if="filteredStocks.length === 0" class="text-center text-gray-500 mt-10">
          找不到符合的股票
        </div>
      </div>
    </div>
    <Card class="flex-1 p-4 flex flex-col">
      <CardHeader class="mb-4 flex justify-between items-center">
        <h1 class="text-2xl font-bold text-gray-800">
          {{ selectedStock?.name || '台積電' }} 
          <span class="text-lg text-gray-500 font-normal">({{ currentSymbol }})</span>
        </h1>
        <div class="text-sm text-gray-500">
          資料來源: TradingView
        </div>
      </CardHeader>
      
      <CardContent class="flex-1 relative bg-white shadow-lg rounded-lg overflow-hidden">
        <StockChart :symbol="currentSymbol" :market="currentMarket" />
      </CardContent>
    </Card>

  </Card>
</template>