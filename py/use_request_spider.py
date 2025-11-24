import requests
from bs4 import BeautifulSoup
import pandas as pd
import mysql.connector
from mysql.connector import Error
import  datetime
from time import sleep

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
        query = "SELECT DISTINCT stock_symbol FROM portfolios "
        # delete_news = "DELETE FROM investment_db.news"
        cursor.execute(query)
        # cursor.execute(delete_news)
        # cursor.commit() # 提交事务

        for (stock_symbol,) in cursor:
            symbols.add(stock_symbol)  # 假設所有股票都是台灣股票，添加 ".TW" 後綴

        print(f"資料庫中找到的股票代號: {symbols}")
        return list(symbols)

    except Error as e:
        print(f"讀取股票代號時發生錯誤: {e}")
        return [] # 返回空列表
    finally:
        if connection and connection.is_connected():
            cursor.close()
            connection.close()

def fetch_news_for_symbol(symbols):
    base_url = "https://news.google.com/search"
    
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)...'
    }
    news_data = []
    for symbol in symbols:
        params = {
            'q': symbol+'股票新聞',
            'hl': 'zh-TW',
            'gl': 'TW',
            'ceid': 'TW:zh-Hant'
        }
        response = requests.get(base_url, params=params, headers=headers)
        html = response.text
        soup = BeautifulSoup(html, 'html.parser')
        articles = soup.find_all('a','JtKRv')
        
        cnt = 0
        for art in articles:
            if(cnt==10):
                break
            title = art.get_text() if art else None
            link = art['href'] if art else ''
            if link.startswith('./'):
                link = 'https://news.google.com' + link[1:]
            time_text = datetime.datetime.now().isoformat(sep=' ')
            news_data.append({
                "stockSymbol":symbol,
                "title": title,
                "createdAt": time_text,
                "url": link,
            })
            cnt+=1
        sleep(1)
        # df = pd.DataFrame(news_data)
        # df.to_json(f"google_news_results_{symbol}.json",orient='records')
    return news_data

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

if __name__ == "__main__":
    
    symbols_to_fetch = get_all_unique_symbols()
    if not symbols_to_fetch:
        print("資料庫中沒有任何持股，爬蟲結束。")
    else:
        all_news = []
        # 2. 為每支股票抓新聞

        news_data = fetch_news_for_symbol(symbols_to_fetch)
        all_news.extend(news_data)

        # 3. 一次性全部存入資料庫
        if all_news:
            print(f"爬取完成，共 {len(all_news)} 筆新聞。正在寫入資料庫...")
            save_news_to_db(all_news)
        else:
            print("所有股票都沒有爬取到任何新新聞。")


    print("--- 爬蟲執行完畢 ---")
    
    pass