package com.think.firewaiter.network;

import java.util.List;

public class HttpDataSyncResp extends HttpResp {

  private static final long serialVersionUID = -9120933275815197238L;

  private List<DataSync> dataList;

  public List<DataSync> getDataList() {
    return dataList;
  }

  public void setDataList(List<DataSync> dataList) {
    this.dataList = dataList;
  }
}
