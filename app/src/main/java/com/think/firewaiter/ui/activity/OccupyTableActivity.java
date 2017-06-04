package com.think.firewaiter.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.mingle.entity.MenuEntity;
import com.mingle.sweetpick.DimEffect;
import com.mingle.sweetpick.RecyclerViewDelegate;
import com.mingle.sweetpick.SweetSheet;
import com.think.firewaiter.R;
import com.think.firewaiter.adapter.DetailListAdapter;
import com.think.firewaiter.chat.AppDetailsCollection;
import com.think.firewaiter.chat.FireMessage;
import com.think.firewaiter.chat.chatUtils.XMPPConnectUtils;
import com.think.firewaiter.chat.req.CancelOrderReq;
import com.think.firewaiter.chat.req.ServingProdReq;
import com.think.firewaiter.chat.resp.OrderInfoResp;
import com.think.firewaiter.chat.resp.SimpleResp;
import com.think.firewaiter.common.App;
import com.think.firewaiter.config.Setting;
import com.think.firewaiter.dao.DaoServiceUtil;
import com.think.firewaiter.dao.PxPromotioInfoDao;
import com.think.firewaiter.module.PxPromotioInfo;
import com.think.firewaiter.ui.base.BaseToolbarActivity;
import com.think.firewaiter.utils.NetUtils;
import com.think.firewaiter.utils.ToastUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;

/**
 * Created by zjq on 2016/5/4.
 */
public class OccupyTableActivity extends BaseToolbarActivity implements DetailListAdapter.OnDetailClickListener {

  @Bind(R.id.content) RelativeLayout mContentView;
  @Bind(R.id.tv_table_name) TextView mTvTableName;
  @Bind(R.id.tv_people_num) TextView mTvPeopleNum;
  @Bind(R.id.tv_order_no) TextView mTvOrderNo;
  @Bind(R.id.tv_start_time) TextView mTvStartTime;
  @Bind(R.id.rcv_details) RecyclerView mRcvDetails;
  @Bind(R.id.swipe_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
  @Bind(R.id.tv_remarks) TextView mTvRemarks;
  @Bind(R.id.tv_promotio_info) TextView mTvPromotioInfo;//促销计划

  private List<AppDetailsCollection> mDetailsCollectionList;//Data
  private DetailListAdapter mDetailListAdapter;//Adapter
  private String mTableId;//桌台id
  private String mTableName;//桌台名
  private long mOrderId;//订单id
  private long mPeopleNum;//人数
  private String mRemarks;//备注
  private PxPromotioInfo mPromotioInfo;//促销计划

  private SweetSheet mSweetSheet;
  private MaterialDialog dialog;//提交后显示对话框

  private UUID mCurrentOrderUUID;//当前订单uuid
  private UUID mCurrentCancelUUID;//当前撤单uuid
  private UUID mCurrentServingTagUUID;//当前标记uuid

  private Chat mChat;

  private long mLastMenuClickTime;
  private IMChatMessageListener mChatMessageListener;

  @Override protected String provideToolbarTitle() {
    return "已下单的菜";
  }

  @Override protected int provideContentViewId() {
    return R.layout.activity_occupy_table;
  }

  //@formatter:off
  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ButterKnife.bind(this);
    mTableId = getIntent().getStringExtra("TableId");
    //初始化下拉刷新
    initSwipeRefresh();
    //初始化Rcv
    initRcv();
    //设置BottomSheet
    initBottomSheet();

    //获取chat
    if (XMPPConnectUtils.getConnection() != null) {
      ChatManager chatManager = ChatManager.getInstanceFor(XMPPConnectUtils.getConnection());
      if (chatManager != null) {
        mChat = chatManager.createChat("admin" + ((App) (App.getContext())).getUser().getCompanyCode() + "@" + Setting.SERVER_NAME);
        getChatMessages();
      }
    }

    //请求订单信息
    reqOrderInfo();
  }

  /**
   * 获取消息
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
        //订单信息
        if (fireMessage.getOperateType() == FireMessage.ORDER_INFO_RESP) {
          runOnUiThread(new Runnable() {
            @Override public void run() {
              if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);
              }
              //判断是否是之前的离线消息
              if (!fireMessage.getUUID().equals(mCurrentOrderUUID)) {
                mDetailsCollectionList = new ArrayList<>();
                mDetailListAdapter.setData(mDetailsCollectionList);
              } else {
                String data = fireMessage.getData();
                OrderInfoResp orderInfoResp = new Gson().fromJson(data, OrderInfoResp.class);
                if (orderInfoResp.getResult() == orderInfoResp.SUCCESS) {
                  mDetailsCollectionList = orderInfoResp.getDetailsList();
                  mOrderId = orderInfoResp.getOrderId();//订单id
                  mTableName = orderInfoResp.getTableName();//桌名
                  mPeopleNum = orderInfoResp.getPeopleNum();//人数
                  if (orderInfoResp.getRemarks() != null){
                    mRemarks = orderInfoResp.getRemarks();//备注
                  } else {
                    mRemarks = "";
                  }
                  //促销计划
                  if (orderInfoResp.getPromotioInfoId() != null){
                    mPromotioInfo = DaoServiceUtil.getPromotioService()
                        .queryBuilder()
                        .where(PxPromotioInfoDao.Properties.ObjectId.eq(orderInfoResp.getPromotioInfoId()))
                        .unique();
                    mTvPromotioInfo.setText(mPromotioInfo == null ? "" : mPromotioInfo.getName());
                  }
                  mTvTableName.setText(mTableName);//桌名
                  mTvPeopleNum.setText(mPeopleNum + "");//人数
                  mTvOrderNo.setText("No." + orderInfoResp.getOrderReqNum());//编号
                  mTvStartTime.setText(orderInfoResp.getStartTime());//开始时间
                  mDetailListAdapter.setData(mDetailsCollectionList);//Details
                  mTvRemarks.setText(mRemarks);
                }
              }
            }
          });
        }
        //撤单信息
        if (fireMessage.getOperateType() == FireMessage.CANCEL_ORDER_RESP) {
          runOnUiThread(new Runnable() {
            @Override public void run() {
              //判断是否是之前的离线消息
              if (!fireMessage.getUUID().equals(mCurrentCancelUUID)) {
                ToastUtils.showShort(OccupyTableActivity.this, "网络错误");
              } else {
                //关闭对话框
                if (dialog != null && dialog.isShowing()) {
                  dialog.dismiss();
                }
                String data = fireMessage.getData();
                SimpleResp cancelOrderResp = new Gson().fromJson(data, SimpleResp.class);
                int result = cancelOrderResp.getResult();
                String des = cancelOrderResp.getDes();
                //确认对话框
                mDialog = new MaterialDialog.Builder(OccupyTableActivity.this).title("警告")
                    .content(des)
                    .positiveText("确认")
                    .negativeColor(getResources().getColor(R.color.primary_text))
                    .canceledOnTouchOutside(false)
                    .cancelable(false)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                      @Override public void onClick(MaterialDialog dialog, DialogAction which) {
                        //跳转
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
        //标记信息
        if (fireMessage.getOperateType() == FireMessage.SERVING_PROD_RESP) {
          runOnUiThread(new Runnable() {
            @Override public void run() {
              //判断是否是之前的离线消息
              if (!fireMessage.getUUID().equals(mCurrentServingTagUUID)) {
                ToastUtils.showShort(OccupyTableActivity.this, "网络错误");
              } else {
                //关闭对话框
                if (dialog != null && dialog.isShowing()) {
                  dialog.dismiss();
                }
                String data = fireMessage.getData();
                SimpleResp cancelOrderResp = new Gson().fromJson(data, SimpleResp.class);
                int result = cancelOrderResp.getResult();
                String des = cancelOrderResp.getDes();
                //确认对话框
                mDialog = new MaterialDialog.Builder(OccupyTableActivity.this).title("警告")
                    .content(des)
                    .positiveText("确认")
                    .negativeColor(getResources().getColor(R.color.primary_text))
                    .canceledOnTouchOutside(false)
                    .cancelable(false)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                      @Override public void onClick(MaterialDialog dialog, DialogAction which) {
                        reqOrderInfo();
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
   * 初始化下拉刷新
   */
  private void initSwipeRefresh() {
    if (mSwipeRefreshLayout != null) {
      mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
        @Override public void onRefresh() {
          reqOrderInfo();
        }
      });
    }
  }

  /**
   * 初始化Rcv
   */
  //@formatter:off
  private void initRcv() {
    LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
    mDetailsCollectionList= new ArrayList<>();
    mDetailListAdapter = new DetailListAdapter(this, mDetailsCollectionList);
    mDetailListAdapter.setOnDetailClickListener(this);
    mRcvDetails.setHasFixedSize(true);
    mRcvDetails.setLayoutManager(layoutManager);
    mRcvDetails.setAdapter(mDetailListAdapter);
  }

  /**
   * 初始化BottomSheet
   */
  private void initBottomSheet() {
    mSweetSheet = new SweetSheet(mContentView);

    mSweetSheet.setMenuList(R.menu.occupy_table_sheet);
    mSweetSheet.setDelegate(new RecyclerViewDelegate(true));
    mSweetSheet.setBackgroundEffect(new DimEffect(4));
    mSweetSheet.setBackgroundClickEnable(false);
    mSweetSheet.setOnMenuItemClickListener(new SweetSheet.OnMenuItemClickListener() {
      @Override public boolean onItemClick(int position, MenuEntity menuEntity) {
        return menuClickEvent(position, menuEntity);
      }
    });
  }

  private boolean menuClickEvent(int position, MenuEntity menuEntity) {
    switch (position) {
      case 0://加菜
        //跳转
        Bundle addProdBundle = new Bundle();
        addProdBundle.putBoolean("IsInitOrder", false);
        addProdBundle.putLong("OrderId", mOrderId);
        addProdBundle.putString("TableId", mTableId);
        addProdBundle.putLong("PeopleNum", mPeopleNum);
        addProdBundle.putString("Remarks",mRemarks);
        if (mPromotioInfo != null){
          addProdBundle.putSerializable("PromotioInfo",mPromotioInfo);
        }
        openActivity(BillActivity.class, addProdBundle);
        break;
      case 1://改单
        Bundle bundle = new Bundle();
        bundle.putString("TableId", mTableId);
        bundle.putLong("OrderId", mOrderId);
        bundle.putString("TableName", mTableName);
        bundle.putLong("PeopleNum", mPeopleNum);
        bundle.putString("Remarks",mRemarks);
        if (mPromotioInfo != null){
          bundle.putSerializable("PromotioInfo",mPromotioInfo);
        }
        openActivity(ModifyBillActivity.class, bundle);
        break;
      case 2://撤单
        new MaterialDialog.Builder(this).title("警告")
            .content("确认撤单")
            .positiveText("确认")
            .negativeText("取消")
            .negativeColor(getResources().getColor(R.color.primary_text))
            .onPositive(new MaterialDialog.SingleButtonCallback() {
              @Override public void onClick(MaterialDialog dialog, DialogAction which) {
                if (!NetUtils.isConnected(OccupyTableActivity.this.getApplicationContext())) {
                  ToastUtils.showShort(OccupyTableActivity.this, "网络差，请稍后再试");
                  return;
                }
                App app = (App) App.getContext();
                if (app == null || app.getUser() == null) {
                  return;
                }
                CancelOrderReq cancelOrderReq = new CancelOrderReq();
                cancelOrderReq.setOrderId(mOrderId);
                cancelOrderReq.setWaiterId(app.getUser().getObjectId());
                String data = new Gson().toJson(cancelOrderReq);
                FireMessage fireMessage = new FireMessage();
                fireMessage.setOperateType(FireMessage.CANCEL_ORDER_REQ);
                fireMessage.setData(data);
                mCurrentCancelUUID = UUID.randomUUID();
                fireMessage.setUUID(mCurrentCancelUUID);
                String json = new Gson().toJson(fireMessage);

                //发送消息
                if (mChat!=null){
                  try {
                    mChat.sendMessage(json);
                  } catch (SmackException.NotConnectedException e) {
                    if (OccupyTableActivity.this.dialog != null) {
                      OccupyTableActivity.this.dialog.dismiss();
                    }
                    e.printStackTrace();
                  }
                }
                //显示提交对话框
                try {
                  showSubmitDialog();
                }catch (MaterialDialog.DialogException e){
                  e.printStackTrace();
                }
              }
            })
            .show();
        break;
      case 3://催单

        break;
    }
    return true;
  }


  /**
   * 请求订单信息
   */
  private void reqOrderInfo() {
    if (!NetUtils.isConnected(this.getApplicationContext())) {
      ToastUtils.showShort(this, "网络差，请稍后再试");
      if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
        mSwipeRefreshLayout.setRefreshing(false);
      }
      return;
    }
    mSwipeRefreshLayout.setRefreshing(true);
    FireMessage fireMessage = new FireMessage();
    fireMessage.setOperateType(FireMessage.ORDER_INFO_REQ);
    fireMessage.setTableId(mTableId);
    mCurrentOrderUUID = UUID.randomUUID();
    fireMessage.setUUID(mCurrentOrderUUID);

    String json = new Gson().toJson(fireMessage);

    //发送消息
    if (mChat != null) {
      try {
        mChat.sendMessage(json);
      } catch (SmackException.NotConnectedException e) {
        e.printStackTrace();
      }
    }

  }

  /**
   * OptionMenu
   */
  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.occupy_table_menu, menu);
    MenuItem menuPopup = menu.findItem(R.id.action_popup);
    menuPopup.setActionView(R.layout.layout_menu_popup);
    menuPopup.getActionView().setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (System.currentTimeMillis() - mLastMenuClickTime < 1000) {
          Toast.makeText(OccupyTableActivity.this, "请稍后点击", Toast.LENGTH_SHORT).show();
          return;
        }
        mLastMenuClickTime = System.currentTimeMillis();
        showSheet();
      }
    });
    return true;
  }

  /**
   * 显示Sheet
   */
  private void showSheet() {
    if (mSweetSheet.isShow()) {
      mSweetSheet.dismiss();
    }
    mSweetSheet.toggle();
  }

  /**
   * 长按点击
   */
  //@formatter:on
  @Override public void onItemLongClick(final int pos) {
    final AppDetailsCollection appDetailsCollection = mDetailsCollectionList.get(pos);
    if (appDetailsCollection.getOrderStatus().equals(AppDetailsCollection.ORDER_STATUS_REFUND)) {
      return;
    }
    if (appDetailsCollection.getIsComboDetails().equals(AppDetailsCollection.IS_COMBO_TRUE)) {
      ToastUtils.showShort(this, "暂不支持操作套餐");
      return;
    }
    //标记未上和退菜
    if (appDetailsCollection.isServing()) {
      new MaterialDialog.Builder(this).title("选择操作")
          .items(R.array.refund_unserving_operate)
          .itemsCallbackSingleChoice(1, new MaterialDialog.ListCallbackSingleChoice() {
            @Override public boolean onSelection(MaterialDialog dialog, View view, int which,
                CharSequence text) {
              //退菜
              if (which == 0) {
                App app = (App) App.getContext();
                if (app != null && app.getUser() != null) {
                  Intent intent = new Intent(OccupyTableActivity.this, RefundProdActivity.class);
                  intent.putExtra(RefundProdActivity.REFUND_PROD, appDetailsCollection);
                  intent.putExtra(RefundProdActivity.ORDER_ID, mOrderId);
                  intent.putExtra(RefundProdActivity.TABLE_ID, mTableId);
                  startActivity(intent);
                }
              }
              //标记
              else {
                changeServingTag(appDetailsCollection, false);
              }
              return true;
            }
          })
          .positiveText("确定")
          .show();
    }
    //标记已上和退菜
    else {
      new MaterialDialog.Builder(this).title("选择操作")
          .items(R.array.refund_serving_operate)
          .itemsCallbackSingleChoice(1, new MaterialDialog.ListCallbackSingleChoice() {
            @Override public boolean onSelection(MaterialDialog dialog, View view, int which,
                CharSequence text) {
              //退菜
              if (which == 0) {
                App app = (App) App.getContext();
                if (app != null && app.getUser() != null) {
                  Intent intent = new Intent(OccupyTableActivity.this, RefundProdActivity.class);
                  intent.putExtra(RefundProdActivity.REFUND_PROD, appDetailsCollection);
                  intent.putExtra(RefundProdActivity.ORDER_ID, mOrderId);
                  intent.putExtra(RefundProdActivity.TABLE_ID, mTableId);
                  startActivity(intent);
                }
              }
              //标记
              else {
                changeServingTag(appDetailsCollection, true);
              }
              return true;
            }
          })
          .positiveText("确定")
          .show();
    }
  }

  /**
   * 改变已上/未上标记
   */
  private void changeServingTag(AppDetailsCollection appDetailsCollection, boolean servingTag) {
    ServingProdReq req = new ServingProdReq();
    req.setTableId(mTableId);
    req.setOrderId(mOrderId);
    req.setObjId(appDetailsCollection.getObjectId());

    if (servingTag == true) {
      req.setIsServing(1);
    } else {
      req.setIsServing(0);
    }

    String data = new Gson().toJson(req);
    FireMessage fireMessage = new FireMessage();
    fireMessage.setOperateType(FireMessage.SERVING_PROD_REQ);
    fireMessage.setData(data);
    mCurrentServingTagUUID = UUID.randomUUID();
    fireMessage.setUUID(mCurrentServingTagUUID);
    String json = new Gson().toJson(fireMessage);
    //发送消息
    if (mChat != null) {
      try {
        mChat.sendMessage(json);
      } catch (SmackException.NotConnectedException e) {
        if (dialog != null) {
          dialog.dismiss();
        }
        e.printStackTrace();
      }
    }
    //显示提交对话框
    try {
      showSubmitDialog();
    } catch (MaterialDialog.DialogException e) {
      e.printStackTrace();
    }
  }

  /**
   * 退菜确认后用于刷新页面
   */
  @Override protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    boolean refresh = intent.getExtras().getBoolean("Refresh");
    if (refresh) {
      reqOrderInfo();
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
    if (mChat != null && mChatMessageListener != null) {
      mChat.removeMessageListener(mChatMessageListener);
    }
    mChat = null;
    ButterKnife.unbind(this);
  }
}
