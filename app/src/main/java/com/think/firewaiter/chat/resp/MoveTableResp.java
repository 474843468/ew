package com.think.firewaiter.chat.resp;

import com.think.firewaiter.chat.AppMoveTableItem;
import java.io.Serializable;
import java.util.List;

/**
 * Created by zjq on 2016/5/9.
 * 可移动桌台的桌台列表
 */
public class MoveTableResp implements Serializable {
  //可移动桌台list
  private List<AppMoveTableItem> mMoveTableItemList;

  public List<AppMoveTableItem> getMoveTableItemList() {
    return mMoveTableItemList;
  }

  public void setMoveTableItemList(List<AppMoveTableItem> moveTableItemList) {
    mMoveTableItemList = moveTableItemList;
  }
}
