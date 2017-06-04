package com.think.firewaiter.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.orhanobut.logger.Logger;
import com.think.firewaiter.R;
import com.think.firewaiter.chat.chatUtils.XMPPConnectUtils;
import com.think.firewaiter.common.App;
import com.think.firewaiter.config.Setting;
import com.think.firewaiter.dao.DaoServiceUtil;
import com.think.firewaiter.dao.UserDao;
import com.think.firewaiter.module.User;
import com.think.firewaiter.network.Base64;
import com.think.firewaiter.network.HttpCheckUpdateReq;
import com.think.firewaiter.network.HttpCheckUpdateResp;
import com.think.firewaiter.network.RestClient;
import com.think.firewaiter.ui.activity.InitDataActivity;
import com.think.firewaiter.ui.activity.LoginActivity;
import com.think.firewaiter.ui.activity.MainActivity;
import com.think.firewaiter.ui.activity.ServeTablesActivity;
import com.think.firewaiter.ui.activity.SyncActivity;
import com.think.firewaiter.utils.MaterialDialogUtils;
import com.think.firewaiter.utils.NetUtils;
import com.think.firewaiter.utils.PackageUtils;
import com.think.firewaiter.utils.SPUtils;
import com.think.firewaiter.utils.ToastUtils;
import cz.msebera.android.httpclient.Header;
import java.io.File;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

/**
 * Created by THINK on 2016/4/8.
 */
public class UserFragment extends Fragment {

  private String mParam;
  private static final String USER_PARAM = "param";
  private MainActivity mAct;

  public static UserFragment newInstance(String param) {
    UserFragment fragment = new UserFragment();
    Bundle args = new Bundle();
    args.putString(USER_PARAM, param);
    fragment.setArguments(args);
    return fragment;
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      mParam = getArguments().getString(USER_PARAM);
    }
  }

  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_user, null);
    mAct = (MainActivity) getActivity();
    ButterKnife.bind(this, view);
    return view;
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    //重置ButterKnife
    ButterKnife.unbind(this);
  }

  /**
   * 数据更新
   */
  @OnClick(R.id.rl_update_data) public void updateData() {
    //对话框
    new MaterialDialog.Builder(mAct).title("警告")
        .content("确认要从服务器更新数据到这台设备吗?")
        .positiveText("确定")
        .negativeText("取消")
        .negativeColor(getResources().getColor(R.color.primary_text))
        .onPositive(new MaterialDialog.SingleButtonCallback() {
          @Override public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
            refreshData();
          }
        })
        .show();
  }

  /**
   * 数据更新
   */
  //@formatter:off
  private void refreshData() {
    if (App.getContext() == null || ((App)App.getContext()).getUser() == null){
      ToastUtils.showShort(mAct,"App有异常发生，请重新启动App");
      return;
    }
     if (!NetUtils.isConnected(mAct)) {
      ToastUtils.showShort(mAct, "暂无网络，稍后重试");
      return;
    }
    String username = (String) SPUtils.get(mAct, Setting.SP_USERNAME, "");
    String password = ((App) App.getContext()).getUser().getInitPassword();
    if (TextUtils.isEmpty(username.trim()) || password == null || TextUtils.isEmpty(password.trim())) {
      ToastUtils.showShort(mAct, "信息有误，无法更新");
      return;
    }
    User user = DaoServiceUtil.getUserService()
        .queryBuilder()
        .where(UserDao.Properties.DelFlag.eq("0"))
        .where(UserDao.Properties.LoginName.eq(username))
        .unique();
    if (user == null) {
      ToastUtils.showShort(mAct, "信息有误，无法更新");
      return;
    }
    String companyCode = user.getCompanyCode();
    if (companyCode == null || TextUtils.isEmpty(companyCode.trim())) {
      ToastUtils.showShort(mAct, "信息有误，无法更新");
      return;
    }
    //跳转更新
    Intent intent = new Intent();
    Bundle bundle = new Bundle();
    bundle.putString("companyCode",companyCode);
    bundle.putString("username",username);
    bundle.putString("password",password);
    intent.putExtra("bundle",bundle);
    intent.putExtra("fromWhere",SyncActivity.FROM_USER_FRAGMENT);
    intent.setClass(mAct,SyncActivity.class);
    startActivity(intent);
    mAct.finish();
  }

  /**
   * 登出
   */
  @OnClick(R.id.rl_log_out) public void logOut() {
    new MaterialDialog.Builder(mAct).title("警告")
        .content("是否登出?")
        .positiveText("确认")
        .negativeText("取消")
        .negativeColor(getResources().getColor(R.color.primary_text))
        .canceledOnTouchOutside(false)
        .onPositive(new MaterialDialog.SingleButtonCallback() {
          @Override public void onClick(MaterialDialog dialog, DialogAction which) {
            SPUtils.put(mAct, Setting.SP_USERNAME, "");
            SPUtils.put(mAct, Setting.SP_PWD, "");
            SPUtils.put(mAct, Setting.REMEMBER_PWD, false);
            Intent intent = new Intent(mAct, LoginActivity.class);
            startActivity(intent);
            XMPPTCPConnection connection = XMPPConnectUtils.getConnection();
            if (connection != null){
              connection.disconnect();
              connection.instantShutdown();
            }
            getActivity().finish();
          }
        })
        .show();
  }

  /**
   * 检查更新版本
   */
  @OnClick(R.id.rl_check_update) public void checkUpdate() {
    if (!NetUtils.isConnected(mAct)) {
      ToastUtils.showShort(mAct, "暂无网络，稍后重试");
      return;
    }

    String path = Setting.BASE_URL + "version/update";
    //显示检查更新dialog
    final MaterialDialog checkUpdateDialog = MaterialDialogUtils.showCheckUpdateDialog(mAct);

    HttpCheckUpdateReq req = new HttpCheckUpdateReq();
    PackageUtils packageUtils = new PackageUtils(mAct);
    int localVersionCode = packageUtils.getLocalVersionCode();
    String localVersionName = packageUtils.getLocalVersionName();
    req.setVersionName(localVersionName);
    req.setVersionCode(localVersionCode);
    req.setType("1");
    User user = DaoServiceUtil.getUserService()
        .queryBuilder()
        .where(UserDao.Properties.LoginName.eq("admin"))
        .unique();
    if (user == null){
      ToastUtils.showShort(mAct,"无用户,请初始化数据");
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
        ToastUtils.showShort(mAct, "更新失败");
        MaterialDialogUtils.dismissDialog(checkUpdateDialog);
      }

      @Override protected void success(String responseString) {
        Logger.i("---" + responseString);
        HttpCheckUpdateResp checkUpdateResp = new Gson().fromJson(responseString, HttpCheckUpdateResp.class);
        if (checkUpdateResp.getStatusCode() == 1) {
          //检查版本匹配
          checkVersion(checkUpdateResp);
        } else {
          ToastUtils.showShort(mAct, "错误:" + checkUpdateResp.getMsg());
        }
        MaterialDialogUtils.dismissDialog(checkUpdateDialog);
      }
    }.post(mAct,path,req);
  }

  /**
   * 判断是否是新的版本
   */
  private void checkVersion(final HttpCheckUpdateResp resp) {
    //获取当前版本
    int localVersionCode = new PackageUtils(mAct).getLocalVersionCode();
    //比较版本
    if (localVersionCode < resp.getVersionCode()) {
      final MaterialDialog loadingDialog = MaterialDialogUtils.showDownLoadDialog(mAct,resp.getVersionNumber(),resp.getUpdateInfo());
      //更新
      MDButton positive = loadingDialog.getActionButton(DialogAction.POSITIVE);
      positive.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          MaterialDialogUtils.dismissDialog(loadingDialog);
          downLoadNewVersion(resp.getUrl());
        }
      });
    } else {
      ToastUtils.showShort(mAct, "已是最新版本，无需更新!");
    }

  }

  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

  }

  /**
   * 下载新版本
   */
  private void downLoadNewVersion(String url) {
    //下载中的dialog
    final MaterialDialog loadingDialog = MaterialDialogUtils.showDownLoadingDialog(mAct);
    //下载
    File file = PackageUtils.getSaveAPKFile(mAct);
    final AsyncHttpClient client = new AsyncHttpClient();
    client.setMaxRetriesAndTimeout(1, 10 * 1000);
    client.get(mAct, url, new FileAsyncHttpResponseHandler(file) {
      @Override
      public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
        ToastUtils.showShort(mAct, "网络错误:" + throwable.getMessage());
        MaterialDialogUtils.dismissDialog(loadingDialog);
      }

      @Override public void onProgress(long bytesWritten, long totalSize) {
        super.onProgress(bytesWritten, totalSize);
        loadingDialog.setMaxProgress((int) totalSize);
        loadingDialog.setProgress((int) bytesWritten);
      }

      @Override public void onSuccess(int statusCode, Header[] headers, File file) {
        MaterialDialogUtils.dismissDialog(loadingDialog);
        PackageUtils.installApk(mAct,file);
      }
    });
    //取消更新
    loadingDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        loadingDialog.dismiss();
        client.cancelAllRequests(true);
        client.removeAllHeaders();
      }
    });
  }

  /**
   * 选择服务桌台
   */
  @OnClick(R.id.rl_serve_tables) public void serTable() {
    Intent intent = new Intent(mAct, ServeTablesActivity.class);
    startActivity(intent);
  }
}
