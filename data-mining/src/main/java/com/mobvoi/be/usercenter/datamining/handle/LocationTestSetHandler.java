//Copyright 2016 Mobvoi Inc. All Rights Reserved
//Author : chhyu@mobvoi.com(Changhe Yu)

package com.mobvoi.be.usercenter.datamining.handle;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mobvoi.be.common.util.CollectionUtil;
import com.mobvoi.be.common.util.GeoUtil;
import com.mobvoi.be.common.util.StringUtil;
import com.mobvoi.be.usercenter.client.IUserCenterTable;
import com.mobvoi.be.usercenter.client.UserCenterClientFactory;
import com.mobvoi.be.usercenter.client.UserCenterConstants;
import com.mobvoi.be.usercenter.client.UserCenterException;
import com.mobvoi.be.usercenter.client.model.Value;
import com.mobvoi.be.usercenter.datamining.model.KeyValueData;
import com.mobvoi.be.usercenter.datamining.model.LocationData;
import com.mobvoi.be.usercenter.datamining.model.LocationTestSetData;
import com.mobvoi.be.usercenter.datamining.model.LocationTestSetData.Location;

public class LocationTestSetHandler implements IHandler {
  
  private static final Log LOG = LogFactory.getLog(LocationTestSetHandler.class);
  
  private double distance;
  
  private int threshold;
  
  private List<String> locationTypes;

  private IUserCenterTable table;
  
  public void init() throws UserCenterException {
    table = UserCenterClientFactory.getUserCenterTable(UserCenterConstants.TABLE_NAME);
  }

  @Override
  public KeyValueData handle(String key, List<Object> datas) throws Exception {
    try {
      if (StringUtil.isNotEmpty(key) && CollectionUtil.isNotEmpty(datas)) {
        Value value = table.get(key, UserCenterConstants.Common.FAMILY_NAME,
            UserCenterConstants.Common.CUSTOM_LOCATION_COLUMN);
        if (value != null && !value.isEmpty()) {
          JSONObject obj = JSON.parseObject(value.getString());
          LocationTestSetData data = new LocationTestSetData();
          for (String type : locationTypes) {
            if (obj.containsKey(type)) {
              JSONObject item = obj.getJSONObject(type);
              double lat = item.getDouble("lat");
              double lng = item.getDouble("lng");
              int validCnt = 0;
              for (Object d : datas) {
                LocationData location = (LocationData)d;
                String[] ss = StringUtil.split(location.address, ',');
                double dataLat = Double.valueOf(ss[6]);
                double dataLng = Double.valueOf(ss[7]);
                if (GeoUtil.calculateDistance(lng, lat, dataLng, dataLat) < distance) {
                  validCnt++;
                }
              }
              if (validCnt >= threshold) {
                data.put(type, new Location(lat, lng));
              } else {
                LOG.debug("valid cnt is smaller than " + threshold + ", lat = " + lat + ", lng = " + lng);
              }
            }
          }
          if (!data.isEmpty()) {
            return new KeyValueData(key, data);
          }
        }
      }
    } catch(Exception e) {
      LOG.debug(e, e);
    }
    return null;
  }

  public void setThreshold(int threshold) {
    this.threshold = threshold;
  }

  public void setLocationTypes(List<String> locationTypes) {
    this.locationTypes = locationTypes;
  }

  public void setDistance(double distance) {
    this.distance = distance;
  }

}
