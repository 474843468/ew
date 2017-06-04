package com.think.firewaiter.config;

import android.os.Environment;
import com.think.firewaiter.BuildConfig;
import java.io.File;

/**
 * Created by zjq on 2016/5/12.
 */
public class Setting {

  public static final String BASE_URL = BuildConfig.BASE_URL;
  public static final String HOST = BuildConfig.HOST;
  public static final String SERVER_NAME = BuildConfig.SERVER_NAME;

  /**
   * 公网
   */
  //public static String BASE_URL = "http://api.yizhanggui.cc/api/";
  //public static final String HOST = "im.yizhanggui.cc";
  //public static final String SERVER_NAME = "iz25tedwz5sz";

  /**
   * 63
   */

  //public static String BASE_URL = "http://192.168.1.63:8080/api/";
  //public static final String HOST = "192.168.1.62";
  //public static final String SERVER_NAME = "2013-20150715FR";

  /**
   * 62
   */
  //public static String BASE_URL = "http://192.168.1.62:8182/api/";
  //public static final String HOST = "192.168.1.62";
  //public static final String SERVER_NAME = "2013-20150715FR";

  /**
   * 测试
   */
  //public static String BASE_URL = "http://192.168.1.209:8181/api/";
  //public static final String HOST = "192.168.1.62";
  //public static final String SERVER_NAME = "2013-20150715FR";

  /**
   * Chat端口
   */
  public static final int PORT = 5222;

  /**
   * 加密盐
   */
  public static final String SALT = "ap4rf7ax";

  /**
   * 是否为新安装的app
   */
  public static String IS_INSTALL = "isInstall";

  /**
   * 拉数据版本
   */
  public static String DATA_VERION_ON_UPDATE = "DataVersionOnUpdate";

  /**
   * remember pwd
   */
  public static String REMEMBER_PWD = "rememberPwd";

  /**
   * sp username
   */
  public static String SP_USERNAME = "Username";
  /**
   * sp companycode
   */
  public static final String SP_COMPANY_CODE = "CompanyCode";
  /**
   * sp pwd
   */
  public static String SP_PWD = "Password";

  /**
   * sp initpwd
   */
  public static String SP_INIT_PWD = "InitPassword";

  /**
   * Crash log路径
   */
  public static final String LOG_DIR =
      Environment.getExternalStorageDirectory() + File.separator + "crash" + File.separator + "log";

  /**
   * Crash log文件名
   */
  public static final String LOG_NAME = "Waiter.log";

  /**
   * login user id
   */
  public static String LOGIN_USER = "LoginUser";
}
