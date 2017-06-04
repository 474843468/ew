package com.think.firewaiter.ui.activity;

import android.content.Intent;
import android.os.Bundle;
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
import com.think.firewaiter.adapter.ProdCartAdapter;
import com.think.firewaiter.chat.AppConfirmOrderDetails;
import com.think.firewaiter.chat.FireMessage;
import com.think.firewaiter.chat.chatUtils.XMPPConnectUtils;
import com.think.firewaiter.chat.req.AddProdReq;
import com.think.firewaiter.chat.req.ConfirmOrderReq;
import com.think.firewaiter.chat.resp.SimpleResp;
import com.think.firewaiter.common.App;
import com.think.firewaiter.config.Setting;
import com.think.firewaiter.dao.DaoServiceUtil;
import com.think.firewaiter.dao.PxTableInfoDao;
import com.think.firewaiter.dao.ShoppingCartDao;
import com.think.firewaiter.event.RefreshCartEvent;
import com.think.firewaiter.module.PxProductInfo;
import com.think.firewaiter.module.PxPromotioInfo;
import com.think.firewaiter.module.PxTableInfo;
import com.think.firewaiter.module.ShoppingCart;
import com.think.firewaiter.ui.base.BaseToolbarActivity;
import com.think.firewaiter.utils.NetUtils;
import com.think.firewaiter.utils.ToastUtils;
import de.greenrobot.dao.query.Join;
import de.greenrobot.dao.query.QueryBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;

/**
 * Created by zjq on 2016/5/4.
 */
public class BillCartActivity extends BaseToolbarActivity
    implements ProdCartAdapter.OnCartProdClickListener {
  @Bind(R.id.content) RelativeLayout mContentView;
  @Bind(R.id.tv_table_name) TextView mTvTableName;
  @Bind(R.id.tv_people_num) TextView mTvPeopleNum;
  @Bind(R.id.rcv) RecyclerView mRcv;
  @Bind(R.id.tv_details_total) TextView mTvDetailsTotal;

  private PxTableInfo mTableInfo;
  private String mTableInfoId;
  private int mPeopleNum;
  private PxPromotioInfo mPromotioInfo;

  private ProdCartAdapter mCartAdapter;
  private List<ShoppingCart> mShoppingCartList;

  private SweetSheet mSweetSheet;

  private MaterialDialog dialog;//提交后显示对话框

  private boolean isInitOrder;//是否为初始化下单
  private long mOrderId;//订单id
  private String mTableId;//桌台id

  private UUID mCurrentOrderUUID;//当前下单uuid
  private UUID mCurrentAddProdUUID;//当前加菜uuid
  private Chat mChat;
  private long mLastMenuClickTime;
  private String mRemarks;//备注
  private IMChatMessageListener mChatMessageListener;

  @Override protected String provideToolbarTitle() {
    return "下单信息";
  }

  @Override protected int provideContentViewId() {
    return R.layout.activity_bill_cart;
  }

  //@formatter:on
  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ButterKnife.bind(this);
    EventBus.getDefault().register(this);
    //获取基础信息
    Bundle bundle = getIntent().getExtras();
    isInitOrder = bundle.getBoolean("IsInitOrder");
    mTableInfoId = bundle.getString("TableInfoId");
    mPeopleNum = bundle.getInt("PeopleNum");
    mTableInfo = DaoServiceUtil.getTableInfoService()
        .queryBuilder()
        .where(PxTableInfoDao.Properties.ObjectId.eq(mTableInfoId))
        .unique();
    mRemarks = getIntent().getStringExtra("Remarks");
    if (!isInitOrder) {
      mOrderId = bundle.getLong("OrderId");
    }

    //促销计划
    mPromotioInfo = (PxPromotioInfo) bundle.getSerializable("PromotioInfo");
    //初始化标题
    initTitle();
    //设置Rcv
    initRcv();
    //设置BottomSheet
    initBottomSheet();

    //获取chat
    if (XMPPConnectUtils.getConnection() != null) {
      ChatManager chatManager = ChatManager.getInstanceFor(XMPPConnectUtils.getConnection());
      if (chatManager != null) {
        mChat = chatManager.createChat(
            "admin" + ((App) (App.getContext())).getUser().getCompanyCode() + "@"
                + Setting.SERVER_NAME);
        getChatMessages();
      }
    }
  }

  /**
   * 获取消息
   */
  private MaterialDialog mDialog;

  private void getChatMessages() {
    mChatMessageListener = new IMChatMessageListener();
    mChat.addMessageListener(mChatMessageListener);
  }

  class IMChatMessageListener implements ChatMessageListener {
    @Override public void processMessage(Chat chat, Message message) {
      String body = message.getBody();
      final FireMessage fireMessage = new Gson().fromJson(body, FireMessage.class);
      if (fireMessage == null || fireMessage.getOperateType() == 0) {
        return;
      }
      //下单回执
      if (fireMessage.getOperateType() == FireMessage.CONFIRM_ORDER_RESP) {
        runOnUiThread(new Runnable() {
          @Override public void run() {
            //判断是否是之前的离线消息
            if (!fireMessage.getUUID().equals(mCurrentOrderUUID)) {
              ToastUtils.showShort(BillCartActivity.this, "网络错误");
            } else {
              //关闭对话框
              if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
              }
              //获取回执信息
              String data = fireMessage.getData();
              SimpleResp confirmOrderResp = new Gson().fromJson(data, SimpleResp.class);
              final int result = confirmOrderResp.getResult();
              if (result == SimpleResp.SUCCESS) {
                QueryBuilder<ShoppingCart> queryBuilder =
                    DaoServiceUtil.getShoppingCartService().queryBuilder();
                Join<ShoppingCart, PxTableInfo> join =
                    queryBuilder.join(ShoppingCartDao.Properties.TableId, PxTableInfo.class);
                join.where(PxTableInfoDao.Properties.ObjectId.eq(mTableInfo.getObjectId()));
                List<ShoppingCart> shoppingCartList = queryBuilder.list();
                if (shoppingCartList != null && shoppingCartList.size() != 0) {
                  DaoServiceUtil.getShoppingCartService().delete(shoppingCartList);
                }
              }
              String des = confirmOrderResp.getDes();
              //确认对话框
              mDialog = new MaterialDialog.Builder(BillCartActivity.this).title("警告")
                  .content(des)
                  .positiveText("确认")
                  .negativeColor(getResources().getColor(R.color.primary_text))
                  .canceledOnTouchOutside(false)
                  .cancelable(false)
                  .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override public void onClick(MaterialDialog dialog, DialogAction which) {
                      dialog.dismiss();
                      if (result == SimpleResp.SUCCESS) {
                        openActivity(MainActivity.class);
                      }
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
      //加菜回执
      if (fireMessage.getOperateType() == FireMessage.ADD_PROD_RESP) {
        runOnUiThread(new Runnable() {
          @Override public void run() {
            //判断是否是之前的离线消息
            if (!fireMessage.getUUID().equals(mCurrentAddProdUUID)) {
              ToastUtils.showShort(BillCartActivity.this, "网络错误");
            } else {
              //关闭对话框
              if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
              }
              //获取回执信息
              String data = fireMessage.getData();
              SimpleResp simpleResp = new Gson().fromJson(data, SimpleResp.class);
              final int result = simpleResp.getResult();
              String des = simpleResp.getDes();
              //确认对话框
              mDialog = new MaterialDialog.Builder(BillCartActivity.this).title("警告")
                  .content(des)
                  .positiveText("确认")
                  .negativeColor(getResources().getColor(R.color.primary_text))
                  .canceledOnTouchOutside(false)
                  .cancelable(false)
                  .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override public void onClick(MaterialDialog dialog, DialogAction which) {
                      dialog.dismiss();
                      if (result == SimpleResp.SUCCESS) {
                        openActivity(MainActivity.class);
                      }
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
   * 初始化标题信息
   */
  private void initTitle() {
    mTvTableName.setText(mTableInfo.getName());
    mTvPeopleNum.setText(mPeopleNum + "");
  }

  /**
   * 设置Rcv
   */
  //@formatter:off
  private void initRcv() {
    if (mTableInfo == null) return;
    mShoppingCartList = DaoServiceUtil.getShoppingCartService().queryBuilder().where(ShoppingCartDao.Properties.TableId.eq(mTableInfo.getId())).list();
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
   * 设置BottomSheet
   */
  private void initBottomSheet() {
    mSweetSheet = new SweetSheet(mContentView);

    mSweetSheet.setMenuList(R.menu.bill_cart_sheet);
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
      case 0://清空
        //对话框
        new MaterialDialog.Builder(this).title("警告")
            .content("确认清空?")
            .positiveText("确认")
            .negativeText("取消")
            .negativeColor(getResources().getColor(R.color.primary_text))
            .onPositive(new MaterialDialog.SingleButtonCallback() {
              @Override public void onClick(MaterialDialog dialog, DialogAction which) {
                clearShoppingCart();
              }
            })
            .show();
        break;
      case 1://确认提交
        //对话框
        new MaterialDialog.Builder(this).title("警告")
            .content("确认提交?")
            .positiveText("确认")
            .negativeText("取消")
            .negativeColor(getResources().getColor(R.color.primary_text))
            .onPositive(new MaterialDialog.SingleButtonCallback() {
              @Override public void onClick(MaterialDialog dialog, DialogAction which) {
                if (mShoppingCartList == null || mShoppingCartList.size() == 0) {
                  ToastUtils.showShort(BillCartActivity.this, "请添加商品");
                  return;
                }
                if (isInitOrder) {
                  reqConfirmOrder();
                } else {
                  reqAddProd();
                }
              }
            })
            .show();
        break;
    }
    return true;
  }

  /**
   * 清空购物车
   */
  private void clearShoppingCart() {
    mShoppingCartList = DaoServiceUtil.getShoppingCartService()
        .queryBuilder()
        .where(ShoppingCartDao.Properties.TableId.eq(mTableInfo.getId()))
        .list();
    DaoServiceUtil.getShoppingCartService().delete(mShoppingCartList);
    mShoppingCartList = null;
    mCartAdapter.setData(new ArrayList<ShoppingCart>());
    mTvDetailsTotal.setText(0 + "");
  }

  /**
   * 确认下单
   */
  private void reqConfirmOrder() {
    if (!NetUtils.isConnected(this.getApplicationContext())) {
      ToastUtils.showShort(this, "网络差，请稍后再试");
      return;
    }
    FireMessage fireMessage = new FireMessage();
    fireMessage.setTableId(mTableInfoId);
    fireMessage.setOperateType(FireMessage.CONFIRM_ORDER_REQ);
    List<AppConfirmOrderDetails> confirmOrderDetailsList = new ArrayList<AppConfirmOrderDetails>();
    for (ShoppingCart shoppingCart : mShoppingCartList) {
      AppConfirmOrderDetails confirmOrderDetails = new AppConfirmOrderDetails();
      //商品id
      confirmOrderDetails.setProdId(shoppingCart.getDbProd().getObjectId());
      //规格id
      if (shoppingCart.getDbFormat() != null) {
        confirmOrderDetails.setFormatId(shoppingCart.getDbFormat().getObjectId());
      }
      //做法id
      if (shoppingCart.getDbMethod() != null) {
        confirmOrderDetails.setMethodId(shoppingCart.getDbMethod().getObjectId());
      }
      //数量
      confirmOrderDetails.setNum(shoppingCart.getNum().intValue());
      if (shoppingCart.getDbProd().getMultipleUnit().equals(PxProductInfo.IS_TWO_UNIT_TURE)) {
        //多单位数量
        confirmOrderDetails.setMultNum(shoppingCart.getMultipleUnitNum());
      }
      //延迟
      if (shoppingCart.getIsDelay() == true) {
        confirmOrderDetails.setIsDelay(1);
      } else {
        confirmOrderDetails.setIsDelay(0);
      }
      //备注
      confirmOrderDetails.setRemarks(shoppingCart.getRemarks());
      //添加
      confirmOrderDetailsList.add(confirmOrderDetails);
    }
    ConfirmOrderReq req = new ConfirmOrderReq();
    req.setConfirmOrderDetailsList(confirmOrderDetailsList);
    req.setPeopleNum(mPeopleNum);
    req.setTableId(mTableInfoId);
    req.setRemarks(mRemarks);
    //促销计划
    if (mPromotioInfo != null) {
      req.setPromotioInfoId(mPromotioInfo.getObjectId());
    }
    if (App.getContext() == null || ((App) App.getContext()).getUser() == null) return;
    req.setWaiterId(((App) App.getContext()).getUser().getObjectId());
    String s = new Gson().toJson(req);
    fireMessage.setData(s);
    mCurrentOrderUUID = UUID.randomUUID();
    fireMessage.setUUID(mCurrentOrderUUID);

    String json = new Gson().toJson(fireMessage);

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
    //显示提交对话框
    try {
      showSubmitDialog();
    } catch (MaterialDialog.DialogException e) {
      e.printStackTrace();
    }
  }

  /**
   * 添加商品
   */
  private void reqAddProd() {
    if (!NetUtils.isConnected(this.getApplicationContext())) {
      ToastUtils.showShort(this, "网络差，请稍后再试");
      return;
    }

    App app = (App) App.getContext();
    if (app == null || app.getUser() == null) {
      return;
    }

    FireMessage fireMessage = new FireMessage();
    fireMessage.setOperateType(FireMessage.ADD_PROD_REQ);
    mCurrentAddProdUUID = UUID.randomUUID();
    fireMessage.setUUID(mCurrentAddProdUUID);
    List<AppConfirmOrderDetails> confirmOrderDetailsList = new ArrayList<>();
    for (ShoppingCart shoppingCart : mShoppingCartList) {
      AppConfirmOrderDetails confirmOrderDetails = new AppConfirmOrderDetails();
      //商品id
      confirmOrderDetails.setProdId(shoppingCart.getDbProd().getObjectId());
      //规格id
      if (shoppingCart.getDbFormat() != null) {
        confirmOrderDetails.setFormatId(shoppingCart.getDbFormat().getObjectId());
      }
      //做法id
      if (shoppingCart.getDbMethod() != null) {
        confirmOrderDetails.setMethodId(shoppingCart.getDbMethod().getObjectId());
      }
      //数量
      confirmOrderDetails.setNum(shoppingCart.getNum().intValue());
      if (shoppingCart.getDbProd().getMultipleUnit().equals(PxProductInfo.IS_TWO_UNIT_TURE)) {
        //多单位数量
        confirmOrderDetails.setMultNum(shoppingCart.getMultipleUnitNum());
      }
      //延迟
      if (shoppingCart.getIsDelay() == true) {
        confirmOrderDetails.setIsDelay(1);
      } else {
        confirmOrderDetails.setIsDelay(0);
      }
      //备注
      confirmOrderDetails.setRemarks(shoppingCart.getRemarks());
      //添加
      confirmOrderDetailsList.add(confirmOrderDetails);
    }
    AddProdReq addProdReq = new AddProdReq();
    addProdReq.setOrderId(mOrderId);
    addProdReq.setTableId(mTableInfoId);
    addProdReq.setConfirmOrderDetailsList(confirmOrderDetailsList);
    addProdReq.setWaiterId(app.getUser().getObjectId());
    fireMessage.setData(new Gson().toJson(addProdReq));
    String json = new Gson().toJson(fireMessage);

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
    //显示提交对话框
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
   * OptionMenu
   */
  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.bill_cart_menu, menu);
    MenuItem menuPopup = menu.findItem(R.id.action_popup);
    menuPopup.setActionView(R.layout.layout_menu_popup);
    menuPopup.getActionView().setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (System.currentTimeMillis() - mLastMenuClickTime < 1000) {
          Toast.makeText(BillCartActivity.this, "请稍后点击", Toast.LENGTH_SHORT).show();
          return;
        }
        mLastMenuClickTime = System.currentTimeMillis();
        showSheet();
      }
    });
    return true;
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
   * 显示Sheet
   */
  private void showSheet() {
    if (mSweetSheet.isShow()) {
      mSweetSheet.dismiss();
    }
    mSweetSheet.toggle();
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
    Intent intent = new Intent(BillCartActivity.this, ModifyProdActivity.class);
    intent.putExtra(ModifyProdActivity.SHOPPING_CART, shoppingCart);
    intent.putExtra(ModifyProdActivity.IS_TEMPORARY, false);
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
                    .where(ShoppingCartDao.Properties.TableId.eq(mTableInfo.getId()))
                    .list();
                mCartAdapter = new ProdCartAdapter(BillCartActivity.this, mShoppingCartList);
                mCartAdapter.setOnCartProdClickListener(BillCartActivity.this);
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
                    .where(ShoppingCartDao.Properties.TableId.eq(mTableInfo.getId()))
                    .list();
                mCartAdapter = new ProdCartAdapter(BillCartActivity.this, mShoppingCartList);
                mCartAdapter.setOnCartProdClickListener(BillCartActivity.this);
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
    if (mChat != null && mChatMessageListener != null) {
      mChat.removeMessageListener(mChatMessageListener);
    }
    mChat = null;
    EventBus.getDefault().unregister(this);
    ButterKnife.unbind(this);
  }

  /**
   * 刷新购物车Event
   */
  @Subscribe(threadMode = ThreadMode.MAIN) public void refreshCartData(RefreshCartEvent event) {
    refreshCart();
  }

  /**
   * 刷新购物车
   */
  private void refreshCart() {
    mShoppingCartList = DaoServiceUtil.getShoppingCartService()
        .queryBuilder()
        .where(ShoppingCartDao.Properties.TableId.eq(mTableInfo.getId()))
        .list();
    if (mShoppingCartList != null && mShoppingCartList.size() != 0) {
      mCartAdapter.setData(mShoppingCartList);
    } else {
      mCartAdapter.setData(new ArrayList<ShoppingCart>());
    }
    calculateTotalInfo();
  }
}
