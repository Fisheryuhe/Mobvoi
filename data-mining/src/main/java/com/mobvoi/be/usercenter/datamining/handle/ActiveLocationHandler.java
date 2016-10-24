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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mobvoi.be.common.model.Pair;
import com.mobvoi.be.common.util.GeoUtil;
import com.mobvoi.be.usercenter.datamining.cluster.Cluster;
import com.mobvoi.be.usercenter.datamining.cluster.DataPoint;
import com.mobvoi.be.usercenter.datamining.handle.filter.DataScaleFilter;
import com.mobvoi.be.usercenter.datamining.model.ActiveLocationData;
import com.mobvoi.be.usercenter.datamining.model.AddressDataPoint;
import com.mobvoi.be.usercenter.datamining.model.KeyValueData;
import com.mobvoi.be.usercenter.datamining.model.LocationData;
import com.mobvoi.be.usercenter.userinfo.model.ActiveArea;


public class ActiveLocationHandler implements IHandler {

  private static final Log LOG = LogFactory.getLog(ActiveLocationHandler.class);

  private static final double maxWeight = 30.0;

  private static final double minWeight = 1.0;

  private static final double msOfMinute = 60000.0;

  private static final double combineCenterThreshold = 200.0;

  private static final int numClusters = 8;

  private static final double minRadius = 200.0;

  private static final double maxRadius = 1000.0;

  private static final double densityThreshold = 0.5;

  private static final double radiusStep = 100;

  private static final double validWeightThreshold = 60 * 3;
  
  private DataScaleFilter scaleFilter = new DataScaleFilter();

  @Override
  public KeyValueData handle(String key, List<Object> datas) throws Exception {
    GeoKMeans geoKMeans = new GeoKMeans();
    // construct input data
    List<LocationData> datas_new = prepareData(geoKMeans, datas);
    if (!scaleFilter.filter(datas_new)) return null;
    
    // start the kmeans
    geoKMeans.runCluster();
    geoKMeans.combine(combineCenterThreshold);

    // return
    ActiveLocationData value = getAreas(key, geoKMeans);
    if (value == null)
      return null;

    KeyValueData ret = new KeyValueData(key, value);
    return ret;
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
      AddressDataPoint point = new AddressDataPoint();
      point.value = new double[2];
      point.value[0] = t1;
      point.value[1] = t2;
      point.address = locationData.address;
      KMeansInput.add(point);
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
    geoKMeans.setK(numClusters);
    geoKMeans.setData(KMeansInput);
    return datas_new;
  }

  /**
   * sort the locationData by timestamp
   */
  public class TimeComparator implements Comparator<Object> {
    @Override
    public int compare(Object o1, Object o2) {
      LocationData l1 = (LocationData) o1;
      LocationData l2 = (LocationData) o2;
      if (l1.timestamp == l2.timestamp)
        return 0;
      return l1.timestamp < l2.timestamp ? -1 : 1;
    }
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
   * given clusters and data points, return the addresses of centers
   * 
   * @param clusters
   *          , clusterd by kmeans
   */
  private ActiveLocationData getAreas(String key, GeoKMeans geoKMeans) {
    List<Cluster> clusters = geoKMeans.getClusters();
    int n = geoKMeans.getData().size();
    double totalWeight = 0;
    for (DataPoint p : geoKMeans.getData()) {
      totalWeight += p.weight;
    }
    ActiveLocationData data = new ActiveLocationData();
    int clusterId = 0;
    for (Cluster cluster : clusters) {
      clusterId++;
      List<Pair<Double, DataPoint>> dists = new ArrayList<Pair<Double, DataPoint>>();
      for (DataPoint dataPoint : cluster.points) {
        Pair<Double, DataPoint> p = new Pair<Double, DataPoint>();
        p.first = GeoUtil.calculateDistance(dataPoint.value[1], dataPoint.value[0],
            cluster.value[1], cluster.value[0]);
        p.second = dataPoint;
        dists.add(p);
      }
      Collections.sort(dists, new Comparator<Pair<Double, DataPoint>>() {
        @Override
        public int compare(Pair<Double, DataPoint> o1, Pair<Double, DataPoint> o2) {
          return o1.first.compareTo(o2.first);
        }
      });
      double timeCnt = 0, pointCnt = 0;
      double radius = minRadius;
      int i = 0;
      for (Pair<Double, DataPoint> pair : dists) {
        if (pair.first > radius)
          break;
        timeCnt += pair.second.weight;
        pointCnt++;
        i++;
      }
      double baseRatio = timeCnt / (radius * radius);
      while (radius < maxRadius) {
        double addTime = timeCnt, addPoint = pointCnt;
        double addRadius = radius + radiusStep;
        for (; i < dists.size(); i++) {
          if (dists.get(i).first < addRadius) {
            addTime += dists.get(i).second.weight;
            addPoint++;
          } else {
            break;
          }
        }
        if (addTime / (addRadius * addRadius) < baseRatio * densityThreshold) {
          break;
        }
        timeCnt = addTime;
        pointCnt = addPoint;
        radius = addRadius;
      }

      if (timeCnt < validWeightThreshold) {
        LOG.debug("user " + key + " cluster " + clusterId + " timeCoverage = " + timeCnt
            / totalWeight + ", point = " + pointCnt + ", radius = " + radius);
        continue;
      }

      AddressDataPoint point = (AddressDataPoint) geoKMeans.getMinDistPoint(cluster);
      ActiveArea area = new ActiveArea(point.getFormatedAddress(), point.getCity(),
          cluster.value[0], cluster.value[1], radius, pointCnt / n, timeCnt / totalWeight, timeCnt);
      data.areas.add(area);
      data.pointCoverage += area.pointCoverage;
      data.timeCoverage += area.timeCoverage;
      data.radius += area.radius;
    }
    if (data.areas.size() <= 0)
      return null;
    data.radius /= data.areas.size();
    return data;
  }

}
