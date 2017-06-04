package com.think.firewaiter.dao;

import com.think.firewaiter.module.PxFormatInfo;
import com.think.firewaiter.module.UserTableRel;
import de.greenrobot.dao.AbstractDao;

/**
 * Created by dorado on 2016/5/28.
 */
public class UserTableRelService extends DbService<UserTableRel,Long> {
  public UserTableRelService(AbstractDao dao) {
    super(dao);
  }
}
