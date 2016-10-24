//Copyright 2016 Mobvoi Inc. All Rights Reserved
//Author : chhyu@mobvoi.com(Changhe Yu)

package com.mobvoi.be.usercenter.datamining.upload;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mobvoi.be.common.Global.OneboxStatus;
import com.mobvoi.be.common.log.MobvoiLog;
import com.mobvoi.be.usercenter.client.IUserCenterTable;
import com.mobvoi.be.usercenter.client.UserCenterClientFactory;
import com.mobvoi.be.usercenter.client.UserCenterConstants;
import com.mobvoi.be.usercenter.client.model.Value;
import com.mobvoi.be.usercenter.datamining.model.KeyValueData;
import com.mobvoi.be.usercenter.datamining.upload.ResultUploaderTool.ResultStatus;

public abstract class BasicUserCenterUploader implements IUploader {
  
  private static final Log LOG = LogFactory.getLog(BasicUserCenterUploader.class);
  
  private static final MobvoiLog ERROR_LOG = new MobvoiLog(BasicUserCenterUploader.class);
  
  protected IUserCenterTable table;
  
  protected int getItemThreshold() {
    return 5000;
  }
  
  protected int getErrorThreshold() {
    return 10;
  }
  
  /**
   * post precess for result items.
   * sub-class can override this function to check data and return result status.
   */
  public ResultStatus postProcess(List<KeyValueData> items) {
    ResultStatus result = new ResultStatus();
    result.timestamp = System.currentTimeMillis();
    result.totalCount = items.size();
    result.status = OneboxStatus.SUCCESS;
    if (items.size() < getItemThreshold()) {
      LOG.warn("check result failed, item size is smaller than threshold : " + getItemThreshold());
      result.status = OneboxStatus.ERROR;
    }
    return result;
  }
  
  /**
   * load info from user-center and merge item into user-center.
   */
  public boolean upload(List<KeyValueData> items) {
    try {
      table = UserCenterClientFactory.getUserCenterTable(UserCenterConstants.TABLE_NAME);
      int errorCnt = 0;
      for (KeyValueData item : items) {
        try {
          Value value = table.get(item.key, getFamily(), getColumn());
          if (value == null) {
            value = new Value(getColumn());
          }
          value = merge(value, item.value);
          if (value != null && !value.isEmpty()) {
            table.update(item.key, getFamily(), value);
          }
        } catch(Exception e) {
          LOG.warn(e, e);
          errorCnt++;
        }
      }
      if (errorCnt > getErrorThreshold()) {
        ERROR_LOG.error("upload error exceeded threshold, errorCnt = " + errorCnt);
        return false;
      }
      return true;
    } catch(Exception e) {
      ERROR_LOG.error(e, e);
    }
    return false;
  }

  protected abstract String getFamily();
  
  protected abstract String getColumn();
  
  /**
   * merge value in user-center and item,
   * default is naive override.
   * sub-classes can override this function.
   * @return merged value, null if the merge result is not valid.
   */
  protected Value merge(Value v, Object item) {
    v.set(item);
    return v;
  }

}