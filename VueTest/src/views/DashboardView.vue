<script setup lang="ts">
import { computed } from 'vue'
import Card from '@/components/ui/Card.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/Button.vue'
import { Users,  TrendingUp, DollarSign } from 'lucide-vue-next'
import { Line } from 'vue-chartjs'
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
  type ChartData,
  type ChartOptions
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



const newsList = ref<NewsItem[]>([]);
const loadingNews = ref(false);

// 計算按股票代碼分組的新聞
const groupedNews = computed(() => {
  const groups: Record<string, NewsItem[]> = {};
  newsList.value.forEach(news => {
    const key = news.stockSymbol ?? 'unknown';
    groups[key] = groups[key] ?? [];
    groups[key].push(news);
  });
  return groups;
});

const totalAssets = ref(0);
const loadingHistory = ref(false);
const historyList = ref<PortfolioItem[]>([]);
const recommendations = ref<string[]>([]);
const recommendationItems = ref<RecommendationItem[]>([]);
const isRecommendationsClicked = ref(false);

const fetchNews = async () => {
  loadingNews.value = true;
  try {
    const response = await api.get<NewsItem[]>('/api/news');
    console.log('取得新聞成功:', response.data);
    newsList.value = response.data;
  } catch (err) {
    console.error('取得新聞失敗:', err);
  } finally {
    loadingNews.value = false;
  }
};

const fetchTotalAssets = async () => {
  loadingHistory.value = true;
  try {
    const response = await api.get<PortfolioItem[]>('/api/portfolio/history');
    const data = response.data ?? [];
    if (data.length === 0) {
      totalAssets.value = 0;
      historyList.value = [];
      return;
    }
    totalAssets.value = 0;
    for (let i = 0; i < data.length; i++) {
      const item = data[i];
      if (item && typeof item.quantity === 'number' && typeof item.purchasePrice === 'number') {
        totalAssets.value += item.quantity * item.purchasePrice;
      }
    }
    historyList.value = data;
  } catch (err) {
    console.error('取得交易歷史失敗:', err);
  } finally {
    loadingHistory.value = false;
  }
  
};

const revenueData = computed<ChartData<'line'>>(() => ({
  labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
  datasets: [
    {
      label: 'Revenue',
      data: [3000, 5000, 4000, 7000, 6000, 8000],
      borderColor: 'rgb(59, 130, 246)',
      backgroundColor: 'rgba(59, 130, 246, 0.1)',
      tension: 0.4,
      fill: true
    }
  ]
}))

const revenueOptions = computed<ChartOptions<'line'>>(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: false
    },
    tooltip: {
      callbacks: {
        label: (context) => {
          const value = context.parsed.y
          if(value != null){
            return `Revenue: $${(value / 1000).toFixed(0)}k`
          }
        }
      }
    }
  },
  scales: {
    y: {
      beginAtZero: true,
      ticks: {
        callback: (value) => `$${(Number(value) / 1000).toFixed(0)}k`
      }
    }
  }
}))

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

onMounted(() => {
  fetchNews();
  fetchTotalAssets();
  // fetRecommandations();
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
        <CardTitle>累計資產</CardTitle>
      </CardHeader>
      <CardContent>
        <div class="h-[300px]">
          <Line :data="revenueData" :options="revenueOptions" />
        </div>
      </CardContent>
    </Card>

    <div class="grid gap-4 md:grid-cols-2">
      <Card>
        <CardHeader>
          <CardTitle>新聞</CardTitle>
        </CardHeader>
        <CardContent>
          
          <div v-if="loadingNews" class="text-muted-foreground">
            讀取新聞中...
          </div>

          <div v-else-if="newsList.length === 0" class="text-muted-foreground">
            目前沒有新聞。
          </div>

          <div v-else class="space-y-6">
            <!-- 按股票代碼分組顯示 -->
            <div v-for="(newsItems, stockSymbol) in groupedNews" :key="stockSymbol" class="space-y-3">
              <h3 class="text-sm font-semibold text-blue-600 border-b pb-2">{{ stockSymbol }}</h3>
              <div class="space-y-3">
                <div v-for="news in newsItems" :key="news.id" class="flex items-start gap-3 pl-2">  
                  <div class="w-2 h-2 rounded-full mt-2 bg-blue-600 hover:bg-blue-700"></div>
                  <div class="flex-1">
                    <p class="text-sm font-medium">
                      <a :href="news.url" target="_blank" rel="noopener noreferrer" class="hover:underline">
                        {{ news.title }}
                      </a>
                    </p>
                    <p class="text-xs text-muted-foreground mt-1">
                      {{ new Date(news.createdAt).toLocaleString('zh-TW', { 
                        month: 'short', 
                        day: 'numeric', 
                        hour: '2-digit', 
                        minute: '2-digit' 
                      }) }}
                    </p>
                  </div>

                </div>
              </div>
            </div>
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