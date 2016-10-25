//Copyright 2016 Mobvoi Inc. All Rights Reserved.
//Author:chhyu@mobvoi.com (Changhe Yu)
package(default_visibility = ["//visibility:public"])

cc_binary(
    name = 'board_port',
    srcs = [
        'board_port.cc',
    ],
    deps = [
        '//base:base',
        '//base:binary_version',
        '//base/file:simple_line_reader',
        '//crawler/base:dns_client',
        '//crawler/proto:crawldoc_proto',
        '//thirdparty/pbrpc:pbrpc',
        '//thirdparty/jsoncpp:jsoncpp',
        '//util/url/encode:url_encode',
        '//util/url/parser:url_parser',
        '//util/parser/html_parser:html_parser',
        '//util/parser/template_parser:template_parser',
        '//util/net:util',
        '//util/net/http_server:http_server',
        '//util/net/email:email_client',
        '//base/file:file',
        '//crawler/doc_handler:doc_processor',
        '//util/net/http_client:http_client',
    ],
)
