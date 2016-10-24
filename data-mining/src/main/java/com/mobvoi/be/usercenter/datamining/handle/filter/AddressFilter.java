//Copyright 2016 Mobvoi Inc. All Rights Reserved
//Author : chhyu@mobvoi.com(Changhe Yu)

package com.mobvoi.be.usercenter.datamining.handle.filter;
import com.mobvoi.be.common.util.StringUtil;

public class AddressFilter {
  
  public boolean filter(String address) {
    try {
      String[] ss = StringUtil.split(address, ',');
      if (ss.length != 8) return false;
      if (Double.isInfinite(Double.valueOf(ss[6])) || Double.isNaN(Double.valueOf(ss[6]))
          || Double.isInfinite(Double.valueOf(ss[7])) || Double.isNaN(Double.valueOf(ss[7]))) {
        return false;
      }
      return true;
    } catch(Exception e){}
    return false;
  }

}
