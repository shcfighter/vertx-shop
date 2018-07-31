# -*- coding: utf-8 -*-
import urllib.request
import re

from bs4 import BeautifulSoup
import json
from commodity import *
import time
import random
commodity = commodity()


def jd_detail_fun(weburl):
    webheader = {'User-Agent':'Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0'}
    req = urllib.request.Request(url=weburl, headers=webheader)
    webPage=urllib.request.urlopen(req)
    data = webPage.read()
    data = data.decode('gbk')
    data = BeautifulSoup(data, 'html.parser')
    #print(data)

    category_name = data.select_one(".crumb").select(".item")[4].select_one("a").text
    #print(category_name)

    commodity_name = data.select(".sku-name")[0].text.strip()
    #print(commodity_name)

    large_class = []
    items = data.select_one(".crumb").select(".item")
    i_item = 0
    for item in items:
        i_item = i_item + 1
        large_class_a = item.select_one("a")
        if i_item == len(items):
            large_class_a = item
        if large_class_a:
            #print(large_class_a.text)
            large_class.append(large_class_a.text)

    large_class_item = ">".join(large_class)

    brand_name = data.select_one("#parameter-brand").select_one("a").text
    #print(brand_name)
    license_number = data.select_one(".parameter2").select("li")
    commodity_params = []
    for ln in license_number:
        #print(ln.text)
        commodity_params.append(ln.text)

    scripts = data.find_all("script")[0]
    #big_images = re.findall("jfs/[\w\d\/]*.jpg", str(scripts))
    big_images_list = re.findall(r'imageList: \[[\D\d]+.jpg"\]', str(scripts))
    big_images_list0 = ""
    #print(len(big_images_list))
    if len(big_images_list):
        big_images_list0 = big_images_list[0]
    big_images = re.findall("jfs/[\w\d\/]*.jpg", str(big_images_list0))
    image_url_es = []
    i = 0
    for image in big_images:
        if i >= 6:
            break
        #print(image)
        i = i + 1
        image_name = re.split(r"\/", image)
        image = r"http://img14.360buyimg.com/n0/" + image
        urllib.request.urlretrieve(image, "D:\data\images\\" + image_name[-1])
        image_url_es.append( "http://111.231.132.168:8080/images/" + image_name[-1])

    images = data.find_all("style")
    image_url = []
    if len(images):
        image_url = re.findall(r"https://[\.\w\d\/]*.jpg", str(images[-1]))

    detail_image_url = []
    for image in image_url:
        #print(image)
        image_name = re.split(r"\/", image)
        urllib.request.urlretrieve(image, "D:\data\images\\" + image_name[-1]);
        detail_image_url.append("http://111.231.132.168:8080/images/" + image_name[-1])

    commodity_id = re.findall(r"\d+", weburl)[0]
    price_url = "https://p.3.cn/prices/mgets?skuIds=J_" + commodity_id
    price_json=urllib.request.urlopen(urllib.request.Request(price_url)).read().decode('gbk')
    json_price = json.loads(price_json)[0]

    price = json_price["p"]
    original_price = json_price["op"]
    #print(price)
    #print(original_price)

    commodity.commodity_id = int(commodity_id)
    commodity.price = price
    commodity.commodity_params = commodity_params
    commodity.original_price = original_price
    commodity.category_name = category_name
    commodity.commodity_name = commodity_name
    brand_id = random.randint(0, 99999)
    category_id = random.randint(0, 99999)
    commodity.brand_id = brand_id
    commodity.category_id = category_id
    commodity.brand_name = brand_name
    description = commodity_name + " ".join(commodity_params)
    commodity.description = description
    commodity.freight = 15.00
    commodity.month_sales_volume = 0
    commodity.total_sales_volume = 0
    commodity.total_evaluation_num = 0
    commodity.versions = 0
    commodity.remarks = commodity_name
    commodity.status = 1
    commodity.is_deleted = 0
    commodity.create_time = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
    commodity.update_time = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
    num = 5000
    commodity.num = num
    commodity.detail_image_url = detail_image_url
    commodity.image_url = image_url_es
    commodity.large_class = large_class_item

    shop = str(repr(json.dumps(commodity, ensure_ascii=False, default=lambda o: o.__dict__, sort_keys=False, indent=4))).replace("\\n", "", 99)
    print(shop)
    es_image_url = ""
    if len(image_url_es) :
        es_image_url = image_url_es[0]
    with open("D:\data\shop.json", "a") as f:
        f.write("{ \"index\": { \"_index\": \"shop\", \"_type\": \"commodity\" }}\n")
        f.write(eval(shop) + "\n")
    with open("D:\data\shop.sql", "a") as f:
        f.write("INSERT INTO t_commodity(commodity_id, commodity_name, brand_id, brand_name, category_id , category_name, price, original_price, num, status, image_url, create_time, update_time, description , remarks, freight)  VALUES ("
                + commodity_id + ", '" + commodity_name + "', " + str(brand_id) + ", '" + brand_name + "', " + str(category_id) + ",'"
                + category_name + "', " + str(price) + ", " + str(original_price) + ", " + str(num) + ", 1, '" + es_image_url + "', now(), now(), '"
                + description + "', '" + commodity_name + "', '" + commodity.freight + "');\n")

# weburl = "https://item.jd.com/4406840.html"
#weburl = "https://item.jd.com/942300.html"
#jd_detail_fun(weburl)