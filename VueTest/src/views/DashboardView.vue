<script setup lang="ts">
import { computed } from 'vue'
import Card from '@/components/ui/Card.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardContent from '@/components/ui/CardContent.vue'
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

interface Recommendation {
  id: number;
  stockSymbol: string;
  reason: string;
}


const newsList = ref<NewsItem[]>([]);
const loadingNews = ref(false);
const totalAssets = ref(0);
const loadingHistory = ref(false);
const historyList = ref<PortfolioItem[]>([]);
const recommendations = ref<Recommendation[]>([]);

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
      data: [185000, 198000, 192000, 225000, 210000, 234567],
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
            return `Revenue: $${(value / 100).toFixed(0)}k`
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
    const response = await api.get('/api/genimi/recommendations');
    recommendations.value = response.data;
    console.log('取得推薦成功:', response.data);
  } catch (err) {
    console.error('取得推薦失敗:', err);
  }
};

onMounted(() => {
  fetchNews(); // <-- 呼叫新函式
  fetchTotalAssets();
  fetRecommandations();
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

          <div v-else class="space-y-4">
            
            <div v-for="news in newsList" :key="news.id" class="flex items-start gap-3">  
              <div class="w-2 h-2 rounded-full mt-2 bg-blue-600 hover:bg-blue-700"></div>
              <div class="flex-1">
                <p class="text-sm font-medium">
                  <a :href="news.url" target="_blank" rel="noopener noreferrer" class="hover:underline">
                    {{ news.title }}
                  </a>
                </p>
                </div>

            </div> </div> </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>TODAY analysis</CardTitle>
        </CardHeader>
        <CardContent>
          <div class="text-muted-foreground">Coming soon...</div>
        </CardContent>
      </Card>
    </div>
  </div>
</template>
