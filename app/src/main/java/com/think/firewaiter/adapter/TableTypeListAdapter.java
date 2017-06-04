package com.think.firewaiter.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.think.firewaiter.R;
import com.think.firewaiter.module.PxTableInfo;
import java.util.List;

/**
 * User: ylw
 * Date: 2017-02-14
 * Time: 15:12
 * FIXME
 */
public class TableTypeListAdapter extends RecyclerView.Adapter<TableTypeListAdapter.ContentVH> {
  //View  Type

  private List<PxTableInfo> mList;
  private ItemClickListener mListener;
  private LayoutInflater mInflater;
  private boolean mIsEmpty = false;

  public TableTypeListAdapter(Context context, List<PxTableInfo> list, boolean isEmpty) {
    mList = list;
    this.mIsEmpty = isEmpty;
    mInflater = LayoutInflater.from(context);
  }

  public TableTypeListAdapter(Context context, List<PxTableInfo> list) {
    mList = list;
    mInflater = LayoutInflater.from(context);
  }

  @Override public ContentVH onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = mInflater.inflate(R.layout.item_table_content, parent, false);
    return new ContentVH(view);
  }

  @Override public void onBindViewHolder(ContentVH holder, final int position) {
    PxTableInfo tableInfo = mList.get(position);
    holder.mTvTableName.setText(tableInfo.getName());
    if (mListener != null) {
      holder.mCvContainer.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          mListener.onItemClick(position);
        }
      });
    }
    //占用
    if (!mIsEmpty) {
      holder.mTvTableDuration.setVisibility(View.VISIBLE);
      int durationMinutes = (int) (tableInfo.getDuration() / 1000 / 60);
      holder.mTvTableDuration.setText("已开单" + durationMinutes + "分钟");
      //超时
      if (durationMinutes >= 12 * 60) {
        holder.mTvTableStatus.setVisibility(View.VISIBLE);
      } else {
        holder.mTvTableStatus.setVisibility(View.GONE);
      }
      holder.mTvPeopleNum.setText(tableInfo.getActualPeopleNumber() + "人用餐");
    } else {
      holder.mTvPeopleNum.setText("建议" + tableInfo.getPeopleNum() + "人");
    }
  }

  @Override public int getItemCount() {
    return mList.size();
  }

  public List<PxTableInfo> getData() {
    return mList;
  }

  public void setData(List<PxTableInfo> list) {
    this.mList = list;
    notifyDataSetChanged();
  }

  public interface ItemClickListener {
    void onItemClick(int pos);
  }

  public void setItemClickListener(ItemClickListener listener) {
    this.mListener = listener;
  }

  //具体内容holder
  class ContentVH extends RecyclerView.ViewHolder {

    private TextView mTvTableName, mTvPeopleNum, mTvTableDuration, mTvTableStatus;
    private CardView mCvContainer;

    public ContentVH(View itemView) {
      super(itemView);
      mCvContainer = (CardView) itemView;
      mTvTableName = (TextView) itemView.findViewById(R.id.tv_table_name);
      mTvTableDuration = (TextView) itemView.findViewById(R.id.tv_table_duration);
      mTvTableStatus = (TextView) itemView.findViewById(R.id.tv_table_status);
      mTvPeopleNum = (TextView) itemView.findViewById(R.id.tv_people_num);
    }
  }
}