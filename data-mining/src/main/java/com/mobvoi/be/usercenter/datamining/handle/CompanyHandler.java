//Copyright 2016 Mobvoi Inc. All Rights Reserved
//Author : chhyu@mobvoi.com(Changhe Yu)

package com.mobvoi.be.usercenter.datamining.handle;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.mobvoi.be.common.util.GeoUtil;
import com.mobvoi.be.usercenter.datamining.cluster.Cluster;
import com.mobvoi.be.usercenter.datamining.cluster.DataPoint;
import com.mobvoi.be.usercenter.datamining.handle.filter.DataScaleFilter;
import com.mobvoi.be.usercenter.datamining.model.AddressDataPoint;
import com.mobvoi.be.usercenter.datamining.model.KeyValueData;
import com.mobvoi.be.usercenter.datamining.model.LocationData;
import com.mobvoi.be.usercenter.userinfo.model.LocationDetailData;


public class CompanyHandler implements IHandler {
  
  private static final double maxWeight = 30.0;
  
  private static final double minWeight = 1.0;
  
  private static final double msOfMinute = 60000.0;
  
  private static final double combineCenterThreshold = 200.0;
  
  private static final double onePointThreshold = 500.0;
  
  private static final double percentageThreshold = 0.6;
  
  private DataScaleFilter scaleFilter = new DataScaleFilter();

  protected int getNumClusters() {
    return 5;
  }

  protected boolean isValidHour(int hour) {
    return ((hour >= 8 && hour <= 11) || (hour >= 13 && hour <= 18));
  }

  @Override
  public KeyValueData handle(String key, List<Object> datas) throws Exception {
    GeoKMeans geoKMeans = new GeoKMeans();
    // construct input data
    List<LocationData> datas_new = prepareData(geoKMeans, datas);
    if (!scaleFilter.filter(datas_new)) return null;

    // start the kmeans
    geoKMeans.runCluster();
    geoKMeans.combine(combineCenterThreshold);
    Cluster maxCluster = geoKMeans.getMaxCluster();
    double pred_lat = maxCluster.value[0];
    double pred_lng = maxCluster.value[1];

    if (!isValidUser(geoKMeans, pred_lat, pred_lng))
      return null;

    // return
    AddressDataPoint point = (AddressDataPoint)geoKMeans.getMinDistPoint(maxCluster);
    LocationDetailData value = new LocationDetailData(
        point.getFormatedAddress(), point.getCity(), pred_lat, pred_lng);
    KeyValueData ret = new KeyValueData(key, (Object) value);
    return ret;
  }

  /**
   * sort the locationData by timestamp
   */
  public class TimeComparator implements Comparator<Object> {

    @Override
    public int compare(Object o1, Object o2) {
      LocationData l1 = (LocationData) o1;
      LocationData l2 = (LocationData) o2;
      if (l1.timestamp == l2.timestamp) return 0;
      return l1.timestamp - l2.timestamp < 0 ? -1 : 1;
    }

  }

  /**
   * filter the valid data
   */
  private List<LocationData> prepareData(GeoKMeans geoKMeans, List<Object> datas) throws Exception {
    TimeComparator comparator = new TimeComparator();
    Collections.sort(datas, comparator);
    List<LocationData> datas_new = new ArrayList<LocationData>();
    Collection<DataPoint> KMeansInput = new ArrayList<DataPoint>();
    for (Object i : datas) {
      LocationData locationData = (LocationData) i;
      Date date = new Date(locationData.timestamp);
      Calendar c = Calendar.getInstance();
      c.setTime(date);
      int weekday = c.get(Calendar.DAY_OF_WEEK);
      if (weekday == Calendar.SUNDAY || weekday == Calendar.SATURDAY)
        continue;
      if (isHoliday(c)) {
        continue;
      }
      int hour = c.get(Calendar.HOUR_OF_DAY);
      if (!isValidHour(hour))
        continue;
      double t1, t2;
      String[] ss = locationData.address.split(",");
      try {
        t1 = Double.parseDouble(ss[6]);
        t2 = Double.parseDouble(ss[7]);
        if (Double.isInfinite(t1) || Double.isInfinite(t2) || Double.isNaN(t1) || Double.isNaN(t2))
          continue;
      } catch (Exception e) {
        continue;
      }
      AddressDataPoint dataPoint = new AddressDataPoint();
      dataPoint.value = new double[2];
      dataPoint.value[0] = t1;
      dataPoint.value[1] = t2;
      dataPoint.address = locationData.address;
      KMeansInput.add(dataPoint);
      datas_new.add(locationData);
    }
    if (datas_new.size() == 0)
      return datas_new;
    double[] weights = getWeight(datas_new);
    Iterator<DataPoint> iterator = KMeansInput.iterator();
    int id = 0;
    while (iterator.hasNext()) {
      iterator.next().weight = weights[id];
      id++;
    }
    geoKMeans.setK(getNumClusters());
    geoKMeans.setData(KMeansInput);
    return datas_new;
  }

  /**
   * get weights from timestamp
   */
  private double[] getWeight(List<LocationData> datas) {
    double[] weights = new double[datas.size()];
    for (int i = 0; i < datas.size(); i++) {
      if (i > 0) {
        double tmp_weight = (datas.get(i).timestamp - datas.get(i - 1).timestamp) / msOfMinute / 2;
        tmp_weight = Math.min(maxWeight, tmp_weight);
        tmp_weight = Math.max(minWeight, tmp_weight);
        weights[i] += tmp_weight;
      }
      if (i < datas.size() - 1) {
        double tmp_weight = (datas.get(i + 1).timestamp - datas.get(i).timestamp) / msOfMinute / 2;
        tmp_weight = Math.min(maxWeight, tmp_weight);
        tmp_weight = Math.max(minWeight, tmp_weight);
        weights[i] += tmp_weight;
      }
    }
    return weights;
  }

  /**
   * judge if there are enough points near the cluster center
   */
  private boolean isValidUser(GeoKMeans geoKMeans, double plat, double plng) {
    double totalTime = 0, validTime = 0;
    for (DataPoint point : geoKMeans.getData()) {
      totalTime += point.weight;
      if (GeoUtil.calculateDistance(plng, plat, point.value[1], point.value[0]) < onePointThreshold) {
        validTime += point.weight;
      }
    }
    if (validTime > totalTime * percentageThreshold) {
      return true;
    }
    return false;
  }
  
  private boolean isHoliday(Calendar c) {
    if (c.get(Calendar.MONTH) == Calendar.OCTOBER && c.get(Calendar.DAY_OF_MONTH) <= 7) return true;
    if (c.get(Calendar.MONTH) == Calendar.MAY && c.get(Calendar.DAY_OF_MONTH) <= 7) return true;
    return false;
  }

}
