package com.think.firewaiter.chat.req;

import java.io.Serializable;

/**
 * User: ylw
 * Date: 2017-02-14
 * Time: 15:27
 * 所有桌台
 */
public class AllTableReq implements Serializable {

  //模糊查询
  private String mLike;

  public String getLike() {
    return mLike;
  }

  public void setLike(String like) {
    mLike = like;
  }

}