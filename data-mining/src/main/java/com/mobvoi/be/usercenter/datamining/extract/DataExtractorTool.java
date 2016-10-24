//Copyright 2016 Mobvoi Inc. All Rights Reserved
//Author : chhyu@mobvoi.com(Changhe Yu)
package com.mobvoi.be.usercenter.datamining.extract;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mobvoi.be.common.log.MobvoiLog;
import com.mobvoi.be.common.util.FileUtil;
import com.mobvoi.be.common.util.StringUtil;
import com.mobvoi.be.usercenter.datamining.model.KeyValueData;

public class DataExtractorTool {
  
  private static final Log LOG = LogFactory.getLog(DataExtractorTool.class);
  
  private static final MobvoiLog ERROR_LOG = new MobvoiLog(DataExtractorTool.class);
  
  private String logPath;
  
  private String dataPath;
  
  private int inMemorySize;

  private Map<String, IExtractor> extractors = new HashMap<String, IExtractor>();
  
  private List<KeyValueData> data;
  
  public boolean extract() {
    try {
      long start = System.currentTimeMillis();
      LOG.info("start to run data extractor tool.");
      String rawDataPath = dataPath + "_raw";
      File rawData = new File(rawDataPath);
      FileUtil.deleteAllFile(rawData);
      rawData.mkdir();
      FileUtil.deleteAllFile(new File(dataPath));
      
      data = new ArrayList<KeyValueData>();
      int partNum = 0;
      for (Entry<String, IExtractor> entry : extractors.entrySet()) {
        File dir = new File(logPath + "/" + entry.getKey());
        for (File logFile : dir.listFiles()) {
          int totalLine = 0, totalData = 0;
          LOG.info("start to extract " + logFile.getName());
          Scanner in = new Scanner(new FileInputStream(logFile));
          IExtractor extractor = entry.getValue();
          while (in.hasNext()) {
            String line = in.nextLine();
            totalLine++;
            KeyValueData pair = extractor.extract(line);
            if (pair != null && StringUtil.isNotEmpty(pair.key) && pair.value != null) {
              data.add(pair);
              totalData++;
              if (data.size() >= inMemorySize) {
                flushPart(rawDataPath + "/part-" + (partNum++));
              }
            }
          }
          in.close();
          LOG.info("extract " + logFile.getName() + " log finish, total " + totalLine
              + ", extract total " + totalData);
        }
      }
      flushPart(rawDataPath + "/part-" + (partNum++));
      mergeSort(dataPath, rawDataPath, partNum);
      FileUtil.deleteAllFile(rawData);
      LOG.info("run data extractor tool success, elapse = " + (System.currentTimeMillis() - start) + " ms.");
      return true;
    } catch(Exception e) {
      ERROR_LOG.error("extract data from " + logPath + " failed.", e);
    }
    return false;
  }
  
  private void flushPart(String path) throws FileNotFoundException {
    if (data.size() > 0) {
      Collections.sort(data, new Comparator<KeyValueData>() {
        @Override
        public int compare(KeyValueData o1, KeyValueData o2) {
          return o1.key.compareTo(o2.key);
        }
      });
      PrintStream out = new PrintStream(new FileOutputStream(path));
      for (KeyValueData pair : data) {
        out.println(pair);
      }
      out.close();
      data.clear();
    }
  }
  
  private static class NumberedString {
    
    public int number;
    
    public String content;
    
    public NumberedString(int number, String content) {
      this.number = number;
      this.content = content;
    }
    
  }
  
  private void mergeSort(String outputPath, String inputPrefix, int num) throws FileNotFoundException {
    int batch = (inMemorySize - 1)/num + 1;
    Scanner[] ins = new Scanner[num];
    int[] count = new int[num];
    for (int i = 0; i < num; i++) {
      ins[i] = new Scanner(new FileInputStream(inputPrefix + "/part-" + i));
      count[i] = 0;
    }
    PrintStream out = new PrintStream(new FileOutputStream(outputPath));
    PriorityQueue<NumberedString> heap = new PriorityQueue<NumberedString>(
        batch * num,
        new Comparator<NumberedString>() {
          @Override
          public int compare(NumberedString o1, NumberedString o2) {
            return o1.content.compareTo(o2.content);
          }
        });
    int cnt = 0;
    while (true) {
      if (cnt % batch == 0) {
        for (int i = 0; i < num; i++) {
          while (ins[i].hasNext() && count[i] < batch) {
            heap.add(new NumberedString(i, ins[i].nextLine()));
            count[i]++;
          }
        }
      }
      
      NumberedString item = heap.poll();
      out.println(item.content);
      count[item.number]--;
      cnt++;
      if (heap.isEmpty()) break;
    }
    
    for (int i = 0; i < num; i++) {
      ins[i].close();
    }
    out.close();
  }

  public void setLogPath(String logPath) {
    this.logPath = logPath;
  }
  
  public void setDataPath(String dataPath) {
    this.dataPath = dataPath;
  }

  public void setInMemorySize(int inMemorySize) {
    this.inMemorySize = inMemorySize;
  }

  public void setExtractors(Map<String, IExtractor> extractors) {
    this.extractors = extractors;
  }
  
}
