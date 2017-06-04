package com.think.firewaiter.ui.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.think.firewaiter.R;
import com.think.firewaiter.dao.DaoServiceUtil;
import com.think.firewaiter.dao.PxProductRemarksDao;
import com.think.firewaiter.dao.PxTableAreaDao;
import com.think.firewaiter.module.PxProductRemarks;
import com.think.firewaiter.module.PxPromotioInfo;
import com.think.firewaiter.module.PxTableArea;
import com.think.firewaiter.module.PxTableInfo;
import com.think.firewaiter.ui.base.BaseToolbarActivity;
import com.think.firewaiter.utils.MaterialDialogUtils;
import com.think.firewaiter.utils.PromotioDetailsHelp;
import com.think.firewaiter.utils.ToastUtils;
import com.think.firewaiter.widget.InterceptClickLayout;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;
import java.util.List;

/**
 * Created by zjq on 2016/5/4.
 */
public class StartBillActivity extends BaseToolbarActivity {

  @Bind(R.id.tv_table_name) TextView mTvTableName;
  @Bind(R.id.et_people_num) EditText mEtPeopleNum;
  @Bind(R.id.content_view) InterceptClickLayout mContentView;
  @Bind(R.id.progress_view) RelativeLayout mProgressView;
  @Bind(R.id.et_custom_remark) EditText mEtCustomRemark;
  @Bind(R.id.tv_promotio_info) TextView mTvPromotioInfo;

  private PxTableInfo mTableInfo;
  //所有备注list
  private List<PxProductRemarks> mRemarksList;
  //备注适配器
  private TagAdapter mRemarksAdapter;
  //促销计划
  private PxPromotioInfo mPromotioInfo;
  //备注Tags
  @Bind(R.id.tags_remarks) TagFlowLayout mTagsRemarks;

  @Override protected String provideToolbarTitle() {
    return "开单";
  }

  @Override protected int provideContentViewId() {
    return R.layout.activity_start_bill;
  }

  //@formatter:on
  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ButterKnife.bind(this);
    mTableInfo = (PxTableInfo) getIntent().getSerializableExtra("TableInfo");
    String tableType = mTableInfo.getType();
    String tableName = mTableInfo.getName();
    if (tableType != null) {
      PxTableArea tableArea = DaoServiceUtil.getTableAreaService()
          .queryBuilder()
          .where(PxTableAreaDao.Properties.Type.eq(tableType))
          .where(PxTableAreaDao.Properties.DelFlag.eq("0"))
          .unique();
      mTvTableName.setText(
          tableArea == null ? "大厅·" + tableName : tableArea.getName() + "." + tableName);
    } else {
      mTvTableName.setText("大厅·");
    }
    mEtPeopleNum.addTextChangedListener(new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s != null && s.length() != 0 && s.length() > 3) {
          mEtPeopleNum.setText("");
        }
      }

      @Override public void afterTextChanged(Editable s) {

      }
    });
    //查询备注
    queryRemarks();
  }
  //@formatter:on

  /**
   * 查询备注
   */
  private void queryRemarks() {
    //备注验证
    mRemarksList = DaoServiceUtil.getProdRemarksService()
        .queryBuilder()
        .where(PxProductRemarksDao.Properties.DelFlag.eq("0"))
        .list();
    if (mRemarksList != null && mRemarksList.size() != 0) {
      //TagAdapter
      mRemarksAdapter = new TagAdapter<PxProductRemarks>(mRemarksList) {
        @Override public View getView(FlowLayout parent, int position, PxProductRemarks remarks) {
          TextView tv = (TextView) LayoutInflater.from(StartBillActivity.this).inflate(R.layout.item_tags_remarks, mTagsRemarks, false);
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

  //@formatter:off
  /**
   * 开始点餐
   */
  @OnClick(R.id.btn_start_order) public void startOrder(Button button) {
    String s = mEtPeopleNum.getText().toString();
    if (s == null || s.trim().equals("")) {
      ToastUtils.showShort(this, "请输入人数");
      return;
    }

    if (s.length() > 3) {
      ToastUtils.showShort(this, "人数不能超过3位数");
      return;
    }

    int num = Integer.valueOf(s);
    if (num == 0) {
      ToastUtils.showShort(this, "人数不能为0");
      return;
    }

    //自定义备注
    String customRemark = mEtCustomRemark.getText().toString().trim();
    Bundle bundle = new Bundle();
    bundle.putSerializable("TableInfo", mTableInfo);
    if (mPromotioInfo != null) {
      bundle.putSerializable("PromotioInfo", mPromotioInfo);
    }
    bundle.putInt("PeopleNum", num);
    bundle.putBoolean("IsInitOrder", true);
    bundle.putString("Remarks",customRemark);
    openActivity(BillActivity.class, bundle);
  }

  /**
   * 选择促销计划
   */
  @OnClick(R.id.rl_promotio) public void selectPromotio(){
    //获取有效促销计划
    final List<PxPromotioInfo> promotioInfoList = PromotioDetailsHelp.getPromotioInfoList();
    if (promotioInfoList.isEmpty()) {
      ToastUtils.showShort(null, "请在后台添加促销计划!");
      return;
    }
    String[] items = new String[promotioInfoList.size()];
    for (int i = 0; i < promotioInfoList.size(); i++) {
      PxPromotioInfo promotioInfo = promotioInfoList.get(i);
      items[i] = promotioInfo.getName();
    }
    final MaterialDialog selectPromotioDialog = MaterialDialogUtils.showListDialog(this, "促销计划", items);
    MDButton posBtn = selectPromotioDialog.getActionButton(DialogAction.POSITIVE);
    MDButton negBtn = selectPromotioDialog.getActionButton(DialogAction.NEGATIVE);
    negBtn.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        MaterialDialogUtils.dismissDialog(selectPromotioDialog);
      }
    });
    posBtn.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        int selectedIndex = selectPromotioDialog.getSelectedIndex();
        mPromotioInfo = promotioInfoList.get(selectedIndex);
        mTvPromotioInfo.setText(mPromotioInfo.getName());
        mTvPromotioInfo.setVisibility(View.VISIBLE);
        MaterialDialogUtils.dismissDialog(selectPromotioDialog);
      }
    });
  }
  /**
   * 清除促销计划
   */
  @OnClick(R.id.tv_promotio_info) public void deletePromotio(){
    mPromotioInfo = null;
    mTvPromotioInfo.setVisibility(View.INVISIBLE);
  }
  @Override protected void onDestroy() {
    super.onDestroy();
    ButterKnife.unbind(this);
  }
}
