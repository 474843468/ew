package com.think.firewaiter.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.orhanobut.logger.Logger;
import com.think.firewaiter.R;
import com.think.firewaiter.module.PxProductInfo;
import java.util.List;

/**
 * Created by zjq on 2016/5/4.
 */
public class ProdAdapter extends RecyclerView.Adapter<ProdAdapter.ViewHolder> {

  private Context mContext;
  private List<PxProductInfo> mProductInfoList;

  public ProdAdapter(Context context, List<PxProductInfo> productInfoList) {
    mContext = context;
    mProductInfoList = productInfoList;
  }

  @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(mContext).inflate(R.layout.item_prod_list, parent, false);
    return new ViewHolder(view);
  }

  //@formatter:on
  @Override public void onBindViewHolder(ViewHolder holder, final int position) {
    final PxProductInfo productInfo = mProductInfoList.get(position);
    holder.mTvProdName.setText(productInfo.getName());
    holder.mTvProdPrice.setText("￥:" + productInfo.getPrice());
    holder.mTvCheckoutUnit.setText("/" + productInfo.getUnit());
    holder.mTvProdStatus.setVisibility(View.GONE);
    if (productInfo.getStatus().equals(PxProductInfo.STATUS_STOP_SALE)){
      holder.mTvProdStatus.setVisibility(View.VISIBLE);
    }


    if (mOnProdClickListener != null) {
      holder.mCardView.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          mOnProdClickListener.onProductClick(position);
        }
      });

      holder.mCardView.setOnLongClickListener(new View.OnLongClickListener() {
        @Override public boolean onLongClick(View v) {
          mOnProdClickListener.onProductLongClick(position);
          return true;
        }
      });
    }
  }

  @Override public int getItemCount() {
    return mProductInfoList.size();
  }

  class ViewHolder extends RecyclerView.ViewHolder {
    @Bind(R.id.tv_prod_name) TextView mTvProdName;
    @Bind(R.id.tv_prod_price) TextView mTvProdPrice;
    @Bind(R.id.tv_checkout_unit) TextView mTvCheckoutUnit;
    @Bind(R.id.tv_prod_status)TextView mTvProdStatus;
    @Bind(R.id.cardView) CardView mCardView;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

  /**
   * 设置数据
   */
  public void setData(List<PxProductInfo> data) {
    if (data == null) return;
    this.mProductInfoList = data;
    this.notifyDataSetChanged();
  }

  /**
   * 点击监听
   */
  public static interface OnProdClickListener {
    void onProductClick(int pos);

    void onProductLongClick(int pos);
  }

  private OnProdClickListener mOnProdClickListener;

  public void setOnProdClickListener(OnProdClickListener onProdClickListener) {
    this.mOnProdClickListener = onProdClickListener;
  }
}
