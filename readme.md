### 1. Vue 3 前端 (Frontend)
 
 # 📈 Investment Portfolio Management System - Frontend

這是一個基於 Vue 3 開發的投資理財管理系統前端介面。整合了即時股價圖表、投資組合管理、個人化新聞閱讀以及 AI 投資顧問功能。

## 🛠 技術棧 (Tech Stack)

* **核心框架:** [Vue 3](https://vuejs.org/) (Composition API)
* **建構工具:** [Vite](https://vitejs.dev/)
* **語言:** [TypeScript](https://www.typescriptlang.org/)
* **狀態管理:** [Pinia](https://pinia.vuejs.org/)
* **路由管理:** [Vue Router](https://router.vuejs.org/)
* **樣式與 UI:**
    * [Tailwind CSS](https://tailwindcss.com/)
    * Radix Vue / Lucide Vue (Icons)
* **圖表視覺化:**
    * [Lightweight Charts](https://tradingview.github.io/lightweight-charts/) (K線圖、成交量)
    * Chart.js / Vue-Chartjs
* **API 請求:** Axios
* **測試:** Vitest, Playwright

## 🚀 功能特色 (Features)

* **🔐 用戶認證:** 完整的登入/註冊流程 (JWT 驗證)。
* **📊 股市儀表板:** 整合 TradingView 風格的互動式 K 線圖，支援台股上市櫃股票搜尋。
* **💼 投資組合:** 管理個人持股配置與損益概況。
* **🤖 AI 智能投顧:** 內建 Gemini AI 聊天室，提供投資建議與市場分析。
* **📰 財經新聞:** 根據個人持股自動聚合相關新聞。

## 📦 安裝與執行 (Installation)

### 1. 環境需求
* Node.js (建議 v20.19.0 或 >=22.12.0)

### 2. 安裝依賴

    npm install

### 3. 啟動開發伺服器
 
    npm run dev

### 4. 建置生產版本

    npm run build

## ⚙️設定說明(Configuration)
本專案依賴兩個後端服務，請確保以下服務已啟動：

### 1. Python Data Service (Port 5000): 用於提供 K 線圖歷史數據。

    設定位置: src/views/StockListView.vue (預設 http://127.0.0.1:5000)

### 2. Spring Boot Backend (Port 8080): 用於處理使用者資料、認證與 AI 對話。

    設定位置: src/api/index.ts 或相關 axios 設定 (預設 http://localhost:8080)
請根據實際情況修改 API 端點。

## 📂 專案結構
* src/views: 頁面組件 (Dashboard, StockList, Chat, Login 等)
* src/components: 共用 UI 元件 (Card, Button 等)
* src/stores: Pinia 狀態管理
* src/router: 路由定義

### 2. Spring Boot 後端 (Backend)


# ☕ Investment System Backend API

這是投資管理系統的核心後端服務，基於 Spring Boot 構建。提供使用者認證、資料庫存取操作、新聞聚合 API 以及 Google Gemini AI 的整合服務。

## 🛠 技術棧 (Tech Stack)

* **框架:** Spring Boot 3.5.7
* **語言:** Java 17
* **資料庫:** MySQL
* **ORM:** Spring Data JPA (Hibernate)
* **安全性:** Spring Security, JWT (JSON Web Token)
* **AI 模型整合:**
    * Google GenAI SDK
    * OpenAI Java SDK

## 🚀 主要 API 功能 (API Endpoints)

* **Authentication:** 處理使用者註冊與登入 (JWT)。
* **Portfolio Service:** 管理使用者的股票投資組合 (`PortfolioController`)。
* **News Service:**
    * `GET /api/news`: 根據使用者目前的持股代碼，自動撈取資料庫中的相關新聞。
* **Gemini AI Service:**
    * `POST /api/gemini/chat`: 與 AI 進行對話。
    * `GET /api/gemini/recommendations`: 分析使用者持股的新聞與技術指標，生成簡短投資建議。

## 📦 安裝與執行 (Getting Started)

### 1. 資料庫設定
請先在 MySQL 中建立一個名為 `investment_db` 的資料庫。

修改 `src/main/resources/application.properties`：

#### 資料庫連線設定
* **spring.datasource.url=**
* **spring.datasource.username=root**
* **spring.datasource.password=你的密碼**

### 2.執行應用程式
使用 Maven 進行建置與執行：

    # Windows
    mvn clean install

    # Linux/Mac
    mvn spring-boot:run
服務預設運行於： http://0.0.0.0:8080

## 🏗️ 資料庫結構 (Database)
系統啟動時，Hibernate 會自動根據 Entity 建立或更新資料表：
* **users:** 使用者資料表
* **portfolios:** 投資組合資料表
* **news:** 財經新聞資料表
### ⚠️ 注意事項
* **請確保 application.properties 中的 MySQL 密碼已修正為您本機的設定。**
* **AI 功能需要設定相應的 API Key (於環境變數或 Service 中設定)。**


### 3. Python 資料服務與爬蟲 (Data Service & Crawler)


# 🐍 Stock Data Service & News Crawler

這是一個 Python 模組，包含 Flask API 服務與排程爬蟲腳本。負責抓取台灣股市歷史數據，以及從網路上爬取最新的個股新聞存入資料庫。

## 🛠 技術棧 (Tech Stack)

* **Web 框架:** Flask (搭配 Flask-CORS)
* **金融數據:** yfinance
* **爬蟲工具:** Requests, BeautifulSoup4
* **資料處理:** Pandas, NumPy
* **資料庫:** MySQL Connector Python

## 📂 檔案功能說明

1.  **`app.py` (API Server):**
    * 啟動一個 Flask 伺服器 (Port 5000)。
    * 提供 `POST /api/getStockData` 接口，接收股票代號，回傳歷史股價數據供前端繪圖。

2.  **`spider.py` (yfinance 爬蟲):**
    * 讀取資料庫中 `portfolios` 表的所有股票代號。
    * 利用 `yfinance` 抓取新聞並存入 `news` 資料表。

3.  **`use_request_spider.py` (Google News 爬蟲):**
    * 針對資料庫中的持股代碼，爬取 Google News 的搜尋結果。
    * 解析 HTML 並將標題、連結與時間存入 MySQL。

## 📦 安裝與執行 (Installation)

### 1. 安裝 Python 套件

    pip install flask flask-cors pandas requests beautifulsoup4 mysql-connector-python yfinance


### 2.資料庫設定
請確保 spider.py 和 use_request_spider.py 中的 DB_CONFIG 與你的 MySQL 設定一致：
```python
DB_CONFIG = {
    'host': 'localhost',
    'database': 'investment_db',
    'user': 'root',
    'password': '你的密碼' 
}
```

### 3.啟動 Flask 伺服器

    python app.py
Server: http://localhost:5000

### 4.執行爬蟲腳本
    # 使用 yfinance 爬取新聞
    python spider.py

    # 使用 Requests 爬取 Google News
    python use_request_spider.py
## ⚙️ 注意事項 (Notes)
* 請確保 MySQL 服務已啟動，且資料庫 `investment_db` 已建立。
* 爬蟲腳本可設定為排程任務 (如 cron job)
* 以定期更新新聞資料。
* 爬蟲腳本使用了 INSERT IGNORE 語法，避免重複寫入相同的新聞資料。
* API 服務僅提供股票歷史數據，請確保前端呼叫的 URL 正確。
* 請注意爬蟲頻率，避免被目標網站封鎖 IP。