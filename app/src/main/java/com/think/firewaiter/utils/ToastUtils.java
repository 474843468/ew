package com.think.firewaiter.utils;

import android.content.Context;
import android.widget.Toast;
import com.think.firewaiter.common.App;

/**
 * Toast统一管理类
 */
public class ToastUtils {

  private ToastUtils() {
    /* cannot be instantiated */
    throw new UnsupportedOperationException("cannot be instantiated");
  }

  public static boolean isShow = true;

  /**
   * 短时间显示Toast
   */
  public static void showShort(Context context, CharSequence message) {
    if (isShow && App.getContext() != null) Toast.makeText(App.getContext(), message, Toast.LENGTH_SHORT).show();
  }

  /**
   * 短时间显示Toast
   */
  public static void showShort(Context context, int message) {
    if (isShow) Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
  }

  /**
   * 长时间显示Toast
   */
  public static void showLong(Context context, CharSequence message) {
    if (isShow) Toast.makeText(context, message, Toast.LENGTH_LONG).show();
  }

  /**
   * 长时间显示Toast
   */
  public static void showLong(Context context, int message) {
    if (isShow) Toast.makeText(context, message, Toast.LENGTH_LONG).show();
  }

  /**
   * 自定义显示Toast时间
   */
  public static void show(Context context, CharSequence message, int duration) {
    if (isShow) Toast.makeText(context, message, duration).show();
  }

  /**
   * 自定义显示Toast时间
   */
  public static void show(Context context, int message, int duration) {
    if (isShow) Toast.makeText(context, message, duration).show();
  }
}