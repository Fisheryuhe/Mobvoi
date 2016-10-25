#!/bin/bash
bin=$(cd `dirname $0`;pwd)
date=$(date +"%F %T")

echo "datetime => "$date >> $bin/log/readmysql.log

rm -rf $bin/data/user_cities

mysql -ugenius_readonly -pTKOxDxXs -hrdsynuajfynuajf.mysql.rds.aliyuncs.com -N -e "select id,city from user" genius_01 > $bin/data/user_cities

date2=$(date +"%F")

mkdir $bin/data/$date2
mysql -ugenius_readonly -pTKOxDxXs -hrdsynuajfynuajf.mysql.rds.aliyuncs.com -N -e "select id,city from user" genius_01 > $bin/data/$date2/user_cities
