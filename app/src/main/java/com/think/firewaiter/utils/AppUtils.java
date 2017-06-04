package com.think.firewaiter.utils;

import android.app.ActivityManager;
import android.content.Context;
import java.util.List;

/**
 * Created by dorado on 2016/5/14.
 */
public class AppUtils {
  public static String getTopActivity(Context context) {
    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
    if (runningTaskInfos != null) {
      return (runningTaskInfos.get(0).topActivity).getClassName();
    } else {
      return null;
    }
  }

}
