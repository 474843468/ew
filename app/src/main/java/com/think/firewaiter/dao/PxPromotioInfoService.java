package com.think.firewaiter.dao;

import com.think.firewaiter.module.PxPromotioInfo;
import de.greenrobot.dao.AbstractDao;

/**
 * Created by dorado on 2016/5/18.
 */
public class PxPromotioInfoService extends DbService<PxPromotioInfo,Long> {
  public PxPromotioInfoService(AbstractDao dao) {
    super(dao);
  }
}
