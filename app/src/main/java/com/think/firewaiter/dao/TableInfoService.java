package com.think.firewaiter.dao;

import com.think.firewaiter.module.PxTableInfo;
import de.greenrobot.dao.AbstractDao;

/**
 * Created by zjq on 2016/3/31.
 */
public class TableInfoService extends DbService<PxTableInfo, Long> {
  public TableInfoService(AbstractDao dao) {
    super(dao);
  }
}
