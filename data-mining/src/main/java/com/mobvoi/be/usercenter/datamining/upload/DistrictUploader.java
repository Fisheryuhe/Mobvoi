//Copyright 2016 Mobvoi Inc. All Rights Reserved
//Author : chhyu@mobvoi.com(Changhe Yu)

package com.mobvoi.be.usercenter.datamining.upload;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.mobvoi.be.usercenter.client.UserCenterConstants;
import com.mobvoi.be.usercenter.client.model.Value;
import com.mobvoi.be.usercenter.datamining.model.DistrictData;

public class DistrictUploader extends BasicUserCenterUploader {

  @Override
  protected String getFamily() {
    return UserCenterConstants.Common.FAMILY_NAME;
  }

  @Override
  protected String getColumn() {
    return UserCenterConstants.Common.PREDICTED_DISTRICT_COLUMN;
  }
  
  @Override
  protected Value merge(Value v, Object item) {
    List<String> newDistricts = (DistrictData)item;
    if (CollectionUtils.isEmpty(newDistricts)) return v;
    if (v.isEmpty()) {
      v.set(newDistricts);
      return v;
    }
    List<String> districts = v.getList(String.class);
    Set<String> districtSet = new HashSet<String>();
    districtSet.addAll(districts);
    districtSet.addAll(newDistricts);
    v.set(districtSet);
    return v;
  }

}
