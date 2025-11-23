import json

sourcef =  open ("F:\\Workspace_5\\VueTest\\src\\assets\\stock.json","r",encoding="UTF-8") 
removef = open("C:\\Users\\user\\Downloads\\suspendListingCsvAndHtml.json","r",encoding="UTF-8")

ssf = json.load(sourcef)
rrf = json.load(removef)



print(ssf)
dist = {}
for i in range(0,len(rrf)):
    dist[rrf[i]["Code"]] = True
res = []

for i in range(0,len(ssf)):
    if(not dist.get(ssf[i]["id"])):
        # print("remove : ",ssf[i])
        res.append(ssf[i])
with open("data.json","w",encoding="UTF-8") as f:
    json.dump(res,f, indent=4, ensure_ascii=False)