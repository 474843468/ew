package com.think.firewaiter.dao;

import com.think.firewaiter.module.Office;
import com.think.firewaiter.module.ShoppingCart;
import de.greenrobot.dao.AbstractDao;

/**
 * Created by zjq on 2016/4/6.
 */
public class ShoppingCartService extends DbService<ShoppingCart,Long> {
  public ShoppingCartService(AbstractDao dao) {
    super(dao);
  }
}
