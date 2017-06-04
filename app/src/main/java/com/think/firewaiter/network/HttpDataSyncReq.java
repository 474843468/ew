package com.think.firewaiter.network;

import java.io.Serializable;

public class HttpDataSyncReq extends HttpReq implements Serializable {

  private static final long serialVersionUID = -4373657685186562710L;

  private String loginName;//用户登录名
  private String type;//（数据同步类型 0：收银端 1：服务生端）
  private String password;//店家密码
  private String install; //是否新安装的app （0：是 1：否）
  private String emi;//IMEI码
  private String initPassword;

  public String getLoginName() {
    return loginName;
  }

  public void setLoginName(String loginName) {
    this.loginName = loginName;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getInstall() {
    return install;
  }

  public void setInstall(String install) {
    this.install = install;
  }

  public String getEmi() {
    return emi;
  }

  public void setEmi(String emi) {
    this.emi = emi;
  }

  public String getInitPassword() {
    return initPassword;
  }

  public void setInitPassword(String initPassword) {
    this.initPassword = initPassword;
  }
}
