import csv
import json

# ====== 請修改成你實際的檔案名稱 ======
TWSE_CSV = "twse_listed.csv"   # 上市
TPEX_CSV = "tpex_listed.csv"   # 上櫃
OUTPUT_JSON = "stock.json"
# ====================================

tw_industry_map = {
    "01": "水泥工業",
    "02": "食品工業",
    "03": "塑膠工業",
    "04": "紡織纖維",
    "05": "電機機械",
    "06": "電器電纜",
    "08": "玻璃陶瓷",
    "09": "造紙工業",
    "10": "鋼鐵工業",
    "11": "橡膠工業",
    "12": "汽車工業",
    "14": "建材營造",
    "15": "航運業",
    "16": "觀光餐旅",
    "17": "金融保險",
    "18": "貿易百貨",
    "19": "綜合",
    "20": "其他",
    "21": "化學工業",
    "22": "生技醫療業",
    "23": "油電燃氣業",
    "24": "半導體業",
    "25": "電腦及週邊設備業",
    "26": "光電業",
    "27": "通信網路業",
    "28": "電子零組件業",
    "29": "電子通路業",
    "30": "資訊服務業",
    "31": "其他電子業",
    "32": "文化創意業",
    "33": "農業科技業",
    "34": "電子商務業",
    "35": "綠能環保",
    "36": "數位雲端",
    "37": "運動休閒",
    "38": "居家生活"
}

def is_delisted(row):
    """
    判斷是否已下市 / 停止交易
    只要「下市日期」或「停止買賣日期」有值，就視為已下市
    """
    delist_fields = ["下市日期", "停止買賣日期"]
    for field in delist_fields:
        if field in row and row[field].strip():
            return True
    return False
#以stock1為準，將stock中ETF有關的資料加入@stock.json
def load_csv(file_path, market_type):
    result = []

    with open(file_path, encoding="utf-8-sig") as f:
        reader = csv.DictReader(f)

        for row in reader:
            # 基本防呆
            if "公司代號" not in row or not row["公司代號"].strip():
                continue

            # 排除已下市
            if is_delisted(row):
                continue

            result.append({
                "id": row["公司代號"].strip(),
                "name": row.get("公司簡稱", "").strip(),
                "industry_category": tw_industry_map.get(row.get("產業別", "").strip(), ""),
                "market_type": market_type
            })

    return result

def main():
    all_stocks = []

    # 讀取上市
    all_stocks.extend(load_csv(TWSE_CSV, "上市"))

    # 讀取上櫃
    all_stocks.extend(load_csv(TPEX_CSV, "上櫃"))

    # 輸出 JSON
    with open(OUTPUT_JSON, "w", encoding="utf-8") as f:
        json.dump(all_stocks, f, ensure_ascii=False, indent=2)

    print(f"完成！共輸出 {len(all_stocks)} 筆仍在交易的股票")
    print(f"檔案位置：{OUTPUT_JSON}")

if __name__ == "__main__":
    main()
