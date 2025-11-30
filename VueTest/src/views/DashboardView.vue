<script setup lang="ts">
import { computed } from 'vue'
import Card from '@/components/ui/Card.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/Button.vue'
import { Users,  TrendingUp, DollarSign , ChevronDown, ChevronRight} from 'lucide-vue-next'
import { ref, onMounted} from 'vue';
import api from '@/api';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler,
} from 'chart.js'
import '@/assets/index.css'

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
)

interface NewsItem {
  id: number;
  stockSymbol: string;
  title: string;
  url: string;
  createdAt: string;
}

interface PortfolioItem {
  id?: number;
  stockSymbol: string;
  quantity: number;
  purchasePrice: number;
  createdAt?: string;
}

interface RecommendationItem {
  symbol: string;
  reason: string;
}

// 請在此處替換為你的 Fugle API Key
const FUGLE_API_KEY = 'MTFmY2NmNjctNzNiMS00YWFmLTg5ODQtMDMwZjg2OTk3ZWY4IDFiYmRmNTk4LWJjZWUtNDE0NS04MjBlLTVhZmY3MjUxODRkOQ==';

const newsList = ref<NewsItem[]>([]);
const loadingNews = ref(false);
const expandedGroups = ref<Record<string, boolean>>({});
const totalAssets = ref(0);
const loadingHistory = ref(false);
const historyList = ref<PortfolioItem[]>([]);
const recommendations = ref<string[]>([]);
const recommendationItems = ref<RecommendationItem[]>([]);
const isRecommendationsClicked = ref(false);
const priceMap = ref<Record<string, number>>({});





const fetchNews = async () => {
  loadingNews.value = true;
  try {
    const response = await api.get<NewsItem[]>('/api/news');
    newsList.value = response.data;
  } catch (err) {
    console.error('取得新聞失敗:', err);
  } finally {
    loadingNews.value = false;
  }
};

const fetchHistory = async () => {
  loadingHistory.value = true;
  try {
    const response = await api.get<PortfolioItem[]>('/api/portfolio/history');
    historyList.value = response.data ?? [];
  } catch (err) {
    console.error('取得交易歷史失敗:', err);
  } finally {
    loadingHistory.value = false;
  }
  
};

const fetchTotalAssets =  () => {
  loadingHistory.value = true;
  try {
    if (historyList.value.length === 0) {
      totalAssets.value = 0;
      historyList.value = [];
      return;
    }
    totalAssets.value = 0;
    for (let i = 0; i < historyList.value.length; i++) {
      const item = historyList.value[i];
      if (item && typeof item.quantity === 'number' && typeof item.purchasePrice === 'number') {
        totalAssets.value += item.quantity * (priceMap.value[item.stockSymbol] || 0);
        }
    }
  } catch (err) {
    console.error('取得交易歷史失敗:', err);
  } finally {
    loadingHistory.value = false;
  }
  
};


const fetRecommandations = async () => {
  try {
    isRecommendationsClicked.value = true;
    const response = await api.get('/api/gemini/recommendations');
    recommendations.value = response.data ?? [];
    for (let i = 0; i < recommendations.value.length; i++) {
      const rec = recommendations.value[i];
      if (!rec) continue;
      const newlineIndex = rec.indexOf('\n');
      if (newlineIndex !== -1) {
        recommendationItems.value.push({
          symbol: rec.substring(0, newlineIndex).trim(),
          reason: rec.substring(newlineIndex + 1).trim()
        });
      } else {
        // No newline found — store the whole string as symbol and leave reason empty
        recommendationItems.value.push({
          symbol: rec.trim(),
          reason: ''
        });
      }
    }

  } catch (err) {
    console.error('取得推薦失敗:', err);
  }
};

const toggleGroup = (symbol: string) => {
  expandedGroups.value[symbol] = !expandedGroups.value[symbol];
};

// 計算屬性：將新聞列表依 stockSymbol 分組
const groupedNews = computed(() => {
  const groups: Record<string, NewsItem[]> = {};
  if (!newsList.value) return groups;

  newsList.value.forEach(news => {
    // 如果後端沒有回傳 symbol，就歸類為 "Other"
    const symbol = news.stockSymbol || 'Other';
    if (!groups[symbol]) {
      groups[symbol] = [];
    }
    groups[symbol].push(news);
  });
  return groups;
});

const getCurrentPrice = (symbol: string): number => {
  // 直接從 Map 拿，如果找不到就回傳 0



  return priceMap.value[symbol] || 0;
};

const calculateProfitLoss = (item: PortfolioItem): number => {
  const currentPrice = getCurrentPrice(item.stockSymbol);
  // 如果價格還是 0 (還沒抓完)，損益就先顯示 0
  if (currentPrice === 0) return 0;
  return Math.round((currentPrice - item.purchasePrice) * item.quantity);
};




// 獲取昨天的日期（避免使用今天，因為今天可能還沒有收盤資料）
const getYesterday = (number: number) => {
  const date = new Date();
  date.setDate(date.getDate() - number);
  return date.toISOString().split('T')[0];
};

const fetchStockData = async (stockId: string) => {
  // loading.value = true;
  
  try {
    const fromDate = getYesterday(3);
    const toDate = getYesterday(1);
    
    // console.log('請求參數:', { stockId, fromDate, toDate });
    
    const url = `https://api.fugle.tw/marketdata/v1.0/stock/historical/candles/${stockId}`;
    
    const params = new URLSearchParams({
      from: fromDate,
      to: toDate,
      fields: 'close'
    } as Record<string, string>);
    
    const response = await fetch(`${url}?${params}`, {
      method: 'GET',
      headers: {
        'X-API-KEY': FUGLE_API_KEY,
        'Content-Type': 'application/json'
      }
    });
    
    // console.log('API Response Status:', response.status);
    
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      console.error('API 錯誤詳情:', errorData);
      throw new Error(`API 請求失敗: ${response.status} - ${errorData.message || '請檢查 API Key 或股票代號'}`);
    }
    
    const data = await response.json();
    console.log('API 返回資料:', data.data);
    priceMap.value[stockId] = data.data[0]["close"];
    // return data.data || [];
  } catch (err) {
    const errorMessage = err instanceof Error ? err.message : '獲取資料失敗';
    console.error('獲取股價資料錯誤:', errorMessage);
    return [];
  } finally {
    // loading.value = false;
  }
};

const updateAllPrices = async () => {
  if (historyList.value.length === 0) return;

  // 1. 取得所有不重複的股票代碼
  const symbols = [...new Set(historyList.value.map(item => item.stockSymbol))];
  
  console.log(`準備更新 ${symbols.length} 檔股票價格...`);

  // 2. 平行發送請求 (這樣 3 支股票會同時抓，不用等)
  await Promise.all(symbols.map(sym => fetchStockData(sym)));
  
  console.log('所有股價更新完畢');
}

onMounted(async () => {
  fetchNews();
  await fetchHistory(); // 2. 加上 await，程式會暫停在這裡直到資料抓回來
  
  // 3. 確保 historyList 有資料後，這行才會執行
  if (historyList.value.length > 0) {
    await updateAllPrices(); 
    fetchTotalAssets(); // 4. 再次計算總資產
  }
});
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-xl font-bold tracking-tight">Dashboard</h1>
      <p class="text-muted-foreground">Welcome to your new CRM dashboard</p>
    </div>

    <div class="grid gap-3 md:grid-cols-2 lg:grid-cols-3">
      <Card>
        <CardHeader class="flex flex-row items-center justify-between pb-2">
          <CardTitle class="text-sm font-medium">Total portfolio</CardTitle>
          <Users class="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div class="text-2xl font-bold">{{historyList.length}}</div>
          <p class="text-xs text-muted-foreground">+12% from last month</p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader class="flex flex-row items-center justify-between pb-2">
          <CardTitle class="text-sm font-medium">Total assets</CardTitle>
          <TrendingUp class="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div class="text-2xl font-bold">{{totalAssets}}</div>
          <p class="text-xs text-muted-foreground">+23% from last month</p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader class="flex flex-row items-center justify-between pb-2">
          <CardTitle class="text-sm font-medium">Revenue</CardTitle>
          <DollarSign class="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div class="text-2xl font-bold">{{ }} </div>
          <p class="text-xs text-muted-foreground">{{}}</p>
        </CardContent>
      </Card>
    </div>

    <Card>
      <CardHeader>
        <CardTitle>損益</CardTitle>
      </CardHeader>
      <CardContent>
        <div class="space-y-2">
          <div class="hidden md:grid grid-cols-5 gap-5 px-5 py-2 text-sm font-medium text-muted-foreground border-b">
            <div>Stock Symbol</div>
            <div>Quantity</div>
            <div>Purchase Price</div>
            <div>Current Price</div>
            <div>Profit/Loss</div>
          </div>

          <div
            v-for="item in historyList"
            :key="item.id"
            class="flex flex-col md:grid md:grid-cols-5 gap-2 md:gap-5 px-5 py-3 hover:bg-accent rounded-md"
          >
            <div class="flex justify-between md:block">
              <span class="text-sm md:hidden font-medium text-muted-foreground">Stock Symbol</span>
              <div class="font-medium">{{ item.stockSymbol }}</div>
            </div>
            <div class="flex justify-between md:block">
              <span class="text-sm md:hidden font-medium text-muted-foreground">Quantity</span>
              <span>{{ item.quantity }}</span>
            </div>
            <div class="flex justify-between md:block">
              <span class="text-sm md:hidden font-medium text-muted-foreground">Purchase Price</span>
              <span>${{ item.purchasePrice }}</span>
            </div>
            <div class="flex justify-between md:block">
              <span class="text-sm md:hidden font-medium text-muted-foreground">Current Price</span>
              <span>
                ${{getCurrentPrice(item.stockSymbol) }}
              </span>
            </div>
            <div class="flex justify-between md:block">
              <span class="text-sm md:hidden font-medium text-muted-foreground">Profit/Loss</span>
              <span>
                ${{calculateProfitLoss(item) }}
              </span>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>

    <div class="grid gap-4 md:grid-cols-2">
      <Card>
        <CardHeader>
          <CardTitle>新聞</CardTitle>
        </CardHeader>
        <CardContent>
          <div class="space-y-4">
            
            <div v-if="loadingNews" class="text-sm text-muted-foreground">讀取新聞中...</div>
            
            <div v-else-if="newsList.length > 0" class="space-y-2">
              
              <div v-for="(items, symbol) in groupedNews" :key="symbol" class="border rounded-lg overflow-hidden">
                
                <button 
                  @click="toggleGroup(String(symbol))"
                  class="w-full flex items-center justify-between p-3 bg-gray-50 hover:bg-gray-100 transition-colors text-left"
                >
                  <div class="flex items-center gap-2">
                    <component 
                      :is="expandedGroups[String(symbol)] ? ChevronDown : ChevronRight" 
                      class="h-4 w-4 text-muted-foreground"
                    />
                    <span class="font-medium">{{ symbol }}</span>
                    <span class="text-xs text-muted-foreground bg-white px-2 py-0.5 rounded-full border">
                      {{ items.length }}
                    </span>
                  </div>
                </button>

                <div v-if="expandedGroups[String(symbol)]" class="border-t bg-white">
                  <ul class="divide-y">
                    <li v-for="news in items" :key="news.id" class="p-3 hover:bg-gray-50 transition-colors">
                      <a :href="news.url" target="_blank" rel="noopener noreferrer" class="flex items-start gap-3 group">
                        <div class="w-2 h-2 bg-blue-600 rounded-full mt-1.5 shrink-0"></div>
                        <span class="text-sm group-hover:text-blue-600 group-hover:underline transition-colors">
                          {{ news.title }}
                        </span>
                      </a>
                    </li>
                  </ul>
                </div>

              </div>
            </div>

            <p v-else class="text-sm text-muted-foreground">目前沒有新聞。</p>

          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>TODAY analysis</CardTitle>
        </CardHeader>
        <CardContent>
          <Button v-if="!isRecommendationsClicked" @click="fetRecommandations">載入推薦</Button>
          <div v-if="recommendationItems.length === 0 && !isRecommendationsClicked" class="text-muted-foreground">
            點擊上方按鈕以載入推薦。
          </div>
          <div v-else-if="recommendationItems.length === 0 && isRecommendationsClicked" class="text-muted-foreground">
            分析資料載入中
          </div>
          <div v-else class="space-y-4">
            <div v-for="item in recommendationItems" :key="item.symbol" class="p-4 border rounded-lg hover:bg-gray-50 transition-colors">
              <h2 class="text-lg font-semibold">{{ item.symbol }}</h2>
              <p class="text-sm text-gray-600 mt-2">{{ item.reason }}</p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  </div>
</template>


