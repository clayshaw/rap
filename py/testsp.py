import yfinance as yf


# 設定要查詢的股票代號（含市場識別碼）
stock_list = ['2330.TW', 'AAPL', '0005.HK']
stock_str = ' '.join(stock_list)

# 批次取得資料，只發出一次 request
tickers = yf.Tickers(stock_str)

# 建立最終合併後的 DataFrame


for symbol in tickers.symbols:
    ticker = tickers.tickers[symbol]
    with open(f'{symbol}_news.txt', 'w', encoding='utf-8') as f:
        f.write(f"新聞列表 for {symbol}:\n")
        news = [news_item for news_item in ticker.news]
        for item in news:
            content = item.get('content',{'No content available'})
            
            title = content.get('title', {'No title available'})
            time_str = content.get('pubDate', {'No time available'})

            url = content.get('clickThroughUrl',{'No link available'})
            if url:
                url = url.get('url', {'No link available'})
            dt_object = str(time_str).replace('T', ' ').replace('Z', '')

            if url:
                f.write(f"標題: {title}\n")
                f.write(f"時間: {dt_object}\n")
                f.write(f"連結: {url}\n")
                f.write('\n')

    print('\n\n\n')
