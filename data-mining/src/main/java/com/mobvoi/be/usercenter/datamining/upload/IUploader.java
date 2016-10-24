//Copyright 2016 Mobvoi Inc. All Rights Reserved
//Author : chhyu@mobvoi.com(Changhe Yu)
package com.mobvoi.be.usercenter.datamining.upload;

import java.util.List;

import com.mobvoi.be.usercenter.datamining.model.KeyValueData;
import com.mobvoi.be.usercenter.datamining.upload.ResultUploAaderTool.ResultStatus;

public interface IUploader {

  /**
   * do post process after handle and before upload.
   * this function implements logics such as data check and reorder.
   */
  public ResultStatus postProcess(List<KeyValueData> items);
  
  /**
   * upload items to online systems such as user-center.
   */
  public boolean upload(List<KeyValueData> items);
  
}