#!/usr/bin/python
# encoding: utf-8

import sys
reload(sys)
sys.setdefaultencoding( "utf-8" ) 

import sys
import urllib
import urllib2
from json import JSONEncoder
from json import JSONDecoder

class WeatherFetcher(object):
   
    def __init__(self):
        self.weather_server_url = "https://m.mobvoi.com/search/pc"

    def get_weather(self, city):
        try:
            data = {"task":"public.weather","appkey":"com.mobvoi.rom","output":"watch","version":"30004"}
            data["address"] = city
            json_data = JSONEncoder().encode(data)
            post_req = urllib2.Request(self.weather_server_url, json_data)
            post_req.add_header("Content-type", "application/json")
            post_res = urllib2.urlopen(post_req)
            res = post_res.read()
            print "Raw weather data: %s" %(res)
            raw_data = JSONDecoder().decode(res)
            raw_message  = raw_data["content"]["data"][0]["params"]["tts"].strip()
            pm25 = raw_data["content"]["data"][0]["params"]["pageData"][0]["pm25"].strip()
            response_message = ""
            if len(raw_message) > 0:
                response_message = raw_message
            else:
                print >> sys.stderr,"WeatherFetcher/getWeather:weather info is null."
                return None

            if len(pm25) > 0 and int(pm25) > 0:
                response_message = response_message + "\nPM2.5指数: %s" %(pm25)
            
            return response_message

        except Exception,e:
            print >> sys.stderr, "city:%s, error:%s" %(city, e)
            return None
    
if __name__ == "__main__":
    city = ",,北京市,,,,0,0,"
    g = WeatherFetcher()
    print g.get_weather(city)
