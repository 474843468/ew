package com.think.firewaiter.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.think.firewaiter.R;
import com.think.firewaiter.adapter.CateAdapter;
import com.think.firewaiter.adapter.ProdAdapter;
import com.think.firewaiter.dao.DaoServiceUtil;
import com.think.firewaiter.dao.PxFormatInfoDao;
import com.think.firewaiter.dao.PxMethodInfoDao;
import com.think.firewaiter.dao.PxProductCategoryDao;
import com.think.firewaiter.dao.PxProductFormatRelDao;
import com.think.firewaiter.dao.PxProductInfoDao;
import com.think.firewaiter.dao.PxProductMethodRefDao;
import com.think.firewaiter.dao.ShoppingCartDao;
import com.think.firewaiter.module.PxFormatInfo;
import com.think.firewaiter.module.PxMethodInfo;
import com.think.firewaiter.module.PxProductCategory;
import com.think.firewaiter.module.PxProductFormatRel;
import com.think.firewaiter.module.PxProductInfo;
import com.think.firewaiter.module.PxProductMethodRef;
import com.think.firewaiter.module.ShoppingCart;
import com.think.firewaiter.ui.activity.AddProdActivity;
import com.think.firewaiter.ui.activity.MainActivity;
import com.think.firewaiter.ui.activity.TemporaryCartActivity;
import com.think.firewaiter.utils.ToastUtils;
import de.greenrobot.dao.query.Join;
import de.greenrobot.dao.query.QueryBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by THINK on 2016/4/8.
 */
public class TemporaryFragment extends Fragment
    implements AdapterView.OnItemClickListener, ProdAdapter.OnProdClickListener {

  @Bind(R.id.rcv_prod) RecyclerView mRcvProd;
  @Bind(R.id.lv_cate) ListView mLvCate;
  @Bind(R.id.fab) FloatingActionButton mFab;
  @Bind(R.id.et_search) EditText mEtSearch;

  private CateAdapter mCateAdapter;//分类Adapter
  private List<PxProductCategory> mCategoryList;//Data

  private ProdAdapter mProdAdapter;//商品适配器
  private List<PxProductInfo> mProductInfoList;//Data

  private PxProductCategory mCurrentCate;//当前分类

  private String mParam;
  private static final String TEMPORARY_PARAM = "param";

  private MainActivity mAct;

  public static TemporaryFragment newInstance(String param) {
    TemporaryFragment fragment = new TemporaryFragment();
    Bundle args = new Bundle();
    args.putString(TEMPORARY_PARAM, param);
    fragment.setArguments(args);
    return fragment;
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      mParam = getArguments().getString(TEMPORARY_PARAM);
    }
  }

  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_temporary, null);
    mAct = (MainActivity) getActivity();
    ButterKnife.bind(this, view);
    return view;
  }

  @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    //初始化Rcv
    initRcv();
    //初始化ListView
    initListView();
    //Et输入监听
    mEtSearch.addTextChangedListener(new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.toString().trim() != null && s.toString().trim().length() != 0) {
          mProductInfoList = DaoServiceUtil.getProductInfoService()
              .queryBuilder()
              .whereOr(PxProductInfoDao.Properties.Py.like("%" + s.toString() + "%"), PxProductInfoDao.Properties.Name.like("%" + s.toString() + "%"))
              .where(PxProductInfoDao.Properties.Type.eq(PxProductInfo.TYPE_NORMAL))//非套餐类商品
              .whereOr(PxProductInfoDao.Properties.Shelf.isNull(), PxProductInfoDao.Properties.Shelf.eq(PxProductInfo.SHELF_PUT_AWAY))
              .list();
          if (mProductInfoList != null && mProductInfoList.size() != 0) {
            mProdAdapter.setData(mProductInfoList);
          } else {
            mProductInfoList = new ArrayList<PxProductInfo>();
            mProdAdapter.setData(mProductInfoList);
          }
        } else {
          getProdListByCategory(mCurrentCate);
        }
      }

      @Override public void afterTextChanged(Editable s) {

      }
    });
  }

  /**
   * 初始化Lv
   * 非套餐
   */
  private void initListView() {
    mCategoryList = DaoServiceUtil.getProductCategoryService()
        .queryBuilder()
        .where(PxProductCategoryDao.Properties.DelFlag.eq("0"))
        .where(PxProductCategoryDao.Properties.Leaf.eq(PxProductCategory.IS_LEAF))
        .whereOr(PxProductCategoryDao.Properties.Type.isNull(), PxProductCategoryDao.Properties.Type.eq(PxProductCategory.TYPE_NORMAL))
        .whereOr(PxProductCategoryDao.Properties.Shelf.isNull(), PxProductCategoryDao.Properties.Shelf.eq(PxProductCategory.SHELF_PUT_AWAY))
        .list();
    mCateAdapter = new CateAdapter(mAct, mCategoryList);
    mLvCate.setAdapter(mCateAdapter);
    mLvCate.setOnItemClickListener(this);
    if (mCategoryList != null && mCategoryList.size() != 0) {
      mLvCate.setItemChecked(0, true);
      mCurrentCate = mCategoryList.get(0);
      getProdListByCategory(mCurrentCate);
    }
  }

  /**
   * 获取该分类下的商品
   * 非套餐类商品
   */
  private void getProdListByCategory(PxProductCategory currentCate) {
    mProductInfoList = DaoServiceUtil.getProductInfoService()
        .queryBuilder()
        .where(PxProductInfoDao.Properties.DelFlag.eq(0))
        .where(PxProductInfoDao.Properties.PxProductCategoryId.eq(currentCate.getId()))
        .whereOr(PxProductInfoDao.Properties.Type.isNull(), PxProductInfoDao.Properties.Type.eq(PxProductInfo.TYPE_NORMAL))
        .whereOr(PxProductInfoDao.Properties.Shelf.isNull(), PxProductInfoDao.Properties.Shelf.eq(PxProductInfo.SHELF_PUT_AWAY))
        .list();
    mProdAdapter.setData(mProductInfoList);
  }

  /**
   * 初始化Rcv
   */
  private void initRcv() {
    LinearLayoutManager layoutManager =
        new LinearLayoutManager(mAct, LinearLayoutManager.VERTICAL, false);
    mProductInfoList = new ArrayList<PxProductInfo>();
    mRcvProd.setHasFixedSize(true);
    mRcvProd.setLayoutManager(layoutManager);
    mProdAdapter = new ProdAdapter(mAct, mProductInfoList);
    mProdAdapter.setOnProdClickListener(this);
    mRcvProd.setAdapter(mProdAdapter);
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    //重置ButterKnife
    ButterKnife.unbind(this);
  }

  /**
   * Item点击
   */
  @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    //当前分类
    mCurrentCate = mCategoryList.get(position);
    getProdListByCategory(mCurrentCate);
  }

  @Override public void onProductClick(int pos) {
    if (mProductInfoList == null || mProductInfoList.size() == 0) return;
    //商品
    PxProductInfo productInfo = mProductInfoList.get(pos);
    //是否停售
    if (productInfo.getStatus().equals(PxProductInfo.STATUS_STOP_SALE)) {
      ToastUtils.showShort(mAct, "停售商品不能添加");
      return;
    }
    //查询可用的规格引用关系
    QueryBuilder<PxProductFormatRel> formatRelQb =
        DaoServiceUtil.getProductFormatRelServiceService()
            .queryBuilder()
            .where(PxProductFormatRelDao.Properties.DelFlag.eq("0"))
            .where(PxProductFormatRelDao.Properties.PxProductInfoId.eq(productInfo.getId()));
    Join<PxProductFormatRel, PxFormatInfo> formatJoin =
        formatRelQb.join(PxProductFormatRelDao.Properties.PxFormatInfoId, PxFormatInfo.class);
    formatJoin.where(PxFormatInfoDao.Properties.DelFlag.eq("0"));
    List<PxProductFormatRel> formatRelList = formatRelQb.list();
    //查询可用的做法引用关系
    QueryBuilder<PxProductMethodRef> methodRelQb = DaoServiceUtil.getProductMethodRelService()
        .queryBuilder()
        .where(PxProductMethodRefDao.Properties.DelFlag.eq("0"))
        .where(PxProductMethodRefDao.Properties.PxProductInfoId.eq(productInfo.getId()));
    Join<PxProductMethodRef, PxMethodInfo> methodJoin =
        methodRelQb.join(PxProductMethodRefDao.Properties.PxMethodInfoId, PxMethodInfo.class);
    methodJoin.where(PxMethodInfoDao.Properties.DelFlag.eq("0"));
    List<PxProductMethodRef> methodRelList = methodRelQb.list();

    //规格可用
    boolean formatCanUse = (formatRelList != null && formatRelList.size() != 0);
    //做法可用
    boolean methodCanUse = (methodRelList != null && methodRelList.size() != 0);
    //有双单位
    boolean hasTwoUnit = productInfo.getMultipleUnit().equals(PxProductInfo.IS_TWO_UNIT_TURE);

    //没有双单位，没有规格，没有做法
    if (!hasTwoUnit && !formatCanUse && !methodCanUse) {
      ShoppingCart exist = DaoServiceUtil.getShoppingCartService()
          .queryBuilder()
          .where(ShoppingCartDao.Properties.IsDelay.eq(false))
          .where(ShoppingCartDao.Properties.TableId.isNull())
          .where(ShoppingCartDao.Properties.ProdId.eq(productInfo.getId()))
          .where(ShoppingCartDao.Properties.FormatId.isNull())
          .where(ShoppingCartDao.Properties.MethodId.isNull())
          .unique();
      //如果没有记录，则插入新的
      if (exist == null) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setIsDelay(false);
        shoppingCart.setNum((double) 1);
        shoppingCart.setDbProd(productInfo);
        shoppingCart.setDbTable(null);
        shoppingCart.setDbFormat(null);
        shoppingCart.setDbMethod(null);
        shoppingCart.setRemarks("");
        DaoServiceUtil.getShoppingCartService().saveOrUpdate(shoppingCart);
        Snackbar.make(mFab, productInfo.getName() + ":" + shoppingCart.getNum(),
            Snackbar.LENGTH_SHORT).show();
      } else {
        exist.setNum(exist.getNum() + 1);
        DaoServiceUtil.getShoppingCartService().saveOrUpdate(exist);
        Snackbar.make(mFab, productInfo.getName() + ":" + exist.getNum(), Snackbar.LENGTH_SHORT)
            .show();
      }
    }
    //有双单位、规格、做法
    else {
      Intent intent = new Intent(mAct, AddProdActivity.class);
      intent.putExtra(AddProdActivity.PROD_INFO, productInfo);
      intent.putExtra(AddProdActivity.IS_TEMPORARY, true);
      startActivity(intent);
    }
  }

  @Override public void onProductLongClick(int pos) {

  }

  /**
   * 打开购物车
   */
  @OnClick(R.id.fab) public void openCart(View view) {
    Intent intent = new Intent(mAct, TemporaryCartActivity.class);
    startActivity(intent);
  }

  @Override public void onHiddenChanged(boolean hidden) {
    super.onHiddenChanged(hidden);
    if (!hidden) {
      if (mLvCate != null && mCategoryList != null && mCategoryList.size() != 0) {
        int checkedItemPosition = mLvCate.getCheckedItemPosition();
        PxProductCategory category = mCategoryList.get(checkedItemPosition);
        getProdListByCategory(category);
      }
    }
  }
}
