// Copyright 2016 Mobvoi Inc. All Rights Reserved.
// Author: chhyu@mobvoi.com(Changhe Yu)

#ifndef RECOMMENDATION_ONLINE_INTELLIGENT_PUSH_BIRTHDAY_PUSH_CONTROLLER_PUSH_PROCESSOR_H_
#define RECOMMENDATION_ONLINE_INTELLIGENT_PUSH_BIRTHDAY_PUSH_CONTROLLER_PUSH_PROCESSOR_H_

#include <memory>

#include "base/basictypes.h"
#include "base/compat.h"
#include "base/thread.h"
#include "recommendation/online/intelligent_push/birthday_push_controller/proto/birthday_meta.pb.h"
#include "recommendation/online/intelligent_push/push_controller/push_sender.h"
#include "recommendation/online/intelligent_push/push_controller/proto/push_meta.pb.h"

namespace birthday_push_controller {

class PushProcessor : public mobvoi::Thread {
  public:
    PushProcessor();
    virtual ~PushProcessor();
    void Run();
    void Init();
  protected:
    std::unique_ptr<push_controller::PushSender> push_sender_;
  private:
    DISALLOW_COPY_AND_ASSIGN(PushProcessor);
};
class BirthPushProcessor : public PushProcessor {
  public:
    BirthPushProcessor();
    virtual ~BirthPushProcessor();
    bool Process(vector<BirthInfo>* birth_infos);
    bool BuildPushMessage(const string& user_wwid, const string& birthday,
                          Json::Value* message);
  private:
    DISALLOW_COPY_AND_ASSIGN(BirthPushProcessor);
};
} // namespace birthday_push_controller

#endif //  RECOMMENDATION_ONLINE_INTELLIGENT_PUSH_BIRTHDAY_PUSH_CONTROLLER_PUSH_PROCESSOR_H_
