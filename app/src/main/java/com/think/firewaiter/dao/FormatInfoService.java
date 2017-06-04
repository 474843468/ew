package com.think.firewaiter.dao;

import com.think.firewaiter.module.PxFormatInfo;
import de.greenrobot.dao.AbstractDao;

/**
 * Created by dorado on 2016/5/28.
 */
public class FormatInfoService extends DbService<PxFormatInfo,Long> {
  public FormatInfoService(AbstractDao dao) {
    super(dao);
  }
}
