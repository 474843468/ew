package com.think.firewaiter.chat;

import java.io.Serializable;

/**
 * Created by dorado on 2016/6/10.
 */
public class AppTableStatus implements Serializable {
  //桌台id
  private String mTableId;
  //状态
  private String mStatus;
  //持续时间
  private long mDuration;
  //实际就餐人数
  private int mActualPeopleNumber;

  public String getTableId() {
    return mTableId;
  }

  public void setTableId(String tableId) {
    mTableId = tableId;
  }

  public String getStatus() {
    return mStatus;
  }

  public void setStatus(String status) {
    mStatus = status;
  }

  public long getDuration() {
    return mDuration;
  }

  public void setDuration(long duration) {
    mDuration = duration;
  }

  public int getActualPeopleNumber() {
    return mActualPeopleNumber;
  }

  public void setActualPeopleNumber(int actualPeopleNumber) {
    mActualPeopleNumber = actualPeopleNumber;
  }
}
