package com.think.firewaiter.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.orhanobut.logger.Logger;
import com.think.firewaiter.R;
import com.think.firewaiter.adapter.TableTypeAdapter;
import com.think.firewaiter.adapter.TableTypeListAdapter;
import com.think.firewaiter.chat.FireMessage;
import com.think.firewaiter.chat.chatUtils.XMPPConnectUtils;
import com.think.firewaiter.chat.req.AllTableReq;
import com.think.firewaiter.chat.resp.AllTableResp;
import com.think.firewaiter.common.App;
import com.think.firewaiter.config.Setting;
import com.think.firewaiter.dao.DaoServiceUtil;
import com.think.firewaiter.dao.PxTableInfoDao;
import com.think.firewaiter.event.AdminStatusEvent;
import com.think.firewaiter.event.ChatConnectEvent;
import com.think.firewaiter.module.PxTableInfo;
import com.think.firewaiter.module.TableAreaType;
import com.think.firewaiter.module.User;
import com.think.firewaiter.utils.NetUtils;
import com.think.firewaiter.utils.ToastUtils;
import com.think.firewaiter.widget.TitleItemDecoration;
import com.think.firewaiter.widget.TouchMoveRcv;
import java.util.ArrayList;
import java.util.Collections;
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
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import static com.think.firewaiter.R.id.toolbar;

/**
 * User: ylw
 * Date: 2017-02-14
 * Time: 13:41
 * FIXME
 */
public class SelectedTableStartBillActivity extends AppCompatActivity {

  @Bind(R.id.rcv_table_type) RecyclerView mRcvTableType;
  @Bind(R.id.rcv_table_list) TouchMoveRcv mRcvTableList;

  @Bind(R.id.swp) SwipeRefreshLayout mSwp;//

  @Bind(toolbar) Toolbar mToolbar;
  @Bind(R.id.search_view) MaterialSearchView mSearchView;

  private List<TableAreaType> mTableAreaTypeList;
  private List<PxTableInfo> mTableList;
  private TableTypeListAdapter mTableListAdapter;
  private Chat mChat;
  private IMChatMessageListener mChatMessageListener;
  private UUID mCurrentUUID;
  private User mUser;
  private TableTypeAdapter mTableTypeAdapter;

  private boolean mMove = false;//Rcv 联动用

  private String mChatConnectStatus = null;//Smack 连接状态
  private String mAdminStatus = null;//收银端在线状态
  private TitleItemDecoration mTableListItemDecoration;// 桌台列表 头部
  private UIHandler mUiHandler;
  private int mExceptPos;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_selected_table_start_bill);
    ButterKnife.bind(this);
    //init user
    initUser();
    //init handler
    mUiHandler = new UIHandler();
    //init chat
    initChat();
    //init view
    initView();
    //regist Smack 连接状态、收银端在线状态
    EventBus.getDefault().register(this);
    EventBus.getDefault().getStickyEvent(ChatConnectEvent.class);
    EventBus.getDefault().getStickyEvent(AdminStatusEvent.class);
  }

  @Override protected void onResume() {
    super.onResume();
    //req
    mSwp.setRefreshing(true);
    reqEmptyTableList();
  }

  /**
   * init user
   */
  //@formatter:off
  private void initUser() {
    App app = (App) App.getContext();
    if (app == null) {
      ToastUtils.showShort(this, "App发生异常，请重新登录App");
      this.finish();
      return;
    }
    mUser = app.getUser();
    if (mUser == null) {
      ToastUtils.showShort(this, "App发生异常，请重新登录App");
      this.finish();
      return;
    }
  }

  /**
   * init chat
   */
  //@formatter:off
  private void initChat() {
    XMPPTCPConnection connection = XMPPConnectUtils.getConnection();
    if (connection == null){
      ToastUtils.showShort(this, "通信故障");
      this.finish();
      return;
    }
    ChatManager chatManager = ChatManager.getInstanceFor(connection);
    if (chatManager != null) {
      mChat = chatManager.createChat("admin" + mUser.getCompanyCode() + "@" + Setting.SERVER_NAME);
    }
    if (mChat == null) {
      ToastUtils.showShort(this, "通信故障");
      this.finish();
    } else { // 添加消息监听器
      mChatMessageListener = new IMChatMessageListener();
      mChat.addMessageListener(mChatMessageListener);
    }
  }

  /**
   * init view
   */
  private void initView() {
    //tool bar
    mToolbar.setTitle("选择开单桌台");
    setSupportActionBar(mToolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setDisplayShowHomeEnabled(true);
    mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        if (mSearchView.isSearchOpen()){
          mSearchView.closeSearch();
        }else {
          SelectedTableStartBillActivity.this.finish();
        }
      }
    });

    //search view
    mSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
      @Override public boolean onQueryTextSubmit(String query) {
       if (TextUtils.isEmpty(query)){
         ToastUtils.showShort(null,"输入有误");
         return false;
       }
        if (mSwp.isRefreshing()) return false;
        mSwp.setRefreshing(true);
        reqEmptyTableList(query);
        mSearchView.closeSearch();
        return true;
      }

      @Override public boolean onQueryTextChange(String newText) {
        return false;
      }
    });


    final EditText etSearch = (EditText) mSearchView.findViewById(R.id.searchTextView);

    final ImageButton btnSearch = (ImageButton) LayoutInflater.from(this).inflate(R.layout.view_btn_search, null);
    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT);
    mSearchView.addView(btnSearch,params);
    btnSearch.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        String query = etSearch.getText().toString();
          if (TextUtils.isEmpty(query)){
            ToastUtils.showShort(null,"输入有误");
            return ;
          }
        if (mSwp.isRefreshing()) return ;
          mSwp.setRefreshing(true);
          reqEmptyTableList(query);
          mSearchView.closeSearch();
      }
    });
    mSearchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
      @Override public void onSearchViewShown() {
        btnSearch.setVisibility(View.VISIBLE);
      }

      @Override public void onSearchViewClosed() {
        btnSearch.setVisibility(View.GONE);
      }
    });

    //rcv table list
    mTableList = new ArrayList<>();
    mTableListAdapter = new TableTypeListAdapter(this, mTableList,true);
    mTableListAdapter.setItemClickListener(new TableItemClickListener());
    final LinearLayoutManager tableListLm = new LinearLayoutManager(this);
    mRcvTableList.setLayoutManager(tableListLm);
    mRcvTableList.setAdapter(mTableListAdapter);
    mRcvTableList.addOnScrollListener(new TableListRcvOnScrollListener(tableListLm));

    //rcv table type
    mTableAreaTypeList = new ArrayList<>();
    mTableTypeAdapter = new TableTypeAdapter( this,mTableAreaTypeList);
    mRcvTableType.setAdapter(mTableTypeAdapter);
    mRcvTableType.setLayoutManager(new LinearLayoutManager(this));
    mTableTypeAdapter.addItemSelectedListener(new TableTypeSelectListener(tableListLm));

    //Swipe Refresh
    mSwp.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override public void onRefresh() {
        reqEmptyTableList();
      }
    });
  }


  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main_menu, menu);
    MenuItem item = menu.findItem(R.id.action_search);
    mSearchView.setMenuItem(item);
    return true;
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK){
      if (mSearchView.isSearchOpen()){
        mSearchView.closeSearch();
        return true;
      }
    }
    return super.onKeyDown(keyCode, event);
  }

  class UIHandler extends Handler{
    @Override public void handleMessage(android.os.Message msg) {
      if (msg.what == 100 && mSwp != null){
        mSwp.setRefreshing(true);
        reqEmptyTableList();
      }
    }
  }
  /**
   * chat 连接状态
   */
  @Subscribe(threadMode = ThreadMode.MAIN ,sticky = true)
  public void receiveChatConnectEvnet(ChatConnectEvent event) {
    if (mChatConnectStatus != null && mAdminStatus != null) {
      mUiHandler.postDelayed(new Runnable() {
        @Override public void run() {
          mUiHandler.sendEmptyMessage(100);
        }
      },1000);
    }
    mChatConnectStatus = event.getConnectStatus();
    if (!isConnectedAndAdminOnline()){
      stopRefreshing();
    }
  }

  /**
   * 收银端 在线状态
   */
  @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
  public void receiveAdminStatusEvnet(AdminStatusEvent event) {
    if (mChatConnectStatus != null && mAdminStatus != null) {
      mUiHandler.postDelayed(new Runnable() {
        @Override public void run() {
          mUiHandler.sendEmptyMessage(100);
        }
      },1000);
    }
    mAdminStatus = event.getStatus();
    if (!isConnectedAndAdminOnline()){
      stopRefreshing();
    }
  }

  /**
   * 是否可以请求数据
   */
  private boolean isConnectedAndAdminOnline() {
    return AdminStatusEvent.ADMIN_ONLINE.equals(mAdminStatus)
        && (ChatConnectEvent.CONNECTED.equals(mChatConnectStatus) || ChatConnectEvent.RECONNECTION_SUCCESSFUL.equals(mChatConnectStatus));
  }
  //@formatter:on

  /**
   * table list rcv scroll listener
   */
  class TableListRcvOnScrollListener extends RecyclerView.OnScrollListener {
    private LinearLayoutManager mLm;

    public TableListRcvOnScrollListener(LinearLayoutManager lm) {
      mLm = lm;
    }

    @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
      super.onScrolled(recyclerView, dx, dy);
      if (mMove) {
        int n = mExceptPos - mLm.findFirstVisibleItemPosition();
        if (0 <= n && n < mRcvTableList.getChildCount()) {
          int top = mRcvTableList.getChildAt(n).getTop();
          //计算 真实高度
          if (mTableListItemDecoration != null) {
            top = top - mTableListItemDecoration.getTitleHeight();
          }
          mRcvTableList.smoothScrollBy(0, top);
        }
        boolean canScrollVertically = mRcvTableList.canScrollVertically(1);
        //无法滑动 直接false
        mMove = !canScrollVertically ? false : mLm.findFirstVisibleItemPosition() != mExceptPos;
      } else {
        ////左边联动
        ////if (!recyclerView.canScrollVertically(1)) return;//无法下滑
        ////是否触摸滑
        //if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_DRAGGING) {
        //  int firstVisiblePos = mLm.findFirstVisibleItemPosition();
        //  List<PxTableInfo> tableInfoList = mTableListAdapter.getData();
        //  if (firstVisiblePos > 0 && firstVisiblePos < tableInfoList.size()) {
        //    String type = tableInfoList.get(firstVisiblePos).getType();
        //    //联动
        //    int selectedPos = getTypePosByType(type);
        //    mTableTypeAdapter.setSelectedItem(selectedPos);
        //    mRcvTableType.smoothScrollToPosition(selectedPos);
        //  }
        //}
      }
    }
  }

  /**
   * table type select listener
   */
  class TableTypeSelectListener implements TableTypeAdapter.ItemSelectedListener {
    private LinearLayoutManager mLm;

    public TableTypeSelectListener(LinearLayoutManager lm) {
      mLm = lm;
    }

    @Override public void itemSelected(int pos) {
      //if (!mRcvTableList.canScrollVertically(1)) return; //无法滑动
      String type = mTableTypeAdapter.getData().get(pos).getType();
      int firstTablePos = getFirstTablePosByType(type);
      mTableTypeAdapter.setSelectedItem(pos);
      //scroll
      moveToPosition(mLm, firstTablePos);
      mExceptPos = firstTablePos;
    }
  }
  //@formatter:off
  /**
   * table item click
   */
  class TableItemClickListener implements TableTypeListAdapter.ItemClickListener {
    @Override public void onItemClick(int pos) {
      Intent intent = new Intent(SelectedTableStartBillActivity.this, StartBillActivity.class);
      intent.putExtra("TableInfo", mTableListAdapter.getData().get(pos));
      startActivity(intent);
    }
  }
  /**
  * rcv 移动到指定位置
   */
  private void moveToPosition(LinearLayoutManager lm, int pos) {
    int firstItem = lm.findFirstVisibleItemPosition();
    int lastItem = lm.findLastVisibleItemPosition();
    //区分情况
    if (pos <= firstItem) {
      mRcvTableList.smoothScrollToPosition(pos);
    } else if (pos <= lastItem) {
      int top = mRcvTableList.getChildAt(pos - firstItem).getTop();
      if (mTableListItemDecoration != null){
        top = top -  mTableListItemDecoration.getTitleHeight();
      }
      mRcvTableList.smoothScrollBy(0, top);
    } else {
      mRcvTableList.smoothScrollToPosition(pos);
      mMove = true;
    }
  }


  //@formatter:off
   /**
   * 请求桌台列表
   */
  private void reqEmptyTableList(){
    reqEmptyTableList(null);
  }

  private void reqEmptyTableList(String like) {
    if (!NetUtils.isConnected(this)) {
      stopRefreshing();
      ToastUtils.showShort(this, "网络差，请稍后再试");
      return;
    }

    if (!isConnectedAndAdminOnline()){
      ToastUtils.showShort(null,"连接异常或收银端不在线!");
      stopRefreshing();
      return;
    }

    Gson gson = new Gson();
    FireMessage fireMessage = new FireMessage();
    fireMessage.setOperateType(FireMessage.ALL_EMPTY_TABLE_REQ);
    mCurrentUUID = UUID.randomUUID();
    fireMessage.setUUID(mCurrentUUID);
    //setData
    AllTableReq req = new AllTableReq();
    req.setLike(like);

    fireMessage.setData(gson.toJson(req));

    String json = gson.toJson(fireMessage);
    if (mChat != null && json != null) {
      try {
        mChat.sendMessage(json);
      } catch (SmackException.NotConnectedException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * chat 消息监听
   */
  class IMChatMessageListener implements ChatMessageListener {
    @Override public void processMessage(Chat chat, Message message) {
      String body = message.getBody();
      final FireMessage fireMessage = new Gson().fromJson(body, FireMessage.class);
      if (fireMessage == null || fireMessage.getOperateType() == 0) {
        stopRefreshing();
        return;
      }
      //所有空桌台列表回执
      if (fireMessage.getOperateType() != FireMessage.ALL_EMPTY_TABLE_RESP) {
        stopRefreshing();
        return;
      }
      //判断是否是之前的离线消息
      if (fireMessage.getUUID().equals(mCurrentUUID)) {
        String data = fireMessage.getData();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();;
        AllTableResp resp = gson.fromJson(data, AllTableResp.class);
        //所有空桌台
        final List<PxTableInfo> allEmptyTableList = resp.getTableInfoList();
        //sort 先type后sort
        Collections.sort(allEmptyTableList);
        //table type 数据
        final List<TableAreaType> areaTypeList = new ArrayList<>();
        String preType = "";
        for (int i = 0; i < allEmptyTableList.size(); i++) {
          PxTableInfo pxTableInfo = allEmptyTableList.get(i);
          PxTableInfo dbTable = DaoServiceUtil.getTableInfoService()
              .queryBuilder()
              .where(PxTableInfoDao.Properties.ObjectId.eq(pxTableInfo.getObjectId()))
              .unique();
          if (dbTable == null ) continue;
          pxTableInfo.setId(dbTable.getId());

          String currentType = pxTableInfo.getType();
          if (!currentType.equals(preType)) {
            TableAreaType type = new TableAreaType(1, currentType);
            areaTypeList.add(type);
          } else {
            TableAreaType type = areaTypeList.get(areaTypeList.size() - 1);
            type.setNum(type.getNum() + 1);
          }
          preType = currentType;
        }
        //rcv
        runOnUiThread(new Runnable() {
          @Override public void run() {
            setTableListData(allEmptyTableList);
            mTableTypeAdapter.setData(areaTypeList);
            //mRcvTableType.setSelection(0);
            mTableTypeAdapter.setSelectedItem(0);
            stopRefreshing();
            if (allEmptyTableList.isEmpty()){
              ToastUtils.showShort(null,"没有空桌台!");
            }
          }
        });
      }
    }
  }


  /**
  * 桌台列表填充数据
   */
  private void setTableListData(List<PxTableInfo> list){
    if (mTableListItemDecoration != null){
      mRcvTableList.removeItemDecoration(mTableListItemDecoration);
    }
    mTableListAdapter.setData(list);
    mTableListItemDecoration = new TitleItemDecoration(this,list);
    mRcvTableList.addItemDecoration(mTableListItemDecoration);
  }

  /**
  * stop swp refreshing
   */
  private void stopRefreshing() {
    if (mSwp.isRefreshing()) mSwp.setRefreshing(false);
  }

  /**
   * 获取第一个table pos
   */
  private int getFirstTablePosByType(String type) {
    List<PxTableInfo> data = mTableListAdapter.getData();
    for (int i = 0; i < data.size(); i++) {
      PxTableInfo pxTableInfo = data.get(i);
      if (type.equals(pxTableInfo.getType())) {
        return i;
      }
    }
    return 0;
  }

  /**
   * 获取左边table type 根部右边PxTableInfo
   */
  private int getTypePosByType(String type) {
    List<TableAreaType> data = mTableTypeAdapter.getData();
    for (int i = 0; i < data.size(); i++) {
      TableAreaType areaType = data.get(i);
      if (type.equals(areaType.getType())) {
        return i;
      }
    }
    return 0;
  }

  private void showToast(String msg){
    ToastUtils.showShort(null,msg);
  }
  @Override protected void onDestroy() {
    super.onDestroy();
    EventBus.getDefault().unregister(this);
    if (mChat != null && mChatMessageListener != null){
      mChat.removeMessageListener(mChatMessageListener);
    }
    if (mUiHandler!= null) {
      mUiHandler.removeCallbacksAndMessages(null);
    }
    ButterKnife.unbind(this);
  }
}