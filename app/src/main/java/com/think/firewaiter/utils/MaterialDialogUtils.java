package com.think.firewaiter.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import com.afollestad.materialdialogs.MaterialDialog;

/**
 * User: ylw
 * Date: 2016-09-01
 * Time: 11:07
 * FIXME
 */
public class MaterialDialogUtils {
  /**
   * 检查更新Dialog
   */
  public static MaterialDialog showCheckUpdateDialog(Context context) {
    MaterialDialog chechUpdateDialog = new MaterialDialog.Builder(context).title("检查更新")
        .content("Please Wait")
        .progress(true, 0)
        .progressIndeterminateStyle(false)
        .canceledOnTouchOutside(false)
        .cancelable(false)
        .show();
    return chechUpdateDialog;
  }

  /**
   * 是否下载最新版本dialog
   */
  public static MaterialDialog showDownLoadDialog(Context context, String versionName,
      String updateInfo) {
    MaterialDialog dialog = new MaterialDialog.Builder(context).title("发现新版本:" + versionName)
        .content("说明:" + updateInfo)
        .positiveText("更新")
        .negativeText("取消")
        .show();
    return dialog;
  }

  /**
   * 显示 下载进度 dialog
   */
  public static MaterialDialog showDownLoadingDialog(Context context) {
    MaterialDialog dialog = new MaterialDialog.Builder(context).title("下载中")
        .content("Please Wait")
        .progress(false, 0, true)
        .positiveText("取消更新")
        .progressIndeterminateStyle(false)
        .canceledOnTouchOutside(false)
        .cancelable(false)
        .show();
    return dialog;
  }

  /**
   * submit dialog
   */
  public static MaterialDialog showSubmitDialog(Activity act) {
    return new MaterialDialog.Builder(act).title("警告")
        .content("正在发送中")
        .progress(true, 0)
        .progressIndeterminateStyle(true)
        .canceledOnTouchOutside(false)
        .build();
  }

  /**
   * 单选 dialog
   */
  public static MaterialDialog showListDialog(Activity act, String title, String[] items) {
    return new MaterialDialog.Builder(act).title(title)
        .items(items)
        .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
          @Override public boolean onSelection(MaterialDialog dialog, View view, int which,
              CharSequence text) {
            return true;
          }
        })
        .positiveText("确定")
        .negativeText("取消")
        .cancelable(false)
        .autoDismiss(false)
        .canceledOnTouchOutside(false)
        .show();
  }

  /**
   * 关闭Dialog
   */
  public static void dismissDialog(MaterialDialog dialog) {
    if (dialog != null && dialog.isShowing()) {
      dialog.dismiss();
      dialog = null;
    }
  }
}  