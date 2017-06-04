package com.think.firewaiter.chat.req;

import java.io.Serializable;
import java.util.List;

/**
 * Created by dorado on 2016/6/10.
 * 桌台状态
 */
public class TableStatusReq implements Serializable {
  //桌台列表id
  private List<String> mTableIdList;

  public List<String> getTableIdList() {
    return mTableIdList;
  }

  public void setTableIdList(List<String> tableIdList) {
    mTableIdList = tableIdList;
  }
}
