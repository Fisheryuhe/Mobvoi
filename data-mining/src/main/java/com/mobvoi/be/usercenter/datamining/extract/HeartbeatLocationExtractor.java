//Copyright 2016 Mobvoi Inc. All Rights Reserved
//Author : chhyu@mobvoi(Changhe Yu)
package com.mobvoi.be.usercenter.datamining.extract;

import java.text.SimpleDateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mobvoi.be.common.location.GeoPoint;
import com.mobvoi.be.common.util.StringUtil;
import com.mobvoi.be.usercenter.datamining.model.KeyValueData;
import com.mobvoi.be.usercenter.datamining.model.LocationData;

public class HeartbeatLocationExtractor implements IExtractor {
  
  private static final Log LOG = LogFactory.getLog(HeartbeatLocationExtractor.class);
  
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

  @Override
  public KeyValueData extract(String line) {
    try {
      JSONObject obj = JSON.parseObject(line);
      String userId = obj.getString("user_id");
      String timeStr = obj.getString("@timestamp");
      String address = obj.getString("address");
      if (StringUtil.isEmpty(userId) || StringUtil.isEmpty(timeStr) || StringUtil.isEmpty(address)) {
        return null;
      }
      LocationData location = new LocationData();
      location.timestamp = DATE_FORMAT.parse(timeStr).getTime();
      String[] ss = StringUtil.split(address,',');
      if (ss.length != 8 || StringUtil.isEmpty(ss[6]) || StringUtil.isEmpty(ss[7])) {
        return null;
      }
      if (!Double.isInfinite(Double.valueOf(ss[6]))
          || Double.isInfinite(Double.valueOf(ss[7]))) {
        return null;
      }
      location.address = address;
      return new KeyValueData(userId, location);
    } catch(Exception e) {
      LOG.debug("extract line failed, line : " + line);
    }
    return null;
  }

}
