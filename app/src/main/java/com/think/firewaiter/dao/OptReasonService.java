package com.think.firewaiter.dao;

import com.think.firewaiter.module.PxOptReason;
import de.greenrobot.dao.AbstractDao;

/**
 * Created by zjq on 2016/3/31.
 */
public class OptReasonService extends DbService<PxOptReason, Long> {
  public OptReasonService(AbstractDao dao) {
    super(dao);
  }
}
