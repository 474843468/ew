package com.think.firewaiter.dao;

import com.think.firewaiter.module.PxMethodInfo;
import de.greenrobot.dao.AbstractDao;

/**
 * Created by dorado on 2016/5/28.
 */
public class MethodInfoService extends DbService<PxMethodInfo, Long> {
  public MethodInfoService(AbstractDao dao) {
    super(dao);
  }
}
