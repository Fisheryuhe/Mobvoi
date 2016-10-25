#!/usr/bin/python
#encoding: utf-8

import json
import urllib2

class BoardingPort(object):

    def __init__(self):
        pass

    def get_board_port(self,flight_no,time):
        #url = "http://ali-hz-search-dev-0:8160/board_port?flight_no="+flight_no+"&time="+time
        #url = "https://www.sogou.com/reventondc/transform?key=MU5181&uuid=71186dbe-570d-4a72-bd8b-115991c0733c&vrid=70038401&type=2&objid=70038401&userarea=sss&charset=utf8&callback=jQuery111007160342431650215_1461575839729&url=http%3A%2F%2Fcoop.flight.qunar.com%2Fapi%2Fflightstatus%2FgetByFnoDate%3Fdate%3D2016-04-25%26token%3DUxzB3okMPlj12hnx%26flightno%3DMU5181%26client%3Dsougou&_=1461575839730"
        url = "http://localhost:8160/board_port?flight_no="+flight_no+"&time="+time
        request = urllib2.Request(url)
        response = urllib2.urlopen(request)
        result = response.read()
        print type(result)
        res_dict = {}
        d = json.loads(result)
        print d["time_jounery"][0],d["time_jounery"][1]
        res_dict["flight_no"] = flight_no

        if "flight_name" in d.keys():
            res_dict["flight_name"] = d["flight_name"]

        if "from" in d.keys():
            if len(d["from"]) > 3:
                stra = d["from"][0].split(" ")
                res_dict["flight_from_city"] = stra[0]
                res_dict["flight_from_airport"] = stra[1]
                res_dict["flight_from_tower"] = stra[1].split("机场",1)[1]

        if "to" in d.keys():
            if len(d["to"]) > 3:
                stra = d["to"][0].split(" ")
                res_dict["flight_to_city"] = stra[0]
                res_dict["flight_to_airport"] = stra[1]
                res_dict["flight_to_tower"] = stra[1].split("机场", 1)[1]

        if "flight_time" in d.keys():
            if len(d["flight_time"][0]) > 3 :
                stra = d["flight_time"][0].split(" ")
                res_dict["flight_plan_takeoff_time"] = stra[1]
                res_dict["flight_actual_takeoff_time"] = stra[0].split("计划", 1)[0].split("起飞", 1)[1]
            if len(d["flight_time"][2]) >3 :
                stra = d["flight_time"][2].split(" ")
                res_dict["flight_plan_land_time"] =  stra[1]
                res_dict["flight_actual_land_time"] = stra[0].split("计划", 1)[0].split("到达", 1)[1]

            res_dict["flight_status"] = d["flight_time"][1]

        if "port_info" in d.keys():
            res_dict["flight_checkin_counter"] = d["port_info"][0]
            res_dict["flight_carousel"] = d["port_info"][-1]
            res_dict["flight_boarding_gate"] = d["port_info"][1]

        res_dict["flight_type"] = ""

        res_dict["flight_takeoff_late_time"] = ""
        print res_dict


if __name__ == "__main__":
    board_port = BoardingPort()
    board_port.get_board_port("MU5181","20160428")

