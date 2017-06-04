package com.think.firewaiter.dao;

import com.think.firewaiter.module.PxProductInfo;
import de.greenrobot.dao.AbstractDao;

/**
 * Created by zjq on 2016/3/31.
 */
public class ProductInfoService extends DbService<PxProductInfo,Long> {
  public ProductInfoService(AbstractDao dao) {
    super(dao);
  }
}
