# -*- coding: utf-8 -*-

import urllib.request
import re
from bs4 import BeautifulSoup
import json
from commodity import *
import time
import random
from jd_details import *


def jd_list_fun(url):
    webheader = {'User-Agent':'Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0'}
    req = urllib.request.Request(url=url, headers=webheader)
    data = urllib.request.urlopen(req).read().decode('utf-8')
    data = BeautifulSoup(data, 'html.parser')
    #print(data)
    p_img = data.select(".p-img")
    for img in p_img:
        commodity_url = img.find("a")["href"]
        if re.search("[\D\d]+.html", commodity_url):
            jd_url = "http:" + commodity_url
            print(jd_url)
            jd_detail_fun(jd_url)



url = "https://search.jd.com/Search?keyword=%e7%89%b9%e4%ba%a7&enc=utf-8&page=1"
jd_list_fun(url)
