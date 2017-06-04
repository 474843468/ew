package com.think.firewaiter.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.think.firewaiter.ui.activity.MainActivity;
import com.think.firewaiter.R;
import com.think.firewaiter.adapter.FindBillPagerAdapter;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;

/**
 * Created by THINK on 2016/4/8.
 */
public class FindBillFragment extends Fragment {

  @Bind(R.id.tabLayout) TabLayout mTabLayout;//Tabs
  @Bind(R.id.viewPager) ViewPager mViewPager;//ViewPager

  private String mParam;
  private static final String FIND_BILL_PARAM = "param";

  private String[] mTitles = { "大厅", "包间" };//标题
  private int[] mImgs = { R.drawable.ic_tab_hall, R.drawable.ic_tab_parlor };//对应图片

  private MainActivity mAct;

  public static FindBillFragment newInstance(String param) {
    FindBillFragment fragment = new FindBillFragment();
    Bundle args = new Bundle();
    args.putString(FIND_BILL_PARAM, param);
    fragment.setArguments(args);
    return fragment;
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      mParam = getArguments().getString(FIND_BILL_PARAM);
    }
  }

  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_find_bill, null);
    mAct = (MainActivity) getActivity();
    ButterKnife.bind(this, view);
    initTabsVp();
    return view;
  }

  /**
   * 初始化Tablayout和ViewPager
   */
  private void initTabsVp() {
    List<Fragment> fragmentList = new ArrayList<Fragment>();
    fragmentList.add(HallListFragment.newInstance("Hall"));
    fragmentList.add(ParlorListFragment.newInstance("Parlor"));
    FindBillPagerAdapter pagerAdapter = new FindBillPagerAdapter(getChildFragmentManager(), fragmentList);
    mViewPager.setAdapter(pagerAdapter);
    mTabLayout.setupWithViewPager(mViewPager);
    //设置tab图标
    setTabIcon();
  }

  /**
   * 设置tab图标
   */
  private void setTabIcon() {
    for (int i = 0; i < mTabLayout.getTabCount(); i++) {
      TabLayout.Tab tab = mTabLayout.getTabAt(i);
      if (tab != null) {
        tab.setCustomView(getTabView(i));
      }
    }
  }

  /**
   * 给tab设置自定义view
   */
  public View getTabView(int position) {
    View v = LayoutInflater.from(mAct).inflate(R.layout.layout_tab, null);
    TextView tv = (TextView) v.findViewById(R.id.tv_tab);
    tv.setText(mTitles[position]);
    ImageView img = (ImageView) v.findViewById(R.id.iv_tab);
    img.setImageResource(mImgs[position]);
    return v;
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    //重置ButterKnife
    ButterKnife.unbind(this);
  }

}
