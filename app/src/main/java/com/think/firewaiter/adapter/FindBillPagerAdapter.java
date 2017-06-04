package com.think.firewaiter.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import java.util.List;

/**
 * Created by THINK on 2016/4/9.
 */
public class FindBillPagerAdapter extends FragmentStatePagerAdapter {
  private List<Fragment> mFragmentList;

  public FindBillPagerAdapter(FragmentManager fm, List<Fragment> fragmentList) {
    super(fm);
    this.mFragmentList = fragmentList;
  }

  @Override public Fragment getItem(int position) {
    return mFragmentList.get(position);
  }

  @Override public int getCount() {
    return mFragmentList.size();
  }
}
