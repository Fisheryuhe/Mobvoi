//Copyright 2016 Mobvoi Inc. All Rights Reserved
//Author : chhyu@mobvoi.com(Changhe Yu)

package com.mobvoi.be.usercenter.datamining.model;

import java.util.ArrayList;
import java.util.List;

import com.mobvoi.be.usercenter.userinfo.model.ActiveArea;

public class ActiveLocationData {

  public List<ActiveArea> areas = new ArrayList<ActiveArea>();
  
  public double pointCoverage = 0, timeCoverage = 0, time = 0, radius = 0;
  
}