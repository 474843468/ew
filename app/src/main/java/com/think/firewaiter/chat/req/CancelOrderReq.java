package com.think.firewaiter.chat.req;

import java.io.Serializable;

/**
 * Created by zjq on 2016/5/7.
 * 撤单申请
 */
public class CancelOrderReq implements Serializable {
  //订单id
  private long mOrderId;
  //服务生id
  private String mWaiterId;

  public long getOrderId() {
    return mOrderId;
  }

  public void setOrderId(long orderId) {
    mOrderId = orderId;
  }

  public String getWaiterId() {
    return mWaiterId;
  }

  public void setWaiterId(String waiterId) {
    mWaiterId = waiterId;
  }
}
