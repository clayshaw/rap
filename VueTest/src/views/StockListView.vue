<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
// 注意這裡新增了 ColorType, CandlestickSeries, HistogramSeries 的引入
import {
  createChart,
  type IChartApi,
  type ISeriesApi,
  ColorType,
  CandlestickSeries,
  HistogramSeries,
} from 'lightweight-charts'
import stocksData from '@/assets/stock.json'
import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import CardHeader from '@/components/ui/CardHeader.vue'

// 定義股票資料的型別
interface Stock {
  id: string
  name: string
  industry_category: string
}

interface CandleData {
  date: string
  open: number
  high: number
  low: number
  close: number
  volume: number
}

// 請在此處替換為你的 Fugle API Key
const FUGLE_API_KEY =
  'MTFmY2NmNjctNzNiMS00YWFmLTg5ODQtMDMwZjg2OTk3ZWY4IDFiYmRmNTk4LWJjZWUtNDE0NS04MjBlLTVhZmY3MjUxODRkOQ=='

// 狀態
const searchQuery = ref('')
const selectedStock = ref<Stock | null>(null)
const currentSymbol = ref('2330')
const currentMarket = ref('TWSE')
const chartContainer = ref<HTMLElement | null>(null)
const loading = ref(false)
const error = ref('')

let chart: IChartApi | null = null
// 修正 1: 指定正確的 Series 型別，解決 ESLint 'any' 錯誤
let candlestickSeries: ISeriesApi<'Candlestick'> | null = null
let volumeSeries: ISeriesApi<'Histogram'> | null = null
let resizeObserver: ResizeObserver | null = null

// 判斷市場別
const getMarket = (stock: Stock) => {
  if (stock.industry_category.includes('上櫃') || stock.id === 'TPEx') {
    return 'TPEX'
  }
  return 'TWSE'
}

// 搜尋過濾
const filteredStocks = computed(() => {
  const query = searchQuery.value.toLowerCase()
  let result = stocksData as Stock[]

  if (query) {
    result = result.filter(
      (s) => s.id.includes(query) || s.name.includes(query) || s.industry_category.includes(query),
    )
  }

  return result.slice(0, 100)
})

// 獲取一年前的日期
const getOneYearAgo = () => {
  const date = new Date()
  date.setFullYear(date.getFullYear() - 1)
  const today = new Date()
  return date > today ? today.toISOString().split('T')[0] : date.toISOString().split('T')[0]
}

// 獲取昨天的日期
const getYesterday = () => {
  const date = new Date()
  date.setDate(date.getDate() - 1)
  return date.toISOString().split('T')[0]
}

// 從 Fugle API 獲取歷史股價資料
const fetchStockData = async (stockId: string) => {
  loading.value = true
  error.value = ''

  try {
    const fromDate = getOneYearAgo()
    const toDate = getYesterday()

    const url = `https://api.fugle.tw/marketdata/v1.0/stock/historical/candles/${stockId}`

    const params = new URLSearchParams({
      from: fromDate,
      to: toDate,
      fields: 'open,high,low,close,volume',
    } as Record<string, string>)

    const response = await fetch(`${url}?${params}`, {
      method: 'GET',
      headers: {
        'X-API-KEY': FUGLE_API_KEY,
        'Content-Type': 'application/json',
      },
    })

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
        // 清除舊圖表
      if (chart) {
        chart.remove()
        chart = null
      }
      if (resizeObserver) {
        resizeObserver.disconnect()
        resizeObserver = null
      }
      throw new Error(
        `API 請求失敗: ${response.status} - ${errorData.message || '請檢查 API Key 或股票代號'}`,
      )
    }

    const data = await response.json()
    // console.log('獲取的股價資料:', data)
    return data.data || []
  } catch (err) {
    error.value = err instanceof Error ? err.message : '獲取資料失敗'
    console.error('獲取股價資料錯誤:', err)
    return []
  } finally {
    loading.value = false
  }
}

// 初始化圖表
const initChart = () => {
  // 這裡有第一層檢查，但 TS 在 callback 或後面的流程中可能會丟失型別保護
  if (!chartContainer.value) return

  // 清除舊圖表
  if (chart) {
    chart.remove()
    chart = null
  }
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }

  chart = createChart(chartContainer.value, {
    layout: {
      background: { type: ColorType.Solid, color: '#ffffff' },
      textColor: '#333',
    },
    grid: {
      vertLines: { color: '#f0f0f0' },
      horzLines: { color: '#f0f0f0' },
    },
    width: chartContainer.value.clientWidth,
    height: chartContainer.value.clientHeight,
    timeScale: {
      timeVisible: true,
      secondsVisible: false,
    },
    rightPriceScale: {
      borderColor: '#e0e0e0',
    },
    crosshair: {
      mode: 1,
    },
  })

  // 修正 2: v5 使用 addSeries(CandlestickSeries, options)
  candlestickSeries = chart.addSeries(CandlestickSeries, {
    upColor: '#ef5350',
    downColor: '#26a69a',
    borderUpColor: '#ef5350',
    borderDownColor: '#26a69a',
    wickUpColor: '#ef5350',
    wickDownColor: '#26a69a',
  })

  // 修正 3: v5 使用 addSeries(HistogramSeries, options)
  volumeSeries = chart.addSeries(HistogramSeries, {
    color: '#26a69a',
    priceFormat: {
      type: 'volume',
    },
    priceScaleId: 'volume',
  })

  chart.priceScale('volume').applyOptions({
    scaleMargins: {
      top: 0.8,
      bottom: 0,
    },
  })

  resizeObserver = new ResizeObserver((entries) => {
    if (entries.length === 0 || !chart || !chartContainer.value) return
    if(entries[0]){
      const newRect = entries[0].contentRect
      chart.applyOptions({
        width: newRect.width,
        height: newRect.height,
      })
    }
  })

  // 修正 4: 加上非空檢查，解決 'Object is possibly undefined'
  if (chartContainer.value) {
    resizeObserver.observe(chartContainer.value)
  }
}

// 更新圖表資料
const updateChart = async (stockId: string) => {
  const rawData = await fetchStockData(stockId)

  if (!rawData || rawData.length === 0) {
    error.value = '無可用的股價資料'
    return
  }

  const sorted = [...rawData].sort(
    (a, b) => new Date(a.date).getTime() - new Date(b.date).getTime(),
  )

  const candleData = sorted.map((item: CandleData) => ({
    time: item.date,
    open: item.open,
    high: item.high,
    low: item.low,
    close: item.close,
  }))

  const volumeData = sorted.map((item: CandleData) => ({
    time: item.date,
    value: item.volume,
    color: item.close >= item.open ? '#ef535080' : '#26a69a80',
  }))

  if (candlestickSeries && volumeSeries) {
    candlestickSeries.setData(candleData)
    volumeSeries.setData(volumeData)

    if (chart) {
      chart.timeScale().fitContent()
    }
  }
}

// 點擊股票
const selectStock = async (stock: Stock) => {
  selectedStock.value = stock
  currentSymbol.value = stock.id
  currentMarket.value = getMarket(stock)

  await nextTick()

  if (!chart) {
    initChart()
  }

  await updateChart(stock.id)
}

// 監聽圖表容器
watch(chartContainer, (newVal) => {
  if (newVal && !chart) {
    initChart()
    if (currentSymbol.value) {
      updateChart(currentSymbol.value)
    }
  }
})

// 修正: 記得加入 onUnmounted 清理資源
onUnmounted(() => {
  if (chart) {
    chart.remove()
    chart = null
  }
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
})

// 初始化
onMounted(async () => {
  // const initial = (stocksData as Stock[]).find((s) => s.id === '2330')
  // if (initial) {
  //   await selectStock(initial)
  // }
})
</script>

<template>
  <Card class="flex h-[calc(100vh-4rem)] bg-gray-100">
    <CardContent class="w-1/3 md:w-1/4 bg-white border-r border-gray-200 flex flex-col">
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
          :class="
            currentSymbol === stock.id
              ? 'bg-blue-100 border-l-4 border-blue-500'
              : 'hover:bg-gray-50'
          "
        >
          <div>
            <div class="font-bold text-gray-900">{{ stock.name }}</div>
            <div class="text-sm text-gray-500">{{ stock.id }} | {{ stock.industry_category }}</div>
          </div>
          <span v-if="currentSymbol === stock.id" class="text-blue-600 font-bold">&gt;</span>
        </div>

        <div v-if="filteredStocks.length === 0" class="text-center text-gray-500 mt-10">
          找不到符合的股票(可能已下市)
        </div>
      </div>
    </CardContent>

    <CardContent class="flex-1 p-4 flex flex-col">
      <CardHeader class="mb-4 flex justify-between items-center">
        <h1 class="text-2xl font-bold text-gray-800">
          {{ selectedStock?.name || '台積電' }}
          <span class="text-lg text-gray-500 font-normal">({{ currentSymbol }})</span>
        </h1>
      </CardHeader>

      <CardContent class="flex-1 relative bg-white shadow-lg rounded-lg overflow-hidden">
        <!-- Loading 狀態 -->
        <div
          v-if="loading"
          class="absolute inset-0 flex items-center justify-center bg-white bg-opacity-75 z-10"
        >
          <div class="text-center">
            <div
              class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto mb-4"
            ></div>
            <p class="text-gray-600">載入中...</p>
          </div>
        </div>

        <!-- Error 狀態 -->
        <div v-if="error && !loading" class="absolute inset-0 flex items-center justify-center">
          <div class="text-center text-red-500">
            <p class="text-xl mb-2">⚠️</p>
            <p>{{ error }}</p>
            <p class="text-sm text-gray-500 mt-2">請確認 API Key 是否正確設定</p>
          </div>
        </div>

        <!-- 圖表容器 -->
        <div ref="chartContainer" class="w-full h-full"></div>
      </CardContent>
    </CardContent>
  </Card>
</template>
