//Copyright 2016 mobvoi Inc. All Rights Reserved.
//Author : chhyu@mobvoi.com(Changhe Yu)

package com.mobvoi.be.usercenter.datamining.upload;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mobvoi.be.common.Global.OneboxStatus;
import com.mobvoi.be.usercenter.client.UserCenterConstants;
import com.mobvoi.be.usercenter.client.model.Value;
import com.mobvoi.be.usercenter.datamining.model.ActiveLocationData;
import com.mobvoi.be.usercenter.datamining.model.KeyValueData;
import com.mobvoi.be.usercenter.datamining.upload.ResultUploaderTool.ResultStatus;

public class ActiveLocationUploader extends BasicUserCenterUploader {

  private static final Log LOG = LogFactory.getLog(ActiveLocationUploader.class);

  private static final double COVERAGE_THRESHOLD = 0.85;

  @Override
  protected String getFamily() {
    return UserCenterConstants.Common.FAMILY_NAME;
  }

  @Override
  protected String getColumn() {
    return UserCenterConstants.Common.PREDICTED_AREA_COLUMN;
  }

  public class ActiveLocationResult extends ResultStatus {
    public double avgPointCoverage = 0;
    public double avgTimeCoverage = 0;
    public double avgRadius = 0;
  }

  @Override
  public ResultStatus postProcess(List<KeyValueData> items) {
    ActiveLocationResult result = new ActiveLocationResult();
    result.timestamp = System.currentTimeMillis();
    result.totalCount = items.size();
    result.status = OneboxStatus.SUCCESS;
    double av_cluster = 0;
    Map<Integer, Integer> clusterNumCnt = new TreeMap<Integer, Integer>();
    for (KeyValueData item : items) {
      ActiveLocationData activeLocation = (ActiveLocationData) item.value;
      result.avgPointCoverage += activeLocation.pointCoverage;
      result.avgTimeCoverage += activeLocation.timeCoverage;
      result.avgRadius += activeLocation.radius;
      av_cluster += activeLocation.areas.size();
      Integer cnt = clusterNumCnt.get(activeLocation.areas.size());
      if (cnt != null)
        cnt++;
      else
        cnt = 1;
      clusterNumCnt.put(activeLocation.areas.size(), cnt);
    }
    av_cluster /= items.size();
    result.avgPointCoverage /= result.totalCount;
    result.avgTimeCoverage /= result.totalCount;
    result.avgRadius /= result.totalCount;
    LOG.info(String.format("average point coverage is %.2f %%.", result.avgPointCoverage * 100));
    LOG.info(String.format("average time coverage is %.2f %%.", result.avgTimeCoverage * 100));
    LOG.info(String.format("average area radius is %.2f m.", result.avgRadius));
    LOG.info(String.format("average cluster number is %.2f .", av_cluster));
    for (Entry<Integer, Integer> entry : clusterNumCnt.entrySet()) {
      LOG.info(entry.getValue() + " users have " + entry.getKey() + " clusters");
    }
    if (result.avgPointCoverage < COVERAGE_THRESHOLD || result.avgTimeCoverage < COVERAGE_THRESHOLD) {
      result.status = OneboxStatus.ERROR;
      result.errorMsg = String
          .format(
              "coverage is smaller than threshold %.2f %%, point coverage is %.2f %%, time coverage is %.2f %%.",
              COVERAGE_THRESHOLD, result.avgPointCoverage, result.avgTimeCoverage);
    }
    return result;
  }

  @Override
  protected Value merge(Value v, Object item) {
    ActiveLocationData data = (ActiveLocationData) item;
    if (data == null || CollectionUtils.isEmpty(data.areas))
      return null;
    v.set(data.areas);
    return v;
  }

}
