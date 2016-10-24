//Copyright 2016 Mobvoi Inc. All Rights Reserved
//Author : chhyu@mobvoi.com(Changhe Yu)

package com.mobvoi.be.usercenter.datamining.model;

import java.util.HashMap;

import com.mobvoi.be.usercenter.datamining.model.LocationTestSetData.Location;

@SuppressWarnings("serial")
public class LocationTestSetData extends HashMap<String, Location> {

  public static class Location {
    
    public double lat, lng;
    
    public Location(){}
    
    public Location(double lat, double lng) {
      this.lat = lat;
      this.lng = lng;
    }
    
  }
  
}
