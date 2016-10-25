#!/usr/bin/python
# encoding: utf-8

import os
import sys

class UserCityFetcher(object):
   
    def __init__(self):
        self.user_city_map = {}
        self.city_set = set()
        pwd=os.path.split(os.path.realpath(__file__))[0]
        self.user_file = pwd + "/../data/user_cities"
        self.test_file = pwd + "/../data/testUser"
    
    def _get_address(self, city):
        address_prefix = ",,"
        address_suffix = ",,,,0,0,"
        return address_prefix + city + address_suffix
   
    def fetch_user_city(self, is_test=False):
        if not is_test:
            user_file = self.user_file
        else:
            user_file = self.test_file
        for line in open(user_file):
            try:
                line = line.strip()
                if len(line) <=0:
                    print >> sys.stderr, "Error line: %s" %(line)
                    continue
                list_line = line.split("\t")
                if len(list_line) != 2:
                    print >> sys.stderr, "Error line: %s" %(line)
                    continue
                user = list_line[0]
                city = list_line[1]
                if city == "NULL" or len(city) < 3:
                    print >> sys.stderr, "Error line: %s" %(line)
                    continue
                else:
                    address = self._get_address(city) 
                
                self.user_city_map[user] = address
                self.city_set.add(address)
            except Exception,e:
                print >> sys.stderr, "getUsers Exception:%s" %(e)

    def log(self):
        for (k,v) in self.user_city_map.items():
            print k, v
        print "userCityMap size: %d" %(len(self.user_city_map))
        print "citySet size: %d" %(len(self.city_set))

if __name__ == "__main__":
    user_reader = UserCityFetcher()
    user_reader.fetch_user_city()
    user_reader.log()
