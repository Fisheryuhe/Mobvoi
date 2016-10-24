// Copyright 2016 Mobvoi Inc.All Rights Reserved
// Author chhyu@mobvoi.com(Changhe Yu)
package com.mobvoi.be.usercenter.datamining.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mobvoi.be.usercenter.datamining.model.AddressDataPoint;

import weka.clusterers.DBSCAN;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.ProtectedProperties;

public class DBscan {
	private static final Log LOG = LogFactory.getLog(DBscan.class);
	private DBSCAN dbscan;
	private int minPoints;
	private double epsilon;
	private List<DataPoint> data = new ArrayList<DataPoint>();
	private List<Cluster> clusters ;
	private final Attribute ID_ATTRIBUTE = new Attribute("id");
	private final Attribute LATITUDE_ATTRIBUTE = new Attribute("latitude");
	private final Attribute LONGITUDE_ATTRIBUTE = new Attribute("longitude");
//	FastVector attr = null;
//	private final Attribute ADDR_ATTRIBUTE = new Attribute("address", attr, new ProtectedProperties(new Properties()));
	private final Attribute ADDR_ATTRIBUTE = new Attribute("address");
	private final Attribute WEIGHT_ATTRIBUTE = new Attribute("weight");
	private HashMap<Double,String> address_map = new HashMap<Double,String>();

	public DBscan() {
		dbscan = new DBSCAN();
		data = null;
		clusters = null;
		// clusterer.setEpsilon(0.00898);
		// clusterer.setMinPoints(1);
		// clusterer.setDatabase_Type("weka.clusterers.forOPTICSAndDBScan.Databases.SequentialDatabase");
		// clusterer.setDatabase_distanceType("weka.clusterers.forOPTICSAndDBScan.DataObjects.EuclidianDataObject");
		
	}

	public DBscan(int minPoints, double epsilon,
			Collection<? extends DataPoint> input) throws Exception {
		this.minPoints = minPoints;
		this.epsilon = epsilon;
		dbscan = new DBSCAN();
		dbscan.setMinPoints(this.minPoints);
		dbscan.setEpsilon(this.epsilon);
		clusters = null;
		data.addAll(input);
//	    for (DataPoint dataPoint:data) {
//	    	  System.out.println(dataPoint.value[0] + " " + dataPoint.value[1]);
//	      }
	}

	public void setMinPoints(int minPoints) throws Exception {
		minPoints = minPoints;
		dbscan.setMinPoints(minPoints);
	}

	public void setEpsilon(double epsilon) throws Exception {
		epsilon = epsilon;
		dbscan.setEpsilon(epsilon);
	}

	public void setData(Collection<? extends DataPoint> input) {
		data = new ArrayList<DataPoint>();
		data.addAll(input);
	}

	public double getEpsilon() {
		return epsilon;
	}

	public int getMinPoints() {
		return minPoints;
	}

	public List<DataPoint> getData() {
		return data;
	}

	public List<Cluster> getClusters() {
		return clusters;
	}

	/**
	 * subclass can override this function to recalculate dist. default is
	 * Euclidean distance.
	 */
	protected double distFunc(double[] c1, double[] c2) {
		double dist = 0;
		for (int i = 0; i < c1.length; i++) {
			dist += (c1[i] - c2[i]) * (c1[i] - c2[i]);
		}
		return Math.sqrt(dist);
	}

	public void runCluster() throws Exception {
		Instances dbscanInsts = this.createDbscanInstances();
		if (dbscanInsts == null) {
			LOG.error("KMeans : no input data for cluster");
			return;
		}
		dbscan.buildClusterer(dbscanInsts);
		Enumeration<Instance> enumeration = dbscanInsts.enumerateInstances();
		System.out.println(dbscan.numberOfClusters());
		Cluster[] dbscanClusters = new Cluster[dbscan.numberOfClusters()];
		for (int i = 0; i < dbscan.numberOfClusters(); i++) {
			dbscanClusters[i] = new Cluster();
		}
		boolean isAddrPoint = (data != null && data.get(0) instanceof AddressDataPoint);

		while (enumeration.hasMoreElements()) {
			Instance instance = enumeration.nextElement();
			int i = dbscan.clusterInstance(instance);
			DataPoint point = (isAddrPoint ? new AddressDataPoint() : new DataPoint());
			point.value = new double[2];
			point.value[0] = instance.value(LATITUDE_ATTRIBUTE);
			point.value[1] = instance.value(LONGITUDE_ATTRIBUTE);
			if (isAddrPoint) {
				AddressDataPoint addrPoint = (AddressDataPoint)point;
//				addrPoint.address = instance.stringValue(ADDR_ATTRIBUTE);
				double address_id = instance.value(ADDR_ATTRIBUTE);
				addrPoint.address = address_map.get(address_id);
				addrPoint.weight = instance.value(WEIGHT_ATTRIBUTE);
				dbscanClusters[i].points.add(addrPoint);
			} else {
			    dbscanClusters[i].points.add(point);
			}
			dbscanClusters[i].value[0] = instance.value(LATITUDE_ATTRIBUTE);
			dbscanClusters[i].value[1] = instance.value(LONGITUDE_ATTRIBUTE);
		}
		clusters = new ArrayList<Cluster>();
		Cluster cluster;
		for (int i = 0; i < dbscanClusters.length; i++) {
			cluster = new Cluster();
			cluster.points = new ArrayList<DataPoint>();
			cluster.points.addAll(dbscanClusters[i].points);
			cluster.value = new double[2];
			cluster.value[0] = dbscanClusters[i].value[0];
			cluster.value[1] = dbscanClusters[i].value[1];
			clusters.add(cluster);
			
		}
	}

	private Instances createDbscanInstances() {
		FastVector attributes = new FastVector(5);
		attributes.addElement(ID_ATTRIBUTE);
		attributes.addElement(LATITUDE_ATTRIBUTE);
		attributes.addElement(LONGITUDE_ATTRIBUTE);
		attributes.addElement(WEIGHT_ATTRIBUTE);
		attributes.addElement(ADDR_ATTRIBUTE);
		Instances instances = new Instances("dbscan instances", attributes, 0);
		int id = 0;
		double address_id = 0;
		for (DataPoint point : data) {
			Instance inst = new Instance(5);
			inst.setValue(ID_ATTRIBUTE, ++id);
			inst.setValue(LATITUDE_ATTRIBUTE, point.value[0]);
			inst.setValue(LONGITUDE_ATTRIBUTE, point.value[1]);
			if (point instanceof AddressDataPoint) {
				AddressDataPoint addrPoint = (AddressDataPoint)point;
				inst.setValue(WEIGHT_ATTRIBUTE, addrPoint.weight);
				inst.setValue(ADDR_ATTRIBUTE, address_id);
				address_map.put(address_id, addrPoint.address);
				++address_id;

			}
			inst.setDataset(instances);
			instances.add(inst);
		}
		return instances;
	}

	public HashMap<Double,String> getAddressMap() {
		return address_map;
	}
	 /**
     * Returns a description of the clusterer
     * 
     * @return a string representation of the clusterer
     */
	 public String toString() {
		 return dbscan.toString();
	 }

	/**
	 * return the cluster that has the most points
	 */
	public Cluster getMaxCluster() {
		if (clusters.size() == 0) {
			return null;
		}
		int max_cluster_id = 0, max_cluster_size = clusters.get(0).points
				.size();
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
			if (flag)
				break;
		}
	}

}
