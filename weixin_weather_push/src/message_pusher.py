#!/usr/bin/python
# encoding: utf-8
import sys
reload(sys)
sys.setdefaultencoding( "utf-8" )
import urllib
import urllib2
from json import *

class WechatPushService(object):

    def __init__(self,):
        self.weixin_push_url_format = "http://genius/push/user?user=%s&message=%s&silent=true&source=wenwenzaobao"
        self.weixin_push_url = "http://genius/push/user"

    def push_msg(self, user_id, msg):
        push_url = self.weixin_push_url_format %(user_id, msg)
        res = urllib.urlopen(push_url)
        return True 
    
    def push_post_msg(self, user_id, msg):
        try:
            data = {}
            data["user"] = user_id
            data["message"] = msg
            data["silent"] = True
            data["source"] = "wenwenzaobao"
            json_data = JSONEncoder().encode(data)

            post_req = urllib2.Request(self.weixin_push_url, json_data)
            post_req.add_header("Content-type", "application/json")
            post_res = urllib2.urlopen(post_req)
            res = post_res.read()
            return True
        except Exception,e:
            print >> sys.stderr,"WeixinPushWeather/push_post_msg:exception %s"%(e)
            return False

if __name__ == "__main__":
    w = WechatPushService()
    w.push_post_msg("804795eb5f5ce4a5fb4492af", "北京市今天多云转晴，-4℃到6℃")
