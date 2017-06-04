package com.think.firewaiter.chat.resp;

import java.io.Serializable;

/**
 * Created by zjq on 2016/5/7.
 * 简易回执 返回成功或失败、描述
 */
public class SimpleResp implements Serializable {
  public static final int SUCCESS = 1;
  public static final int FAILURE = 2;
  //结果
  private int result;
  //描述
  private String des;

  public int getResult() {
    return result;
  }

  public void setResult(int result) {
    this.result = result;
  }

  public String getDes() {
    return des;
  }

  public void setDes(String des) {
    this.des = des;
  }
}
