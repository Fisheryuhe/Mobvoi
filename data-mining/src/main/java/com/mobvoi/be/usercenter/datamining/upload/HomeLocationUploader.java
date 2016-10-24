//Copyright 2016 Mobvoi Inc. All Rights Reserved
//Author : chhyu@mobvoi.com(Changhe Yu)

package com.mobvoi.be.usercenter.datamining.upload;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSONObject;
import com.mobvoi.be.common.Global.OneboxStatus;
import com.mobvoi.be.common.config.ConfigCenterManager;
import com.mobvoi.be.common.log.MobvoiLog;
import com.mobvoi.be.common.util.GeoUtil;
import com.mobvoi.be.usercenter.client.UserCenterConstants;
import com.mobvoi.be.usercenter.client.model.Value;
import com.mobvoi.be.usercenter.datamining.conf.DataMiningConfig;
import com.mobvoi.be.usercenter.datamining.model.KeyValueData;
import com.mobvoi.be.usercenter.datamining.upload.ResultUploaderTool.ResultStatus;
import com.mobvoi.be.usercenter.userinfo.model.LocationDetailData;

public class HomeLocationUploader extends BasicUserCenterUploader {

  private static final Log LOG = LogFactory.getLog(HomeLocationUploader.class);
  
  private static final MobvoiLog ERROR_LOG = new MobvoiLog(HomeLocationUploader.class);
  
  private static final double PRECISION_THRESHOLD = 0.85;
  
  private static final double notValidPointThreshold = 0.00001;

  @Override
  protected String getFamily() {
    return UserCenterConstants.Common.FAMILY_NAME;
  }

  @Override
  protected String getColumn() {
    return UserCenterConstants.Common.PREDICTED_LOCATION_COLUMN;
  }

  protected double getHomeThreshold() {
    return 1000.0;
  }

  public class HomeLocationResult extends ResultStatus {
    public int nCorrect = 0;
    public int nError = 0;
    public int nUser = 0;
    public double acc = 0;
  }

  @Override
  public ResultStatus postProcess(List<KeyValueData> items) {
    JSONObject obj = ConfigCenterManager.getConfig(DataMiningConfig.NAME);
    JSONObject locations = obj.getJSONObject("LocationTestSet");

    HomeLocationResult result = new HomeLocationResult();
    result.timestamp = System.currentTimeMillis();
    result.totalCount = items.size();
    result.status = OneboxStatus.SUCCESS;
    result.nUser = items.size();
    try {
      for (KeyValueData item : items) {
        LocationDetailData pred_home = (LocationDetailData) item.value;
        double[] pred_loc = new double[2];
        pred_loc[0] = pred_home.lat;
        pred_loc[1] = pred_home.lng;

        JSONObject customLocJson = locations.getJSONObject(item.key);
        if (customLocJson == null)
          continue;
        String customHome = customLocJson.getString("家");
        if (customHome == null)
          continue;

        JSONObject customHomeJson = JSONObject.parseObject(customHome);
        double[] cust_loc = new double[2];
        cust_loc[0] = customHomeJson.getDoubleValue("lat");
        cust_loc[1] = customHomeJson.getDoubleValue("lng");
        if (Math.abs(cust_loc[0]) < notValidPointThreshold || Math.abs(cust_loc[1]) < notValidPointThreshold)
          continue;
        if (GeoUtil.calculateDistance(pred_loc[1], pred_loc[0], cust_loc[1], cust_loc[0]) < getHomeThreshold()) {
          result.nCorrect++;
        } else {
          result.nError++;
        }
      }

      result.acc = (1.0 * result.nCorrect) / (1.0 * result.nCorrect + 1.0 * result.nError);
      LOG.info("There are total " + Integer.toString(result.nUser) + " users home");
      LOG.info("Accuracy of home is " + Double.toString(result.acc));
      LOG.info("Total number of right users' home is " + Integer.toString(result.nCorrect));
      LOG.info("Total number of wrong users' home is " + Integer.toString(result.nError));
      if (result.acc < PRECISION_THRESHOLD) {
        result.status = OneboxStatus.ERROR;
        result.errorMsg = "home predict precision is less than threshold, precision = " + result.acc;
      }
    } catch (Exception e) {
      ERROR_LOG.error(e, e);
      result.status = OneboxStatus.ERROR;
      result.errorMsg = e.getMessage();
    }

    return result;
  }

  @Override
  protected Value merge(Value v, Object item) {
    JSONObject json = new JSONObject();

    try {
      if (v.isEmpty()) {
      } else {
        String jString = v.getString();
        json = JSONObject.parseObject(jString);
      }
      json.put("家", JSONObject.toJSON(item));
      v.set(json.toJSONString());
      return v;
    } catch (Exception e) {
      ERROR_LOG.error(e, e);
    }
    return null;
  }

}
