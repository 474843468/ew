package com.think.firewaiter.chat.req;

import java.io.Serializable;

/**
 * Created by zjq on 2016/5/9.
 * 改单请求
 */
public class ModifyBillReq implements Serializable {
  //桌台id
  private String mTableId;
  //目标桌台id
  private String mMoveTableId;
  //订单id
  private long mOrderId;
  //人数
  private long mPeopleNum;
  //备注
  private String mRemarks;
  //服务生id
  private String mWaiterId;
  //促销计划
  private String mPromotioInfoId;

  public String getPromotioInfoId() {
    return mPromotioInfoId;
  }

  public void setPromotioInfoId(String promotioInfoId) {
    mPromotioInfoId = promotioInfoId;
  }

  public String getTableId() {
    return mTableId;
  }

  public void setTableId(String tableId) {
    mTableId = tableId;
  }

  public String getMoveTableId() {
    return mMoveTableId;
  }

  public void setMoveTableId(String moveTableId) {
    mMoveTableId = moveTableId;
  }

  public long getPeopleNum() {
    return mPeopleNum;
  }

  public void setPeopleNum(long peopleNum) {
    mPeopleNum = peopleNum;
  }

  public long getOrderId() {
    return mOrderId;
  }

  public void setOrderId(long orderId) {
    mOrderId = orderId;
  }

  public String getRemarks() {
    return mRemarks;
  }

  public void setRemarks(String remarks) {
    mRemarks = remarks;
  }

  public String getWaiterId() {
    return mWaiterId;
  }

  public void setWaiterId(String waiterId) {
    mWaiterId = waiterId;
  }
}
