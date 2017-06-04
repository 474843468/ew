package com.think.firewaiter.chat.resp;

import com.google.gson.annotations.Expose;
import com.think.firewaiter.module.PxTableInfo;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: ylw
 * Date: 2017-02-14
 * Time: 15:41
 * FIXME
 */
public class AllTableResp implements Serializable{
  @Expose
  private List<PxTableInfo> mTableInfoList;

  public List<PxTableInfo> getTableInfoList() {
    return mTableInfoList == null ? new ArrayList<PxTableInfo>() : mTableInfoList;
  }

  public void setTableInfoList(List<PxTableInfo> tableInfoList) {
    mTableInfoList = tableInfoList;
  }
}