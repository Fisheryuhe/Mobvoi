//Copyright 2016 Mobvoi Inc. All Rights Reserved
//Author : chhyu@mobvoi.com(Changhe Yu)

package com.mobvoi.be.usercenter.datamining.handle.filter;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.mobvoi.be.usercenter.datamining.model.LocationData;

public class DataScaleFilter {
  
  private int pointThreshold = 10;
  
  private int dayThreshold = 0;
  
  public boolean filter(List<LocationData> datas) {
    if (CollectionUtils.isEmpty(datas)) return false;
    if (datas.size() < pointThreshold) return false;
    int cnt = 0;
    int lastDay = 0;
    Calendar c = Calendar.getInstance();
    for (LocationData data : datas) {
      c.setTime(new Date(data.timestamp));
      int day = c.get(Calendar.DAY_OF_YEAR);
      if (day != lastDay) cnt++;
      lastDay = day;
    }
    if (cnt < dayThreshold) return false;
    return true;
  }

}
 
