#!/usr/bin/python
#encoding: utf-8

import json
import urllib2

class BoardingPort(object):

    def __init__(self):
        pass

    def get_board_port(self,flight_no,time):
        url = "http://ali-hz-search-dev-0:8160/board_port?flight_no="+flight_no+"&time="+time
        request = urllib2.Request(url)
        response = urllib2.urlopen(request)
        result = response.read()
        print type(result),result,"\r\n"
        json_str_result = result.split("(")[1].split(")")[0]
        print json_str_result,"\r\n"
        json_result = json.loads(json_str_result)
        d = json_result["data"][0]
        print type(d)
        #print json_result["data"][0]["dptairport_name"]
          

if __name__ == "__main__":
    board_port = BoardingPort()
    board_port.get_board_port("MU5181","20160507")

