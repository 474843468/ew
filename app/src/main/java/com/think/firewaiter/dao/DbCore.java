package com.think.firewaiter.dao;

import android.content.Context;
import com.think.firewaiter.dao.data.DbUpdateHelper;
import de.greenrobot.dao.identityscope.IdentityScopeType;
import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by zjq on 2016/3/31.
 * 核心辅助类,用于获取DaoMaster和DaoSession
 */
public class DbCore {
  private static final String DEFAULT_DB_NAME = "fire-db";
  private static DaoMaster daoMaster;
  private static DaoSession daoSession;

  private static Context mContext;
  private static String DB_NAME;

  public static void init(Context context) {
    init(context, DEFAULT_DB_NAME);
  }

  public static void init(Context context, String dbName) {
    if (context == null) {
      throw new IllegalArgumentException("context can't be null");
    }
    mContext = context.getApplicationContext();
    DB_NAME = dbName;
  }

  public static DaoMaster getDaoMaster() {
    if (daoMaster == null) {
      DaoMaster.OpenHelper helper = new DaoMaster.DevOpenHelper(mContext, DB_NAME, null);
      daoMaster = new DaoMaster(helper.getWritableDatabase());
    }
    return daoMaster;
  }

  public static DaoSession getDaoSession() {
    if (daoSession == null) {
      if (daoMaster == null) {
        daoMaster = getDaoMaster();
      }
      daoSession = daoMaster.newSession(IdentityScopeType.None);
    }
    return daoSession;
  }

  public static void enableQueryBuilderLog() {
    QueryBuilder.LOG_SQL = true;
    QueryBuilder.LOG_VALUES = true;
  }


  public static void update(Context context) {
    DbUpdateHelper helper = new DbUpdateHelper(context, DEFAULT_DB_NAME, null);
    daoMaster = new DaoMaster(helper.getWritableDatabase());
  }
}
