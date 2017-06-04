package com.think.firewaiter.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import com.orhanobut.logger.Logger;
import com.think.firewaiter.chat.chatUtils.XMPPConnectUtils;
import com.think.firewaiter.common.App;
import com.think.firewaiter.config.Setting;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.ping.PingManager;

/**
 * Created by dorado on 2016/6/28.
 */
public class ConnectService extends Service {
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
        if (XMPPConnectUtils.getConnection() != null && !XMPPConnectUtils.getConnection().isConnected()) {
          try {
            XMPPConnectUtils.getConnection().connect();
          } catch (SmackException e) {
            e.printStackTrace();
          } catch (IOException e) {
            e.printStackTrace();
          } catch (XMPPException e) {
            e.printStackTrace();
          }
        }
      }
    };
    mTimer.schedule(mTimerTask, 1000, 3000);
  }

  @Override public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    return super.onStartCommand(intent, flags, startId);
  }

  @Override public void onDestroy() {
    super.onDestroy();
    mTimer.cancel();
    mTimerTask.cancel();
  }
}
