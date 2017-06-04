package com.think.firewaiter.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.google.gson.Gson;
import com.think.firewaiter.R;
import com.think.firewaiter.adapter.TableListAdapter;
import com.think.firewaiter.chat.AppTableStatus;
import com.think.firewaiter.chat.FireMessage;
import com.think.firewaiter.chat.chatUtils.XMPPConnectUtils;
import com.think.firewaiter.chat.req.TableStatusReq;
import com.think.firewaiter.chat.resp.TableStatusResp;
import com.think.firewaiter.common.App;
import com.think.firewaiter.config.Setting;
import com.think.firewaiter.dao.DaoServiceUtil;
import com.think.firewaiter.dao.PxTableInfoDao;
import com.think.firewaiter.dao.UserDao;
import com.think.firewaiter.dao.UserTableRelDao;
import com.think.firewaiter.event.ConnNotifyEvent;
import com.think.firewaiter.event.ChatConnectEvent;
import com.think.firewaiter.event.ReqTableStatusEvent;
import com.think.firewaiter.event.UpdateServeTableEvent;
import com.think.firewaiter.module.PxTableInfo;
import com.think.firewaiter.module.User;
import com.think.firewaiter.module.UserTableRel;
import com.think.firewaiter.ui.activity.MainActivity;
import com.think.firewaiter.ui.activity.OccupyTableActivity;
import com.think.firewaiter.ui.activity.StartBillActivity;
import com.think.firewaiter.utils.NetUtils;
import com.think.firewaiter.utils.ToastUtils;
import de.greenrobot.dao.query.Join;
import de.greenrobot.dao.query.QueryBuilder;
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

/**
 * Created by THINK on 2016/4/8.
 */
public class ParlorListFragment extends Fragment implements TableListAdapter.OnTableClickListener {

  @Bind(R.id.rcv_parlor_list) RecyclerView mRcvParlorList;
  @Bind(R.id.swipe_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
  private String mParam;
  private static final String PARLOR_LIST_PARAM = "param";
  private MainActivity mAct;
  private User mUser;
  //Data
  private List<PxTableInfo> mTableInfoList;
  //Adapter
  private TableListAdapter mTableListAdapter;

  //当前发送的UUID
  private UUID mCurrentUUID;
  private IMChatMessageListener mChatMessageListener;
  private Chat mChat;

  public static ParlorListFragment newInstance(String param) {
    ParlorListFragment fragment = new ParlorListFragment();
    Bundle args = new Bundle();
    args.putString(PARLOR_LIST_PARAM, param);
    fragment.setArguments(args);
    return fragment;
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EventBus.getDefault().register(this);
    if (getArguments() != null) {
      mParam = getArguments().getString(PARLOR_LIST_PARAM);
    }
  }

  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_parlor_list, null);
    mAct = (MainActivity) getActivity();
    ButterKnife.bind(this, view);
    return view;
  }

  @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    if (App.getContext() == null || ((App) App.getContext()).getUser() == null) return;
    mUser = ((App) App.getContext()).getUser();
    //初始化下拉刷新
    initSwipeRefresh();
    //初始化Rcv
    initRcv();
  }

  /**
   * 初始化下拉刷新
   */
  private void initSwipeRefresh() {
    mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override public void onRefresh() {
        reqParlorList();
      }
    });
  }

  /**
   * 初始化Rcv
   */
  private void initRcv() {
    LinearLayoutManager layoutManager =
        new LinearLayoutManager(mAct, LinearLayoutManager.VERTICAL, false);
    QueryBuilder<UserTableRel> relQb = DaoServiceUtil.getUserTableRelService().queryBuilder();
    Join<UserTableRel, User> userJoin = relQb.join(UserTableRelDao.Properties.UserId, User.class);
    userJoin.where(UserDao.Properties.ObjectId.eq(mUser.getObjectId()));
    Join<UserTableRel, PxTableInfo> tableJoin =
        relQb.join(UserTableRelDao.Properties.TableId, PxTableInfo.class);
    tableJoin.where(PxTableInfoDao.Properties.DelFlag.eq("0"));
    tableJoin.where(PxTableInfoDao.Properties.Type.eq(PxTableInfo.TYPE_PARLOR));

    List<UserTableRel> relList = relQb.list();
    if (relList == null || relList.size() == 0) {
      ToastUtils.showShort(mAct, "请在个人中心选择工作桌台");
    }
    mTableInfoList = new ArrayList<PxTableInfo>();
    for (UserTableRel rel : relList) {
      mTableInfoList.add(rel.getDbTable());
    }
    Collections.sort(mTableInfoList);

    mTableListAdapter = new TableListAdapter(mAct, mTableInfoList);
    mTableListAdapter.setOnTableClickListener(this);
    mRcvParlorList.setHasFixedSize(true);
    mRcvParlorList.setLayoutManager(layoutManager);
    mRcvParlorList.setAdapter(mTableListAdapter);
  }

  /**
   * 请求大厅列表
   */
  private void reqParlorList() {
    if (!NetUtils.isConnected(mAct.getApplicationContext())) {
      ToastUtils.showShort(mAct, "网络差，请稍后再试");
      if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
        mSwipeRefreshLayout.setRefreshing(false);
      }
      return;
    }
    //重置状态
    if (mTableInfoList == null) {
      return;
    }
    for (PxTableInfo tableInfo : mTableInfoList) {
      tableInfo.setStatusNow("");
      tableInfo.setActualPeopleNumber(0);
      tableInfo.setDuration(0);
    }
    mTableListAdapter.setData(mTableInfoList);

    //发请求
    mSwipeRefreshLayout.setRefreshing(true);
    FireMessage fireMessage = new FireMessage();
    fireMessage.setOperateType(FireMessage.PARLOR_LIST_REQ);
    mCurrentUUID = UUID.randomUUID();
    fireMessage.setUUID(mCurrentUUID);
    List<String> tableIdList = new ArrayList<String>();
    for (PxTableInfo tableInfo : mTableInfoList) {
      String tableId = tableInfo.getObjectId();
      tableIdList.add(tableId);
    }
    TableStatusReq req = new TableStatusReq();
    req.setTableIdList(tableIdList);
    fireMessage.setData(new Gson().toJson(req));

    String json = new Gson().toJson(fireMessage);

    //发送消息
    if (App.getContext() == null) {
      ToastUtils.showShort(mAct, "App发生异常，请重新登录App");
      return;
    }
    //发送消息
    ChatManager chatManager = ChatManager.getInstanceFor(XMPPConnectUtils.getConnection());
    if (chatManager != null){
      mChat = chatManager.createChat("admin" + ((App) (App.getContext())).getUser().getCompanyCode() + "@" + Setting.SERVER_NAME);
    }
    if (mChat != null && json != null) {
      try {
        mChat.sendMessage(json);
      } catch (SmackException.NotConnectedException e) {
        //关闭刷新
        if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
          mSwipeRefreshLayout.setRefreshing(false);
        }
        e.printStackTrace();
      }
      mChatMessageListener = new IMChatMessageListener();
      mChat.addMessageListener(mChatMessageListener);
    }
  }

  class IMChatMessageListener implements ChatMessageListener {
    @Override public void processMessage(Chat chat, Message message) {
      String body = message.getBody();
      final FireMessage fireMessage = new Gson().fromJson(body, FireMessage.class);
      if (fireMessage == null || fireMessage.getOperateType() == 0) {
        return;
      }
      //桌台列表 回执
      if (fireMessage.getOperateType() == FireMessage.PARLOR_LIST_RESP) {
        mAct.runOnUiThread(new Runnable() {
          @Override public void run() {
            //关闭刷新
            if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
              mSwipeRefreshLayout.setRefreshing(false);
            }
            //判断是否是之前的离线消息
            if (fireMessage.getUUID().equals(mCurrentUUID)) {
              String data = fireMessage.getData();
              TableStatusResp resp = new Gson().fromJson(data, TableStatusResp.class);
              List<AppTableStatus> tableStatusList = resp.getTableStatusList();
              for (PxTableInfo tableInfo : mTableInfoList) {
                for (AppTableStatus tableStatus : tableStatusList) {
                  if (tableInfo.getObjectId().equals(tableStatus.getTableId()) == false) {
                    continue;
                  }
                  tableInfo.setStatusNow(tableStatus.getStatus());
                  tableInfo.setActualPeopleNumber(tableStatus.getActualPeopleNumber());
                  tableInfo.setDuration(tableStatus.getDuration());
                }
              }
              mTableListAdapter.setData(mTableInfoList);
            }
          }
        });
      }
    }
  }

  @Override public void onResume() {
    super.onResume();
    boolean userVisibleHint = getUserVisibleHint();
    if (userVisibleHint) {
      reqParlorList();
    }
  }

  @Override public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    if (isVisibleToUser && mAct != null) {
      reqParlorList();
    }
  }

  /**
   * 桌台点击
   */
  @Override public void onEmptyClick(int pos) {
    if (!NetUtils.isConnected(mAct)) {
      ToastUtils.showShort(mAct, "暂无网络");
      return;
    }
    Intent intent = new Intent(mAct, StartBillActivity.class);
    intent.putExtra("TableInfo", mTableInfoList.get(pos));
    startActivity(intent);
  }

  @Override public void onOccupyClick(int pos) {
    if (!NetUtils.isConnected(mAct)) {
      ToastUtils.showShort(mAct, "暂无网络");
      return;
    }
    Intent intent = new Intent(mAct, OccupyTableActivity.class);
    intent.putExtra("TableId", mTableInfoList.get(pos).getObjectId());
    startActivity(intent);
  }

  @Override public void onOvertimeClick(int pos) {
    ToastUtils.showShort(mAct, "订单已经超过12小时未结账，请到收银端操作");
  }

  @Override public void onInvalidClick(int pos) {
    ToastUtils.showShort(mAct, "非最新数据，请刷新后再次尝试");
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    if (mChat != null && mChatMessageListener != null){
      mChat.removeMessageListener(mChatMessageListener);
    }
    EventBus.getDefault().unregister(this);
    //重置ButterKnife
    ButterKnife.unbind(this);
  }

  /**
   * MainActivity建立chat后发送请求
   */
  @Subscribe(threadMode = ThreadMode.MAIN) public void reqEvent(ReqTableStatusEvent event) {
    if (getUserVisibleHint()) {
      reqParlorList();
    }
  }

  /**
   * 断开时显示刷新按钮
   */
  @Subscribe(threadMode = ThreadMode.MAIN) public void disconnNotifyEvent(
      ChatConnectEvent event) {
    if (mSwipeRefreshLayout != null && getUserVisibleHint()) {
      if (NetUtils.isConnected(mAct) && !mSwipeRefreshLayout.isRefreshing()) {
        mSwipeRefreshLayout.setRefreshing(true);
      } else {
        mSwipeRefreshLayout.setRefreshing(false);
      }
      //置为待刷新状态
      if (mTableInfoList != null && mTableListAdapter != null) {
        for (int i = 0; i < mTableInfoList.size(); i++) {
          PxTableInfo pxTableInfo = mTableInfoList.get(i);
          pxTableInfo.setStatusNow(null);
        }
        mTableListAdapter.setData(mTableInfoList);
      }
    }
  }

  /**
   * 重连成功时显示刷新按钮
   */
  @Subscribe(threadMode = ThreadMode.MAIN) public void connNotiftEvent(ConnNotifyEvent event) {
    if (mSwipeRefreshLayout != null && !mSwipeRefreshLayout.isRefreshing()) {
      mSwipeRefreshLayout.setRefreshing(true);
    }
  }

  /**
   * 更新桌台列表
   */
  @Subscribe(threadMode = ThreadMode.MAIN) public void updateTableList(
      UpdateServeTableEvent event) {
    if (App.getContext() == null || ((App) App.getContext()).getUser() == null) return;
    QueryBuilder<UserTableRel> relQb = DaoServiceUtil.getUserTableRelService().queryBuilder();
    Join<UserTableRel, User> userJoin = relQb.join(UserTableRelDao.Properties.UserId, User.class);
    userJoin.where(UserDao.Properties.ObjectId.eq(mUser.getObjectId()));
    Join<UserTableRel, PxTableInfo> tableJoin =
        relQb.join(UserTableRelDao.Properties.TableId, PxTableInfo.class);
    tableJoin.where(PxTableInfoDao.Properties.DelFlag.eq("0"));
    tableJoin.where(PxTableInfoDao.Properties.Type.eq(PxTableInfo.TYPE_PARLOR));

    List<UserTableRel> relList = relQb.list();
    mTableInfoList = new ArrayList<PxTableInfo>();
    for (UserTableRel rel : relList) {
      mTableInfoList.add(rel.getDbTable());
    }
    Collections.sort(mTableInfoList);
    mTableListAdapter.setData(mTableInfoList);
  }
}
