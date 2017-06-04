package com.think.firewaiter.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.think.firewaiter.R;
import com.think.firewaiter.module.PxProductCategory;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zjq 2015/7/9.
 * 商品分类 适配器
 */
public class CateAdapter extends BaseAdapter {
  private Context context;
  private List<PxProductCategory> mCategoryList;

  public CateAdapter(Context context,List<PxProductCategory> categoryList ) {
    this.context = context;
    this.mCategoryList = categoryList;
  }

  @Override public int getCount() {
    return mCategoryList.size();
  }

  @Override public Object getItem(int position) {
    return mCategoryList.get(position);
  }

  @Override public long getItemId(int position) {
    return position;
  }

  @Override public View getView(int position, View convertView, ViewGroup parent) {
    Holder holder = null;
    if (convertView == null) {
      holder = new Holder();
      convertView = LayoutInflater.from(context).inflate(R.layout.item_category, null);
      holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
      convertView.setTag(holder);
    } else {
      holder = (Holder) convertView.getTag();
    }
    //获取当前数据
    PxProductCategory category = mCategoryList.get(position);

    holder.tv_name.setText(category.getName());
    return convertView;
  }

  class Holder {
    TextView tv_name;
  }

  /**
   * 设置数据
   */
  public void setData(List<PxProductCategory> data) {
    if (null == data) return;
    this.mCategoryList = data;
    this.notifyDataSetChanged();
  }

  /**
   * 清空数据
   */
  public void cleanData() {
    this.mCategoryList = new ArrayList<PxProductCategory>();
    this.notifyDataSetChanged();
  }
}
