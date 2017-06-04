package com.think.firewaiter.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.orhanobut.logger.Logger;
import com.think.firewaiter.R;
import com.think.firewaiter.chat.chatUtils.XMPPConnectUtils;
import com.think.firewaiter.common.App;
import com.think.firewaiter.config.Setting;
import com.think.firewaiter.dao.DaoServiceUtil;
import com.think.firewaiter.event.AdminStatusEvent;
import com.think.firewaiter.event.ChatConnectEvent;
import com.think.firewaiter.event.LoginConflictEvent;
import com.think.firewaiter.event.ReqTableStatusEvent;
import com.think.firewaiter.module.User;
import com.think.firewaiter.service.ConnectService;
import com.think.firewaiter.service.PingService;
import com.think.firewaiter.utils.ToastUtils;
import com.think.firewaiter.widget.InterceptClickLayout;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

public class MainActivity extends AppCompatActivity {

  private InterceptClickLayout mContentView;//蒙层
  private RelativeLayout mRlConnect;//连接中
  private FrameLayout mFlNetError;//网络错误

  //连接配置
  private XMPPTCPConnectionConfiguration mConnectionConfiguration;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mFlNetError = (FrameLayout) findViewById(R.id.fl_net_error);
    mRlConnect = (RelativeLayout) findViewById(R.id.progress_view);
    mContentView = (InterceptClickLayout) findViewById(R.id.content_view);

    ButterKnife.bind(this);
    EventBus.getDefault().register(this);

    User user = ((App) App.getContext()).getUser();
    if (user == null) {
      ToastUtils.showShort(MainActivity.this, "用户错误,请重新登录");
      return;
    }

    String imUserName = user.getImUserName();
    String initPassword = user.getInitPassword();
    if (imUserName == null || initPassword == null) {
      ToastUtils.showShort(MainActivity.this, "用户错误,请重新登录");
      return;
    }
    //初始化连接配置
    mConnectionConfiguration = XMPPTCPConnectionConfiguration.builder()
        .setConnectTimeout(10 * 1000)
        .setServiceName(Setting.SERVER_NAME + System.currentTimeMillis())
        .setUsernameAndPassword(imUserName, initPassword)
        .setHost(Setting.HOST)
        .setCompressionEnabled(false)
        .setDebuggerEnabled(false)
        .setSendPresence(true)
        .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
        .build();

    //通信登录
    new Thread() {
      @Override public void run() {
        super.run();
        connectAndLogin();
      }
    }.start();
    //显示连接中 蒙层
    isShowConnecting(true);
  }

  /**
   * 是否显示连接中蒙层
   */
  private void isShowConnecting(boolean show) {
    mRlConnect.setVisibility(show ? View.VISIBLE : View.GONE);
    mContentView.setIntercept(show);
  }

  /**
   * 连接并登陆
   */
  private void connectAndLogin() {
    XMPPConnectUtils.connectAndLogin(mConnectionConfiguration);
    mChatHandler.sendEmptyMessage(CREATE_CHAT);
  }

  private static final int CREATE_CHAT = 1;
  //@formatter:on
  private Handler mChatHandler = new Handler() {
    @Override public void handleMessage(android.os.Message msg) {
      super.handleMessage(msg);
      switch (msg.what) {
        case CREATE_CHAT:
          //关闭连接中 蒙层
          isShowConnecting(false);
          //获取当前对象
          String user = XMPPConnectUtils.getConnection().getUser();
          Logger.i("user:" + user);
          //如果为空，重新登录
          if (user == null) {
            //状态图
            mFlNetError.setVisibility(View.VISIBLE);
            isShowConnecting(true);
            //发起登录
            new Thread() {
              @Override public void run() {
                super.run();
                try {
                  sleep(3000);
                  connectAndLogin();
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }
              }
            }.start();
            return;
          }
          //状态图
          mFlNetError.setVisibility(View.GONE);
          isShowConnecting(false);
          //建立对话
          if (XMPPConnectUtils.getConnectStatus()) {
            //连接成功
            EventBus.getDefault().postSticky(new ChatConnectEvent(ChatConnectEvent.CONNECTED));
            //addStanzaListener
            addStanzaListener(XMPPConnectUtils.getConnection());
            //建立监听
            createConnectListener();
            //开启connect service
            Intent intent = new Intent(MainActivity.this, ConnectService.class);
            startService(intent);
            //开启ping service
            Intent intentPing = new Intent(MainActivity.this, PingService.class);
            startService(intentPing);
            //判断 收银端是否在线
            boolean adminOnLine = isAdminOnLine();
            //发送event，刷新桌台
            if (adminOnLine) {
              //EventBus.getDefault().post(new ReqTableStatusEvent());
              EventBus.getDefault().postSticky(new AdminStatusEvent(AdminStatusEvent.ADMIN_ONLINE));
            } else {
              //收银端不在线
              ToastUtils.showShort(MainActivity.this, "收银端不在线");
            }
          }
          break;
      }
    }
  };

  /**
   * 收银端是否在线
   */
  private boolean isAdminOnLine() {
    XMPPTCPConnection connection = XMPPConnectUtils.getConnection();
    Roster roster = Roster.getInstanceFor(connection);
    List<User> userList = DaoServiceUtil.getUserService().queryAll();
    if (userList == null || userList.size() == 0) {
      return false;
    }
    String adminUser = "admin" + userList.get(0).getCompanyCode().toLowerCase() + "@" + Setting.SERVER_NAME;
    if (roster == null || roster.getPresence(adminUser) == null) return false;
    return roster.getPresence(adminUser).isAvailable();
  }

  /**
   * 上下线监听 刷新最新数据
   */
  private void addStanzaListener(XMPPTCPConnection connection) {
    List<User> userList = DaoServiceUtil.getUserService().queryAll();
    if (userList == null || userList.size() == 0) return;
    String companyCode = userList.get(0).getCompanyCode();
    final String from = "admin" + companyCode;
    StanzaFilter filter = new StanzaFilter() {
      @Override public boolean accept(Stanza stanza) {
        if (stanza instanceof Presence) {
          return true;
        }
        return false;
      }
    };
    connection.addAsyncStanzaListener(new StanzaListener() {
      @Override public void processPacket(Stanza packet)
          throws SmackException.NotConnectedException {
        if (!(packet instanceof Presence)) return;
        Presence presence = (Presence) packet;
        String presenceFrom = "";
        try {
          String[] strings = presence.getFrom().split("@");
          presenceFrom = strings[0];
        } catch (Exception e) {

        }
        //来自 收银端
        if (presenceFrom.equalsIgnoreCase(from)) {
          //停顿 一秒
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          Looper.prepare();
          String msg = "收银端不在线";
          String adminStatus = AdminStatusEvent.ADMIN_NOT_ONLINE;
          if (Presence.Type.available.equals(presence.getType())) {
            msg = "收银端上线了";
            adminStatus = AdminStatusEvent.ADMIN_ONLINE;
          } else if (Presence.Type.unavailable.equals(presence.getType())) {
            msg = "收银端下线了";
            adminStatus = AdminStatusEvent.ADMIN_OFFLINE;
          }
          ToastUtils.showShort(MainActivity.this, msg);
          //刷新桌台列表 刷新数据
          EventBus.getDefault().postSticky(new AdminStatusEvent(adminStatus));
          Looper.loop();
        }
      }
    }, filter);
  }

  /**
   * 建立连接监听
   */
  private void createConnectListener() {
    final XMPPTCPConnection connection = XMPPConnectUtils.getConnection();
    connection.addConnectionListener(new ConnectionListener() {
      @Override public void connected(final XMPPConnection connection) {
        Logger.i("Connect");
        EventBus.getDefault().postSticky(new ChatConnectEvent(ChatConnectEvent.CONNECTED));
        //状态图
        runOnUiThread(new Runnable() {
          @Override public void run() {
            mFlNetError.setVisibility(View.GONE);
            //关闭连接中 蒙层
            isShowConnecting(false);
          }
        });
      }

      @Override public void authenticated(XMPPConnection connection, boolean resumed) {

      }

      @Override public void connectionClosed() {
        Logger.i("ConnectClosed");
        //状态图
        runOnUiThread(new Runnable() {
          @Override public void run() {
            mFlNetError.setVisibility(View.VISIBLE);
            //关闭连接中 蒙层
            isShowConnecting(true);
          }
        });
        //刷新
        EventBus.getDefault().post(new ChatConnectEvent(ChatConnectEvent.CONNECTION_CLOSED));
      }

      @Override public void connectionClosedOnError(Exception e) {
        Logger.e("ConnectClosedOnError:" + e.getMessage());
        //关闭连接中 蒙层
        mRlConnect.postDelayed(new Runnable() {
          @Override public void run() {
            isShowConnecting(false);
          }
        }, 3000);
        //状态图
        runOnUiThread(new Runnable() {
          @Override public void run() {
            mFlNetError.setVisibility(View.VISIBLE);
            //显示连接中 蒙层
            isShowConnecting(true);
          }
        });
        //刷新
        EventBus.getDefault().post(new ChatConnectEvent(ChatConnectEvent.CONNECTION_CLOSED_ON_ERROR));
      }

      @Override public void reconnectionSuccessful() {
        Logger.i("ReconnectionSuccessful");
        //状态图
        runOnUiThread(new Runnable() {
          @Override public void run() {
            mFlNetError.setVisibility(View.GONE);
            //关闭连接中 蒙层
            isShowConnecting(false);
          }
        });
        //显示 刷新
        EventBus.getDefault().postSticky(new ChatConnectEvent(ChatConnectEvent.RECONNECTION_SUCCESSFUL));
      }

      @Override public void reconnectingIn(int seconds) {
        Logger.i("ReconnectingIn");
      }

      @Override public void reconnectionFailed(Exception e) {
        Logger.i("ReconnectionFailed");
        //状态图
        runOnUiThread(new Runnable() {
          @Override public void run() {
            mFlNetError.setVisibility(View.VISIBLE);
            isShowConnecting(true);
          }
        });
        EventBus.getDefault().postSticky(new ChatConnectEvent(ChatConnectEvent.RECONNECTION_FAILED));
      }
    });
  }

  //
  @OnClick({
      R.id.tv_start_bill, R.id.tv_user_center, R.id.tv_modify_bill
  }) public void onClick(View view) {
    switch (view.getId()) {
      case R.id.tv_start_bill:
        startActivity(new Intent(this, SelectedTableStartBillActivity.class));
        break;
      case R.id.tv_modify_bill:
        startActivity(new Intent(this, SelectedTableModifyBillActivity.class));
        break;
      case R.id.tv_user_center:
        startActivity(new Intent(this, UserCenterActivity.class));
        break;
    }
  }

  @Override protected void onDestroy() {
    exit();
    EventBus.getDefault().unregister(this);
    super.onDestroy();
  }

  private void exit() {
    ButterKnife.unbind(this);
    //断开Smack
    if (XMPPConnectUtils.getConnection() != null) {
      XMPPConnectUtils.closeConnection();
      XMPPConnectUtils.getConnection().instantShutdown();
    }
    //关闭connect service
    Intent intent = new Intent(MainActivity.this, ConnectService.class);
    stopService(intent);
    //关闭ping service
    Intent intentPing = new Intent(MainActivity.this, PingService.class);
    stopService(intentPing);
  }

  @Override public void onBackPressed() {
    //对话框
    new MaterialDialog.Builder(this).title("警告")
        .content("是否退出?")
        .positiveText("确认")
        .negativeText("取消")
        .negativeColor(getResources().getColor(R.color.primary_text))
        .onPositive(new MaterialDialog.SingleButtonCallback() {
          @Override public void onClick(MaterialDialog dialog, DialogAction which) {
            MainActivity.this.finish();
            System.exit(0);
          }
        })
        .canceledOnTouchOutside(false)
        .show();
  }

  @Subscribe(threadMode = ThreadMode.MAIN) public void loginConflict(LoginConflictEvent event) {
    ToastUtils.showShort(MainActivity.this, "该帐号已在别处登录，不能重复登录");
  }
}
