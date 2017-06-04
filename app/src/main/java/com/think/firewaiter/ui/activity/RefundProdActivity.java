package com.think.firewaiter.ui.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;
import com.think.firewaiter.R;
import com.think.firewaiter.chat.AppDetailsCollection;
import com.think.firewaiter.chat.FireMessage;
import com.think.firewaiter.chat.chatUtils.XMPPConnectUtils;
import com.think.firewaiter.chat.req.RefundProdReq;
import com.think.firewaiter.chat.resp.SimpleResp;
import com.think.firewaiter.common.App;
import com.think.firewaiter.config.Setting;
import com.think.firewaiter.dao.DaoServiceUtil;
import com.think.firewaiter.dao.PxOptReasonDao;
import com.think.firewaiter.module.PxOptReason;
import com.think.firewaiter.ui.base.BaseToolbarActivity;
import com.think.firewaiter.utils.NetUtils;
import com.think.firewaiter.utils.NumberFormatUtils;
import com.think.firewaiter.utils.RegExpUtils;
import com.think.firewaiter.utils.ToastUtils;
import java.util.List;
import java.util.UUID;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;

/**
 * Created by zjq on 2016/5/10.
 * 退菜
 */
public class RefundProdActivity extends BaseToolbarActivity {
  public static final String REFUND_PROD = "RefundProd";
  public static final String ORDER_ID = "OrderId";
  public static final String TABLE_ID = "TableId";

  @Bind(R.id.tv_prod_name) TextView mTvProdName;
  @Bind(R.id.tv_origin_num) TextView mTvOriginNum;
  @Bind(R.id.et_refund_num) EditText mEtRefundNum;
  @Bind(R.id.tv_multiple_origin_num) TextView mTvMultipleOriginNum;
  @Bind(R.id.tv_multiple_refund_num) TextView mTvMultipleRefundNum;
  @Bind(R.id.tv_disc_rate) TextView mTvDiscRate;
  @Bind(R.id.tv_format) TextView mTvFormat;
  @Bind(R.id.tv_method) TextView mTvMethod;
  @Bind(R.id.ll_multiple_unit) LinearLayout mLlMultipleUnit;
  @Bind(R.id.rl_refund_reasons) RelativeLayout mRlRefundReasons;
  @Bind(R.id.tv_refund_reason) TextView mTvRefundReason;
  @Bind(R.id.btn_confirm) Button mBtnConfirm;
  @Bind(R.id.content) RelativeLayout mContentView;
  @Bind(R.id.tv_remarks) TextView mTvRemarks;

  private AppDetailsCollection mDetailsCollection;//详情
  private long mOrderId;
  private int mRefundNum;//退货数量
  private double mRefundMultipleNum;//多单位退货数量

  private int mOriginNum;//原始数量
  private double mOriginMultipleNum;//原始多单位数量

  private MaterialDialog dialog;//提交后显示对话框

  private UUID mCurrentUUID;//当前uuid

  private Chat mChat;

  private String mTableId;//桌台id

  //所有退菜原因
  private List<PxOptReason> mOptReasonList;
  //所选退菜原因
  private PxOptReason mRefundReason;
  private IMChatMessageListener mChatMessageListener;

  @Override protected String provideToolbarTitle() {
    return "退菜";
  }

  @Override protected int provideContentViewId() {
    return R.layout.activity_refund_prod;
  }

  //@formatter:off
  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ButterKnife.bind(this);
    mDetailsCollection = (AppDetailsCollection) getIntent().getExtras().getSerializable(REFUND_PROD);
    mOrderId = getIntent().getExtras().getLong(ORDER_ID);
    mTableId = getIntent().getExtras().getString(TABLE_ID);
    //读取退菜原因
    mOptReasonList = DaoServiceUtil.getOptReasonService()
        .queryBuilder()
        .where(PxOptReasonDao.Properties.DelFlag.eq("0"))
        .where(PxOptReasonDao.Properties.Type.eq(PxOptReason.REFUND_REASON))
        .list();
    //非多单位
    if (mDetailsCollection.getIsMultipleUnit().equals(AppDetailsCollection.IS_TWO_UNIT_FALSE)) {
      mLlMultipleUnit.setVisibility(View.GONE);
    }
    //多单位
    else {
      mTvMultipleOriginNum.setText(NumberFormatUtils.formatFloatNumber(mDetailsCollection.getMultipleNum()) + "");
      mTvMultipleRefundNum.setText(NumberFormatUtils.formatFloatNumber(mDetailsCollection.getMultipleNum()) + "");
      //默认多单位退货量
      mRefundMultipleNum = mDetailsCollection.getMultipleNum();
      //原始多单位数量
      mOriginMultipleNum = mDetailsCollection.getMultipleNum();
    }
    //商品名
    if (mDetailsCollection.getIsGift() != null && mDetailsCollection.getIsGift().equals(AppDetailsCollection.IS_GIFT_TRUE)){
        mTvProdName.setText( mDetailsCollection.getProdName() + "(赠)");
    } else {
        mTvProdName.setText(mDetailsCollection.getProdName());
    }
    //折扣率
    mTvDiscRate.setText(mDetailsCollection.getDiscRate() == 100 ? "无折扣" : "(" + mDetailsCollection.getDiscRate() + "%)");
    //规格
    mTvFormat.setText(mDetailsCollection.getFormatName() == null ? "无" : mDetailsCollection.getFormatName());
    //做法
    mTvMethod.setText(mDetailsCollection.getMethodName() == null ? "无" : mDetailsCollection.getMethodName());
    //数量
    mTvOriginNum.setText(mDetailsCollection.getNum() + "");
    //退货数量
    mEtRefundNum.setText(mDetailsCollection.getNum() + "");
    //默认退货量
    mRefundNum = mDetailsCollection.getNum();
    //原始数量
    mOriginNum = mDetailsCollection.getNum();
    //备注
    mTvRemarks.setText(mDetailsCollection.getRemarks());

    mEtRefundNum.addTextChangedListener(new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s != null && s.length() != 0 && "".equals(s.toString()) == false && ".".toString().equals(s.toString()) == false) {
          Integer num = Integer.valueOf(s.toString());
          if (s.length() >= 4) {
            mRefundNum = 0;
            mEtRefundNum.setText("");
          } else {
            mRefundNum = num;
          }
        } else {
          mRefundNum = 0;
        }
      }

      @Override public void afterTextChanged(Editable s) {

      }
    });

    //获取chat
    if (XMPPConnectUtils.getConnection() != null) {
      ChatManager chatManager = ChatManager.getInstanceFor(XMPPConnectUtils.getConnection());
      if (chatManager != null) {
        mChat = chatManager.createChat("admin" + ((App) (App.getContext())).getUser().getCompanyCode() + "@" + Setting.SERVER_NAME);
        getChatMessages();
      }
    }
  }

  @OnClick(R.id.tv_multiple_refund_num) public void multipleRefundNum() {
    new MaterialDialog.Builder(this)
        .content("多单位退货数量")
        .inputType(InputType.TYPE_NUMBER_FLAG_DECIMAL)
        .positiveText("确定")
        .negativeText("取消")
        .negativeColor(this.getResources().getColor(R.color.primary_text))
        .alwaysCallInputCallback()
        .input("数量", "", false, new MaterialDialog.InputCallback() {
          @Override public void onInput(MaterialDialog dialog, CharSequence input) {
            if (input.toString().trim() != null && !input.toString().toString().equals("") && input.toString().length() > 4) {
              ToastUtils.showShort(RefundProdActivity.this, "输入过长!");
              dialog.getInputEditText().setText("");
              return;
            }

            if (input.toString() == null || input.toString().trim().equals("")
                || !RegExpUtils.matchFloatNum(input.toString())
                || Double.valueOf(input.toString().trim()).doubleValue() <= 0) {
              dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
            } else {
              dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
            }
          }
        })
        .onPositive(new MaterialDialog.SingleButtonCallback() {
          @Override public void onClick(MaterialDialog dialog, DialogAction which) {
            mRefundMultipleNum =
                Double.valueOf(dialog.getInputEditText().getText().toString().trim());
            mTvMultipleRefundNum.setText(mRefundMultipleNum + "");
          }
        })
        .show();
  }

  @OnClick(R.id.rl_refund_reasons) public void refundReason() {
    if (mOptReasonList == null || mOptReasonList.size() == 0) {
      ToastUtils.showShort(this, "请在后台设置操作原因");
      return;
    }
    String[] reasonNames = new String[mOptReasonList.size()];
    for (int i = 0; i < mOptReasonList.size(); i++) {
      reasonNames[i] = mOptReasonList.get(i).getName();
    }
    new MaterialDialog.Builder(this).title("选择退菜原因")
        .items(reasonNames)
        .itemsCallback(new MaterialDialog.ListCallback() {
          @Override
          public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
            mRefundReason = mOptReasonList.get(which);
            mTvRefundReason.setText(mRefundReason.getName());
          }
        })
        .show();
  }

  /**
   * 接收消息
   */
  private MaterialDialog mDialog;

  private void getChatMessages() {
    mChatMessageListener = new IMChatMessageListener();
    mChat.addMessageListener(mChatMessageListener);
  }
  class IMChatMessageListener implements ChatMessageListener{
      @Override public void processMessage(Chat chat, Message message) {
        String body = message.getBody();
        final FireMessage fireMessage = new Gson().fromJson(body, FireMessage.class);
        if (fireMessage == null || fireMessage.getOperateType() == 0) {
          return;
        }
        //退菜回执
        if (fireMessage.getOperateType() == FireMessage.REFUND_PROD_RESP) {
          runOnUiThread(new Runnable() {
            @Override public void run() {
              //关闭对话框
              if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
              }
              //判断是否是之前的离线消息
              if (fireMessage.getUUID().equals(mCurrentUUID)) {
                //获取回执信息
                String data = fireMessage.getData();
                SimpleResp refundProdResp = new Gson().fromJson(data, SimpleResp.class);
                String des = refundProdResp.getDes();
                //确认对话框
                mDialog = new MaterialDialog.Builder(RefundProdActivity.this).title("警告")
                    .content(des)
                    .positiveText("确认")
                    .negativeColor(getResources().getColor(R.color.primary_text))
                    .canceledOnTouchOutside(false)
                    .cancelable(false)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                      @Override public void onClick(MaterialDialog dialog, DialogAction which) {
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("Refresh", true);
                        openActivity(OccupyTableActivity.class, bundle);
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
   * 确认退菜
   */
  @OnClick(R.id.btn_confirm) public void confirmRefund(View view) {
    if (mDetailsCollection.getIsMultipleUnit().equals(AppDetailsCollection.IS_TWO_UNIT_TURE)) {
      if (mRefundNum > mOriginNum || mRefundMultipleNum > mOriginMultipleNum) {
        ToastUtils.showShort(RefundProdActivity.this, "错误，数量超出限制");
        return;
      }
      if (mRefundNum == mOriginNum && mOriginMultipleNum != mRefundMultipleNum) {
        ToastUtils.showShort(RefundProdActivity.this, "错误，不能只退全部点菜单位数量");
        return;
      }
      if (mRefundMultipleNum == mOriginMultipleNum && mRefundNum != mOriginNum) {
        ToastUtils.showShort(RefundProdActivity.this, "错误，不能只退全部结账单位数量");
        return;
      }
      if (mRefundNum == 0 && mRefundMultipleNum == 0) {
        ToastUtils.showShort(RefundProdActivity.this, "错误，数量不能都为0");
        return;
      }
    } else {
      if (mRefundNum > mOriginNum || mRefundNum == 0) {
        ToastUtils.showShort(RefundProdActivity.this, "数量填写错误");
        return;
      }
    }
    //禁止点击
    mBtnConfirm.setClickable(false);
    new MaterialDialog.Builder(this).title("警告")
        .content("确认退菜?")
        .positiveText("确认")
        .negativeText("取消")
        .negativeColor(getResources().getColor(R.color.primary_text))
        .onPositive(new MaterialDialog.SingleButtonCallback() {
          @Override public void onClick(MaterialDialog dialog, DialogAction which) {
            //退菜
            refundProd();
            //恢复点击
            mBtnConfirm.setClickable(true);
          }
        })
        .onNegative(new MaterialDialog.SingleButtonCallback() {
          @Override public void onClick(MaterialDialog dialog, DialogAction which) {
            //恢复点击
            mBtnConfirm.setClickable(true);
          }
        })
        .canceledOnTouchOutside(false)
        .cancelable(false)
        .show();
  }

  /**
   * 提交退菜
   */
  private void refundProd() {
    if (!NetUtils.isConnected(this.getApplicationContext())) {
      ToastUtils.showShort(this, "网络差，请稍后再试");
      return;
    }
    mCurrentUUID = UUID.randomUUID();
    FireMessage fireMessage = new FireMessage();
    fireMessage.setOperateType(FireMessage.REFUND_PROD_REQ);
    fireMessage.setUUID(mCurrentUUID);
    RefundProdReq refundProdReq = new RefundProdReq();
    refundProdReq.setOrderId(mOrderId);
    refundProdReq.setRefundNum(mRefundNum);
    refundProdReq.setTableId(mTableId);
    refundProdReq.setObjId(mDetailsCollection.getObjectId());


    App app = (App) App.getContext();
    if (app == null || app.getUser() == null) {
      return;
    }
    refundProdReq.setWaiterId(app.getUser().getObjectId());
    if (mRefundReason != null) {
      refundProdReq.setRefundReasonObjId(mRefundReason.getObjectId());
    }
    if (mDetailsCollection.getIsMultipleUnit().equals(AppDetailsCollection.IS_TWO_UNIT_TURE)) {
      refundProdReq.setRefundMultipleNum(mRefundMultipleNum);
    }
    fireMessage.setData(new Gson().toJson(refundProdReq));
    String json = new Gson().toJson(fireMessage);

    Logger.json(fireMessage.getData());
    //发送消息
    if (mChat != null) {
      try {
        mChat.sendMessage(json);
      } catch (SmackException.NotConnectedException e) {
        //关闭对话框
        if (dialog != null) {
          dialog.dismiss();
        }
        e.printStackTrace();
      }
    }
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
   * OnDestroy
   */
  @Override protected void onDestroy() {
    super.onDestroy();
    if (mChat != null && mChatMessageListener != null){
      mChat.removeMessageListener(mChatMessageListener);
    }
    mChat = null;
    ButterKnife.unbind(this);
  }
}
