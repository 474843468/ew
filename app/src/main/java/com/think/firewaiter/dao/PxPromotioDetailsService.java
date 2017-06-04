package com.think.firewaiter.dao;

import com.think.firewaiter.module.PxPromotioDetails;
import de.greenrobot.dao.AbstractDao;

/**
 * Created by dorado on 2016/5/18.
 */
public class PxPromotioDetailsService extends DbService<PxPromotioDetails,Long> {
  public PxPromotioDetailsService(AbstractDao dao) {
    super(dao);
  }
}
