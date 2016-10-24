//Copyright 2016 Mobvoi Inc. All Rights Reserved
//Author : chhyu@mobvoi.com(Changhe Yu)

package com.mobvoi.be.usercenter.datamining.handle;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mobvoi.be.common.log.MobvoiLog;
import com.mobvoi.be.common.util.FileUtil;
import com.mobvoi.be.common.util.StringUtil;
import com.mobvoi.be.usercenter.datamining.model.KeyValueData;

@SuppressWarnings({"rawtypes"})
public class DataHandlerTool {
  
  private static final Log LOG = LogFactory.getLog(DataHandlerTool.class);
  
  private static final MobvoiLog ERROR_LOG = new MobvoiLog(DataHandlerTool.class);
  
  private Map<String, IHandler> handlers = new HashMap<String, IHandler>();
  
  private Map<String, PrintStream> outs = new HashMap<String, PrintStream>();
  
  private String dataPath;
  
  private String resultPath;
  
  private Class dataClazz;
  
  //TODO multi-thread handle.
  public boolean handleData() {
    try {
      long start = System.currentTimeMillis();
      LOG.info("run data handler tool start.");
      
      FileUtil.mkOrClearDir(resultPath);
      
      for (String name : handlers.keySet()) {
        outs.put(name, new PrintStream(new FileOutputStream(resultPath + "/" + name)));
      }

      Scanner in = new Scanner(new FileInputStream(dataPath));

      String key = null;
      List<Object> datas = new ArrayList<Object>();
      while (in.hasNext()) {
        KeyValueData data = null;
        try {
          data = new KeyValueData(in.nextLine(), dataClazz);
        } catch(Exception e) {
          LOG.warn(e, e);
          continue;
        }
        if (!data.key.equals(key)) {
          handleGroup(key, datas);
        }
        key = data.key;
        datas.add(data.value);
      }
      handleGroup(key, datas);
      in.close();
      
      for (PrintStream out : outs.values()) {
        out.close();
      }

      LOG.info("run data handler tool success, elapse = " + (System.currentTimeMillis() - start)
          + " ms.");
      return true;
    } catch (Exception e) {
      ERROR_LOG.error("run data handler tool failed.", e);
    }
    return false;
  }
  
  protected void handleGroup(String key, List<Object> datas) throws Exception {
    if (CollectionUtils.isEmpty(datas) || StringUtil.isEmpty(key)) return ;
    for (Entry<String, IHandler> entry : handlers.entrySet()) {
      try {
        PrintStream out = outs.get(entry.getKey());
        IHandler handler = entry.getValue();
        KeyValueData item = handler.handle(key, datas);
        if (item != null) {
          String s = item.toString();
          out.println(s);
        }
      } catch(Exception e) {
        LOG.warn(e, e);
      }
    }
    datas.clear();
  }

  public void setHandlers(Map<String, IHandler> handlers) {
    this.handlers = handlers;
  }

  public void setDataPath(String dataPath) {
    this.dataPath = dataPath;
  }

  public void setResultPath(String resultPath) {
    this.resultPath = resultPath;
  }

  public void setDataClass(String dataClass) throws ClassNotFoundException {
    this.dataClazz = Class.forName(dataClass);
  }
  
}
