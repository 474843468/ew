package com.think.firewaiter.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.think.firewaiter.R;
import com.think.firewaiter.dao.DaoServiceUtil;
import com.think.firewaiter.dao.PxTableAreaDao;
import com.think.firewaiter.module.PxTableArea;
import com.think.firewaiter.module.PxTableInfo;
import com.think.firewaiter.module.ServeTable;
import java.util.List;

/**
 * Created by dorado on 2016/6/30.
 */
public class ServeTableAdapter extends RecyclerView.Adapter<ServeTableAdapter.ViewHolder> {

  private Context mContext;
  private List<ServeTable> mServeTableList;

  public ServeTableAdapter(Context context, List<ServeTable> serveTableList) {
    mContext = context;
    mServeTableList = serveTableList;
  }

  @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(mContext).inflate(R.layout.item_serve_table, parent, false);
    return new ViewHolder(view);
  }

  @Override public void onBindViewHolder(ViewHolder holder, final int position) {
    ServeTable serveTable = mServeTableList.get(position);
    String type = serveTable.getTableInfo().getType();
    if (type != null) {
      PxTableArea tableArea = DaoServiceUtil.getTableAreaService()
          .queryBuilder()
          .where(PxTableAreaDao.Properties.Type.eq(type))
          .where(PxTableAreaDao.Properties.DelFlag.eq("0"))
          .unique();
      holder.mTvType.setText(tableArea == null ? "大厅"  : tableArea.getName());
    }else{
      holder.mTvType.setText("大厅");
    }

    holder.mTvName.setText(serveTable.getTableInfo().getName());

    holder.mCbIsUsed.setOnCheckedChangeListener(null);

    holder.mCbIsUsed.setChecked(serveTable.isSelected());

    if (mOnTableSelectedListener != null) {
      holder.mCbIsUsed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
          mOnTableSelectedListener.onTableSelected(position, isChecked);
        }
      });
    }

  }

  @Override public int getItemCount() {
    return mServeTableList.size();
  }

  class ViewHolder extends RecyclerView.ViewHolder {
    @Bind(R.id.tv_type) TextView mTvType;
    @Bind(R.id.tv_name) TextView mTvName;
    @Bind(R.id.cb_is_used) CheckBox mCbIsUsed;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

  /**
   * 设置数据
   */
  public void setData(List<ServeTable> data) {
    if (null == data) return;
    mServeTableList = data;
    this.notifyDataSetChanged();
  }

  /**
   * 点击
   */
  public static interface OnTableSelectedListener {
    void onTableSelected(int pos, boolean selected);
  }

  private OnTableSelectedListener mOnTableSelectedListener;

  public void setOnTableSelectedListener(OnTableSelectedListener onTableSelectedListener) {
    mOnTableSelectedListener = onTableSelectedListener;
  }
}
