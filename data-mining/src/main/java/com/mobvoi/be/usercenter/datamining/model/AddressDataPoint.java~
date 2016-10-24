//Copyright 2016 Mobvoi Inc. All Rights Reserved
//Author : chhyu@mobvoi.com(Changhe Yu)

package com.mobvoi.be.usercenter.datamining.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.mobvoi.be.common.util.StringUtil;
import com.mobvoi.be.usercenter.datamining.cluster.DataPoint;

public class AddressDataPoint extends DataPoint {
  
  public String address;
  
  @JSONField(serialize=false)
  public String getFormatedAddress() {
    String[] parts = StringUtil.split(address, ',');
    StringBuilder sb = new StringBuilder();
    for (int j = 2; j < 6; j++) {
      sb.append(parts[j]);
    }
    return sb.toString();
  }
  
  @JSONField(serialize=false)
  public String getCity() {
    String[] parts = StringUtil.split(address, ',');
    return parts[2];
  }
  
}
