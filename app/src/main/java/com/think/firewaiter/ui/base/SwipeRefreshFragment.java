/*
 * Copyright (C) 2015 Drakeet <drakeet.me@gmail.com>
 *
 * This file is part of Meizhi
 *
 * Meizhi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Meizhi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Meizhi.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.think.firewaiter.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import com.think.firewaiter.R;
import com.think.firewaiter.widget.MultiSwipeRefreshLayout;

public class SwipeRefreshFragment extends Fragment {

  public SwipeRefreshLayout mSwipeRefreshLayout;

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    trySetupSwipeRefresh(view);
  }

  void trySetupSwipeRefresh(View root) {
    mSwipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh_layout);
    if (mSwipeRefreshLayout != null) {
      mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
        @Override public void onRefresh() {
          requestDataRefresh();
        }
      });
    }
  }

  public void requestDataRefresh() {

  }

  public void setRefreshing(boolean refreshing) {
    if (mSwipeRefreshLayout == null) {
      return;
    }
    if (!refreshing) {
      // 防止刷新消失太快，让子弹飞一会儿
      mSwipeRefreshLayout.postDelayed(new Runnable() {
        @Override public void run() {mSwipeRefreshLayout.setRefreshing(false);
        }
      }, 1000);
    } else {
      mSwipeRefreshLayout.setRefreshing(true);
    }
  }

  public void setProgressViewOffset(boolean scale, int start, int end) {
    mSwipeRefreshLayout.setProgressViewOffset(scale, start, end);
  }

}
