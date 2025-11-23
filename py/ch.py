import yfinance as yf
import mysql.connector
from mysql.connector import Error
# import mplfinance as mpf
import datetime
import json
import ast
import twstock


# (*** 1. 你的資料庫連線設定 ***)
DB_CONFIG = {
    'host': 'localhost',
    'database': 'investment_db',
    'user': 'root',
    'password': 'qweasdzxc123' # (*** 請改成你的密碼 ***)
}



def transform_json_structure(input_file, output_file):
    # 讀取原始 JSON
    with open(input_file, 'r', encoding='utf-8') as f:
        raw_data = json.load(f)

    # 用來暫存資料的結構: { symbol: { date: { open, high, ... } } }
    data_map = {}

    for key_str, series in raw_data.items():
        try:
            # 原始 key 是字串形式的 Tuple，例如 "('00625K.TW', 'Open')"
            # 使用 ast.literal_eval 安全地將字串轉回 Tuple
            sym, field = ast.literal_eval(key_str)
        except (ValueError, SyntaxError):
            print(f"跳過無法解析的 Key: {key_str}")
            continue

        # 處理欄位名稱 (轉小寫) 和 股票代號 (移除 .TW/.TWO 後綴以符合您的需求)
        field = field.lower() # open, high, low, close, volume
        
        if sym.endswith('.TW'):
            clean_sym = sym[:-3]
        elif sym.endswith('.TWO'):
            clean_sym = sym[:-4]
        else:
            clean_sym = sym
            
        if clean_sym not in data_map:
            data_map[clean_sym] = {}
        
        # 遍歷該欄位下的所有日期數據
        for date_str, value in series.items():
            # 解析 ISO 日期 "2021-01-04T00:00:00.000" 轉為 "2021-01-04"
            try:
                dt = datetime.datetime.fromisoformat(date_str)
                date_fmt = dt.strftime('%Y-%m-%d')
            except ValueError:
                date_fmt = date_str.split('T')[0] # 如果解析失敗，直接取 T 前面的部分

            if date_fmt not in data_map[clean_sym]:
                data_map[clean_sym][date_fmt] = {}
            
            data_map[clean_sym][date_fmt][field] = value

    # 轉換為最終的列表格式
    output_list = []
    for sym, dates_data in data_map.items():
        entry = {
            "symbol": sym,
            "data": []
        }
        
        # 將日期依照降序排列 (最新的在前面)
        sorted_dates = sorted(dates_data.keys(), reverse=True)
        
        for d in sorted_dates:
            day_vals = dates_data[d]
            data_point = {
                "date": d,
                "open": day_vals.get("open"),
                "high": day_vals.get("high"),
                "low": day_vals.get("low"),
                "close": day_vals.get("close"),
                "volume": day_vals.get("volume")
            }
            entry["data"].append(data_point)
        
        output_list.append(entry)

    # 寫入新檔案
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(entry,f, indent=4, ensure_ascii=False)
        
    
    print(f"轉換完成！檔案已儲存為: {output_file}")







def getStockData(symbols):
    if symbols in twstock.codes:
        market_type = twstock.codes[symbols].market
    else:
        # 2. 處理特殊代號 (例如 2881A, 2881B 等特別股)
        # 邏輯：取出代號中的數字部分 (2881A -> 2881) 再查一次
        parent_id = ''.join(filter(str.isdigit, symbols))
        
        if parent_id and parent_id in twstock.codes:
            market_type = twstock.codes[parent_id].market
        else:
            # 3. 最後檢查：如果是 4 碼且以 00 開頭 (ETF)，通常 twstock 會有，
            # 若真的沒有，大部分 ETF 在台灣多為上市，但為了準確保留 '未知' 或視需求手動調整。
            pass
    if market_type =="上市":
        symbols = symbols + ".TW"
    else :
        symbols = symbols + ".TWO"


    res = yf.download(symbols,start="2021-01-01",end=datetime.datetime.today(), interval="1d", group_by='ticker', threads=True)
    res.to_json("ch_data.json", date_format='iso')
    transform_json_structure("ch_data.json", "ch_trans_data.json")
    with open("ch_trans_data.json","r",encoding="UTF-8") as f:
        res = json.load(f)
    return res
