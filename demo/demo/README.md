# Smart Investment Portfolio Assistant (Spring Boot Demo)

這是一個基於 Spring Boot 的智慧投資組合管理應用程式，整合了 Google Gemini AI 來提供股票分析與建議。

## 專案功能

- **使用者認證 (User Authentication)**:
  - 支援使用者註冊與登入。
  - 使用 JWT (JSON Web Token) 進行安全驗證。
- **投資組合管理 (Portfolio Management)**:
  - 使用者可以新增股票到自己的投資組合。
  - 查看個人的投資組合清單與歷史紀錄。
- **AI 智慧對話 (AI Chat)**:
  - 整合 Google Gemini AI (`gemini-2.5-flash`)。
  - 提供通用的 AI 對話功能。
- **智慧個股分析 (Smart Recommendations)**:
  - 系統會自動讀取使用者投資組合中的股票代碼。
  - 利用 AI 分析該股票的近期新聞、價格走勢及技術指標。
  - 提供簡短的中文分析報告 (50 字內)。

## 技術棧 (Tech Stack)

- **語言**: Java 17
- **框架**: Spring Boot 3.5.7
- **資料庫**: MySQL
- **ORM**: Spring Data JPA
- **安全性**: Spring Security, JWT (JJWT 0.12.3)
- **AI 整合**: Google GenAI SDK (`google-genai`)
- **工具**: Maven, Lombok

## 前置需求 (Prerequisites)

在執行此專案之前，請確保您的環境已安裝：

- Java Development Kit (JDK) 17 或更高版本
- Apache Maven
- MySQL Server

## 安裝與設定 (Setup & Installation)

1.  **複製專案 (Clone Repository)**

    ```bash
    git clone <repository-url>
    cd demo
    ```

2.  **設定資料庫 (Database Setup)**

    - 請在 MySQL 中建立一個名為 `investment_db` 的資料庫。
    - 修改 `src/main/resources/application.properties` 中的資料庫連線資訊：
      ```properties
      spring.datasource.url=jdbc:mysql://localhost:3306/investment_db?useSSL=false&serverTimezone=Asia/Taipei
      spring.datasource.username=root
      spring.datasource.password=您的密碼
      ```

3.  **設定 Gemini API Key**

    - **注意**: 目前 API Key 硬編碼於 `src/main/java/com/example/demo/service/GenimiService.java` 中。
    - 建議您將其替換為您自己的 Google Gemini API Key，或改為從環境變數/設定檔讀取以提高安全性。

4.  **執行專案 (Run Application)**
    ```bash
    mvn spring-boot:run
    ```
    專案啟動後，預設會在 `http://localhost:8080` 運行。

## API 文件 (API Endpoints)

### 認證 (Authentication)

- `POST /api/auth/register`: 註冊新帳號
  - Body: `{ "username": "...", "password": "...", "email": "..." }`
- `POST /api/auth/login`: 登入並取得 JWT Token
  - Body: `{ "username": "...", "password": "..." }`

### 投資組合 (Portfolio)

- `GET /api/portfolio`: 取得我的投資組合
- `POST /api/portfolio`: 新增投資項目
  - Body: `{ "symbol": "2330", "cost": 500, "shares": 1000 }` (範例)
- `GET /api/portfolio/history`: 取得投資組合歷史紀錄

### AI 功能 (Gemini)

- `POST /api/gemini/chat`: 與 AI 進行對話
  - Body: `{ "messages": [ { "role": "user", "content": "你好" } ] }`
- `GET /api/gemini/recommendations`: 取得投資組合的 AI 分析建議
