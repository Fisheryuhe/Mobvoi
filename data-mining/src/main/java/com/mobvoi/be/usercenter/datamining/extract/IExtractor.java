//Copyright 2016 Mobvoi Inc. All Rights Reserved
//Author :chhyu@mobvoi(Changhe Yu)
package com.mobvoi.be.usercenter.datamining.extract;

import com.mobvoi.be.usercenter.datamining.model.KeyValueData;

public interface IExtractor {
  
  /**
   * extract data from a line.
   * @return formated-data with key-value pair.
   */
  public KeyValueData extract(String line);

}
