package com.think.firewaiter.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.daimajia.numberprogressbar.NumberProgressBar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.orhanobut.logger.Logger;
import com.think.firewaiter.R;
import com.think.firewaiter.common.App;
import com.think.firewaiter.config.Setting;
import com.think.firewaiter.dao.DaoServiceUtil;
import com.think.firewaiter.dao.UserDao;
import com.think.firewaiter.dao.data.DataLoader;
import com.think.firewaiter.module.User;
import com.think.firewaiter.network.HttpCheckUpdateReq;
import com.think.firewaiter.network.HttpCheckUpdateResp;
import com.think.firewaiter.network.HttpDataSyncReq;
import com.think.firewaiter.network.HttpDataSyncResp;
import com.think.firewaiter.network.RestClient;
import com.think.firewaiter.utils.MaterialDialogUtils;
import com.think.firewaiter.utils.PackageUtils;
import com.think.firewaiter.utils.SPUtils;
import com.think.firewaiter.utils.ToastUtils;
import cz.msebera.android.httpclient.Header;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class SyncActivity extends AppCompatActivity {
  @Bind(R.id.tv_data_size) TextView mTvDataSize;
  @Bind(R.id.number_progress_bar) NumberProgressBar mPb;
  private static final int DATA_SIZE = 0;//数据size
  private static final int UPDATE_PROGRESS = 1;//进度条更新
  public static final int FROM_INITDATA_ACTIVITY = 0;//来自初始化数据 初始化或更新数据 --  跳LoginActivity
  public static final int FROM_USER_FRAGMENT = 1;//来自UserFragment 更新数据 --- 跳LoginActivity
  public static final int FROM_LOGIN_ACTIVITY = 2;//来自 LoginActivity 登录更新数据 -- 跳MainActivity

  private int mDataSize;//更新数据量
  private Timer mTimer;//更新progressbar
  private int mFromWhere;
  private TimerTask mTask;
  private MyHandler mHandler;//UI Handler

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.activity_sync);
    ButterKnife.bind(this);
    mHandler = new MyHandler();

    Intent intent = getIntent();
    mFromWhere = intent.getIntExtra("fromWhere", FROM_INITDATA_ACTIVITY);
    Bundle bundle = intent.getBundleExtra("bundle");

    String companyCode = bundle.getString("companyCode");
    String username = bundle.getString("username");
    String password = bundle.getString("password");
    // 来自登录界面先检查版本更新
    if (FROM_LOGIN_ACTIVITY == mFromWhere) {
      checkVersion(companyCode, username, password);
    } else {
      //2.同步数据
      syncData(companyCode, username, password);
    }
  }

  /**
   * 检查版本
   */
  //@formatter:off
  private void checkVersion(final String companyCode, final String username,
      final String password) {
    String path = Setting.BASE_URL + "version/update";
    //显示检查更新dialog
    final MaterialDialog checkUpdateDialog = MaterialDialogUtils.showCheckUpdateDialog(SyncActivity.this);

    HttpCheckUpdateReq req = new HttpCheckUpdateReq();
    PackageUtils packageUtils = new PackageUtils(SyncActivity.this);
    int localVersionCode = packageUtils.getLocalVersionCode();
    String localVersionName = packageUtils.getLocalVersionName();
    req.setVersionName(localVersionName);
    req.setVersionCode(localVersionCode);
    req.setType("1");
    final User user = DaoServiceUtil.getUserService()
        .queryBuilder()
        .where(UserDao.Properties.LoginName.eq("admin"))
        .unique();
    if (user == null) {
      ToastUtils.showShort(App.getContext(), "无用户,请初始化数据");
      return;
    }
    req.setCompanyCode(user.getCompanyCode());
    req.setUserId(user.getObjectId());
    new RestClient(0, 1000, 3000, 3000) {
      @Override protected void start() {

      }

      @Override protected void finish() {

      }

      @Override protected void failure(String responseString, Throwable throwable) {
        Logger.i("---" + responseString);
        //ToastUtils.showShort(App.getContext(), "更新失败");
        MaterialDialogUtils.dismissDialog(checkUpdateDialog);
        //继续更新数据
        syncData(companyCode, username, password);
      }

      @Override protected void success(String responseString) {
        Logger.i("---" + responseString);
        HttpCheckUpdateResp checkUpdateResp = new Gson().fromJson(responseString, HttpCheckUpdateResp.class);
        if (checkUpdateResp.getStatusCode() == 1) {
          //检查版本匹配
          checkVersionCode(checkUpdateResp, companyCode, username, password);
        } else {
          //ToastUtils.showShort(App.getContext(), "错误:" + checkUpdateResp.getMsg());
          //继续更新数据
          syncData(companyCode, username, password);
        }
        MaterialDialogUtils.dismissDialog(checkUpdateDialog);
      }
    }.post(App.getContext(), path, req);
  }

  /**
   * 匹配版本
   */
  //@formatter:off
  private void checkVersionCode(final HttpCheckUpdateResp resp, final String companyCode,
      final String username, final String password) {
    //获取当前版本
    int localVersionCode = new PackageUtils(SyncActivity.this).getLocalVersionCode();
    //比较版本
    if (localVersionCode < resp.getVersionCode()) {
      //强制更新
      if (HttpCheckUpdateResp.FORCE_UPDATE_TRUE.equals(resp.getForceUpdate())) {
        downLoadNewVersion(resp, companyCode, username, password);
      } else {
        final MaterialDialog loadingDialog = MaterialDialogUtils.showDownLoadDialog(SyncActivity.this, resp.getVersionNumber(), resp.getUpdateInfo());
        MDButton positive = loadingDialog.getActionButton(DialogAction.POSITIVE);
        MDButton negBtn = loadingDialog.getActionButton(DialogAction.NEGATIVE);
        positive.setOnClickListener(new View.OnClickListener() {
          @Override public void onClick(View v) {
            MaterialDialogUtils.dismissDialog(loadingDialog);
            downLoadNewVersion(resp, companyCode, username, password);
          }
        });

        negBtn.setOnClickListener(new View.OnClickListener() {
          @Override public void onClick(View v) {
            MaterialDialogUtils.dismissDialog(loadingDialog);
             //继续更新数据
              syncData(companyCode, username, password);
          }
        });
      }
    } else {
      //继续更新数据
      syncData(companyCode, username, password);
    }
  }
  /**
   * 下载新版本
   */
  private void downLoadNewVersion(final HttpCheckUpdateResp resp, final String companyCode, final String username, final String password) {
    //下载中的dialog
    final MaterialDialog loadingDialog = MaterialDialogUtils.showDownLoadingDialog(SyncActivity.this);
    //下载
    File file = PackageUtils.getSaveAPKFile(SyncActivity.this);
    final AsyncHttpClient client = new AsyncHttpClient();
    client.setMaxRetriesAndTimeout(1, 10 * 1000);
    client.get(App.getContext(), resp.getUrl(), new FileAsyncHttpResponseHandler(file) {
      @Override
      public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
        ToastUtils.showShort(App.getContext(), "网络错误:" + throwable.getMessage());
        MaterialDialogUtils.dismissDialog(loadingDialog);
        //继续更新数据
        syncData(companyCode, username, password);
      }

      @Override public void onProgress(long bytesWritten, long totalSize) {
        super.onProgress(bytesWritten, totalSize);
        loadingDialog.setMaxProgress((int) totalSize);
        loadingDialog.setProgress((int) bytesWritten);
      }

      @Override public void onSuccess(int statusCode, Header[] headers, File file) {
        MaterialDialogUtils.dismissDialog(loadingDialog);
        PackageUtils.installApk(SyncActivity.this,file);
      }
    });

    //取消更新
    MDButton posBtn = loadingDialog.getActionButton(DialogAction.POSITIVE);
    posBtn.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        loadingDialog.dismiss();
        client.cancelAllRequests(true);
        client.removeAllHeaders();
      }
    });
    //强制更新影藏
    if (HttpCheckUpdateResp.FORCE_UPDATE_TRUE.equals(resp.getForceUpdate())){
      posBtn.setVisibility(View.INVISIBLE);
    }
  }

  /**
   * 同步数据
   */
  //@formatter:off
  private void syncData(final String companyCode, final String username, final String password) {

    String imei = "";
    if (isTabletDevice(SyncActivity.this)) {
      //String m_szAndroidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
      //we make this look like a valid IMEI
      imei = Build.SERIAL + Build.BOARD.length() % 10 + Build.BRAND.length() % 10
          + Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 + Build.DISPLAY.length() % 10
          + Build.HOST.length() % 10 + Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10
          + Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 + Build.TAGS.length() % 10
          + Build.TYPE.length() % 10 + Build.USER.length() % 10;
    } else {
      imei = ((TelephonyManager) this.getSystemService(TELEPHONY_SERVICE)).getDeviceId();
    }
    //发送请求
    HttpDataSyncReq req = new HttpDataSyncReq();
    req.setCompanyCode(companyCode.toUpperCase());
    req.setLoginName(username);
    req.setPassword(password);
    req.setInitPassword(password);
    req.setType("1");
    req.setEmi(imei);
    String isInstall = (String) SPUtils.get(SyncActivity.this, Setting.IS_INSTALL, "0");
    //如果不是第一次安装
    if (isInstall.equals("1")) {
      //如果是版本更新后第一次拉数据，要拉所有的
      String dateVersionUpdate = (String) SPUtils.get(SyncActivity.this, Setting.DATA_VERION_ON_UPDATE, "1.05");
      PackageUtils packageUtils = new PackageUtils(SyncActivity.this);
      String localVersionName = packageUtils.getLocalVersionName();
      if (localVersionName.equals(dateVersionUpdate) == false) {
        isInstall = "0";
      }
    }
    req.setInstall(isInstall);
    new RestClient() {
      @Override protected void start() {

      }

      @Override protected void finish() {

      }

      @Override protected void failure(String responseString, Throwable throwable) {
        if (mFromWhere == FROM_LOGIN_ACTIVITY) {
          turnToMainAct();
        } else {
          turnToLoginAct();
        }
      }

      @Override protected void success(String responseString) {
        Log.i("Data",responseString);
        Logger.json(responseString);
        //保存打印机数据到本地数据库
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        final HttpDataSyncResp syncResp = gson.fromJson(responseString, HttpDataSyncResp.class);
        if (syncResp == null) {
          if (mFromWhere == FROM_LOGIN_ACTIVITY) {
            turnToMainAct();
          } else {
            turnToLoginAct();
          }
          return;
        }
        if (syncResp.getStatusCode() != 1) {
          ToastUtils.showShort(SyncActivity.this, "请求错误:" + syncResp.getMsg());
          if (mFromWhere == FROM_LOGIN_ACTIVITY) {
            turnToMainAct();
          } else {
            turnToLoginAct();
          }
          return;
        }
        if (syncResp.getDataList() == null || syncResp.getDataList().size() == 0) {
          ToastUtils.showShort(SyncActivity.this, "服务器无最新数据");
          if (mFromWhere == FROM_LOGIN_ACTIVITY) {
            turnToMainAct();
          } else {
            turnToLoginAct();
          }
        } else {
          //变更Sp里的数据版本
          PackageUtils packageUtils = new PackageUtils(SyncActivity.this);
          String localVersionName = packageUtils.getLocalVersionName();
          SPUtils.put(SyncActivity.this, Setting.DATA_VERION_ON_UPDATE, localVersionName);
          //dataList 的数量
          Message msg = Message.obtain();
          msg.what = DATA_SIZE;
          mDataSize = syncResp.getDataList().size();
          msg.obj = mDataSize;
          mHandler.sendMessage(msg);
          new Thread() {
            @Override public void run() {
              super.run();
              //sp companyCode
               if (mFromWhere == FROM_INITDATA_ACTIVITY) {
                SPUtils.put(App.getContext(), Setting.SP_COMPANY_CODE, companyCode);
               }
              //储存数据
              DataLoader.getInstance().saveData(syncResp.getDataList());
              //更新进度条
              updateProgress();
            }
          }.start();
        }
      }
    }.post(this, Setting.BASE_URL + "v1/dataSync/list", req);
  }

  /**
   * 更新进度条，关闭蒙层
   */
  int i = 0;

  private void updateProgress() {
    int timeUnit = 100;//毫秒值
    if (mDataSize < 50) {
      timeUnit = 200;
    } else {
      timeUnit = 500;
    }
    mTimer = new Timer();
    mTask = new TimerTask() {
      @Override public void run() {
        mHandler.sendEmptyMessage(UPDATE_PROGRESS);
        i = i + 1;
        if (i == 4) {
          if (mFromWhere == FROM_LOGIN_ACTIVITY) {
            turnToMainAct();
          } else {
            turnToLoginAct();
          }
        }
      }
    };
    mTimer.schedule(mTask, timeUnit, timeUnit);
  }

  /**
   * UI Handler
   */
  class MyHandler extends android.os.Handler {
    @Override public void handleMessage(Message msg) {
      switch (msg.what) {
        case DATA_SIZE:
          int size = (int) msg.obj;
          if (mTvDataSize != null) {
            mTvDataSize.setText("更新" + size + "条数据");
          }
          break;
        case UPDATE_PROGRESS:
          if (mPb != null) {
            mPb.incrementProgressBy(25);
          }
          break;
      }
    }
  }

  /**
   * 跳转 LoginAct
   */
  private void turnToLoginAct() {
    Intent intent = new Intent();
    intent.putExtra("hasSync", true);
    intent.setClass(this, LoginActivity.class);
    startActivity(intent);
    finish();
  }

  /**
   * 跳转 MainAct
   */
  private void turnToMainAct() {
    startActivity(new Intent(this, MainActivity.class));
    finish();
  }
  /**
   * 判断是否平板设备 (官方的例子)
   *
   * @return true:平板,false:手机
   */
  private boolean isTabletDevice(Context context) {
    return (context.getResources().getConfiguration().screenLayout
        & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
  }

  @Override protected void onDestroy() {
    ButterKnife.unbind(this);
    if (mTimer != null) {
      mTimer.cancel();
      mTimer = null;
    }
    if (mTask != null) {
      mTask.cancel();
      mTask = null;
    }
    mHandler = null;
    super.onDestroy();
  }
}
