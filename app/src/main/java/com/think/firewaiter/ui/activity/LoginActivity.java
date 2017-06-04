package com.think.firewaiter.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.Bind;
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
import com.think.firewaiter.common.App;
import com.think.firewaiter.config.Setting;
import com.think.firewaiter.dao.DaoServiceUtil;
import com.think.firewaiter.dao.UserDao;
import com.think.firewaiter.module.User;
import com.think.firewaiter.network.Base64;
import com.think.firewaiter.network.HttpCheckUpdateReq;
import com.think.firewaiter.network.HttpCheckUpdateResp;
import com.think.firewaiter.network.RestClient;
import com.think.firewaiter.utils.CryptosUtils;
import com.think.firewaiter.utils.MaterialDialogUtils;
import com.think.firewaiter.utils.NetUtils;
import com.think.firewaiter.utils.PackageUtils;
import com.think.firewaiter.utils.SPUtils;
import com.think.firewaiter.utils.ToastUtils;
import cz.msebera.android.httpclient.Header;
import java.io.File;

/**
 * Created by zjq on 2016/4/9.
 */
public class LoginActivity extends AppCompatActivity {

  @Bind(R.id.input_name) TextInputLayout mInputUsername;//用户名
  @Bind(R.id.input_password) TextInputLayout mInputPassword;//密码
  @Bind(R.id.cb_save_pwd) CheckBox mCheckBox;
  @Bind(R.id.tv_app_version) TextView mTvVersion;

  private EditText mEtName;//用户名
  private EditText mEtPassword;//密码
  private boolean mHasSync;//已经 更新数据

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    ButterKnife.bind(this);
    Intent intent = getIntent();
    mHasSync = intent.getBooleanExtra("hasSync", false);

    mEtName = mInputUsername.getEditText();
    mEtPassword = mInputPassword.getEditText();

    mInputUsername.setHint("用户名");
    mInputPassword.setHint("密码");
    //之前是否记住密码
    Boolean isSavePwd = (Boolean) SPUtils.get(this, Setting.REMEMBER_PWD, false);
    mCheckBox.setChecked(isSavePwd);
    //如果记住密码，则显示在tv上
    if (isSavePwd) {
      String username = (String) SPUtils.get(this, Setting.SP_USERNAME, "");
      String pwd = (String) SPUtils.get(this, Setting.SP_PWD, "");
      mEtName.setText(username);
      mEtPassword.setText(pwd);
    }
    //初始化版本号
    initVersion();
  }

  /**
   * 显示当前版本
   */
  private void initVersion() {
    PackageUtils packageUtils = new PackageUtils(this);
    String versionName = packageUtils.getLocalVersionName();
    char[] chars = versionName.toCharArray();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < chars.length; i++) {
      char c = chars[i];
      if (!Character.isDigit(c)) continue;
      if (i == chars.length - 1) {
        sb.append(c);
      } else {
        sb.append(c + ".");
      }
    }
    mTvVersion.setText("当前版本:" + sb.toString());
  }

  /**
   * 登录
   */
  //@formatter:on
  @OnClick(R.id.btn_login) public void login(Button button) {
    String username = mInputUsername.getEditText().getText().toString().trim();
    String password = mInputPassword.getEditText().getText().toString().trim();
    if (TextUtils.isEmpty(username)) {
      mEtName.setError("用户名不能为空");
      return;
    }
    if (username.equals("admin")) {
      mEtName.setError("用户名不能为Admin，请添加新用户");
      return;
    }
    if (TextUtils.isEmpty(password)) {
      mEtPassword.setError("密码不能为空");
      return;
    }
    String isInstall = (String) SPUtils.get(this, Setting.IS_INSTALL, "0");
    if (isInstall.equals("0")) {
      ToastUtils.showShort(this, "请先初始化数据");
      return;
    }
    User user = DaoServiceUtil.getUserService()
        .queryBuilder()
        .where(UserDao.Properties.LoginName.eq(mEtName.getText().toString().trim()))
        .unique();
    if (user == null) {
      ToastUtils.showShort(this, "无此用户");
      return;
    }
    String dbPwd = user.getPassword();
    boolean pwdCorrect =
        CryptosUtils.validatePassword(mEtPassword.getText().toString().trim(), dbPwd);
    if (pwdCorrect) {
      //设置聊天密码
      user.setChatPwd(user.getInitPassword());
      //设置用户
      ((App) App.getContext()).setUser(user);

      SPUtils.put(LoginActivity.this, Setting.REMEMBER_PWD, mCheckBox.isChecked());
      SPUtils.put(LoginActivity.this, Setting.SP_USERNAME, username);
      SPUtils.put(LoginActivity.this, Setting.SP_PWD, password);
      SPUtils.put(LoginActivity.this, Setting.LOGIN_USER, user.getObjectId());

      if (!NetUtils.isConnected(LoginActivity.this)) {
        ToastUtils.showShort(LoginActivity.this, "请检查网络!");
        turnToMain();
      } else if (!mHasSync) {//判断是不是跳转来自于初始化数据页 数据同步
        refreshData(user);
      } else {//刚初始化 、更新数据完
        turnToMain();
      }
    } else {
      ToastUtils.showShort(this, "密码错误");
    }
  }

  private void turnToMain() {
    //跳转
    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
    startActivity(intent);
    finish();
  }

  /**
   * 数据更新
   */
  //@formatter:off
  private void refreshData(User user) {
    String username = (String) SPUtils.get(LoginActivity.this, Setting.SP_USERNAME, "");
    String password = ((App) App.getContext()).getUser().getInitPassword();
    if (TextUtils.isEmpty(username.trim()) || password == null || TextUtils.isEmpty(password.trim())) {
      ToastUtils.showShort(LoginActivity.this, "信息有误，无法更新");
      return;
    }
    String companyCode = user.getCompanyCode();
    if (companyCode == null || TextUtils.isEmpty(companyCode.trim())) {
      ToastUtils.showShort(LoginActivity.this, "信息有误，无法更新");
      return;
    }
    //初始化数据
    Intent intent = new Intent();
    Bundle bundle = new Bundle();
    bundle.putString("companyCode",companyCode);
    bundle.putString("username",username);
    bundle.putString("password",password);
    intent.putExtra("bundle",bundle);
    intent.putExtra("fromWhere", SyncActivity.FROM_LOGIN_ACTIVITY);
    intent.setClass(this,SyncActivity.class);
    startActivity(intent);
    finish();
  }

  /**
   * 初始化接口
   */
  @OnClick(R.id.tv_init) public void initData() {
    Intent intent = new Intent(this, InitDataActivity.class);
    startActivity(intent);
  }

  /**
   * 退出
   */
  @Override protected void onDestroy() {
    super.onDestroy();
    ButterKnife.unbind(this);
  }
}
