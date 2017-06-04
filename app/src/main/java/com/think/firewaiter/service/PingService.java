package com.think.firewaiter.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import com.orhanobut.logger.Logger;
import com.think.firewaiter.chat.chatUtils.XMPPConnectUtils;
import java.util.Timer;
import java.util.TimerTask;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smackx.ping.PingManager;

/**
 * Created by dorado on 2016/8/13.
 */
public class PingService extends Service {

  private Timer mTimer;
  private TimerTask mTimerTask;

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }

  @Override public void onCreate() {
    super.onCreate();
    //断开检测
    mTimer = new Timer();
    mTimerTask = new TimerTask() {
      @Override public void run() {
        if (XMPPConnectUtils.getConnection() == null || XMPPConnectUtils.getConnection().isConnected() == false) {
          return;
        }
        PingManager pingManager = PingManager.getInstanceFor(XMPPConnectUtils.getConnection());
        try {
          boolean ping = pingManager.pingMyServer();
          if (!ping) {
            XMPPConnectUtils.getConnection().disconnect();
          }
        } catch (SmackException.NotConnectedException e) {
          e.printStackTrace();
        }
      }
    };
    mTimer.schedule(mTimerTask, 1000, 10 * 1000);
  }

  @Override public void onDestroy() {
    super.onDestroy();
    mTimer.cancel();
    mTimerTask.cancel();
  }
}
