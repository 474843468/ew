package com.think.firewaiter.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import com.think.firewaiter.ui.activity.MainActivity;
import com.think.firewaiter.R;

/**
 * Created by THINK on 2016/4/8.
 */
public class NotificationsFragment extends Fragment {

  private String mParam;
  private static final String NOTIFICATIONS_PARAM = "param";

  private MainActivity mAct;

  public static NotificationsFragment newInstance(String param) {
    NotificationsFragment fragment = new NotificationsFragment();
    Bundle args = new Bundle();
    args.putString(NOTIFICATIONS_PARAM, param);
    fragment.setArguments(args);
    return fragment;
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      mParam = getArguments().getString(NOTIFICATIONS_PARAM);
    }
  }

  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_notifications, null);
    mAct = (MainActivity) getActivity();
    ButterKnife.bind(this, view);
    return view;
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    //重置ButterKnife
    ButterKnife.unbind(this);
  }
}
