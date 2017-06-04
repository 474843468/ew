package com.think.firewaiter.common;

import android.content.Context;
import android.net.Uri;
import android.os.Looper;

import com.think.firewaiter.config.Setting;
import com.think.firewaiter.utils.DeviceUtils;
import com.think.firewaiter.utils.ToastUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * 进行错误收集，并由用户选择是否发送回来
 */
public class AppCrashCatcher implements Thread.UncaughtExceptionHandler {

  private static AppCrashCatcher sAppCrashCatcher;
  private Context mContext;

  private AppCrashCatcher() {
  }

  public static AppCrashCatcher newInstance() {
    if (sAppCrashCatcher != null) {
      return sAppCrashCatcher;
    } else {
      return new AppCrashCatcher();
    }
  }

  public void setDefaultCrashCatcher() {
    mContext = App.getContext();
    Thread.setDefaultUncaughtExceptionHandler(this);
  }

  @Override public void uncaughtException(Thread thread, Throwable ex) {
    ex.printStackTrace();
    new Thread(new Runnable() {
      @Override public void run() {
        Looper.prepare();
        ToastUtils.showLong(App.getContext(), "发生异常，App即将退出,请重新启动!");
        Looper.loop();
      }
    }).start();

    try {
      Thread.sleep(3000);
      //退出程序
      android.os.Process.killProcess(android.os.Process.myPid());
      System.exit(0);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private String catchErrors(Throwable throwable) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    throwable.printStackTrace(printWriter);
    printWriter.close();
    return stringWriter.toString();
  }

  private Uri saveToSDCard(Throwable throwable) {
    StringBuilder buffer = new StringBuilder();
    List<String> info = DeviceUtils.getDeviceMsg(mContext);
    for (String s : info) {
      buffer.append(s).append("\n");
    }
    buffer.append("=====\tError Log\t=====\n");
    String errorMsgs = catchErrors(throwable);
    buffer.append(errorMsgs);
    File dir = new File(Setting.LOG_DIR);
    if (!dir.exists()) {
      dir.mkdirs();
    }
    File crash = new File(dir, Setting.LOG_NAME);
    try {
      FileOutputStream fos = new FileOutputStream(crash);
      OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
      osw.write(buffer.toString());
      osw.flush();
      osw.close();
      fos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return Uri.fromFile(crash);
  }
}
