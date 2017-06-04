package com.think.firewaiter.dao;

import com.think.firewaiter.module.PxProductFormatRel;
import de.greenrobot.dao.AbstractDao;

/**
 * Created by dorado on 2016/5/30.
 */
public class ProductFormatRelService extends DbService<PxProductFormatRel,Long> {
  public ProductFormatRelService(AbstractDao dao) {
    super(dao);
  }
}
