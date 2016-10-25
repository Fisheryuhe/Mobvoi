//Copyright 2016 Mobvoi Inc. All Rights Reserved
//Author chhyu@mobvoi.com(Changhe Yu)
package com.mobvoi.be.usercenter.dashboard.servlet;

import org.apache.log4j.Logger; 
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;

import com.mobvoi.be.usercenter.client.UserCenterException;
import com.mobvoi.be.usercenter.client.IUserCenterTable;
import com.mobvoi.be.usercenter.client.UserCenterClientFactory;
import com.mobvoi.be.usercenter.client.UserCenterConstants;
import com.mobvoi.be.usercenter.client.model.Value;

public class UserPreLocationServlet extends HttpServlet {
 
 private static final Logger logger = Logger.getLogger(UserPreLocationServlet.class);
 private static final long serialVersionUID = 2834567617382736296L;
   
 private JSONObject getUserLocation(String userid) 
     throws UserCenterException {
  JSONObject result = new JSONObject(); 
  IUserCenterTable table = UserCenterClientFactory.getUserCenterTable(UserCenterConstants.TABLE_NAME);
  Value value = table.get(userid, UserCenterConstants.Common.FAMILY_NAME, UserCenterConstants.Common.PREDICTED_LOCATION_COLUMN);
  if (value == null || value.isEmpty()) {
    logger.warn("Fail to get column predictedLocation of user  id: " +  userid);
    return result;
  }
  JSONObject obj = JSON.parseObject(value.getString());
  String type_home = "家";
  String home_address = null;
  String type_company = "公司";
  String company_address = null;
  if (obj.containsKey(type_home)) {
   JSONObject item = obj.getJSONObject(type_home);
   home_address = item.getString("address");
  }
  if (obj.containsKey(type_company)) {
   JSONObject item = obj.getJSONObject(type_company);
   company_address = item.getString("address");
  }
  result.put("home_address", home_address);
  result.put("company_address", company_address);
  return result;
 }

@Override
protected void doGet(HttpServletRequest req,HttpServletResponse resp)
  throws ServletException,IOException {
  JSONObject user_location = new JSONObject();
  String user_id = req.getParameter("user_id");
  if (user_id.length() < 1) {
   logger.warn("Skip empty user");
   return;
  }
  try {
   user_location = getUserLocation(user_id);
  } catch (Exception e) {
   logger.error("Caught exeception retrieving user home address: " + user_id, e);
  }
  resp.setHeader("Content-Type", "text/plain;charset=UTF-8");
  resp.getWriter().write(user_location.toJSONString());
  }
}
