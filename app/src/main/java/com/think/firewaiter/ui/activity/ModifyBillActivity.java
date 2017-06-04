package com.think.firewaiter.ui.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.afollestad.materialdialogs.internal.MDButton;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;
import com.think.firewaiter.R;
import com.think.firewaiter.chat.AppMoveTableItem;
import com.think.firewaiter.chat.FireMessage;
import com.think.firewaiter.chat.chatUtils.XMPPConnectUtils;
import com.think.firewaiter.chat.req.ModifyBillReq;
import com.think.firewaiter.chat.resp.MoveTableResp;
import com.think.firewaiter.chat.resp.SimpleResp;
import com.think.firewaiter.common.App;
import com.think.firewaiter.config.Setting;
import com.think.firewaiter.dao.DaoServiceUtil;
import com.think.firewaiter.dao.PxProductRemarksDao;
import com.think.firewaiter.module.PxProductRemarks;
import com.think.firewaiter.module.PxPromotioInfo;
import com.think.firewaiter.ui.base.BaseToolbarActivity;
import com.think.firewaiter.utils.MaterialDialogUtils;
import com.think.firewaiter.utils.NetUtils;
import com.think.firewaiter.utils.PromotioDetailsHelp;
import com.think.firewaiter.utils.ToastUtils;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;
import java.util.List;
import java.util.UUID;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;

/**
 * Created by zjq on 2016/5/9.
 */
public class ModifyBillActivity extends BaseToolbarActivity {
  @Bind(R.id.tv_table_name) TextView mTvTableName;
  @Bind(R.id.et_people_num) EditText mEtPeopleNum;
  @Bind(R.id.content) RelativeLayout mContentView;
  //备注Tags
  @Bind(R.id.tags_remarks) TagFlowLayout mTagsRemarks;
  //自定义备注
  @Bind(R.id.et_custom_remark) EditText mEtCustomRemark;
  //促销计划
  @Bind(R.id.tv_promotio_info) TextView mTvPromotioInfo;

  private String mTableId;//桌台id
  private long mOrderId;//订单id
  private String mTableName;//桌台名
  private long mPeopleNum;//人数
  private String mMoveTableId;//目标桌位id
  private String mMoveTableName;//目标桌名
  private List<AppMoveTableItem> mMoveTableItemList;//桌台列表


  private String[] mMoveTableNames;

  private MaterialDialog dialog;//提交后显示对话框

  private UUID mCurrentTableUUID;//当前获取桌台uuid
  private UUID mCurrentModifyUUID;//当前改单uuid

  private Chat mChat;

  //备注适配器
  private TagAdapter mRemarksAdapter;
  //所有备注list
  private List<PxProductRemarks> mRemarksList;
  private IMChatMessageListener mMessageListener;

  //促销计划
  PxPromotioInfo mPromotioInfo;

  @Override protected String provideToolbarTitle() {
    return "改单";
  }

  @Override protected int provideContentViewId() {
    return R.layout.activity_modify_bill;
  }

  //@formatter:off
  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ButterKnife.bind(this);
    Bundle bundle = getIntent().getExtras();
    mTableId = bundle.getString("TableId");
    mTableName = bundle.getString("TableName");
    mOrderId = bundle.getLong("OrderId");
    mPeopleNum = bundle.getLong("PeopleNum");
    mPromotioInfo = (PxPromotioInfo)bundle.getSerializable("PromotioInfo");
    String remarks = getIntent().getStringExtra("Remarks");

    mEtCustomRemark.setText(remarks);
    mTvTableName.setText(mTableName);
    mEtPeopleNum.setText(mPeopleNum + "");

    if (mPromotioInfo != null){
      mTvPromotioInfo.setVisibility(View.VISIBLE);
      mTvPromotioInfo.setText(mPromotioInfo.getName());
    }
    //获取chat
    if (XMPPConnectUtils.getConnection() != null) {
      ChatManager chatManager = ChatManager.getInstanceFor(XMPPConnectUtils.getConnection());
      if (chatManager != null) {
        mChat = chatManager.createChat("admin" + ((App) (App.getContext())).getUser().getCompanyCode() + "@" + Setting.SERVER_NAME);
        getChatMessages();
      }
    }
    mEtPeopleNum.addTextChangedListener(new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s != null && s.length() != 0 && s.length() > 3) {
          mEtPeopleNum.setText("");
          mPeopleNum = 0;
        }
      }

      @Override public void afterTextChanged(Editable s) {

      }
    });

    //查询备注
    queryRemarks();
  }


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
          TextView tv = (TextView) LayoutInflater.from(ModifyBillActivity.this).inflate(R.layout.item_tags_remarks, mTagsRemarks, false);
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
   * 接收消息
   */
  private MaterialDialog mDialog;

  private void getChatMessages() {
    mMessageListener = new IMChatMessageListener();
    mChat.addMessageListener(mMessageListener);
  }
  class IMChatMessageListener implements ChatMessageListener{
      @Override public void processMessage(Chat chat, Message message) {
        String body = message.getBody();
        final FireMessage fireMessage = new Gson().fromJson(body, FireMessage.class);
        if (fireMessage == null || fireMessage.getOperateType() == 0) {
          return;
        }
        //桌台信息回执
        if (fireMessage.getOperateType() == FireMessage.MOVE_TABLE_RESP) {
          runOnUiThread(new Runnable() {
            @Override public void run() {
              //判断是否是之前的离线消息
              if (!fireMessage.getUUID().equals(mCurrentTableUUID)) {
                ToastUtils.showShort(ModifyBillActivity.this, "网络错误");
              } else {
                //显示对话框
                showMoveTableListDialog(fireMessage);
              }
            }
          });
        }
        //改单回执
        if (fireMessage.getOperateType() == FireMessage.MODIFY_BILL_RESP) {
          Logger.i("body:" + body);
          runOnUiThread(new Runnable() {
            @Override public void run() {
              if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
              }
              //判断是否是之前的离线消息
              if (!fireMessage.getUUID().equals(mCurrentModifyUUID)) {
                ToastUtils.showShort(ModifyBillActivity.this, "网络错误");
              } else {
                String data = fireMessage.getData();
                SimpleResp modifyBillResp = new Gson().fromJson(data, SimpleResp.class);
                String des = modifyBillResp.getDes();
                //确认对话框
                mDialog = new MaterialDialog.Builder(ModifyBillActivity.this).title("警告")
                    .content(des)
                    .positiveText("确认")
                    .negativeColor(getResources().getColor(R.color.primary_text))
                    .canceledOnTouchOutside(false)
                    .cancelable(false)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                      @Override public void onClick(MaterialDialog dialog, DialogAction which) {
                        dialog.dismiss();
                        openActivity(MainActivity.class);
                      }
                    })
                    .build();
                if (mDialog != null && mDialog.isShowing() == false) {
                  mDialog.show();
                }
              }
            }
          });
        }
      }
  }
  /**
   * 显示对话框
   */
  private void showMoveTableListDialog(FireMessage fireMessage) {
    //关闭正在发送中dialog
    dismissSubmitDialog();
    String data = fireMessage.getData();
    MoveTableResp moveTableResp = new Gson().fromJson(data, MoveTableResp.class);
    mMoveTableItemList = moveTableResp.getMoveTableItemList();
    if (mMoveTableItemList != null && mMoveTableItemList.size() != 0) {
      int length = mMoveTableItemList.size();
      mMoveTableNames = new String[length];
      //遍历结果，取出名称
      for (int i = 0; i < mMoveTableItemList.size(); i++) {
        String name = mMoveTableItemList.get(i).getTableName();
        mMoveTableNames[i] = name;
      }
      new MaterialDialog.Builder(this).items(mMoveTableNames)
          .title("换桌")
          .content("选择目标桌位")
          .itemColorRes(R.color.colorAccent)
          .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
            @Override public boolean onSelection(MaterialDialog dialog, View view, int which,
                CharSequence text) {
              AppMoveTableItem tableItem = mMoveTableItemList.get(which);
              if (tableItem.isOccupy() && !tableItem.getTableId().equals(mTableId)) {
                ToastUtils.showShort(ModifyBillActivity.this, "暂不支持一桌多单");
                return true;
              } else if (tableItem.isOccupy() && tableItem.getTableId().equals(mTableId)) {
                ToastUtils.showShort(ModifyBillActivity.this, "目标桌位不能为当前桌位");
                return true;
              } else {
                mMoveTableId = tableItem.getTableId();
                mMoveTableName = tableItem.getTableName();
                mTvTableName.setText("移动至" + mMoveTableName);
              }
              return true;
            }
          })
          .negativeText("取消")
          .negativeColor(getResources().getColor(R.color.primary_text))
          .positiveText("确定")
          .show();
    }
  }

  /**
   * 桌台
   */
  @OnClick(R.id.rl_table) public void changeTable(View view) {
    showSubmitDialog();
    reqMoveTableInfo();
  }

  /**
   * 请求移动桌台信息
   */
  private void reqMoveTableInfo() {
    FireMessage fireMessage = new FireMessage();
    fireMessage.setOperateType(FireMessage.MOVE_TABLE_REQ);
    mCurrentTableUUID = UUID.randomUUID();
    fireMessage.setUUID(mCurrentTableUUID);
    String json = new Gson().toJson(fireMessage);

    //发送消息
    if (mChat != null) {
      try {
        mChat.sendMessage(json);
      } catch (SmackException.NotConnectedException e) {
        dismissSubmitDialog();
        e.printStackTrace();
      }
    }
  }


  /**
   * 确认改单
   */
  @OnClick(R.id.btn_confirm) public void modifyBill(View view) {
    App app = (App) App.getContext();
    if (app == null || app.getUser() == null) {
      return;
    }

    String peopleNum = mEtPeopleNum.getText().toString().trim();
    if (peopleNum == null || peopleNum.length() == 0 || peopleNum.length() > 3) {
      ToastUtils.showShort(this, "请正确填写人数");
      return;
    }
    mPeopleNum = Integer.valueOf(peopleNum);
    if (mPeopleNum == 0) {
      ToastUtils.showShort(this, "请正确填写人数");
      return;
    }

    if (!NetUtils.isConnected(ModifyBillActivity.this.getApplicationContext())) {
      ToastUtils.showShort(ModifyBillActivity.this, "网络差，请稍后再试");
      return;
    }

    //自定义备注
    String customRemark = mEtCustomRemark.getText().toString().trim();

    ModifyBillReq modifyBillReq = new ModifyBillReq();
    modifyBillReq.setTableId(mTableId);
    modifyBillReq.setMoveTableId(mMoveTableId);
    modifyBillReq.setOrderId(mOrderId);
    modifyBillReq.setPeopleNum(mPeopleNum);
    modifyBillReq.setRemarks(customRemark);
    modifyBillReq.setWaiterId(app.getUser().getObjectId());
    //促销计划
    if (mPromotioInfo != null){
      modifyBillReq.setPromotioInfoId(mPromotioInfo.getObjectId());
    }
    String data = new Gson().toJson(modifyBillReq);

    FireMessage fireMessage = new FireMessage();
    fireMessage.setOperateType(FireMessage.MODIFY_BILL_REQ);
    fireMessage.setData(data);
    mCurrentModifyUUID = UUID.randomUUID();
    fireMessage.setUUID(mCurrentModifyUUID);

    String json = new Gson().toJson(fireMessage);

    //发送消息
    if (mChat != null) {
      try {
        mChat.sendMessage(json);
      } catch (SmackException.NotConnectedException e) {
        //关闭对话框
        dismissSubmitDialog();
        e.printStackTrace();
      }
    }
    //对话框
    try {
      showSubmitDialog();
    } catch (MaterialDialog.DialogException e) {
      e.printStackTrace();
    }
  }

  /**
   * 显示提交对话框
   */
  private void showSubmitDialog() {
    dialog = new MaterialDialog.Builder(this).title("警告")
        .content("正在发送中")
        .progress(true, 0)
        .progressIndeterminateStyle(true)
        .canceledOnTouchOutside(false)
        .build();
    dialog.show();
  }

  /**
   * 关闭提交对话框
   */
  private void dismissSubmitDialog() {
    if (dialog != null && dialog.isShowing()) {
      dialog.dismiss();
    }
  }

  /**
   * 更换促销计划
   */
  //@formatter:on
  @OnClick(R.id.rl_promotio) public void changePromotioInfo() {
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
   * 删除促销计划
   */
  @OnClick(R.id.tv_promotio_info) public void deletePromotioInfo() {
    mPromotioInfo = null;
    mTvPromotioInfo.setVisibility(View.INVISIBLE);
  }

  /**
   * 退出
   */
  @Override protected void onDestroy() {
    super.onDestroy();
    if (mChat != null && mMessageListener != null) {
      mChat.removeMessageListener(mMessageListener);
    }
    mChat = null;
    ButterKnife.unbind(this);
  }
}
