from cnsenti import Sentiment
import json

senti = Sentiment(pos='正面词.txt',  #正面词典txt文件相对路径
        neg='负面词.txt',  #负面词典txt文件相对路径
        merge=True,             #融合cnsenti自带词典和用户导入的自定义词典
        encoding='utf-8')      #两txt均为utf-8编码

while(True):
    ans=[]

    rank=[0]*5

    lines = []

    jsonData = input()

    datas = json.loads(jsonData)

    for data in datas:
        data.replace('\\n','')
        lines.append(data)

    for line in lines:
        if line!='\n' and line!='':
            result = senti.sentiment_count(line)
            pos_count = int(result['pos'])
            neg_count = int(result['neg'])

            if pos_count==neg_count:
                ans.append(3)
            elif pos_count>neg_count and neg_count==0:
                ans.append(5)
            elif pos_count>neg_count and neg_count!=0:
                ans.append(4)
            elif pos_count<neg_count and pos_count==0:
                ans.append(1)
            elif pos_count<neg_count and pos_count!=0:
                ans.append(2)
                
    for i in range(5):
        rank[i]+=ans.count(i+1)
                    
    print(rank)