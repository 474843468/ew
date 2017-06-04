package com.think.firewaiter.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.widget.EditText;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.think.firewaiter.R;
import com.think.firewaiter.ui.base.BaseToolbarActivity;
import com.think.firewaiter.utils.ToastUtils;

/**
 * Created by dorado on 2016/6/10.
 */
public class InitDataActivity extends BaseToolbarActivity {
  private static final int PERMISSION_READ_PHONE_STATE = 1;
  @Bind(R.id.input_company_code) TextInputLayout mInputCompanyCode;
  @Bind(R.id.input_username) TextInputLayout mInputUsername;
  @Bind(R.id.input_password) TextInputLayout mInputPassword;
  private EditText mEtCompanyCode;
  private EditText mEtUsername;
  private EditText mEtPassword;

  @Override protected int provideContentViewId() {
    return R.layout.activity_init_data;
  }

  @Override protected String provideToolbarTitle() {
    return "更新数据";
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ButterKnife.bind(this);

    mEtCompanyCode = mInputCompanyCode.getEditText();
    mEtUsername = mInputUsername.getEditText();
    mEtPassword = mInputPassword.getEditText();

    mInputCompanyCode.setHint("公司编码:");
    mInputUsername.setHint("用户名:");
    mInputPassword.setHint("初始化密码:");
  }

  /**
   * 登录
   */
  //@formatter:off
  @OnClick(R.id.btn_init) public void login() {
    String companyCode = mInputCompanyCode.getEditText().getText().toString().trim();
    String username = mInputUsername.getEditText().getText().toString().trim();
    String password = mInputPassword.getEditText().getText().toString().trim();

    if (TextUtils.isEmpty(companyCode.trim())) {
      mEtCompanyCode.setError("公司编码不能为空");
      return;
    }

    if (TextUtils.isEmpty(username.trim())) {
      mEtUsername.setError("用户名不能为空");
      return;
    }

    if (TextUtils.isEmpty(password.trim())) {
      mEtPassword.setError("初始化密码不能为空");
      return;
    }
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      //初始化数据
      initData();
    }else{
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        //申请READ_PHONE_STATE
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_READ_PHONE_STATE);
      }else{
        initData();
      }
    }
  }

  /**
   * 初始化数据
   */
  private void initData() {
    String companyCode = mInputCompanyCode.getEditText().getText().toString().trim();
    String username = mInputUsername.getEditText().getText().toString().trim();
    String password = mInputPassword.getEditText().getText().toString().trim();

    Intent intent = new Intent();
    Bundle bundle = new Bundle();
    bundle.putString("companyCode", companyCode);
    bundle.putString("username", username);
    bundle.putString("password", password);
    intent.putExtra("bundle", bundle);
    intent.setClass(this, SyncActivity.class);
    intent.putExtra("fromWhere", SyncActivity.FROM_INITDATA_ACTIVITY);
    startActivity(intent);
    finish();
  }

  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == PERMISSION_READ_PHONE_STATE){
      if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
        initData();
      }else{
        ToastUtils.showShort(InitDataActivity.this,"缺少权限,无法初始化数据.请打开权限!");
      }
    }
  }
}




