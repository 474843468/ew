package com.think.firewaiter.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * User: ylw
 * Date: 2017-02-23
 * Time: 14:28
 * 触摸移动 Rcv
 */
public class TouchMoveRcv extends RecyclerView {
  private boolean mTouchMoving = false;

  public TouchMoveRcv(Context context) {
    super(context);
  }

  public TouchMoveRcv(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public TouchMoveRcv(Context context, @Nullable AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override public boolean onTouchEvent(MotionEvent e) {
    switch (e.getAction()) {
      case MotionEvent.ACTION_MOVE:
        mTouchMoving = true;
        break;
      default:
        mTouchMoving = false;
        break;
    }
    return super.onTouchEvent(e);
  }

  /**
   * 正在手势滑动
   */
  public boolean isTouchMoving() {
    boolean b = mTouchMoving || getScrollState() == SCROLL_STATE_SETTLING;
    Log.w("Fire","getScrollState:"+getScrollState()+"--"+b);
    return b;
  }
}