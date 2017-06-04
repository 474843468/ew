package com.think.firewaiter.dao;

import com.think.firewaiter.module.PxProductRemarks;
import de.greenrobot.dao.AbstractDao;

/**
 * Created by zjq on 2016/4/6.
 */
public class ProdRemarksService extends DbService<PxProductRemarks,Long> {
  public ProdRemarksService(AbstractDao dao) {
    super(dao);
  }
}
