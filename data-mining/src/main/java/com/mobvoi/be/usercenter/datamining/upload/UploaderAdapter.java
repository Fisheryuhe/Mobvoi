//Copyright 2016 Mobvoi Inc. All Rights Reserved
//Author : chhyu@mobvoi.com(Changhe Yu)

package com.mobvoi.be.usercenter.datamining.upload;

import java.util.List;

import com.mobvoi.be.common.Global.OneboxStatus;
import com.mobvoi.be.usercenter.datamining.model.KeyValueData;
import com.mobvoi.be.usercenter.datamining.upload.ResultUploaderTool.ResultStatus;

public class UploaderAdapter implements IUploader {

  @Override
  public ResultStatus postProcess(List<KeyValueData> items) {
    ResultStatus result = new ResultStatus();
    result.status = OneboxStatus.SUCCESS;
    result.timestamp = System.currentTimeMillis();
    result.totalCount = items.size();
    return result;
  }

  @Override
  public boolean upload(List<KeyValueData> items) {
    return true;
  }

}
