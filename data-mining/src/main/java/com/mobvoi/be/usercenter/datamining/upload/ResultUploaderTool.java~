//Copyright 2016 Mobvoi Inc. All Rights Reserved
//Author : chhyu@mobvoi.com(Changhe Yu)

package com.mobvoi.be.usercenter.datamining.upload;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mobvoi.be.common.Global.OneboxStatus;
import com.mobvoi.be.common.log.MobvoiLog;
import com.mobvoi.be.common.net.HttpCaller;
import com.mobvoi.be.common.net.NetCode;
import com.mobvoi.be.common.net.NetResponse;
import com.mobvoi.be.common.util.JSONUtil;
import com.mobvoi.be.usercenter.datamining.model.KeyValueData;

@SuppressWarnings("rawtypes")
public class ResultUploaderTool {
  
  private static final Log LOG = LogFactory.getLog(ResultUploaderTool.class);
  
  private static final MobvoiLog ERROR_LOG = new MobvoiLog(ResultUploaderTool.class);
  
  private static final String UPDATE_URL = "http://data-dashboard/update";
  
  public static class ResultStatus {
    
    public String status = OneboxStatus.ERROR;
    
    public long timestamp;
    
    public int totalCount;
    
    public String errorMsg;
    
    @Override
    public String toString() {
      return JSONUtil.toJSONString(this);
    }
    
  }
  
  private String resultPath;
  
  private Map<String, IUploader> uploaders = new HashMap<String, IUploader>();
  
  private Map<String, Class> clazzes = new HashMap<String, Class>();
  
  public boolean upload() {
    try {
      long start = System.currentTimeMillis();
      LOG.info("run result uploader tool start.");
      for (Entry<String, IUploader> entry : uploaders.entrySet()) {
        long startEntry = System.currentTimeMillis();
        String name = entry.getKey();
        IUploader uploader = entry.getValue();
        Class clazz = clazzes.get(name);
        Scanner in = new Scanner(new FileInputStream(resultPath + "/" + name));
        List<KeyValueData> items = new ArrayList<KeyValueData>();
        while (in.hasNext()) {
          try {
            String line = in.nextLine();
            KeyValueData data = new KeyValueData(line, clazz);
            items.add(data);
          } catch(Exception e) {
            LOG.warn(e, e);
          }
        }
        in.close();
        ResultStatus result = uploader.postProcess(items);
        if (!OneboxStatus.SUCCESS.equals(result.status)) {
          ERROR_LOG.error(result.errorMsg, "[Offline User Data Mining Job] " + name + " failed.");
          return false;
        }
        if (!uploader.upload(items)) {
          ERROR_LOG.error("upload " + name + " failed. item size = " + items.size());
          return false;
        }
        //TODO add status upload
        LOG.info("upload " + name + " success. item size = " + items.size() + ", elapse = "
            + (System.currentTimeMillis() - startEntry) + " ms.");
        
        Map<String, String> params = new TreeMap<String, String>();
        params.put("name", name);
        params.put("source", "UserCenter-DataMining");
        params.put("dataStatus", result.status);
        params.put("content", result.toString());
        NetResponse response = HttpCaller.get(UPDATE_URL, params);
        if (response.getNetCode() != NetCode.OK ||
            response.getNetValueObject() == null ||
            !OneboxStatus.SUCCESS.equals(response.getNetValueObject().getString("status"))) {
          LOG.warn("Update data-dashboard failed");
        }
      }
      LOG.info("upload data success. total elapse = " + (System.currentTimeMillis() - start) + " ms.");
      return true;
    } catch(Exception e) {
      ERROR_LOG.error("upload data failed.", e);
    }
    return false;
  }

  public void setResultPath(String resultPath) {
    this.resultPath = resultPath;
  }

  public void setUploaders(Map<String, IUploader> uploaders) {
    this.uploaders = uploaders;
  }

  public void setClasses(Map<String, String> classes) throws ClassNotFoundException {
    this.clazzes = new HashMap<String, Class>();
    for (Entry<String, String> entry : classes.entrySet()) {
      clazzes.put(entry.getKey(), Class.forName(entry.getValue()));
    }
  }

  
}
