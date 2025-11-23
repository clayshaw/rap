import json
import sys

# 檢查是否已安裝 twstock 套件
try:
    import twstock
except ImportError:
    print("錯誤：找不到 twstock 套件。")
    print("請先執行以下指令安裝：")
    print("pip install twstock")
    sys.exit(1)

def add_market_type(input_file, output_file):
    print(f"正在讀取 {input_file} ...")
    
    try:
        with open(input_file, 'r', encoding='utf-8') as f:
            data = json.load(f)
    except FileNotFoundError:
        print(f"錯誤：找不到檔案 '{input_file}'。")
        print("請確認 stock.json 是否與此程式在同一個資料夾內。")
        return

    print(f"共有 {len(data)} 筆資料，正在查詢市場別...")
    
    # 嘗試更新 twstock 的股票代碼清單 (需要網路連線)
    try:
        print("正在更新即時股票代碼資訊 (請稍候)...")
        twstock.__update_codes()
    except Exception as e:
        print(f"更新代碼清單失敗 (將使用內建資料庫): {e}")

    updated_count = 0
    unknown_count = 0

    for item in data:
        stock_id = item.get('id')
        market_type = "未知"

        # 1. 直接查詢代號
        if stock_id in twstock.codes:
            market_type = twstock.codes[stock_id].market
        else:
            # 2. 處理特殊代號 (例如 2881A, 2881B 等特別股)
            # 邏輯：取出代號中的數字部分 (2881A -> 2881) 再查一次
            parent_id = ''.join(filter(str.isdigit, stock_id))
            
            if parent_id and parent_id in twstock.codes:
                market_type = twstock.codes[parent_id].market
            else:
                # 3. 最後檢查：如果是 4 碼且以 00 開頭 (ETF)，通常 twstock 會有，
                # 若真的沒有，大部分 ETF 在台灣多為上市，但為了準確保留 '未知' 或視需求手動調整。
                pass
        
        # 將結果寫入新的欄位
        item['market_type'] = market_type
        
        if market_type == "未知":
            unknown_count += 1
        else:
            updated_count += 1

    # 儲存結果
    try:
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=4, ensure_ascii=False)
        
        print("-" * 30)
        print(f"處理完成！")
        print(f"成功辨識: {updated_count} 筆")
        print(f"無法辨識: {unknown_count} 筆")
        print(f"新檔案已儲存為: {output_file}")
        print("-" * 30)
        
    except Exception as e:
        print(f"寫入檔案時發生錯誤: {e}")

if __name__ == "__main__":
    # 輸入檔名: stock.json
    # 輸出檔名: stock_classified.json
    add_market_type('stock.json', 'stock_classified.json')