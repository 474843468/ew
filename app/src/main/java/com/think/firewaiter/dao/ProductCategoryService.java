package com.think.firewaiter.dao;

import com.think.firewaiter.module.PxProductCategory;
import de.greenrobot.dao.AbstractDao;

/**
 * Created by zjq on 2016/3/31.
 */
public class ProductCategoryService extends DbService<PxProductCategory, Long> {
  public ProductCategoryService(AbstractDao dao) {
    super(dao);
  }
}
