package com.think.firewaiter.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.think.firewaiter.R;
import com.think.firewaiter.dao.DaoServiceUtil;
import com.think.firewaiter.dao.PxProductFormatRelDao;
import com.think.firewaiter.module.PxProductFormatRel;
import com.think.firewaiter.module.PxProductInfo;
import com.think.firewaiter.module.ShoppingCart;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zjq on 2016/5/5.
 */
public class ProdCartAdapter extends RecyclerView.Adapter<ProdCartAdapter.ViewHolder> {

  private Context mContext;
  private List<ShoppingCart> mShoppingCartList;

  public ProdCartAdapter(Context context, List<ShoppingCart> shoppingCartList) {
    mContext = context;
    if (shoppingCartList != null) {
      mShoppingCartList = shoppingCartList;
    } else {
      mShoppingCartList = new ArrayList<ShoppingCart>();
    }
  }

  @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(mContext).inflate(R.layout.item_prod_cart_list, parent, false);
    return new ViewHolder(view);
  }

  @Override public void onBindViewHolder(final ViewHolder holder, final int position) {
    ShoppingCart shoppingCart = mShoppingCartList.get(position);
    //名称
    holder.mTvProdName.setText(shoppingCart.getDbProd().getName());
    //数量
    holder.mTvNum.setText(shoppingCart.getNum().intValue() + "");
    //等待
    if (shoppingCart.getIsDelay() == true) {
      holder.mTvWaitTag.setVisibility(View.VISIBLE);
    } else {
      holder.mTvWaitTag.setVisibility(View.GONE);
    }
    //价格
    if (shoppingCart.getDbFormat() != null) {
      PxProductFormatRel rel = DaoServiceUtil.getProductFormatRelServiceService()
          .queryBuilder()
          .where(PxProductFormatRelDao.Properties.PxFormatInfoId.eq(
              shoppingCart.getDbFormat().getId()))
          .where(
              PxProductFormatRelDao.Properties.PxProductInfoId.eq(shoppingCart.getDbProd().getId()))
          .unique();
      if (rel != null) {
        holder.mTvProdPrice.setText(rel.getPrice() + "元");
      }
    } else {
      holder.mTvProdPrice.setText(shoppingCart.getDbProd().getPrice() + "元");
    }
    //结账单位
    holder.mTvCheckoutUnit.setText("/" + shoppingCart.getDbProd().getUnit());
    //规格
    if (shoppingCart.getDbFormat() != null) {
      holder.mTvFormat.setText("(" + shoppingCart.getDbFormat().getName() + ")");
      holder.mTvFormat.setVisibility(View.VISIBLE);
    } else {
      holder.mTvFormat.setVisibility(View.GONE);
    }
    //做法
    if (shoppingCart.getDbMethod() != null) {
      holder.mTvMethod.setText("(" + shoppingCart.getDbMethod().getName() + ")");
      holder.mTvMethod.setVisibility(View.VISIBLE);
    } else {
      holder.mTvMethod.setVisibility(View.GONE);
    }

    //双单位
    if (shoppingCart.getDbProd().getMultipleUnit().equals(PxProductInfo.IS_TWO_UNIT_TURE)) {
      holder.mTvMultNum.setText(shoppingCart.getMultipleUnitNum() + "");
      holder.mTvMultNumUnit.setText("" + shoppingCart.getDbProd().getUnit());
      holder.mTvMultNum.setVisibility(View.VISIBLE);
      holder.mTvMultNumUnit.setVisibility(View.VISIBLE);
    } else {
      holder.mTvMultNum.setVisibility(View.GONE);
      holder.mTvMultNumUnit.setVisibility(View.GONE);
    }

    //备注
    String remarks = shoppingCart.getRemarks();
    if (remarks != null && !TextUtils.isEmpty(remarks.trim())) {
      holder.mTvRemarks.setText("(" + remarks + ")");
      holder.mTvRemarks.setVisibility(View.VISIBLE);
    } else {
      holder.mTvRemarks.setText("");
      holder.mTvRemarks.setVisibility(View.GONE);
    }

    if (mOnCartProdClickListener != null) {
      holder.mTvReduce.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          mOnCartProdClickListener.onReduceClick(position);
        }
      });

      holder.mTvAdd.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          mOnCartProdClickListener.onAddClick(position);
        }
      });

      holder.mTvModify.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          mOnCartProdClickListener.onModifyClick(position);
        }
      });
    }

    if (mOnCartProdClickListener != null) {
      holder.mCardView.setOnLongClickListener(new View.OnLongClickListener() {
        @Override public boolean onLongClick(View v) {
          mOnCartProdClickListener.onItemLongClick(position);
          return true;
        }
      });
    }
  }

  @Override public int getItemCount() {
    return mShoppingCartList.size();
  }

  class ViewHolder extends RecyclerView.ViewHolder {
    @Bind(R.id.tv_prod_name) TextView mTvProdName;//名称
    @Bind(R.id.tv_wait_tag) TextView mTvWaitTag;//延迟标记
    @Bind(R.id.tv_prod_price) TextView mTvProdPrice;//商品价格
    @Bind(R.id.tv_checkout_unit) TextView mTvCheckoutUnit;//结账单位
    @Bind(R.id.tv_modify) TextView mTvModify;//修改
    @Bind(R.id.tv_reduce) TextView mTvReduce;//减号
    @Bind(R.id.tv_num) TextView mTvNum;//数量
    @Bind(R.id.tv_add) TextView mTvAdd;//加号
    @Bind(R.id.tv_format) TextView mTvFormat;//规格
    @Bind(R.id.tv_method) TextView mTvMethod;//做法
    @Bind(R.id.tv_mult_num) TextView mTvMultNum;//双单位数量
    @Bind(R.id.tv_mult_num_unit) TextView mTvMultNumUnit;//双单位
    @Bind(R.id.tv_remarks) TextView mTvRemarks;//备注
    @Bind(R.id.rl_ordinary_modify) RelativeLayout mRlOrdinaryModify;//普通商品修改数量
    @Bind(R.id.cardview) CardView mCardView;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

  /**
   * 设置数据
   */
  public void setData(List<ShoppingCart> data) {
    if (data == null) return;
    this.mShoppingCartList = data;
    this.notifyDataSetChanged();
  }

  /**
   * 点击监听
   */
  public static interface OnCartProdClickListener {
    void onReduceClick(int pos);

    void onAddClick(int pos);

    void onModifyClick(int pos);

    void onItemLongClick(int pos);
  }

  private OnCartProdClickListener mOnCartProdClickListener;

  public void setOnCartProdClickListener(OnCartProdClickListener onCartProdClickListener) {
    mOnCartProdClickListener = onCartProdClickListener;
  }
}
