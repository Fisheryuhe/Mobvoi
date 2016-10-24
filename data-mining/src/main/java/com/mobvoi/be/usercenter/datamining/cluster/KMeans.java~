//Copyright 2016 Mobvoi Inc. All Rights Reserved
// Author chhyu@mobvoi.com(Changhe Yu)

package com.mobvoi.be.usercenter.datamining.cluster;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import weka.clusterers.SimpleKMeans;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

public class KMeans {

  private static final Log LOG = LogFactory.getLog(KMeans.class);

  private int K;

  private SimpleKMeans kMeans;

  private List<DataPoint> data;

  private List<Cluster> clusters;

  public KMeans() {
    kMeans = new SimpleKMeans();
    data = null;
    clusters = null;
  }

  public KMeans(int k, Collection<? extends DataPoint> input) throws Exception {
    K = k;
    kMeans = new SimpleKMeans();
    kMeans.setNumClusters(K);
    clusters = null;
    data.addAll(input);
  }

  public void setK(int k) throws Exception {
    K = k;
    kMeans.setNumClusters(K);
  }

  public void setData(Collection<? extends DataPoint> input) {
    data = new ArrayList<DataPoint>();
    data.addAll(input);
  }

  public List<Cluster> getClusters() {
    return clusters;
  }

  public int getNumClusters() {
    return clusters.size();
  }

  public List<DataPoint> getData() {
    return data;
  }
  
  public int getK() {
    K = clusters.size();
    return K;
  }

  /**
   * subclass can override this function to recalculate dist.
   * default is Euclidean distance.
   */
  protected double distFunc(double[] c1, double[] c2) {
    double dist = 0;
    for (int i = 0; i < c1.length; i++) {
      dist += (c1[i] - c2[i]) * (c1[i] - c2[i]);
    }
    return Math.sqrt(dist);
  }

  /**
   * return the cluster that has the most points
   */
  public Cluster getMaxCluster() {
    if (clusters.size() == 0)
      return null;
    int max_cluster_id = 0, max_cluster_size = clusters.get(0).points.size();
    for (int i = 1; i < clusters.size(); i++) {
      if (clusters.get(i).points.size() > max_cluster_size) {
        max_cluster_id = i;
        max_cluster_size = clusters.get(i).points.size();
      }
    }
    return clusters.get(max_cluster_id);
  }

  /**
   * return the data id which has the shortest distance to cluster
   */
  public DataPoint getMinDistPoint(Cluster cluster) {
    double min_dist = Double.MAX_VALUE;
    DataPoint closedPoint = null;
    for (DataPoint point : cluster.points) {
      double tmp_dist = distFunc(point.value, cluster.value);
      if (tmp_dist < min_dist) {
        min_dist = tmp_dist;
        closedPoint = point;
      }
    }
    return closedPoint;
  }

  /**
   * run kmeans of WeKa
   */
  public void runCluster() throws Exception {
    Instances instances = constructInstances();
    if (instances == null) {
      LOG.error("KMeans : no input data for cluster");
      return;
    }
    kMeans.buildClusterer(instances);
    Instances centers = kMeans.getClusterCentroids();
    Cluster cluster;
    clusters = new ArrayList<Cluster>();
    for (int i = 0; i < centers.numInstances(); i++) {
      int n = centers.instance(i).numValues();
      cluster = new Cluster();
      cluster.points = new ArrayList<DataPoint>();
      cluster.value = new double[n];
      for (int j = 0; j < n; j++) {
        cluster.value[j] = centers.instance(i).value(j);
      }
      clusters.add(cluster);
    }
    for (int i = 0; i < instances.numInstances(); i++) {
      int assignment = kMeans.clusterInstance(instances.instance(i));
      clusters.get(assignment).points.add(data.get(i));
    }
  }

  /**
   * combine the closed centers
   */
  public void combine(double threshold) {
    while (clusters.size() > 0) {
      boolean flag = true;
      Iterator<Cluster> it = clusters.iterator();
      while (it.hasNext()) {
        Cluster a = it.next();
        Cluster closedCluster = null;
        for (Cluster b : clusters) {
          if (a != b && distFunc(a.value, b.value) < threshold) {
            closedCluster = b;
            break;
          }
        }
        if (closedCluster != null) {
          closedCluster.points.addAll(a.points);
          closedCluster.recalculateCenter();
          it.remove();
          flag = false;
        }
      }
      if (flag) break;
    }
  }

  /**
   * transform list into instances
   */
  private Instances constructInstances() throws IOException {
    if (data.size() == 0)
      return null;
    StringBuilder sBuilder = new StringBuilder();
    int vector_len = data.get(0).value.length;
    for (int i = 0; i < vector_len - 1; i++) {
      sBuilder.append(Integer.toString(i)).append(",");
    }
    sBuilder.append("x\n");
    for (DataPoint point : data) {
      boolean isFirst = true;
      for (double num : point.value) {
        if (isFirst) isFirst = false;
        else sBuilder.append(',');
        sBuilder.append(num);
      }
      sBuilder.append('\n');
    }
    ByteArrayInputStream istream = new ByteArrayInputStream(sBuilder.toString().getBytes());
    CSVLoader loader = new CSVLoader();
    loader.setSource(istream);
    Instances instances = loader.getDataSet();
    istream.close();
    for (int i = 0; i < data.size(); i++) {
      instances.instance(i).setWeight(data.get(i).weight);
    }
    return instances;
  }
}
