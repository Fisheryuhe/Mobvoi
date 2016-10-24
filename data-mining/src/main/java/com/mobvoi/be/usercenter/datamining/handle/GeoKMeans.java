//Copyright 2016 Mobvoi Inc. All Rights Reserved
//Author : chhyu@mobvoi.com(Changhe Yu)

package com.mobvoi.be.usercenter.datamining.handle;

import com.mobvoi.be.common.util.GeoUtil;
import com.mobvoi.be.usercenter.datamining.cluster.KMeans;

public class GeoKMeans extends KMeans {

  @Override
  protected double distFunc(double[] c1, double[] c2) {
    return GeoUtil.calculateDistance(c1[1], c1[0], c2[1], c2[0]);
  }

}
