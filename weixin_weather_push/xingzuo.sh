#!/bin/bash
bin=$(cd `dirname $0`;pwd)
#date=$(date +"%F %T")
date=$(date +"%F %T")
echo "dateTime => "$date >> $bin/log/err.log

python $bin/src/xingzuo.py 1>>$bin/log/info.log 2>>$bin/log/err.log &
