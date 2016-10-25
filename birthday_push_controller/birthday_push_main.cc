// Copyright 2016. All Rights Reserved.
// Author: chhyu@mobvoi.com (Changhe Yu)

#include "base/at_exit.h"
#include "base/binary_version.h"
#include "base/log.h"
#include "third_party/gflags/gflags.h"
#include "recommendation/online/intelligent_push/birthday_push_controller/birthday_push_scheduler.h"
#include "recommendation/online/intelligent_push/birthday_push_controller/proto/birthday_meta.pb.h"

using namespace birthday_push_controller;

int main(int argc, char** argv) {
  base::AtExitManager at_exit;
  mobvoi::SetupBinaryVersion();
  google::ParseCommandLineFlags(&argc, &argv, false);
  google::InitGoogleLogging(argv[0]);

  vector<BirthInfo> birth_infos;
  BirthInfoFetcher birth_info_fetcher;
  birth_info_fetcher.Fetch(&birth_infos);
  return 0;
}
