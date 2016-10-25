# Copyright 2016 Mobvoi Inc. All Rights Reserved.
# Author: chhyu@mobvoi.com (Changhe Yu)

package(default_visibility = ['//visibility:public'])

cc_binary(
   name = 'birthday_push_main',
   srcs = [
     'birthday_push_main.cc',
   ],
   deps = [
     ':birthday_push_scheduler',
     '//base:base',
     '//base:binary_version',
  ],
)

cc_library(
   name = 'birthday_push_scheduler',
   srcs = [
     'birthday_push_scheduler.h',
     'birthday_push_scheduler.cc',
  ],
  deps = [
    '//base:base',
    '//base/file:proto_util',
    '//proto:mysql_config_proto', 
    '//third_party/gflags:gflags',
    '//third_party/jsoncpp:jsoncpp',
    '//third_party/mysql_client_cpp:mysqlcppconn',
    '//util/mysql:mysql_util',
    '//recommendation/online/intelligent_push/birthday_push_controller/proto:birthday_meta_proto',
  ],
)

cc_library(
  name = 'push_processor',
  srcs = [
    'push_processor.h',
    'push_processor.cc',
 ],
 deps = [
  '//base:base',
  '//base/file:proto_util',
  '//third_party/gflags:gflags',
  '//recommendation/online/intelligent_push/push_controller:push_sender',
  '//recommendation/online/intelligent_push/birthday_push_controller/proto:birthday_meta_proto',
  '//recommendation/online/intelligent_push/push_controller/proto:push_meta_proto',
],
)  
