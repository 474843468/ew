package com.think.firewaiter.ui.base;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import com.think.firewaiter.R;

public abstract class BaseToolbarActivity extends BaseActivity {
  abstract protected String provideToolbarTitle();

  protected AppBarLayout mAppBar;
  protected Toolbar mToolbar;
  protected boolean mIsHidden = false;
  private ActionBar mActionBar;
  protected boolean mIsCanBack = true;
  protected boolean mIsHasToolbarTitle = true;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mAppBar = (AppBarLayout) findViewById(R.id.app_bar_layout);
    mToolbar = (Toolbar) findViewById(R.id.toolbar);
    if (mToolbar == null || mAppBar == null) {
      throw new IllegalStateException("No toolbar");
    }
    if (mIsHasToolbarTitle) {
      mToolbar.setTitle(provideToolbarTitle());
    }
    mToolbar.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        onToolbarClick();
      }
    });

    setSupportActionBar(mToolbar);
    mActionBar = getSupportActionBar();

    if (mIsCanBack) {
      if (mActionBar != null) {
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeAsUpIndicator(R.drawable.ic_toolbar_return);
      }
    }

    if (Build.VERSION.SDK_INT >= 21) {
      mAppBar.setElevation(10.6f);
    }
  }

  public void onToolbarClick() {
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }

  protected void setAppBarAlpha(float alpha) {
    mAppBar.setAlpha(alpha);
  }

  protected void hideOrShowToolbar() {
    mAppBar.animate()
        .translationY(mIsHidden ? 0 : -mAppBar.getHeight())
        .setInterpolator(new DecelerateInterpolator(2))
        .start();

    mIsHidden = !mIsHidden;
  }
}
