package com.think.firewaiter.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bingoogolapple.qrcode.zxing.QRCodeDecoder;
import cn.bingoogolapple.qrcode.zxing.ZXingView;
import com.think.firewaiter.R;

/**
 * Created by THINK on 2016/4/10.
 */
public class ScanActivity extends AppCompatActivity implements QRCodeDecoder.Delegate {

  @Bind(R.id.zxing_view) ZXingView mZxingView;//二维码

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_scan);
    ButterKnife.bind(this);
  }

  @Override protected void onStart() {
    super.onStart();
    mZxingView.startCamera();
  }

  @Override protected void onStop() {
    mZxingView.stopCamera();
    super.onStop();
  }

  /**
   * 扫码回调
   */
  @Override public void onDecodeQRCodeSuccess(String result) {

  }

  @Override public void onDecodeQRCodeFailure() {

  }

  /**
   * 开启闪光灯
   */
  @OnClick(R.id.open_flashlight) public void openFlashLight(View view) {
    mZxingView.openFlashlight();
  }

  /**
   * 关闭闪光灯
   */
  @OnClick(R.id.close_flashlight) public void closeFlashLight(View view) {
    mZxingView.closeFlashlight();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    ButterKnife.unbind(this);
  }
}
