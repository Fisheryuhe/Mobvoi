#!/usr/bin/python
# encoding: utf-8

import sys
reload(sys)
sys.setdefaultencoding( "utf-8" ) 

from message_pusher import WechatPushService
from user_info_fetcher import UserCityFetcher
from time import time
from time import strftime
from time import localtime

def main():
    start = time()
    log_time = strftime("%Y-%m-%d %H:%M:%S", localtime(time()) )
    print "%s [INFO] Xiaowen push start!" %(log_time)
    
    #xingzuo = "今天也就不跟你们遮遮掩掩的了。想知道小问长什么样子？回复【爆照】查看 \n"
    #xingzuo = "[["Mar" 30, 2016 周三","","http://chumenwenwen.com/m/images/321.jpg",""],["早上好，你那边的天气如何？\n\n不要试图使事情正确，事物即本身。－－《妙笔生花》","","",""],["回复[占]，看今日宜忌。","","",""]] \n"
   
    #xingzuo = "[["+"\"Mar 30, 2016 周三\""+","+"\"\""+","+"\"http://chumenwenwen.com/m/images/321.jpg\""+","+"\"\""+"],["+"\"早上好，你那边的天气如何？\\n\\n不要试图使事情正确，事物即本身。－－《妙笔生花》\""+","+"\"\""+","+"\"\""+","+"\"\""+"],["+"\"回复[占]，看今日宜忌。\""+","+"\"\""+","+"\"\""+","+"\"\""+"]]"
    xingzuo = "上四天班后的星期六总是让人倍感放松~听说自然醒后看部电影才是打开周末的正确方式哦~回复【自然醒】，看看小问为你挖到了哪些电影宝贝！"
    wechat_push_weather = WechatPushService()
    
    log_time = strftime("%Y-%m-%d %H:%M:%S", localtime(time()) )
    print "%s [INFO] MsgCenter fetching msg!" %(log_time)
    ## 测试开关 ###################
    #is_test = False
    is_test = True 
    user_reader = UserCityFetcher()
    user_reader.fetch_user_city(is_test)
    log_time = strftime("%Y-%m-%d %H:%M:%S", localtime(time()) )
    print "%s [INFO] MsgCenter fetched msg!" %(log_time)
    cnt = 0
    for (user,city) in user_reader.user_city_map.items():
        log_time = strftime("%Y-%m-%d %H:%M:%S", localtime(time()) )
        print "%s [INFO] Pushing msg to user (%s) xingzuo (%s)!" %(log_time,user, xingzuo )
        wechat_push_weather.push_post_msg(user, xingzuo)
        log_time = strftime("%Y-%m-%d %H:%M:%S", localtime(time()) )
        print "%s [INFO] Pushed complete!" %(log_time)
        cnt = cnt + 1
    
    end = time()
    cost = end - start
    log_time = strftime("%Y-%m-%d %H:%M:%S", localtime(time()) )
    print "%s [INFO] Xiaowen push finished!" %(log_time)
    print "Xiaowen pushed %d messages and cost %d seconds!" %(cnt,cost)


if __name__ == '__main__':
    main()
