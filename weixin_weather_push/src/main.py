#!/usr/bin/python
# encoding: utf-8

import sys
reload(sys)
sys.setdefaultencoding( "utf-8" ) 

from message_pusher import WechatPushService
from weather_service import MsgCenter
from news_fetcher import NewsFetcher
from time import time
from time import strftime
from time import localtime

def main():
    start = time()
    log_time = strftime("%Y-%m-%d %H:%M:%S", localtime(time()) )
    print "%s [INFO] Xiaowen push start!" %(log_time)
    
    title = "hi，美好的一天又开始了\n出门前看看今天的天气哦~\n\n"
    
    news_fetcher_new = NewsFetcher()
    news_fetcher = news_fetcher_new.get_hot_news()
    wechat_push_weather = WechatPushService()
    
    if not news_fetcher:
        print >> sys.stderr, "News shouldn't be None."
        sys.exit(0)
    else:
        pass
    
    log_time = strftime("%Y-%m-%d %H:%M:%S", localtime(time()) )
    print "%s [INFO] MsgCenter fetching msg!" %(log_time)
    msg_center = MsgCenter()
    ## 测试开关 ###################
    is_test = False
    #is_test = True 
    msg_center.fetch_all_msg(is_test)
    log_time = strftime("%Y-%m-%d %H:%M:%S", localtime(time()) )
    print "%s [INFO] MsgCenter fetched msg!" %(log_time)
    
    cnt = 0
    for (user, city) in msg_center.user_reader.user_city_map.items():
        weather_info = msg_center.get_weather_info(city)
        if weather_info:
            log_time = strftime("%Y-%m-%d %H:%M:%S", localtime(time()) )
            message = title + "今日天气： " + weather_info+"\n\n"
            message = message + "对了，回复你的星座（如：白羊座），还可以查看今日运势呢~"
            print "%s [INFO] Pushing msg to user (%s) city (%s)!" %(log_time, user, city)
            print "%s [INFO] Msg is:\n%s" %(log_time, message)
            wechat_push_weather.push_post_msg(user, message)
            log_time = strftime("%Y-%m-%d %H:%M:%S", localtime(time()) )
            print "%s [INFO] Pushed complete!" %(log_time)
            cnt = cnt + 1
        else:
            print >> sys.stderr, "weather_info is null."
            continue
    
    end = time()
    cost = end - start
    log_time = strftime("%Y-%m-%d %H:%M:%S", localtime(time()) )
    print "%s [INFO] Xiaowen push finished!" %(log_time)
    print "Xiaowen pushed %d messages and cost %d seconds!" %(cnt,cost)


if __name__ == '__main__':
    main()
