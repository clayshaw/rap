import yfinance as yf


# 設定要查詢的股票代號（含市場識別碼）

# 批次取得資料，只發出一次 request
tickers = yf.Ticker('0058.TW')

# 建立最終合併後的 DataFrame


print(tickers.history(period ="1mo"))