package com.think.firewaiter.ui.activity;

import android.graphics.Paint;
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
import com.think.firewaiter.dao.ShoppingCartDao;
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

/**
 * Created by dorado on 2016/6/9.
 */
public class AddProdActivity extends BaseToolbarActivity {

  public static final String PROD_INFO = "ProductInfo";
  public static final String TABLE_INFO = "TableInfo";
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
  //是否为临时订单
  private boolean isTemporary;
  //所有规格
  private List<PxProductFormatRel> mFormatRelList;
  //备注适配器
  private TagAdapter mRemarksAdapter;
  //所有备注list
  private List<PxProductRemarks> mRemarksList;
  //所选规格rel
  private PxProductFormatRel productFormatRel;

  @Override protected String provideToolbarTitle() {
    return "商品添加";
  }

  @Override protected int provideContentViewId() {
    return R.layout.activity_add_prod;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ButterKnife.bind(this);
    isTemporary = getIntent().getBooleanExtra(IS_TEMPORARY, false);
    if (!isTemporary) {
      mTableInfo = (PxTableInfo) getIntent().getSerializableExtra(TABLE_INFO);
    }
    mProductInfo = (PxProductInfo) getIntent().getSerializableExtra(PROD_INFO);
    mTvProdName.setText(mProductInfo.getName());
    mNum = 1.0;
    mMultipleNum = 1.0;
    if (mProductInfo.getMultipleUnit().equals(PxProductInfo.IS_TWO_UNIT_FALSE)) {
      mRlMultNum.setVisibility(View.GONE);
      mDivider.setVisibility(View.GONE);
    }
    //初始化规格tags
    initFormatTags();
    //初始化做法tags
    initMethodTags();
    //初始化备注tags
    initRemarksTags();
  }

  //@formatter:on
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
          TextView tv = (TextView) LayoutInflater.from(AddProdActivity.this)
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
      mTagsFormat.setAdapter(new TagAdapter<PxProductFormatRel>(mFormatRelList) {
        @Override public View getView(FlowLayout parent, int position, PxProductFormatRel rel) {
          TextView tv = (TextView) LayoutInflater.from(AddProdActivity.this).inflate(R.layout.item_tags, mTagsFormat, false);
          PxFormatInfo format = rel.getDbFormat();
          if(rel.getStatus().equals(PxProductFormatRel.PRODUCT_FORMAT_STOCK)){
            tv.setText(format.getName() + "(" + rel.getPrice() + "元)"+"(停售)");
            tv.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG|Paint.ANTI_ALIAS_FLAG);
          }else {
            if(rel.getStock()!=null&&rel.getStock()>0.0){
              tv.setText(format.getName()+ "(" + rel.getPrice() + "元)(余"+rel.getStock()+")");
            }else {
              tv.setText(format.getName() + "(" + rel.getPrice() + "元)");
            }
          }
          tv.setText(format.getName() + "(" + rel.getPrice() + "元)");
          return tv;
        }
      });
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
      mTagsMethod.setAdapter(new TagAdapter<PxProductMethodRef>(methodRelList) {
        @Override public View getView(FlowLayout parent, int position, PxProductMethodRef rel) {
          TextView tv = (TextView) LayoutInflater.from(AddProdActivity.this).inflate(R.layout.item_tags, mTagsMethod, false);
          PxMethodInfo methodInfo = rel.getDbMethod();
          tv.setText(methodInfo.getName());
          return tv;
        }
      });
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
  }

  /**
   * 数量
   */
  //@formatter:off
  @OnClick(R.id.rl_num) public void setNum() {
    if (null == AddProdActivity.this) return;
    new MaterialDialog.Builder(this)
        .content("输入数量")
        .inputType(InputType.TYPE_CLASS_NUMBER)
        .positiveText("确定")
        .negativeText("取消")
        .negativeColor(getResources().getColor(R.color.primary_text))
        .alwaysCallInputCallback()
        .input("", "", false, new MaterialDialog.InputCallback() {
          @Override public void onInput(MaterialDialog dialog, CharSequence input) {
            if (input.toString().trim() != null && !input.toString().toString().equals("") && input.toString().length() > 4) {
              ToastUtils.showShort(AddProdActivity.this, "输入过长!");
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
    if (null == AddProdActivity.this) return;
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
              ToastUtils.showShort(AddProdActivity.this, "输入过长!");
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
   * 添加按钮
   */
  //@formatter:on
  @OnClick(R.id.btn_add) public void addProd() {
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
    //有规格的时候 必选一个
    if (mFormatRelList != null && mFormatRelList.size() != 0) {
      if (mFormatInfo == null) {
        ToastUtils.showShort(AddProdActivity.this, "请选择规格");
        return;
      }
      //判断该商品规格状态
      getProductFormat();
      if (productFormatRel.getStatus().equals(PxProductFormatRel.PRODUCT_FORMAT_STOCK)) {
        ToastUtils.showShort(this, "该规格处于停售状态,不能添加");
        return;
      }

      //判断该商品规格余量状态(服务生余量暂不实现)
      if (productFormatRel.getStatus().equals(PxProductFormatRel.PRODUCT_FORMAT_NORMAL)) {
        if (mProductInfo.getMultipleUnit().equals(PxProductInfo.IS_TWO_UNIT_TURE)) {
          if (productFormatRel.getStock() != null && productFormatRel.getStock() < mMultipleNum) {
            ToastUtils.showShort(this, "该规格库存数量不足");
            return;
          }
        } else {
          if (productFormatRel.getStock() != null && productFormatRel.getStock() < mNum) {
            ToastUtils.showShort(this, "该规格库存数量不足");
            return;
          }
        }
      }
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
    ToastUtils.showShort(this, "添加成功");
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
    ToastUtils.showShort(this, "添加成功");
    finish();
  }

  /**
   * 获取该商品所选规格
   */
  private PxProductFormatRel getProductFormat() {
    QueryBuilder<PxProductFormatRel> formatRel = DaoServiceUtil.getProductFormatRelServiceService()
        .queryBuilder()
        .where(PxProductFormatRelDao.Properties.DelFlag.eq("0"))
        .where(PxProductFormatRelDao.Properties.PxProductInfoId.eq(mProductInfo.getId()))
        .where(PxProductFormatRelDao.Properties.PxFormatInfoId.eq(mFormatInfo.getId()));
    productFormatRel = formatRel.unique();
    return productFormatRel;
  }
}
