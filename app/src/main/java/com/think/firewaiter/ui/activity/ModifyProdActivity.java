package com.think.firewaiter.ui.activity;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.kyleduo.switchbutton.SwitchButton;
import com.think.firewaiter.R;
import com.think.firewaiter.dao.DaoServiceUtil;
import com.think.firewaiter.dao.PxFormatInfoDao;
import com.think.firewaiter.dao.PxMethodInfoDao;
import com.think.firewaiter.dao.PxProductFormatRelDao;
import com.think.firewaiter.dao.PxProductMethodRefDao;
import com.think.firewaiter.dao.PxProductRemarksDao;
import com.think.firewaiter.dao.PxTableInfoDao;
import com.think.firewaiter.dao.ShoppingCartDao;
import com.think.firewaiter.event.RefreshCartEvent;
import com.think.firewaiter.event.RefreshTemporaryCartEvent;
import com.think.firewaiter.module.PxFormatInfo;
import com.think.firewaiter.module.PxMethodInfo;
import com.think.firewaiter.module.PxProductFormatRel;
import com.think.firewaiter.module.PxProductInfo;
import com.think.firewaiter.module.PxProductMethodRef;
import com.think.firewaiter.module.PxProductRemarks;
import com.think.firewaiter.module.PxTableInfo;
import com.think.firewaiter.module.ShoppingCart;
import com.think.firewaiter.ui.base.BaseToolbarActivity;
import com.think.firewaiter.utils.RegExpUtils;
import com.think.firewaiter.utils.ToastUtils;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;
import de.greenrobot.dao.query.Join;
import de.greenrobot.dao.query.QueryBuilder;
import java.util.List;
import java.util.Set;
import org.greenrobot.eventbus.EventBus;

/**
 * Created by dorado on 2016/6/9.
 */
public class ModifyProdActivity extends BaseToolbarActivity {
  public static final String SHOPPING_CART = "ShoppingCart";
  public static final String IS_TEMPORARY = "IsTemporary";

  //规格tags
  @Bind(R.id.tags_format) TagFlowLayout mTagsFormat;
  //做法tags
  @Bind(R.id.tags_method) TagFlowLayout mTagsMethod;
  //备注tags
  @Bind(R.id.tags_remarks) TagFlowLayout mTagsRemarks;
  //商品名
  @Bind(R.id.tv_prod_name) TextView mTvProdName;
  //数量
  @Bind(R.id.tv_num) TextView mTvNum;
  //多单位数量
  @Bind(R.id.tv_mult_num) TextView mTvMultNum;
  //多单位数量Rl
  @Bind(R.id.rl_mult_num) RelativeLayout mRlMultNum;
  //分割线
  @Bind(R.id.divider) View mDivider;
  //是否延迟
  @Bind(R.id.sb_switch_delay) SwitchButton mSbDelay;
  //自定义备注
  @Bind(R.id.et_custom_remark) EditText mEtCustomRemark;

  //当前商品
  private PxProductInfo mProductInfo;
  //当前桌台
  private PxTableInfo mTableInfo;
  //所选规格
  private PxFormatInfo mFormatInfo;
  //所选做法
  private PxMethodInfo mMethodInfo;
  //所选数量
  private double mNum;
  //所选多单位数量
  private double mMultipleNum;
  //是否延迟
  private boolean isDelay;
  //购物车信息
  private ShoppingCart mShoppingCart;
  //规格Adapter
  private TagAdapter mFormatTagAdapter;
  //做法Adapter
  private TagAdapter mMethodTagAdapter;

  //是否为临时订单
  private boolean isTemporary;
  //该商品的所有规格
  private List<PxProductFormatRel> mFormatRelList;
  //备注适配器
  private TagAdapter mRemarksAdapter;
  //所有备注list
  private List<PxProductRemarks> mRemarksList;

  @Override protected String provideToolbarTitle() {
    return "商品修改";
  }

  @Override protected int provideContentViewId() {
    return R.layout.activity_modify_prod;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ButterKnife.bind(this);
    mShoppingCart = (ShoppingCart) getIntent().getSerializableExtra(SHOPPING_CART);
    if (mShoppingCart == null) {
      ToastUtils.showShort(this, "无此记录");
      return;
    }
    mProductInfo = mShoppingCart.getDbProd();
    isTemporary = getIntent().getBooleanExtra(IS_TEMPORARY, false);
    if (!isTemporary) {
      Long tableId = mShoppingCart.getTableId();
      mTableInfo = DaoServiceUtil.getTableInfoService()
          .queryBuilder()
          .where(PxTableInfoDao.Properties.Id.eq(tableId))
          .unique();
    }
    if (mShoppingCart.getFormatId() != null) {
      mFormatInfo = mShoppingCart.getDbFormat();
    }
    if (mShoppingCart.getMethodId() != null) {
      mMethodInfo = mShoppingCart.getDbMethod();
    }
    isDelay = mShoppingCart.getIsDelay();
    mNum = mShoppingCart.getNum();
    if (mProductInfo.getMultipleUnit().equals(PxProductInfo.IS_TWO_UNIT_FALSE)) {
      mRlMultNum.setVisibility(View.GONE);
      mDivider.setVisibility(View.GONE);
    } else {
      mMultipleNum = mShoppingCart.getMultipleUnitNum();
    }
    //备注
    String remarks = mShoppingCart.getRemarks();
    mEtCustomRemark.setText(remarks);
    //显示数量
    mTvNum.setText(mNum + "");
    //显示多单位数量
    mTvMultNum.setText(mMultipleNum + "");
    //显示延迟
    mSbDelay.setChecked(isDelay);
    //显示名称
    mTvProdName.setText(mProductInfo.getName());
    //初始化规格tags
    initFormatTags();
    //初始化做法tags
    initMethodTags();
    //初始化备注tags
    initRemarksTags();
  }

  /**
   * 初始化备注tags
   */
  private void initRemarksTags() {
    //备注验证
    mRemarksList = DaoServiceUtil.getProdRemarksService()
        .queryBuilder()
        .where(PxProductRemarksDao.Properties.DelFlag.eq("0"))
        .list();
    if (mRemarksList != null && mRemarksList.size() != 0) {
      //TagAdapter
      mRemarksAdapter = new TagAdapter<PxProductRemarks>(mRemarksList) {
        @Override public View getView(FlowLayout parent, int position, PxProductRemarks remarks) {
          TextView tv = (TextView) LayoutInflater.from(ModifyProdActivity.this)
              .inflate(R.layout.item_tags_remarks, mTagsRemarks, false);
          tv.setText(remarks.getRemarks());
          return tv;
        }
      };
      mTagsRemarks.setAdapter(mRemarksAdapter);
      mTagsRemarks.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
        @Override public boolean onTagClick(View view, int position, FlowLayout parent) {
          String inputRemark = mEtCustomRemark.getText().toString().trim();
          PxProductRemarks productRemarks = mRemarksList.get(position);
          if (TextUtils.isEmpty(inputRemark.trim())) {
            mEtCustomRemark.append(productRemarks.getRemarks());
          } else {
            mEtCustomRemark.append(" ," + productRemarks.getRemarks());
          }
          return false;
        }
      });
    }
  }

  /**
   * 初始化规格tags
   */
  //@formatter:off
  private void initFormatTags() {
    //查询可用的规格引用关系
    QueryBuilder<PxProductFormatRel> formatRelQb = DaoServiceUtil.getProductFormatRelServiceService()
            .queryBuilder()
            .where(PxProductFormatRelDao.Properties.DelFlag.eq("0"))
            .where(PxProductFormatRelDao.Properties.PxProductInfoId.eq(mProductInfo.getId()));
    Join<PxProductFormatRel, PxFormatInfo> formatJoin = formatRelQb.join(PxProductFormatRelDao.Properties.PxFormatInfoId, PxFormatInfo.class);
    formatJoin.where(PxFormatInfoDao.Properties.DelFlag.eq("0"));
     mFormatRelList = formatRelQb.list();

    if (mFormatRelList != null && mFormatRelList.size() != 0) {
      mFormatTagAdapter = new TagAdapter<PxProductFormatRel>(mFormatRelList) {
        @Override public View getView(FlowLayout parent, int position, PxProductFormatRel rel) {
          TextView tv = (TextView) LayoutInflater.from(ModifyProdActivity.this).inflate(R.layout.item_tags, mTagsFormat, false);
          PxFormatInfo format = rel.getDbFormat();
          tv.setText(format.getName() + "(" + rel.getPrice() + "元)");
          return tv;
        }
      };
      mTagsFormat.setAdapter(mFormatTagAdapter);
      mTagsFormat.setOnSelectListener(new TagFlowLayout.OnSelectListener() {
        @Override public void onSelected(Set<Integer> selectPosSet) {
          if (selectPosSet.size() == 1) {
            String posString = selectPosSet.toString();
            int pos = Integer.parseInt(posString.substring(1, 2));
            mFormatInfo = mFormatRelList.get(pos).getDbFormat();
          } else {
            mFormatInfo = null;
          }
        }
      });
    }
    //默认选中
    if (mFormatInfo != null  && mFormatTagAdapter != null){
      PxProductFormatRel currentRel = DaoServiceUtil.getProductFormatRelServiceService()
          .queryBuilder()
          .where(PxProductFormatRelDao.Properties.PxFormatInfoId.eq(mFormatInfo.getId()))
          .where(PxProductFormatRelDao.Properties.PxProductInfoId.eq(mProductInfo.getId()))
          .unique();
      if (currentRel == null) return;
      for (int i = 0; i < mFormatRelList.size(); i++) {
        PxProductFormatRel rel = mFormatRelList.get(i);
        if (currentRel.getObjectId().equals(rel.getObjectId())) {
          mFormatInfo = rel.getDbFormat();
          mFormatTagAdapter.setSelectedList(i);
        }
      }
    }
  }

  /**
   * 初始化做法tags
   */
  //@formatter:off
  private void initMethodTags() {
    //查询可用的规格引用关系
    QueryBuilder<PxProductMethodRef> methodRelQb = DaoServiceUtil.getProductMethodRelService()
        .queryBuilder()
        .where(PxProductMethodRefDao.Properties.DelFlag.eq("0"))
        .where(PxProductMethodRefDao.Properties.PxProductInfoId.eq(mProductInfo.getId()));
    Join<PxProductMethodRef, PxMethodInfo> methodJoin = methodRelQb.join(PxProductMethodRefDao.Properties.PxMethodInfoId, PxMethodInfo.class);
    methodJoin.where(PxMethodInfoDao.Properties.DelFlag.eq("0"));
    final List<PxProductMethodRef> methodRelList = methodRelQb.list();

    if (methodRelList != null && methodRelList.size() != 0) {
      mMethodTagAdapter = new TagAdapter<PxProductMethodRef>(methodRelList) {
        @Override public View getView(FlowLayout parent, int position, PxProductMethodRef rel) {
          TextView tv = (TextView) LayoutInflater.from(ModifyProdActivity.this).inflate(R.layout.item_tags, mTagsMethod, false);
          PxMethodInfo method = rel.getDbMethod();
          tv.setText(method.getName());
          return tv;
        }
      };
      mTagsMethod.setAdapter(mMethodTagAdapter);
      mTagsMethod.setOnSelectListener(new TagFlowLayout.OnSelectListener() {
        @Override public void onSelected(Set<Integer> selectPosSet) {
          if (selectPosSet.size() == 1) {
            String posString = selectPosSet.toString();
            int pos = Integer.parseInt(posString.substring(1, 2));
            mMethodInfo = methodRelList.get(pos).getDbMethod();
          } else {
            mMethodInfo = null;
          }
        }
      });
    }
    //默认选中
    if (mMethodInfo != null && mMethodTagAdapter != null) {
      PxProductMethodRef currentRel = DaoServiceUtil.getProductMethodRelService()
          .queryBuilder()
          .where(PxProductMethodRefDao.Properties.PxMethodInfoId.eq(mMethodInfo.getId()))
          .where(PxProductMethodRefDao.Properties.PxProductInfoId.eq(mProductInfo.getId()))
          .unique();
      if (currentRel == null) return;
      for (int i = 0; i < methodRelList.size(); i++) {
        PxProductMethodRef rel = methodRelList.get(i);
        if (currentRel.getObjectId().equals(rel.getObjectId())) {
          mMethodInfo = rel.getDbMethod();
          mMethodTagAdapter.setSelectedList(i);
        }
      }
    }
  }

  /**
   * 数量
   */
  //@formatter:off
  @OnClick(R.id.rl_num) public void setNum() {
    new MaterialDialog.Builder(this)
        .content("输入数量")
        .inputType(InputType.TYPE_CLASS_NUMBER)
        .positiveText("确定")
        .negativeText("取消")
        .negativeColor(getResources().getColor(R.color.primary_text))
        .alwaysCallInputCallback()
        .input("", "", false, new MaterialDialog.InputCallback() {
          @Override public void onInput(MaterialDialog dialog, CharSequence input) {
            if (input.toString().trim() != null && !input.toString().toString().equals("") && input.toString().length() > 3) {
              ToastUtils.showShort(ModifyProdActivity.this, "输入过长!");
              dialog.getInputEditText().setText("");
              return;
            }
            if (input.toString() == null
                || input.toString().trim().equals("")
                || Integer.valueOf(input.toString().trim()).intValue() <= 0) {
              dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
            } else {
              dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
            }
          }
        })
        .onPositive(new MaterialDialog.SingleButtonCallback() {
          @Override public void onClick(MaterialDialog dialog, DialogAction which) {
            mNum = Integer.valueOf(dialog.getInputEditText().getText().toString().trim());
            mTvNum.setText(mNum + "");
          }
        })
        .show();
  }

  /**
   * 多单位数量
   */
  //@formatter:off
  @OnClick(R.id.rl_mult_num) public void setMultNum() {
    new MaterialDialog.Builder(this)
        .content("结算数量")
        .inputType(InputType.TYPE_NUMBER_FLAG_DECIMAL)
        .positiveText("确定")
        .negativeText("取消")
        .negativeColor(this.getResources().getColor(R.color.primary_text))
        .alwaysCallInputCallback()
        .input("数量", "", false, new MaterialDialog.InputCallback() {
          @Override public void onInput(MaterialDialog dialog, CharSequence input) {
            if (input.toString().trim() != null && !input.toString().toString().equals("") && input.toString().length() > 6) {
              ToastUtils.showShort(ModifyProdActivity.this, "输入过长!");
              dialog.getInputEditText().setText("");
              return;
            }

            if (input.toString() == null
                || input.toString().trim().equals("")
                || !RegExpUtils.match2DecimalPlaces(input.toString())
                || Double.valueOf(input.toString().trim()).doubleValue() <= 0) {
              dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
            } else {
              dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
            }
          }
        })
        .onPositive(new MaterialDialog.SingleButtonCallback() {
          @Override public void onClick(MaterialDialog dialog, DialogAction which) {
            mMultipleNum = Double.valueOf(dialog.getInputEditText().getText().toString().trim());
            mTvMultNum.setText(mMultipleNum + "");
          }
        })
        .show();
  }

  /**
   * 修改按钮
   */
  //@formatter:on
  @OnClick(R.id.btn_modify) public void modifyProd() {
    //数量
    if (mNum == 0) {
      ToastUtils.showShort(this, "请输入数量");
      return;
    }
    //多单位数量
    if (mProductInfo.getMultipleUnit().equals(PxProductInfo.IS_TWO_UNIT_TURE)) {
      if (mMultipleNum == 0) {
        ToastUtils.showShort(this, "请输入结算数量");
        return;
      }
    }
    //删除旧的
    DaoServiceUtil.getShoppingCartService().delete(mShoppingCart);
    //有规格 必选一个
    if (mFormatRelList != null && mFormatRelList.size() > 0 && mFormatInfo == null) {
      ToastUtils.showShort(ModifyProdActivity.this, "请选择规格");
      return;
    }

    //自定义备注
    String customRemark = mEtCustomRemark.getText().toString().trim();

    //临时订单
    if (isTemporary) {
      temporaryOrder();
      return;
    }

    //无规格 无做法
    if (mFormatInfo == null && mMethodInfo == null) {
      ShoppingCart exist = DaoServiceUtil.getShoppingCartService()
          .queryBuilder()
          .where(ShoppingCartDao.Properties.ProdId.eq(mProductInfo.getId()))
          .where(ShoppingCartDao.Properties.TableId.eq(mTableInfo.getId()))
          .where(ShoppingCartDao.Properties.FormatId.isNull())
          .where(ShoppingCartDao.Properties.MethodId.isNull())
          .where(ShoppingCartDao.Properties.IsDelay.eq(mSbDelay.isChecked()))
          .where(ShoppingCartDao.Properties.Remarks.eq(customRemark))
          .unique();
      if (exist == null) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setNum(mNum);
        shoppingCart.setMultipleUnitNum(mMultipleNum);
        shoppingCart.setDbTable(mTableInfo);
        shoppingCart.setDbProd(mProductInfo);
        shoppingCart.setIsDelay(mSbDelay.isChecked());
        shoppingCart.setRemarks(customRemark);
        DaoServiceUtil.getShoppingCartService().saveOrUpdate(shoppingCart);
      } else {
        exist.setNum(exist.getNum() + mNum);
        exist.setMultipleUnitNum(exist.getMultipleUnitNum() + mMultipleNum);
        DaoServiceUtil.getShoppingCartService().saveOrUpdate(exist);
      }
    }
    //无规格 有做法
    else if (mFormatInfo == null && mMethodInfo != null) {
      ShoppingCart exist = DaoServiceUtil.getShoppingCartService()
          .queryBuilder()
          .where(ShoppingCartDao.Properties.ProdId.eq(mProductInfo.getId()))
          .where(ShoppingCartDao.Properties.TableId.eq(mTableInfo.getId()))
          .where(ShoppingCartDao.Properties.FormatId.isNull())
          .where(ShoppingCartDao.Properties.MethodId.eq(mMethodInfo.getId()))
          .where(ShoppingCartDao.Properties.IsDelay.eq(mSbDelay.isChecked()))
          .where(ShoppingCartDao.Properties.Remarks.eq(customRemark))
          .unique();
      if (exist == null) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setNum(mNum);
        shoppingCart.setMultipleUnitNum(mMultipleNum);
        shoppingCart.setDbTable(mTableInfo);
        shoppingCart.setDbProd(mProductInfo);
        shoppingCart.setDbMethod(mMethodInfo);
        shoppingCart.setIsDelay(mSbDelay.isChecked());
        shoppingCart.setRemarks(customRemark);
        DaoServiceUtil.getShoppingCartService().saveOrUpdate(shoppingCart);
      } else {
        exist.setNum(exist.getNum() + mNum);
        exist.setMultipleUnitNum(exist.getMultipleUnitNum() + mMultipleNum);
        DaoServiceUtil.getShoppingCartService().saveOrUpdate(exist);
      }
    }
    //有规格 无做法
    else if (mFormatInfo != null && mMethodInfo == null) {
      ShoppingCart exist = DaoServiceUtil.getShoppingCartService()
          .queryBuilder()
          .where(ShoppingCartDao.Properties.ProdId.eq(mProductInfo.getId()))
          .where(ShoppingCartDao.Properties.TableId.eq(mTableInfo.getId()))
          .where(ShoppingCartDao.Properties.FormatId.eq(mFormatInfo.getId()))
          .where(ShoppingCartDao.Properties.MethodId.isNull())
          .where(ShoppingCartDao.Properties.IsDelay.eq(mSbDelay.isChecked()))
          .where(ShoppingCartDao.Properties.Remarks.eq(customRemark))
          .unique();
      if (exist == null) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setNum(mNum);
        shoppingCart.setMultipleUnitNum(mMultipleNum);
        shoppingCart.setDbTable(mTableInfo);
        shoppingCart.setDbProd(mProductInfo);
        shoppingCart.setDbFormat(mFormatInfo);
        shoppingCart.setIsDelay(mSbDelay.isChecked());
        shoppingCart.setRemarks(customRemark);
        DaoServiceUtil.getShoppingCartService().saveOrUpdate(shoppingCart);
      } else {
        exist.setNum(exist.getNum() + mNum);
        exist.setMultipleUnitNum(exist.getMultipleUnitNum() + mMultipleNum);
        DaoServiceUtil.getShoppingCartService().saveOrUpdate(exist);
      }
    }
    //有规格 有做法
    else if (mFormatInfo != null && mMethodInfo != null) {
      ShoppingCart exist = DaoServiceUtil.getShoppingCartService()
          .queryBuilder()
          .where(ShoppingCartDao.Properties.ProdId.eq(mProductInfo.getId()))
          .where(ShoppingCartDao.Properties.TableId.eq(mTableInfo.getId()))
          .where(ShoppingCartDao.Properties.FormatId.eq(mFormatInfo.getId()))
          .where(ShoppingCartDao.Properties.MethodId.eq(mMethodInfo.getId()))
          .where(ShoppingCartDao.Properties.IsDelay.eq(mSbDelay.isChecked()))
          .where(ShoppingCartDao.Properties.Remarks.eq(customRemark))
          .unique();
      if (exist == null) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setNum(mNum);
        shoppingCart.setMultipleUnitNum(mMultipleNum);
        shoppingCart.setDbTable(mTableInfo);
        shoppingCart.setDbProd(mProductInfo);
        shoppingCart.setDbFormat(mFormatInfo);
        shoppingCart.setDbMethod(mMethodInfo);
        shoppingCart.setIsDelay(mSbDelay.isChecked());
        shoppingCart.setRemarks(customRemark);
        DaoServiceUtil.getShoppingCartService().saveOrUpdate(shoppingCart);
      } else {
        exist.setNum(exist.getNum() + mNum);
        exist.setMultipleUnitNum(exist.getMultipleUnitNum() + mMultipleNum);
        DaoServiceUtil.getShoppingCartService().saveOrUpdate(exist);
      }
    }
    ToastUtils.showShort(this, "修改成功");
    //刷新购物车
    EventBus.getDefault().post(new RefreshCartEvent());
    finish();
  }

  /**
   * 临时订单
   */
  private void temporaryOrder() {
    String customRemark = mEtCustomRemark.getText().toString().trim();
    //无规格 无做法
    if (mFormatInfo == null && mMethodInfo == null) {
      ShoppingCart exist = DaoServiceUtil.getShoppingCartService()
          .queryBuilder()
          .where(ShoppingCartDao.Properties.ProdId.eq(mProductInfo.getId()))
          .where(ShoppingCartDao.Properties.TableId.isNull())
          .where(ShoppingCartDao.Properties.FormatId.isNull())
          .where(ShoppingCartDao.Properties.MethodId.isNull())
          .where(ShoppingCartDao.Properties.IsDelay.eq(mSbDelay.isChecked()))
          .where(ShoppingCartDao.Properties.Remarks.eq(customRemark))
          .unique();
      if (exist == null) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setNum(mNum);
        shoppingCart.setMultipleUnitNum(mMultipleNum);
        shoppingCart.setDbTable(null);
        shoppingCart.setDbProd(mProductInfo);
        shoppingCart.setIsDelay(mSbDelay.isChecked());
        shoppingCart.setRemarks(customRemark);
        DaoServiceUtil.getShoppingCartService().saveOrUpdate(shoppingCart);
      } else {
        exist.setNum(exist.getNum() + mNum);
        exist.setMultipleUnitNum(exist.getMultipleUnitNum() + mMultipleNum);
        DaoServiceUtil.getShoppingCartService().saveOrUpdate(exist);
      }
    }
    //无规格 有做法
    else if (mFormatInfo == null && mMethodInfo != null) {
      ShoppingCart exist = DaoServiceUtil.getShoppingCartService()
          .queryBuilder()
          .where(ShoppingCartDao.Properties.ProdId.eq(mProductInfo.getId()))
          .where(ShoppingCartDao.Properties.TableId.isNull())
          .where(ShoppingCartDao.Properties.FormatId.isNull())
          .where(ShoppingCartDao.Properties.MethodId.eq(mMethodInfo.getId()))
          .where(ShoppingCartDao.Properties.IsDelay.eq(mSbDelay.isChecked()))
          .where(ShoppingCartDao.Properties.Remarks.eq(customRemark))
          .unique();
      if (exist == null) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setNum(mNum);
        shoppingCart.setMultipleUnitNum(mMultipleNum);
        shoppingCart.setDbTable(null);
        shoppingCart.setDbProd(mProductInfo);
        shoppingCart.setDbMethod(mMethodInfo);
        shoppingCart.setIsDelay(mSbDelay.isChecked());
        shoppingCart.setRemarks(customRemark);
        DaoServiceUtil.getShoppingCartService().saveOrUpdate(shoppingCart);
      } else {
        exist.setNum(exist.getNum() + mNum);
        exist.setMultipleUnitNum(exist.getMultipleUnitNum() + mMultipleNum);
        DaoServiceUtil.getShoppingCartService().saveOrUpdate(exist);
      }
    }
    //有规格 无做法
    else if (mFormatInfo != null && mMethodInfo == null) {
      ShoppingCart exist = DaoServiceUtil.getShoppingCartService()
          .queryBuilder()
          .where(ShoppingCartDao.Properties.ProdId.eq(mProductInfo.getId()))
          .where(ShoppingCartDao.Properties.TableId.isNull())
          .where(ShoppingCartDao.Properties.FormatId.eq(mFormatInfo.getId()))
          .where(ShoppingCartDao.Properties.MethodId.isNull())
          .where(ShoppingCartDao.Properties.IsDelay.eq(mSbDelay.isChecked()))
          .where(ShoppingCartDao.Properties.Remarks.eq(customRemark))
          .unique();
      if (exist == null) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setNum(mNum);
        shoppingCart.setMultipleUnitNum(mMultipleNum);
        shoppingCart.setDbTable(null);
        shoppingCart.setDbProd(mProductInfo);
        shoppingCart.setDbFormat(mFormatInfo);
        shoppingCart.setIsDelay(mSbDelay.isChecked());
        shoppingCart.setRemarks(customRemark);
        DaoServiceUtil.getShoppingCartService().saveOrUpdate(shoppingCart);
      } else {
        exist.setNum(exist.getNum() + mNum);
        exist.setMultipleUnitNum(exist.getMultipleUnitNum() + mMultipleNum);
        DaoServiceUtil.getShoppingCartService().saveOrUpdate(exist);
      }
    }
    //有规格 有做法
    else if (mFormatInfo != null && mMethodInfo != null) {
      ShoppingCart exist = DaoServiceUtil.getShoppingCartService()
          .queryBuilder()
          .where(ShoppingCartDao.Properties.ProdId.eq(mProductInfo.getId()))
          .where(ShoppingCartDao.Properties.TableId.isNull())
          .where(ShoppingCartDao.Properties.FormatId.eq(mFormatInfo.getId()))
          .where(ShoppingCartDao.Properties.MethodId.eq(mMethodInfo.getId()))
          .where(ShoppingCartDao.Properties.IsDelay.eq(mSbDelay.isChecked()))
          .where(ShoppingCartDao.Properties.Remarks.eq(customRemark))
          .unique();
      if (exist == null) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setNum(mNum);
        shoppingCart.setMultipleUnitNum(mMultipleNum);
        shoppingCart.setDbTable(null);
        shoppingCart.setDbProd(mProductInfo);
        shoppingCart.setDbFormat(mFormatInfo);
        shoppingCart.setDbMethod(mMethodInfo);
        shoppingCart.setIsDelay(mSbDelay.isChecked());
        shoppingCart.setRemarks(customRemark);
        DaoServiceUtil.getShoppingCartService().saveOrUpdate(shoppingCart);
      } else {
        exist.setNum(exist.getNum() + mNum);
        exist.setMultipleUnitNum(exist.getMultipleUnitNum() + mMultipleNum);
        DaoServiceUtil.getShoppingCartService().saveOrUpdate(exist);
      }
    }
    ToastUtils.showShort(this, "修改成功");
    //刷新购物车
    EventBus.getDefault().post(new RefreshTemporaryCartEvent());
    finish();
  }
}
