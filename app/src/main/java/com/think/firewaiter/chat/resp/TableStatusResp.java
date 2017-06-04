package com.think.firewaiter.chat.resp;

import com.think.firewaiter.chat.AppTableStatus;
import java.io.Serializable;
import java.util.List;

/**
 * Created by dorado on 2016/6/10.
 * 桌台最新状态
 */
public class TableStatusResp implements Serializable {
  //桌台状态list
  private List<AppTableStatus> mTableStatusList;

  public List<AppTableStatus> getTableStatusList() {
    return mTableStatusList;
  }

  public void setTableStatusList(List<AppTableStatus> tableStatusList) {
    mTableStatusList = tableStatusList;
  }
}
