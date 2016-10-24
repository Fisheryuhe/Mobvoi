// CopyRight 2016 Mobvoi Inc. All Rights Reserved
// Author chhyu@mobvoi.com(Changhe Yu)
package com.mobvoi.be.usercenter.datamining.conf;

import com.mobvoi.be.common.config.AbstractConfigCenter;

public class DataMiningConfig extends AbstractConfigCenter {
  
  public static final String URL = "http://config-center/closets/106/drawers.json";
  
  public static final String NAME = "user-center"; 

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  protected String getConfigCenterURL() {
    return URL;
  }

}