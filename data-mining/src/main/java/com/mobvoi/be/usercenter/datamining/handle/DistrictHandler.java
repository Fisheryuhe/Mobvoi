//Copyright 2016 Mobvoi Inc. All Rights Reserved
//Author : chhyu@mobvoi.com(Changhe Yu)

package com.mobvoi.be.usercenter.datamining.handle;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mobvoi.be.common.util.CollectionUtil;
import com.mobvoi.be.common.util.StringUtil;
import com.mobvoi.be.usercenter.datamining.model.DistrictData;
import com.mobvoi.be.usercenter.datamining.model.KeyValueData;
import com.mobvoi.be.usercenter.datamining.model.LocationData;

public class DistrictHandler implements IHandler {
  
  public static final long ACTIVITY_TIME_THRESHOLD = 1000L * 60 * 60 * 3; //3 hours.
  
  public static final int ACTIVITY_DATE_THRESHOLD = 7; //7 days.
  
  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
  
  private static class DistrictInfo {
    
    public int cnt = 0;
    
    public String lastDate = "";
    
  }
  
  @Override
  public KeyValueData handle(String key, List<Object> datas) throws Exception {
    Map<String, DistrictInfo> districtMap = new HashMap<String, DistrictInfo>();
    List<LocationData> locations = getLocations(datas);
    String lastDistrict = "";
    long timestamp = 0;
    for (LocationData location : locations) {
      String district = getDistrict(location.address);
      if (district == null) continue;
      if (district.equals(lastDistrict)) {
        if (location.timestamp - timestamp >= ACTIVITY_TIME_THRESHOLD) {
          String date = DATE_FORMAT.format(new Date(location.timestamp));
          DistrictInfo info = districtMap.get(district);
          if (info == null) {
            info = new DistrictInfo();
            districtMap.put(district, info);
          }
          if (info.lastDate.compareTo(date) < 0) {
            info.cnt++;
            info.lastDate = date;
          }
          timestamp = location.timestamp;
        }
      } else {
        timestamp = location.timestamp;
        lastDistrict = district;
      }
    }
    DistrictData data = new DistrictData();
    for (Entry<String, DistrictInfo> entry : districtMap.entrySet()) {
      String district = entry.getKey();
      DistrictInfo info = entry.getValue();
      if (info.cnt >= ACTIVITY_DATE_THRESHOLD) {
        data.add(district);
      }
    }
    if (CollectionUtil.isNotEmpty(data)) {
      return new KeyValueData(key, data);
    } else {
      return null;
    }
  }
  
  private static String getDistrict(String address) {
    String[] ss = StringUtil.split(address, ',');
    if (ss.length != 8) return null;
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 4; i++) {
      if (StringUtil.isEmpty(ss[i])) return null;
      if (i != 0) sb.append(',');
      sb.append(ss[i]);
    }
    return sb.toString();
  }
  
  private List<LocationData> getLocations(List<Object> datas) {
    List<LocationData> locations = new ArrayList<LocationData>();
    for (Object data : datas) {
      locations.add((LocationData)data);
    }
    Collections.sort(locations, new Comparator<LocationData>() {
      @Override
      public int compare(LocationData o1, LocationData o2) {
        if (o1.timestamp == o2.timestamp) return 0;
        return o1.timestamp < o2.timestamp ? -1 : 1;
      }
    });
    return locations;
  }

}