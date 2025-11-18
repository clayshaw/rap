<script setup lang="ts">
import { onMounted, ref, watch, nextTick } from 'vue';

const props = defineProps<{
  symbol: string;
  market?: string;
}>();

const containerId = 'tradingview_widget_container';
const widgetContainer = ref<HTMLElement | null>(null);
// 用來追蹤腳本載入狀態的變數
let tvScriptLoadingPromise: Promise<void> | null = null;

// 核心函式：確保 TradingView 腳本已完全載入
const loadTVScript = (): Promise<void> => {
  // 1. 如果全域物件已經存在，直接回傳成功
  if (window.TradingView) {
    return Promise.resolve();
  }
  
  // 2. 如果正在載入中，回傳同一個 Promise，避免重複下載
  if (tvScriptLoadingPromise) {
    return tvScriptLoadingPromise;
  }

  tvScriptLoadingPromise = new Promise((resolve, reject) => {
    // 3. 檢查是否 <script> 標籤已經在 head 裡 (可能由其他頁面加入的)
    if (document.getElementById('tv-widget-script')) {
      // 標籤存在但 window.TradingView 還沒好，啟動輪詢檢查 (每 100ms 檢查一次)
       const checkInterval = setInterval(() => {
        if (window.TradingView) {
          clearInterval(checkInterval);
          resolve();
        }
      }, 100);
      // 設定 10 秒超時，避免無限等待
      setTimeout(() => {
        clearInterval(checkInterval);
        // 即使超時，如果物件存在也算成功
        if (window.TradingView) resolve(); 
      }, 10000);
      return;
    }

    // 4. 如果完全沒標籤，則建立新標籤
    const script = document.createElement('script');
    script.id = 'tv-widget-script';
    script.src = 'https://s3.tradingview.com/tv.js';
    script.async = true;
    script.onload = () => {
      if (window.TradingView) {
        resolve();
      } else {
        // 極少見情況：腳本載入完但物件不存在
        reject(new Error('TradingView script loaded but object not found'));
      }
    };
    script.onerror = (err) => reject(err);
    document.head.appendChild(script);
  });
  
  return tvScriptLoadingPromise;
};

const initWidget = async () => {
  try {
    // 等待 DOM 更新，確保 div 已經存在
    await nextTick();
    if (!widgetContainer.value) return;

    // 等待腳本載入完成
    await loadTVScript();
    
    // 雙重確認 TradingView 物件存在
    if (!window.TradingView || !window.TradingView.widget) {
      console.error('TradingView library is not ready.');
      return;
    }

    // 清空舊內容，避免圖表重複堆疊
    widgetContainer.value.innerHTML = '';

    // 初始化圖表
    new window.TradingView.widget({
      "width": "100%",
      "height": "100%",
      "symbol": `${props.market || 'TWSE'}:${props.symbol}`,
      "interval": "D",
      "timezone": "Asia/Taipei",
      "theme": "light",
      "style": "1",
      "locale": "zh_TW",
      "toolbar_bg": "#f1f3f6",
      "enable_publishing": false,
      "allow_symbol_change": true,
      "container_id": containerId
    });
  } catch (e) {
    console.error('Failed to initialize TradingView widget:', e);
  }
};

// 監聽股票代號變更
watch(() => props.symbol, () => {
  initWidget();
});

onMounted(() => {
  initWidget();
});
</script>

<template>
  <div class="w-full h-full bg-white rounded-lg shadow-md overflow-hidden">
    <div :id="containerId" ref="widgetContainer" class="w-full h-full min-h-[500px]"></div>
  </div>
</template>

<script lang="ts">
// 宣告全域型別，避免 TypeScript 報錯
declare global {
  interface Window {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    TradingView: any;
  }
}
</script>