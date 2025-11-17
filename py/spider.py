import yfinance as yf
import mysql.connector
from mysql.connector import Error
import datetime

# (*** 1. 你的資料庫連線設定 ***)
DB_CONFIG = {
    'host': 'localhost',
    'database': 'investment_db',
    'user': 'root',
    'password': 'qweasdzxc123' # (*** 請改成你的密碼 ***)
}


def get_all_unique_symbols():
    """
    從 portfolios 表中讀取所有不重複的股票代號
    """
    symbols = set() # 使用 set 來自動處理重複
    connection = None
    try:
        connection = mysql.connector.connect(**DB_CONFIG)
        cursor = connection.cursor()

        # (*** 關鍵查詢 ***)
        query = "SELECT DISTINCT stock_symbol FROM portfolios"
        cursor.execute(query)

        for (stock_symbol,) in cursor:
            symbols.add(stock_symbol)

        print(f"資料庫中找到的股票代號: {symbols}")
        return list(symbols)

    except Error as e:
        print(f"讀取股票代號時發生錯誤: {e}")
        return [] # 返回空列表
    finally:
        if connection and connection.is_connected():
            cursor.close()
            connection.close()

def fetch_news_for_symbol(symbol):
    """
    抓取 "單一" 股票的新聞
    """
    po = yf.Ticker(symbol)
    extracted_news = []
    news = [news_item for news_item in po.news]

    for item in news:
        content = item.get('content',{'No content available'})
        
        title = content.get('title', {'No title available'})
        time_str = content.get('pubDate', {'No time available'})

        url = content.get('clickThroughUrl',{'No link available'})
        dt_object = str(time_str).replace('T', ' ').replace('Z', '')

        if url:
            url = url.get('url', {'No link available'})
            extracted_news.append({
                'createdAt' : dt_object,
                'stockSymbol': symbol, 
                'title': title, 
                'url': url
            })
        print
    return extracted_news

def save_news_to_db(news_list):
    """
    將新聞列表寫入 MySQL
    """
    connection = None
    try:
        connection = mysql.connector.connect(**DB_CONFIG)
        cursor = connection.cursor()

        # (*** 關鍵修改 ***)
        # 1. "INSERT IGNORE" 會自動跳過 "重複" (unique 鍵衝突) 的資料
        # 2. 我們現在寫入 "stock_symbol", "title", "url"
        query = "INSERT IGNORE INTO news (created_at,stock_symbol, title, url) VALUES (%s,%s, %s, %s)"

        data_to_insert = [(item['createdAt'],item['stockSymbol'], item['title'], item['url']) for item in news_list]

        if data_to_insert:
            cursor.executemany(query, data_to_insert)
            connection.commit()
            print(f"成功新增或忽略了 {cursor.rowcount} 筆新聞。")
        else:
            print("沒有可新增的新聞。")

    except Error as e:
        print(f"寫入新聞時發生錯誤: {e}")
    finally:
        if connection and connection.is_connected():
            cursor.close()
            connection.close()


# (*** 程式主進入點 ***)
if __name__ == "__main__":
    print("--- 開始執行個人化新聞爬蟲 ---")

    # 1. 取得所有股票
    symbols_to_fetch = get_all_unique_symbols()

    if not symbols_to_fetch:
        print("資料庫中沒有任何持股，爬蟲結束。")
    else:
        all_news = []
        # 2. 為每支股票抓新聞
        for symbol in symbols_to_fetch:
            print(f"正在爬取 {symbol} 的新聞...")
            news_data = fetch_news_for_symbol(symbol)
            all_news.extend(news_data)

        # 3. 一次性全部存入資料庫
        if all_news:
            print(f"爬取完成，共 {len(all_news)} 筆新聞。正在寫入資料庫...")
            save_news_to_db(all_news)
        else:
            print("所有股票都沒有爬取到任何新新聞。")

    print("--- 爬蟲執行完畢 ---")