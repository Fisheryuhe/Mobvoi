#!/usr/bin/python
# encoding: utf-8

import sys
reload(sys)
sys.setdefaultencoding( "utf-8" ) 

from user_info_fetcher import UserCityFetcher
from weather_fetcher import WeatherFetcher

class MsgCenter():

    def __init__(self):
        self.city_weather_dict = {}
    
    def fetch_all_msg(self, isTest=False):
        wf = WeatherFetcher()
        self.user_reader = UserCityFetcher()
        self.user_reader.fetch_user_city(isTest)
        for city in self.user_reader.city_set:
            msg = wf.get_weather(city)
            if not msg:
                print >> sys.stderr,"Msg Center/fetch_all_msg:city weather should not None."
                continue
            self.city_weather_dict[city] = msg
    
    def log(self):
        for (k, v) in self.city_weather_dict.items():
            print "%s\n%s" %(k, v)
    
    def get_weather_info(self, city):
        try:
            msg = self.city_weather_dict[city]
            return msg
        except Exception,e:
            print >> sys.stderr,"MsgCenter/get_weather_info:city_weather_dict is not the key "
            return None

if __name__ == "__main__":
    mc = MsgCenter()
    mc.fetch_all_msg(True)
    mc.log()
