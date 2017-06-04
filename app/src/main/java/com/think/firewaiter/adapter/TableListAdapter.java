package com.think.firewaiter.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.orhanobut.logger.Logger;
import com.think.firewaiter.R;
import com.think.firewaiter.module.PxOrderInfo;
import com.think.firewaiter.module.PxTableInfo;
import java.util.Date;
import java.util.List;

/**
 * Created by zjq on 2016/5/3.
 */
public class TableListAdapter extends RecyclerView.Adapter<TableListAdapter.ViewHolder> {

  private Context mContext;
  private List<PxTableInfo> mTableInfoList;

  public TableListAdapter(Context context, List<PxTableInfo> tableInfoList) {
    mContext = context;
    mTableInfoList = tableInfoList;
  }

  @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(mContext).inflate(R.layout.item_table_list, parent, false);
    return new ViewHolder(view);
  }

  // @formatter:off
  @Override public void onBindViewHolder(ViewHolder holder, final int position) {
    PxTableInfo tableInfo = mTableInfoList.get(position);
    //桌名
    holder.mTvTableName.setText(tableInfo.getName());
    //持续时间
    holder.mTvTableDuration.setText("");
    //状态
    holder.mTvStatus.setText("待刷新");
    //桌台初始化
    if (tableInfo.getStatusNow() == null){
      holder.mTvStatus.setText("待刷新");
      holder.mTvStatus.setTextColor(mContext.getResources().getColor(R.color.table_status_occupy));
    }
    //空桌
    if (tableInfo.getStatusNow() != null && tableInfo.getStatusNow().equals(PxTableInfo.STATUS_EMPTY)) {
      holder.mTvStatus.setText("空桌");
      holder.mTvStatus.setTextColor(mContext.getResources().getColor(R.color.table_status_occupy));
      holder.mTvPeopleNum.setText("建议" + tableInfo.getPeopleNum() + "人");
      holder.mCardView.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {mOnTableClickListener.onEmptyClick(position);
        }
      });
    }

    //占用
    if (tableInfo.getStatusNow() != null && tableInfo.getStatusNow().equals(PxTableInfo.STATUS_OCCUPIED)) {
      int durationMinutes = (int) (tableInfo.getDuration() / 1000 / 60);
      //超时
      if (durationMinutes >= 12 * 60) {
        holder.mTvStatus.setText("超时");
        holder.mTvStatus.setTextColor(mContext.getResources().getColor(R.color.table_status_overtime));
        holder.mCardView.setOnClickListener(new View.OnClickListener() {
          @Override public void onClick(View v) {
            mOnTableClickListener.onOvertimeClick(position);
          }
        });
      }
      //已下单
      else {
        holder.mTvStatus.setText("已下单");
        holder.mTvStatus.setTextColor(mContext.getResources().getColor(R.color.table_status_occupy));
        holder.mCardView.setOnClickListener(new View.OnClickListener() {
          @Override public void onClick(View v) {
            mOnTableClickListener.onOccupyClick(position);
          }
        });
      }
      holder.mTvTableDuration.setText("已开单" + durationMinutes + "分钟");
      holder.mTvTableDuration.setTextColor(mContext.getResources().getColor(R.color.table_duration));
      holder.mTvPeopleNum.setText(tableInfo.getActualPeopleNumber() + "人用餐");
    }

    //非最新数据
    if (tableInfo.getStatusNow() == null || tableInfo.getStatusNow().equals("")) {
      holder.mCardView.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          mOnTableClickListener.onInvalidClick(position);
        }
      });
    }
  }

  @Override public int getItemCount() {
    return mTableInfoList.size();
  }

  class ViewHolder extends RecyclerView.ViewHolder {
    @Bind(R.id.tv_people_num) TextView mTvPeopleNum;
    @Bind(R.id.tv_table_name) TextView mTvTableName;
    @Bind(R.id.tv_table_duration) TextView mTvTableDuration;
    @Bind(R.id.tv_status) TextView mTvStatus;
    @Bind(R.id.cardView) CardView mCardView;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

  /**
   * 设置数据
   */
  public void setData(List<PxTableInfo> pxTableInfoList) {
    if (pxTableInfoList == null) return;
    this.mTableInfoList = pxTableInfoList;
    this.notifyDataSetChanged();
  }

  /**
   * 点击点击
   */
  public static interface OnTableClickListener {
    void onEmptyClick(int pos);

    void onOccupyClick(int pos);

    void onOvertimeClick(int pos);

    void onInvalidClick(int pos);
  }

  private OnTableClickListener mOnTableClickListener;

  public void setOnTableClickListener(OnTableClickListener onTableClickListener) {
    mOnTableClickListener = onTableClickListener;
  }
}
