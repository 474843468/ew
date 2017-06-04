package com.think.firewaiter.common;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import com.facebook.stetho.Stetho;
import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;
import com.tencent.bugly.crashreport.CrashReport;
import com.think.firewaiter.config.Setting;
import com.think.firewaiter.dao.DaoMaster;
import com.think.firewaiter.dao.DaoServiceUtil;
import com.think.firewaiter.dao.DbCore;
import com.think.firewaiter.dao.UserDao;
import com.think.firewaiter.module.User;
import com.think.firewaiter.utils.SPUtils;

/**
 * Created by zjq on 2016/5/3.
 */
public class App extends Application {

  private static Context mContext;
  private static User mUser;

  @Override public void onCreate() {
    super.onCreate();
    mContext = getApplicationContext();
    initLogger();
    initDb();
    initStetho();
    initBug();
    initCrashHandler();
  }

  // @formatter:on
  public static Context getContext() {
    return mContext;
  }

  /**
   * 初始化数据库
   */
  private void initDb() {
    DbCore.init(getApplicationContext());
    DbCore.update(getApplicationContext());
  }

  /**
   * 初始化Stetho
   */
  private void initStetho() {
    Stetho.initialize(Stetho.newInitializerBuilder(this)
        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
        .build());
  }

  /**
   * Logger
   */
  private void initLogger() {
    Logger.init("FireLog").setMethodCount(3).hideThreadInfo().setLogLevel(LogLevel.FULL);
  }

  /**
   * Bugly
   */
  private void initBug() {
    CrashReport.initCrashReport(getApplicationContext(), "4258dfa2c6", false);
    String companyCode = (String) SPUtils.get(this, Setting.SP_COMPANY_CODE, "companyCode");
    CrashReport.setUserId(companyCode);
  }

  /**
   * Crash
   */
  private void initCrashHandler() {
    AppCrashCatcher appCrashCatcher = AppCrashCatcher.newInstance();
    appCrashCatcher.setDefaultCrashCatcher();
  }

  public User getUser() {
    if (mUser == null) {
      String userId = (String) SPUtils.get(this, Setting.LOGIN_USER, "");
      if (!TextUtils.isEmpty(userId)) {
        mUser = DaoServiceUtil.getUserService()
            .queryBuilder()
            .where(UserDao.Properties.ObjectId.eq(userId))
            .unique();
      }
    }
    return mUser;
  }

  public void setUser(User user) {
    mUser = user;
  }
}
