package com.think.firewaiter.event;

import com.think.firewaiter.module.AppProdInfo;
import java.util.HashMap;

/**
 * Created by zjq on 2016/5/5.
 */
public class ModifyCartEvent {
  private HashMap<String, AppProdInfo> mMap;

  public HashMap<String, AppProdInfo> getMap() {
    return mMap;
  }

  public ModifyCartEvent setMap(HashMap<String, AppProdInfo> map) {
    mMap = map;
    return this;
  }
}
