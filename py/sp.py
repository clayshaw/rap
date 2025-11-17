from FinMind.data import DataLoader

api = DataLoader()
# api.login_by_token(api_token='token')
# api.login(user_id='user_id',password='password')
df = api.taiwan_stock_info_with_warrant()
df.to_csv("taiwan_stock_info_with_warrant.csv", index=False, encoding='utf_8_sig')