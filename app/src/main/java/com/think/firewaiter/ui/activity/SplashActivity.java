package com.think.firewaiter.ui.activity;

import android.os.Bundle;
import android.widget.FrameLayout;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.think.firewaiter.R;
import com.think.firewaiter.config.Setting;
import com.think.firewaiter.ui.base.BaseActivity;
import com.think.firewaiter.utils.SPUtils;

/**
 * Created by dorado on 2016/6/18.
 */
public class SplashActivity extends BaseActivity {
  @Bind(R.id.fl_content) FrameLayout mFlContent;

  @Override protected int provideContentViewId() {
    return R.layout.activity_slpash;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ButterKnife.bind(this);
    final String isInstall = (String) SPUtils.get(SplashActivity.this, Setting.IS_INSTALL, "0");
    mFlContent.postDelayed(new Runnable() {
      @Override public void run() {
        if (isInstall.equals("0")) {
          openActivity(WelcomeActivity.class);
        } else {
          openActivity(LoginActivity.class);
        }
        finish();
      }
    }, 2000);
  }
}
