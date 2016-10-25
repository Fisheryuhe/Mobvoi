#!/usr/bin/python
#encoding: utf-8

import json
import urllib2
from json import JSONEncoder

class BoardingPort(object):

    def __init__(self):
        pass

    def get_origin_flight_info(self,flight_no,time):

        url = "https://www.sogou.com/reventondc/transform?key=" + flight_no + "&uuid=71186dbe-570d-4a72-bd8b-115991c0733c&vrid=70038401&type=2&objid=70038401&userarea=sss&charset=utf8&callback=jQuery111007160342431650215_1461575839729&url=http%3A%2F%2Fcoop.flight.qunar.com%2Fapi%2Fflightstatus%2FgetByFnoDate%3Fdate%3D" + time + "%26token%3DUxzB3okMPlj12hnx%26flightno%3D" + flight_no + "%26client%3Dsougou&_=1461575839730"
        request = urllib2.Request(url)
        response = urllib2.urlopen(request)
        result = response.read()
        json_str_result = result.split("(", 1)[1].split(")")[0]
        json_result = json.loads(json_str_result)
        return json_result

    def get_process_flight_info(self,flight_no,time):
        fp = open("flight_no_null.txt","a")
        res = {}
        url = "https://www.sogou.com/reventondc/transform?key=" + flight_no + "&uuid=71186dbe-570d-4a72-bd8b-115991c0733c&vrid=70038401&type=2&objid=70038401&userarea=sss&charset=utf8&callback=jQuery111007160342431650215_1461575839729&url=http%3A%2F%2Fcoop.flight.qunar.com%2Fapi%2Fflightstatus%2FgetByFnoDate%3Fdate%3D" + time + "%26token%3DUxzB3okMPlj12hnx%26flightno%3D" + flight_no + "%26client%3Dsougou&_=1461575839730"
        request = urllib2.Request(url)
        response = urllib2.urlopen(request)
        result = response.read()
        json_str_result = result.split("(" , 1)[1][:-1]
        print json_str_result
        json_result = json.loads(json_str_result)
        if len(json_result["data"]) == 0:
            fp.write(flight_no + "\r\n")
            return res
        d = json_result["data"][0]
        
        if "flightno" in d.keys():
            res["flight_no"] = d["flightno"]           
        if "cname" in d.keys():
            res["flight_name"] = d["cname"]
        if "dptcity_name" in d.keys():
            res["flight_from_city"] = d["dptcity_name"]
        if "arrcity_name" in d.keys():
            res["flight_to_city"] = d["arrcity_name"]
        if "plan_local_dep_time" in d.keys():
            res["flight_plan_takeoff_time"] = d["plan_local_dep_time"]
        if "actual_local_dep_time" in d.keys():
            res["flight_actual_takeoff_time"] = d["actual_local_dep_time"]
        if "plan_local_arr_time" in d.keys():
            res["flight_plan_land_time"] = d["plan_local_arr_time"]
        if "actual_local_arr_time" in d.keys():
            res["flight_actual_land_time"] = d["actual_local_arr_time"]
        if "dptairport_name" in d.keys():
            res["flight_from_airport"] = d["dptairport_name"]
        if "dpttower" in d.keys():
            if  len(d["dpttower"]) == 1:
                res["flight_from_tower"] =  "T" + d["dpttower"]
            else:
                 res["flight_from_tower"] = d["dpttower"]
        if "arrairport_name" in d.keys():
            res["flight_to_airport"] = d["arrairport_name"]
        if "arrtower" in d.keys():
            if len(d["arrtower"]) == 1:
                res["flight_to_tower"] = "T" + d["arrtower"]
            else:
                res["flight_to_tower"] = d["arrtower"]
        if "status" in d.keys():
            res["flight_status"] = d["status"]
        if "zhiji" in d.keys():
            res["flight_checkin_counter"] = d["zhiji"]
        if "xingli" in d.keys():
            res["flight_carousel"] = d["xingli"]
        if "dengjikou" in d.keys():
            res["flight_boarding_gate"] = d["dengjikou"]
        if "planetype" in d.keys():
            res["flight_type"] = d["planetype"]
        if "dep_being_late_str" in d.keys():
            res["flight_takeoff_late_time"] = d["dep_being_late_str"]
        return   res

if __name__ == "__main__":
    board_port = BoardingPort()
    board_port.get_process_flight_info("9C8525","20160511")

