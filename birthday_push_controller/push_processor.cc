// Copyright 2016 Mobvoi Inc. All Rights Reserved.
// Author: chhyu@mobvoi.com (Changhe Yu)

#include "base/file/proto_util.h"
#include "base/log.h"
#include "base/string_util.h"
#include "third_party/gflags/gflags.h"

#include "recommendation/online/intelligent_push/birthday_push_controller/push_processor.h"

namespace birthday_push_controller {

PushProcessor::PushProcessor() {
  LOG(INFO) << "start to construct PushProcessor";
  Init();
}

PushProcessor::~PushProcessor() {}

void PushProcessor::Init() {
  LOG(INFO) << "start to init PushProcessor";
}

void PushProcessor::Run() {
  LOG(INFO) << "start to run PushProcessor";
}

BirthPushProcessor::BirthPushProcessor() {
  LOG(INFO) << "start to construct BirthPushProcessor";
}

BirthPushProcessor::~BirthPushProcessor() {}

bool BirthPushProcessor::Process(vector<BirthInfo>* birth_infos) {
  LOG(INFO) << "start to process birth push ...";
  for (auto& birth_info: *birth_infos) {
     string wwid = birth_info.wwid();
     string birthday = birth_info.birth_date();
     Json::Value message;
     if (!BuildPushMessage(wwid, birthday, &message)) {
       LOG(ERROR) << "Build push message failed, wwid:" << birth_info.wwid();
       continue;
     }
     LOG(INFO) << "Build push message success, wwid:" << birth_info.wwid();
     string message_desc = "push user birthday message";
     if (!push_sender_->SendPush(push_controller::kMessageBirth,
                                 message_desc,
                                 birth_info.wwid(),
                                 message)) {
       LOG(ERROR) << "Send push failed, wwid:" << birth_info.wwid();
       continue;       
     }
     LOG(INFO) << "Send push success, wwid:" << birth_info.wwid();
  }
  return true;
}

bool BirthPushProcessor::BuildPushMessage(const string& user_wwid, 
                                          const string& birthday,
                                          Json::Value* message) {
 LOG(INFO) <<"start to build birth push message";
 (*message)["id"] = user_wwid;
 (*message)["birthday"] = birthday;
 return true;
}

} // namespace_birthday_push_controller
