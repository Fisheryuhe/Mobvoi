#!/usr/bin/python
# encoding: utf-8

import sys
reload(sys)
sys.setdefaultencoding( "utf-8" ) 

import sys
import urllib
import urllib2
import json

class NewsFetcher(object):

    def __init__(self,):
        self.news_server_url = "http://218.244.150.66:8180/query?type=hot&count=5&output=watch"
        #self.news_server_url = "http://ali-hz-cdb-8:8150/more_news"
    
    def get_hot_news(self):
        try:
            response = urllib2.urlopen(self.news_server_url)
            #print response.readlines()
            raw_data = json.loads(response.readline().replace('\r\n', ''))
            #raw_data = json.loads(response.readlines().replace('\r\n', ''))
            #raw_data = response.readlines()
            result = ""
            for i in range (0,2):
              title = raw_data["content"]["data"][0] ["params"]["details"][i]["title"]
              detail = raw_data["content"]["data"][0]["params"]["details"][i]["browserUrl"]
              result = result + '\n<a href="%s">%s</a>\n' %(detail, title)
            return result
        except Exception,e:
            print >> sys.stderr, "getNews exception:%s" %(e)
            return None

if __name__ == "__main__":
    g = NewsFetcher()
    print g.get_hot_news()
