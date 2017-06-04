package com.think.firewaiter.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.think.firewaiter.R;
import com.think.firewaiter.adapter.ProdCartAdapter;
import com.think.firewaiter.dao.DaoServiceUtil;
import com.think.firewaiter.dao.ShoppingCartDao;
import com.think.firewaiter.event.RefreshTemporaryCartEvent;
import com.think.firewaiter.module.ShoppingCart;
import com.think.firewaiter.ui.base.BaseToolbarActivity;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by zjq on 2016/5/4.
 */
public class TemporaryCartActivity extends BaseToolbarActivity implements ProdCartAdapter.OnCartProdClickListener {
  @Bind(R.id.rcv) RecyclerView mRcv;
  @Bind(R.id.tv_details_total) TextView mTvDetailsTotal;

  private ProdCartAdapter mCartAdapter;
  private List<ShoppingCart> mShoppingCartList;

  @Override protected String provideToolbarTitle() {
    return "临时订单信息";
  }

  @Override protected int provideContentViewId() {
    return R.layout.activity_temporary_cart;
  }

  //@formatter:off
  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ButterKnife.bind(this);
    EventBus.getDefault().register(this);
    //设置Rcv
    initRcv();
  }


  /**
   * 设置Rcv
   */
  //@formatter:off
  private void initRcv() {
    mShoppingCartList = DaoServiceUtil.getShoppingCartService().queryBuilder().where(ShoppingCartDao.Properties.TableId.isNull()).list();
    mCartAdapter = new ProdCartAdapter(this, mShoppingCartList);
    mCartAdapter.setOnCartProdClickListener(this);
    LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
    mRcv.setHasFixedSize(true);
    mRcv.setLayoutManager(layoutManager);
    mRcv.setAdapter(mCartAdapter);
    //计算统计信息
    calculateTotalInfo();
  }

  /**
   * 计算统计信息
   */
  //@formatter:on
  private void calculateTotalInfo() {
    mTvDetailsTotal.setText(mShoppingCartList.size() + "");
  }

  /**
   * 回退
   */
  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }

  /**
   * 回退
   */
  @Override public void onBackPressed() {
    super.onBackPressed();
  }

  /**
   * Item点击
   */
  //@formatter:on
  @Override public void onReduceClick(int pos) {
    ShoppingCart shoppingCart = mShoppingCartList.get(pos);
    if (shoppingCart.getNum() > 1) {
      shoppingCart.setNum(shoppingCart.getNum() - 1);
      DaoServiceUtil.getShoppingCartService().saveOrUpdate(shoppingCart);
    }
    refreshCart();
  }

  @Override public void onAddClick(int pos) {
    ShoppingCart shoppingCart = mShoppingCartList.get(pos);
    shoppingCart.setNum(shoppingCart.getNum() + 1);
    DaoServiceUtil.getShoppingCartService().saveOrUpdate(shoppingCart);
    refreshCart();
  }

  @Override public void onModifyClick(int pos) {
    ShoppingCart shoppingCart = mShoppingCartList.get(pos);
    Intent intent = new Intent(TemporaryCartActivity.this, ModifyProdActivity.class);
    intent.putExtra(ModifyProdActivity.SHOPPING_CART, shoppingCart);
    intent.putExtra(ModifyProdActivity.IS_TEMPORARY, true);
    startActivity(intent);
  }

  @Override public void onItemLongClick(final int pos) {
    final ShoppingCart shoppingCart = mShoppingCartList.get(pos);
    if (shoppingCart.getIsDelay() == true) {
      new MaterialDialog.Builder(this).title("选择操作")
          .items(R.array.cart_promptly_operate)
          .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
            @Override public boolean onSelection(MaterialDialog dialog, View view, int which,
                CharSequence text) {
              if (which == 0) {
                shoppingCart.setIsDelay(false);
                DaoServiceUtil.getShoppingCartService().saveOrUpdate(shoppingCart);
                mShoppingCartList = DaoServiceUtil.getShoppingCartService()
                    .queryBuilder()
                    .where(ShoppingCartDao.Properties.TableId.isNull())
                    .list();
                mCartAdapter = new ProdCartAdapter(TemporaryCartActivity.this, mShoppingCartList);
                mCartAdapter.setOnCartProdClickListener(TemporaryCartActivity.this);
                mRcv.setAdapter(mCartAdapter);
                refreshCart();
              } else {
                ShoppingCart shoppingCart = mShoppingCartList.get(pos);
                DaoServiceUtil.getShoppingCartService().delete(shoppingCart);
                refreshCart();
              }
              return true;
            }
          })
          .positiveText("确定")
          .show();
    } else {
      new MaterialDialog.Builder(this).title("选择操作")
          .items(R.array.cart_delay_operate)
          .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
            @Override public boolean onSelection(MaterialDialog dialog, View view, int which,
                CharSequence text) {
              if (which == 0) {
                shoppingCart.setIsDelay(true);
                DaoServiceUtil.getShoppingCartService().saveOrUpdate(shoppingCart);
                mShoppingCartList = DaoServiceUtil.getShoppingCartService()
                    .queryBuilder()
                    .where(ShoppingCartDao.Properties.TableId.isNull())
                    .list();
                mCartAdapter = new ProdCartAdapter(TemporaryCartActivity.this, mShoppingCartList);
                mCartAdapter.setOnCartProdClickListener(TemporaryCartActivity.this);
                mRcv.setAdapter(mCartAdapter);
                refreshCart();
              } else {
                ShoppingCart shoppingCart = mShoppingCartList.get(pos);
                DaoServiceUtil.getShoppingCartService().delete(shoppingCart);
                refreshCart();
              }
              return true;
            }
          })
          .positiveText("确定")
          .show();
    }
  }

  /**
   * 重置注入
   */
  @Override protected void onDestroy() {
    super.onDestroy();
    EventBus.getDefault().unregister(this);
    ButterKnife.unbind(this);
  }

  /**
   * 刷新购物车
   */
  private void refreshCart() {
    mShoppingCartList = DaoServiceUtil.getShoppingCartService()
        .queryBuilder()
        .where(ShoppingCartDao.Properties.TableId.isNull())
        .list();
    mCartAdapter.setData(mShoppingCartList);
    calculateTotalInfo();
  }

  /**
   * 刷新购物车Event
   */
  @Subscribe(threadMode = ThreadMode.MAIN) public void refreshCartData(
      RefreshTemporaryCartEvent event) {
    mShoppingCartList = DaoServiceUtil.getShoppingCartService()
        .queryBuilder()
        .where(ShoppingCartDao.Properties.TableId.isNull())
        .list();
    mCartAdapter = new ProdCartAdapter(this, mShoppingCartList);
    mCartAdapter.setOnCartProdClickListener(this);
    mRcv.setAdapter(mCartAdapter);
    calculateTotalInfo();
  }

  /**
   * 删除临时购物车
   */
  @OnClick(R.id.fab) public void clearCart() {
    new MaterialDialog.Builder(this).title("警告")
        .content("是否清空临时订单")
        .inputType(InputType.TYPE_NUMBER_FLAG_DECIMAL)
        .positiveText("确定")
        .negativeText("取消")
        .negativeColor(this.getResources().getColor(R.color.primary_text))
        .onPositive(new MaterialDialog.SingleButtonCallback() {
          @Override public void onClick(MaterialDialog dialog, DialogAction which) {
            mShoppingCartList = DaoServiceUtil.getShoppingCartService()
                .queryBuilder()
                .where(ShoppingCartDao.Properties.TableId.isNull())
                .list();
            DaoServiceUtil.getShoppingCartService().delete(mShoppingCartList);
            mShoppingCartList = null;
            mCartAdapter.setData(new ArrayList<ShoppingCart>());
            mTvDetailsTotal.setText(0 + "");
          }
        })
        .show();
  }
}
