// Copyright 2016 Mobvoi Inc. All Rights Reserved.
// Author: chhyu@mobvoi.com (Changhe Yu)

#include <sstream>

#include "recommendation/online/intelligent_push/birthday_push_controller/birthday_push_scheduler.h"

#include "base/log.h"
#include "base/file/proto_util.h"
#include "base/string_util.h"
#include "third_party/gflags/gflags.h"
#include "third_party/jsoncpp/json.h"
#include "third_party/mysql_client_cpp/include/mysql_connection.h"
#include "third_party/mysql_client_cpp/include/mysql_driver.h"
#include "third_party/mysql_client_cpp/include/cppconn/driver.h"
#include "third_party/mysql_client_cpp/include/cppconn/exception.h"
#include "third_party/mysql_client_cpp/include/cppconn/resultset.h"
#include "third_party/mysql_client_cpp/include/cppconn/statement.h"
#include "util/mysql/mysql_util.h"

DEFINE_string(birthday_mysql_config,
 "config/recommendation/birthday_push_controller/mysql_server_account.conf",
 "config user birthday data");

namespace {
   static const char kQueryFormat[] = (
       "SELECT wwid FROM account WHERE account_id ='%s';"
       );
}

namespace birthday_push_controller {

BirthInfoFetcher::BirthInfoFetcher() {
  LOG(INFO) << "Construct BirthInfoFetcher";
  Init();
}

BirthInfoFetcher::~BirthInfoFetcher() {}

bool BirthInfoFetcher::Fetch(vector<BirthInfo>* birth_infos) {
  birth_infos->clear();
  try {
    sql::Driver* driver = sql::mysql::get_driver_instance();
    std::unique_ptr<sql::Connection> connection(driver->connect(
     mysql_server_->host(),
     mysql_server_->user(),
     mysql_server_->password()));
    connection->setSchema(mysql_server_->database());
    std::unique_ptr<sql::Statement> statement(connection->createStatement());
    string query_sql = BuildQuerySql();
    std::unique_ptr<sql::ResultSet> result_set(
      statement->executeQuery(query_sql));
    if (result_set->rowsCount() > 0) {
      while (result_set->next()) {
        BirthInfo birth_info;
        string user_id = result_set->getString("account_id");
        string birth_date = result_set->getString("birthday");
        if (!IsValidData(user_id, birth_date)) {
          continue;
        }
        string user_wwid;
        if (FetchWwidByAccountID(user_id, &user_wwid)) {
           birth_info.set_wwid(user_wwid);
        } else {
         continue;
        }
        birth_info.set_user_id(user_id);
        birth_info.set_birth_date(birth_date);
        birth_infos->push_back(birth_info);
      }
    }
    PrintInfo(birth_infos);
    return true;
  } catch (const sql::SQLException &e) {
    LOG(ERROR) << "# ERR: " << e.what();
    return false;
  }
}

bool BirthInfoFetcher::FetchWwidByAccountID(const string& id, string* wwid) {
   string command = StringPrintf(kQueryFormat, id.c_str());
   Json::Value user_wwid;
   if (!util::GetMysqlResult(
          FLAGS_birthday_mysql_config, command, &user_wwid, true)) {
        LOG(ERROR) << "Failed execute mysql command: " << command;
        return false;
      }
      for (Json::ArrayIndex i = 0; i < user_wwid.size(); ++i) {
           *wwid = user_wwid[i]["wwid"].asString();
      }    
    return true;
}

bool BirthInfoFetcher::PrintInfo(vector<BirthInfo>* birth_infos) {
   for (auto& birth_info: *birth_infos) {
      LOG(INFO) << "user_id:" << birth_info.user_id() << "birthday:" 
        << birth_info.birth_date() << "wwid :" << birth_info.wwid();
   }
}

void BirthInfoFetcher::Init() {
  mysql_server_.reset(new MysqlServer());
  CHECK(file::ReadProtoFromTextFile(FLAGS_birthday_mysql_config, mysql_server_.get()));
  LOG(INFO) << "Read MySQLConf from file" << FLAGS_birthday_mysql_config;
}

bool BirthInfoFetcher::IsValidData(const string& user_id, 
                                   const string& birth_date) {
  if (user_id.empty() || birth_date.empty()) {
    return false;
  } else if (!IsValidYearRange(birth_date)) {
    return false;
  } else {
    return true;
  }
}

bool BirthInfoFetcher::IsValidYearRange(const string& birth_date) {
  time_t t = time(0);
  struct tm *now = localtime(&t);
  int year_now = now->tm_year + 1900;
  char split = '.';
  int index = birth_date.find_first_of(split, 0);
  string year_str = birth_date.substr(0, index);
  int year = StringToInt(year_str);
  if (year > (year_now -70) && year < (year_now -10)) {
    return true;
  } else {
    LOG(WARNING) << "illegal birthday";
    return false;
  }
}
string BirthInfoFetcher::BuildQuerySql() {
  time_t t = time(0);
  struct tm *now = localtime(&t);
  int month = now->tm_mon + 1;
  int day = now->tm_mday;
  vector<string> month_vec;
  vector<string> day_vec;
  if (month >= 10) {
    month_vec.push_back(StringPrintf("%d", month)); 
  } else {
    month_vec.push_back(StringPrintf("%d", month)); 
    month_vec.push_back(StringPrintf("0%d", month));
  }
  if (day >= 10) {
    day_vec.push_back(StringPrintf("%d", day)); 
  } else {
    day_vec.push_back(StringPrintf("%d", day)); 
    day_vec.push_back(StringPrintf("0%d", day));
  }
  
  std::stringstream ss;
  ss << "SELECT account_id, birthday FROM account_info WHERE ";
  for (auto& month_str : month_vec) {
    for (auto& day_str : day_vec) {
      ss << "birthday LIKE " << "'%." << month_str << "." << day_str << "' OR "; 
    }
  }
  string query = ss.str();
  query = query.substr(0, query.size() -4) + ";";
  LOG(INFO) << query;
  return query;
}

} // namespace birthday_push_controller
