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
import com.think.firewaiter.R;
import com.think.firewaiter.chat.AppDetailsCollection;
import com.think.firewaiter.utils.NumberFormatUtils;
import java.util.List;

/**
 * Created by zjq on 2016/5/4.
 */
public class DetailListAdapter extends RecyclerView.Adapter<DetailListAdapter.ViewHolder> {

  private Context mContext;
  private List<AppDetailsCollection> mCollectionList;

  public DetailListAdapter(Context context, List<AppDetailsCollection> collectionList) {
    mContext = context;
    mCollectionList = collectionList;
  }

  @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(mContext).inflate(R.layout.item_detail_list, parent, false);
    return new ViewHolder(view);
  }

  //@formatter:on
  @Override public void onBindViewHolder(ViewHolder holder, final int position) {
    AppDetailsCollection collection = mCollectionList.get(position);
    //名称
    holder.mTvDetailName.setText(collection.getProdName());
    //数量
    holder.mTvNum.setText(collection.getNum() + "");
    holder.mTvNumUnit.setText(collection.getUnit());
    holder.mTvMultNum.setVisibility(View.GONE);
    holder.mTvMultNumUnit.setVisibility(View.GONE);
    holder.mTvDetailMultipleUnitTag.setVisibility(View.GONE);
    if (collection.getIsMultipleUnit().equals(AppDetailsCollection.IS_TWO_UNIT_TURE)) {
      holder.mTvMultNum.setText(NumberFormatUtils.formatFloatNumber(collection.getMultipleNum()) + "");
      holder.mTvNumUnit.setText(collection.getOrderUnit());
      holder.mTvMultNumUnit.setText(collection.getUnit());
      holder.mTvMultNum.setVisibility(View.VISIBLE);
      holder.mTvMultNumUnit.setVisibility(View.VISIBLE);
      holder.mTvDetailMultipleUnitTag.setVisibility(View.VISIBLE);
    }
    //折扣率
    if (collection.getDiscRate() == 100) {
      holder.mTvDiscRate.setText("无折扣");
    } else {
      holder.mTvDiscRate.setText("(" + collection.getDiscRate() + "%)");
    }

    //状态
    holder.mTvStatusName.setText("已下单");
    holder.mTvStatusName.setTextColor(
        mContext.getResources().getColor(R.color.detail_status_ordered));

    if (collection.getOrderStatus().equals(AppDetailsCollection.ORDER_STATUS_REFUND)) {
      holder.mTvStatusName.setText("已退菜");
      holder.mTvStatusName.setTextColor(mContext.getResources().getColor(R.color.detail_status_refund));
      holder.mTvServingTag.setVisibility(View.GONE);
    } else {
      holder.mTvServingTag.setVisibility(View.VISIBLE);
    }
    //规格
    holder.mTvFormat.setText("");
    if (collection.getFormatName() != null) {
      holder.mTvFormat.setText("(" + collection.getFormatName() + ")");
    }
    //做法
    holder.mTvMethod.setText("");
    if (collection.getMethodName() != null) {
      holder.mTvMethod.setText("(" + collection.getMethodName() + ")");
    }

    //等待标记
    if (collection.getStatus().equals(AppDetailsCollection.STATUS_DELAY)) {
      holder.mTvDetailWaitTag.setVisibility(View.VISIBLE);
    } else {
      holder.mTvDetailWaitTag.setVisibility(View.GONE);
    }

    //划菜标记
    if (collection.isServing()) {
      holder.mTvServingTag.setText("(已上)");
      holder.mTvServingTag.setTextColor(mContext.getResources().getColor(R.color.detail_serving));
    } else {
      holder.mTvServingTag.setText("(未上)");
      holder.mTvServingTag.setTextColor(mContext.getResources().getColor(R.color.detail_not_serving));
    }

    //赠品标记
    holder.mTvGiftTag.setVisibility(View.GONE);
    if (AppDetailsCollection.IS_GIFT_TRUE.equals(collection.getIsGift())) {
      holder.mTvGiftTag.setVisibility(View.VISIBLE);
    }

    //备注
    holder.mTvRemarks.setText("");
    holder.mTvRemarks.setVisibility(View.GONE);
    if (collection.getRemarks() != null && collection.getRemarks().equals("无") == false){
      holder.mTvRemarks.setText("(" + collection.getRemarks() + ")");
      holder.mTvRemarks.setVisibility(View.VISIBLE);
    }

    /**
     * 长按点击
     */
    if (mOnDetailClickListener != null) {
      holder.mCardView.setOnLongClickListener(new View.OnLongClickListener() {
        @Override public boolean onLongClick(View v) {
          mOnDetailClickListener.onItemLongClick(position);
          return true;
        }
      });
    }
  }

  @Override public int getItemCount() {
    return mCollectionList.size();
  }

  class ViewHolder extends RecyclerView.ViewHolder {
    @Bind(R.id.tv_detail_name) TextView mTvDetailName;//名称
    @Bind(R.id.tv_detail_wait_tag) TextView mTvDetailWaitTag;//等待标记
    @Bind(R.id.tv_detail_multiple_unit_tag) TextView mTvDetailMultipleUnitTag;//双单位标记
    @Bind(R.id.tv_disc_rate) TextView mTvDiscRate;//折扣率
    @Bind(R.id.tv_status_name) TextView mTvStatusName;//状态名
    @Bind(R.id.tv_num_unit) TextView mTvNumUnit;//点菜单位
    @Bind(R.id.tv_num) TextView mTvNum;//点菜数量
    @Bind(R.id.tv_mult_unit) TextView mTvMultNumUnit;//多单位
    @Bind(R.id.tv_mult_num) TextView mTvMultNum;//多单位数量
    @Bind(R.id.tv_format) TextView mTvFormat;//规格
    @Bind(R.id.tv_method) TextView mTvMethod;//做法
    @Bind(R.id.tv_serving_tag) TextView mTvServingTag;//标记已上/未上
    @Bind(R.id.tv_remarks) TextView mTvRemarks;//备注
    @Bind(R.id.tv_gift_tag) TextView mTvGiftTag;//赠品
    @Bind(R.id.cardView) CardView mCardView;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

  /**
   * 设置数据
   */
  public void setData(List<AppDetailsCollection> data) {
    if (data == null) return;
    mCollectionList = data;
    this.notifyDataSetChanged();
  }

  /**
   * 点击
   */
  public static interface OnDetailClickListener {

    void onItemLongClick(int pos);
  }

  private OnDetailClickListener mOnDetailClickListener;

  public void setOnDetailClickListener(OnDetailClickListener onDetailClickListener) {
    mOnDetailClickListener = onDetailClickListener;
  }
}
