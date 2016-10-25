// Copyright 2016 Mobvoi Inc. All Rights Reserved.
// Author: chhyu@mobvoi.com (Changhe Yu)

#ifndef RECOMMENDATION_ONLINE_INTELLIGENT_PUSH_BIRTHDAY_PUSH_CONTROLLER_BIRTH_PUSH_SCHEDULER_H_
#define RECOMMENDATION_ONLINE_INTELLIGENT_PUSH_BIRTHDAY_PUSH_CONTROLLER_BIRTH_PUSH_SCHEDULER_H_

#include <memory>

#include "base/basictypes.h"
#include "base/compat.h"
#include "proto/mysql_config.pb.h"
#include "recommendation/online/intelligent_push/birthday_push_controller/proto/birthday_meta.pb.h"

namespace birthday_push_controller {

class BirthInfoFetcher {
 public:
  BirthInfoFetcher();
  ~BirthInfoFetcher();
  bool Fetch(vector<BirthInfo>* birth_infos);

 private:
  bool FetchWwidByAccountID(const string& user_id, string* wwid);
  bool PrintInfo(vector<BirthInfo>* birth_infos);
  void Init();
  bool IsValidData(const string& user_id, const string& birth_date);
  bool IsValidYearRange(const string& birth_date);
  string BuildQuerySql();

  std::unique_ptr<MysqlServer> mysql_server_;
  DISALLOW_COPY_AND_ASSIGN(BirthInfoFetcher);
};

class BirthPushProcessor {
 public:
  BirthPushProcessor();
  ~BirthPushProcessor();
  bool Push(const vector<BirthInfo>& birth_infos);

 private:
  bool PushOneUser(const BirthInfo& birth_info);
  DISALLOW_COPY_AND_ASSIGN(BirthPushProcessor);
};

}  // namespace birthday_push_controller

#endif  // RECOMMENDATION_ONLINE_INTELLIGENT_PUSH_BIRTHDAY_PUSH_CONTROLLER_BIRTH_PUSH_SCHEDULER_H_

