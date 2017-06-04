package com.think.firewaiter.ui.activity;

/**
 * Created by THINK on 2016/4/10.
 */

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.redbooth.WelcomeCoordinatorLayout;
import com.think.firewaiter.R;
import com.think.firewaiter.animator.ChatAvatarsAnimator;
import com.think.firewaiter.animator.InSyncAnimator;
import com.think.firewaiter.animator.RocketAvatarsAnimator;
import com.think.firewaiter.animator.RocketFlightAwayAnimator;

public class WelcomeActivity extends AppCompatActivity {
  @Bind(R.id.coordinator) WelcomeCoordinatorLayout coordinatorLayout;
  @Bind(R.id.skip) Button mSkip;

  private View mLastPage;//最后一页

  private boolean animationReady = false;//控制动画开启
  private ValueAnimator backgroundAnimator;//背景颜色变化动画

  private RocketAvatarsAnimator rocketAvatarsAnimator;
  private ChatAvatarsAnimator chatAvatarsAnimator;
  private RocketFlightAwayAnimator rocketFlightAwayAnimator;
  private InSyncAnimator inSyncAnimator;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_welcome);
    ButterKnife.bind(this);
    //初始化Page监听
    initializeListeners();
    //初始化Page
    initializePages();
    //初始化背景动画
    initializeBackgroundTransitions();
  }

  /**
   * 初始化Page
   */
  private void initializePages() {
    final WelcomeCoordinatorLayout coordinatorLayout = (WelcomeCoordinatorLayout) findViewById(R.id.coordinator);
    coordinatorLayout.addPage(R.layout.welcome_page_1, R.layout.welcome_page_2, R.layout.welcome_page_3, R.layout.welcome_page_4);
    mLastPage = coordinatorLayout.findViewById(R.id.page4);
    mLastPage.findViewById(R.id.android_spin).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
      }
    });
  }

  /**
   * 初始化监听器
   */
  private void initializeListeners() {
    coordinatorLayout.setOnPageScrollListener(new WelcomeCoordinatorLayout.OnPageScrollListener() {
      @Override public void onScrollPage(View v, float progress, float maximum) {
        if (!animationReady) {
          animationReady = true;
          backgroundAnimator.setDuration((long) maximum);
        }
        backgroundAnimator.setCurrentPlayTime((long) progress);
      }

      @Override public void onPageSelected(View v, int pageSelected) {
        switch (pageSelected) {
          case 0://第一页
            if (rocketAvatarsAnimator == null) {
              rocketAvatarsAnimator = new RocketAvatarsAnimator(coordinatorLayout);
              rocketAvatarsAnimator.play();
            }
            mSkip.setVisibility(View.VISIBLE);
            break;
          case 1://第二页
            if (chatAvatarsAnimator == null) {
              chatAvatarsAnimator = new ChatAvatarsAnimator(coordinatorLayout);
              chatAvatarsAnimator.play();
            }
            mSkip.setVisibility(View.VISIBLE);
            break;
          case 2://第三页
            if (inSyncAnimator == null) {
              inSyncAnimator = new InSyncAnimator(coordinatorLayout);
              inSyncAnimator.play();
            }
            mSkip.setVisibility(View.VISIBLE);
            break;
          case 3://第四页
            if (rocketFlightAwayAnimator == null) {
              rocketFlightAwayAnimator = new RocketFlightAwayAnimator(coordinatorLayout);
              rocketFlightAwayAnimator.play();
            }
            mSkip.setVisibility(View.GONE);
            break;
        }
      }
    });
  }

  /**
   * 初始化背景动画
   */
  private void initializeBackgroundTransitions() {
    final Resources resources = getResources();
    final int colorPage1 = ResourcesCompat.getColor(resources, R.color.page1, getTheme());
    final int colorPage2 = ResourcesCompat.getColor(resources, R.color.page2, getTheme());
    final int colorPage3 = ResourcesCompat.getColor(resources, R.color.page3, getTheme());
    final int colorPage4 = ResourcesCompat.getColor(resources, R.color.page4, getTheme());
    backgroundAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), colorPage1, colorPage2, colorPage3, colorPage4);
    backgroundAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(ValueAnimator animation) {
        coordinatorLayout.setBackgroundColor((int) animation.getAnimatedValue());
      }
    });
  }

  /**
   * 跳过Welcome
   */
  @OnClick(R.id.skip) void skip() {
    coordinatorLayout.setCurrentPage(coordinatorLayout.getNumOfPages() - 1, true);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    ButterKnife.unbind(this);
  }
}
