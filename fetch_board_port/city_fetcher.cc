//Copyright 2016 Mobvoi Inc. All Rights Reserved.
//Author: chhyu@mobvoi.com (Changhe Yu)

#include "base/at_exit.h"
#include "base/binary_version.h"
#include "base/compat.h"
#include "base/file/simple_line_reader.h"
#include "base/log.h"
#include "base/string_util.h"
#include "thirdparty/gflags/gflags.h"
#include "thirdparty/pbrpc/output/include/sofa/pbrpc/pbrpc.h"
#include "thirdparty/jsoncpp/json.h"
#include "util/net/http_server/http_request.h"
#include "util/net/http_server/http_response.h"
#include "util/net/http_server/http_server.h"
#include "util/net/http_server/http_handler.h"
#include "util/net/http_server/event_loop.h"
#include "util/url/encode/url_encode.h"
#include "util/net/email/email_client.h"
#include "util/net/http_client/http_client.h"
#include <google/protobuf/stubs/stringprintf.h>
#include "base/file/file.h"
#include "crawler/doc_handler/doc_processor.h"
#include "thirdparty/gflags/gflags.h"
#include "util/parser/template_parser/template_parser.h"
#include "util/net/http_client/http_client.h"

DEFINE_int32(listen_port, 8160, "");
DEFINE_int32(http_server_thread_num, 1, "");
DEFINE_string(html_file, "", "");
namespace serving {

class FetchPortHandler {
public:
	FetchPortHandler() {
		srand (time(NULL));}

		~FetchPortHandler() {
		}

		string ErrorInfo() const {
			Json::Value node;
			node["status"] = "error";
			node["data"] = Json::Value(Json::ValueType::arrayValue);
			return node.toStyledString();
		}

		bool FetchUrl(const string& url, string* content) {
			util::HttpClient http_client;
			if (!http_client.FetchUrl(url) || http_client.response_code() != 200) {
				LOG(ERROR)<< "feature url failed, url = " << url;
				return false;
			}
			content->append(http_client.ResponseBody());
			return true;
		}

		bool BoardingPort(util::HttpRequest* request, util::HttpResponse* response) {
			string url = "http://www.stats.gov.cn/tjsj/tjbz/xzqhdm/201504/t20150415_712722.html";
			string template_file = "config/crawler/doc_merger/parser_templates/city.template";
			//base::AtExitManager at_exit;
			//google::ParseCommandLineFlags(&argc, &argv, false);
      map<string, string> params;
      request->GetQueryParams(&params);
      string flight_no = params["flight_no"];
      string time = params["time"];
      //url = url + flight_no + "-" + time + ".html";
			string html;
			if (!FLAGS_html_file.empty()) {
				file::File::ReadFileToString(FLAGS_html_file, &html);
				CHECK(!html.empty());
				LOG(INFO)<< "html size:" << html.size();
			} else {
				util::HttpClient http_client;
				if (!http_client.FetchUrl(url) || http_client.response_code() != 200) {
					LOG(INFO) << "Fail to fetch url:" << url;
					return false;
				}
				crawl::DocProcessor doc_processor;
				crawl::CrawlDoc doc;
				doc.set_url(url);
				doc.mutable_response()->set_header(http_client.ResponseHeader());
				doc.mutable_response()->set_content(http_client.ResponseBody());
				doc_processor.ParseDoc(&doc);
				html = doc.response().content();
			}

			util::HtmlParser html_parser;
			util::TemplateParser template_parser(template_file, &html_parser);
			template_parser.PreProcess(url, 0);
			html_parser.Parse(html);
			Json::Value root;
			if (!template_parser.Parse(&root)) {
				LOG(ERROR) << "Fail to parse doc with template file.";
			}
			LOG(INFO) << root.toStyledString();
			LOG(INFO) << "Done.";
printf("result ",root.toStyledString().c_str());
			response->AppendBuffer(root.toStyledString());
			return true;
		}
		//  Thread-safe
		bool HandlerFetchRequest(util::HttpRequest* request,
				util::HttpResponse* response) {
			response->AppendHeader("Content-Type", "application/json;charset=UTF-8");
			if (BoardingPort(request, response)) {
				LOG(INFO)<< "Get board port";
				return true;
			} else {
				response->AppendBuffer(ErrorInfo());
			}
			return true;
		}
	};
}
// namespace serving

int main(int argc, char **argv) {
	base::AtExitManager at_exit;
	mobvoi::SetupBinaryVersion();
	google::ParseCommandLineFlags(&argc, &argv, false);
	google::InitGoogleLogging(argv[0]);
	SOFA_PBRPC_SET_LOG_LEVEL (NOTICE);

	util::HttpServer http_server(FLAGS_listen_port,
			static_cast<size_t>(FLAGS_http_server_thread_num));

	serving::FetchPortHandler fetch_port_handler;

	base::ResultCallback2<bool, util::HttpRequest*, util::HttpResponse*>* callback_port =
			base::NewPermanentCallback(&fetch_port_handler,
					&serving::FetchPortHandler::HandlerFetchRequest);
	util::DefaultHttpHandler handler_port(callback_port);
	http_server.RegisterHttpHandler("/board_port", &handler_port);
	REGISTER_STATUS(http_server, fetch_port_server);
	http_server.Serv();
	return 0;
}
