from wordcloud import WordCloud
import jieba
import PIL.Image as Image
import os
import json
import io
import sys
import numpy as np
 
stopwords={"然后":0,"这些":0,"那些":0,"所以":0,"但是":0,'的':0,'这个':0,'老师':0,'是':0,'就是':0,'和':0,'虽然':0,'也':0,'很':0,'做':0,'来':0,'给':0,'中':0,'又':0,'在':0,'使':0,'有些':0,'还':0,'有':0,'对':0,'人':0,'们':0,'了':0,'既':0,'又':0,'及':0,'为':0}

def chinese_jieba(text):
    wordlist_jieba=jieba.cut(text)
    text_jieba=" ".join(wordlist_jieba)
    return text_jieba

def image2byte(image):
    '''
    图片转byte
    image: 必须是PIL格式
    image_bytes: 二进制
    '''
    # 创建一个字节流管道
    img_bytes = io.BytesIO()
    # 将图片数据存入字节流管道， format可以按照具体文件的格式填写
    image.save(img_bytes, format="JPEG")
    # 从字节流管道中获取二进制
    image_bytes = img_bytes.getvalue()
    return image_bytes

def read2byte(path):
    '''
    图片转二进制
    path：图片路径
    byte_data：二进制数据
    '''
    with open(path,"rb") as f:
        byte_data = f.read()
    return byte_data

wordcloud=WordCloud(background_color="white",
                        max_words=150,
                        height=200,
                        width=400,
                        scale=20,
                        prefer_horizontal=0.7,
                        max_font_size=60,
                        font_path="msyh.ttc",
                        relative_scaling=0.2,
                        stopwords=stopwords)

while True:
    jsonData = input()

    datas = json.loads(jsonData)

    text=''
    
    if len(datas)==1 and datas[0]=='':
        img = np.zeros((200,400),np.uint8)
        img[0:199,0:399] = 255
        image = Image.fromarray(img)
        image.save('wordcloud.jpg')
        
        image_path = "wordcloud.jpg"

        '''
        image = Image.open(image_path)
        byte_data = image2byte(image)
        '''

        byte_data = read2byte(image_path)
        os.remove(image_path)
        sys.stdout.buffer.write(byte_data)
        sys.stdout.flush()
        continue
        
    else:
        for data in datas:
            data.replace('\\n','')
            text+=data
        

    text=chinese_jieba(text)
    
    wordcloud=wordcloud.generate(text)

    wordcloud.to_file('wordcloud.jpg')

    image_path = "wordcloud.jpg"

    '''
    image = Image.open(image_path)
    byte_data = image2byte(image)
    '''

    byte_data = read2byte(image_path)
    os.remove(image_path)
    sys.stdout.buffer.write(byte_data)
    sys.stdout.flush()