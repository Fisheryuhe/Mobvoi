//Copyright 2016 Mobvoi Inc. All Rights Reserved
//Author chhyu@mobvoi.com(Changhe Yu)
package com.mobvoi.be.usercenter.datamining.cluster;

import java.util.List;

public class Cluster {
  
  public double[] value;

  public List<DataPoint> points;
  
  public void recalculateCenter() {
    int n = value.length;
    double weight = 0; 
    double[] sum = new double[n];
    for (int i = 0; i < n; i++) sum[i] = 0;
    for (DataPoint point : points) {
      weight += point.weight;
      for (int i = 0; i < n; i++) {
        sum[i] += point.value[i] * point.weight;
      }
    }
    for (int i = 0; i < n; i++) {
      value[i] = sum[i] / weight;
    }
  }

}
