//Copyright 2016 Mobvoi Inc. All Rights Reserved
//Author : chhyu@mobvoi.com(Changhe Yu)

package com.mobvoi.be.usercenter.datamining.model;

import com.alibaba.fastjson.JSON;
import com.mobvoi.be.common.util.JSONUtil;
import com.mobvoi.be.common.util.StringUtil;


@SuppressWarnings({"unchecked","rawtypes"})
public class KeyValueData {
  
  public static final String SEPERATOR = "~!@#";
  
  public String key;
  
  public Object value;
  
  public KeyValueData(String key, Object value) {
    this.key = key;
    this.value = value;
  }
  
  public KeyValueData(String line, Class valueClazz) throws Exception {
    String[] ss = line.split(SEPERATOR);
    if (ss.length != 2) {
      throw new Exception("bad line. line : " + line);
    }
    if (StringUtil.isEmpty(ss[0]) || StringUtil.isEmpty(ss[1])) {
      throw new Exception("empty line. line : " + line);
    }
    key = ss[0];
    value = JSONUtil.objectFromJSONString(ss[1], valueClazz);
  }
  
  @Override
  public String toString() {
    return key + SEPERATOR + JSON.toJSONString(value);
  }
  
}
