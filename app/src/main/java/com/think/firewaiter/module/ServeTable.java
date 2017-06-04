package com.think.firewaiter.module;

import java.io.Serializable;

/**
 * Created by dorado on 2016/6/30.
 */
public class ServeTable implements Serializable {
  private PxTableInfo mTableInfo;
  private boolean isSelected;

  public PxTableInfo getTableInfo() {
    return mTableInfo;
  }

  public void setTableInfo(PxTableInfo tableInfo) {
    mTableInfo = tableInfo;
  }

  public boolean isSelected() {
    return isSelected;
  }

  public void setIsSelected(boolean isSelected) {
    this.isSelected = isSelected;
  }
}
