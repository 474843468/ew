package com.think.firewaiter.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.think.firewaiter.R;
import com.think.firewaiter.adapter.ServeTableAdapter;
import com.think.firewaiter.common.App;
import com.think.firewaiter.dao.DaoServiceUtil;
import com.think.firewaiter.dao.PxTableInfoDao;
import com.think.firewaiter.dao.UserDao;
import com.think.firewaiter.dao.UserTableRelDao;
import com.think.firewaiter.event.UpdateServeTableEvent;
import com.think.firewaiter.module.PxTableInfo;
import com.think.firewaiter.module.ServeTable;
import com.think.firewaiter.module.User;
import com.think.firewaiter.module.UserTableRel;
import com.think.firewaiter.ui.base.BaseToolbarActivity;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;

/**
 * Created by dorado on 2016/6/30.
 */
public class ServeTablesActivity extends BaseToolbarActivity
    implements ServeTableAdapter.OnTableSelectedListener {

  @Bind(R.id.rcv) RecyclerView mRcv;
  @Bind(R.id.btn_all_or_none) Button mBtnAllOrNone;

  private List<ServeTable> mServeTableList;
  private User mUser;
  private ServeTableAdapter mServeTableAdapter;

  //当前是否全选
  private boolean isAll;

  @Override protected String provideToolbarTitle() {
    return "选择服务的桌台";
  }

  @Override protected int provideContentViewId() {
    return R.layout.activity_serve_tables;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ButterKnife.bind(this);
    if (App.getContext() == null || ((App) App.getContext()).getUser() == null) return;
    mUser = ((App) App.getContext()).getUser();
    //初始化Rcv
    initRcv();
    //查询桌台
    queryTableList();
  }

  private void initRcv() {
    LinearLayoutManager manager =
        new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
    mRcv.setHasFixedSize(true);
    mRcv.setLayoutManager(manager);
    mServeTableList = new ArrayList<ServeTable>();
    mServeTableAdapter = new ServeTableAdapter(this, mServeTableList);
    mServeTableAdapter.setOnTableSelectedListener(this);
    mRcv.setAdapter(mServeTableAdapter);
  }

  /**
   * 查询桌台
   */
  private void queryTableList() {
    List<PxTableInfo> allTableList = DaoServiceUtil.getTableInfoService()
        .queryBuilder()
        .where(PxTableInfoDao.Properties.DelFlag.eq("0"))
        .orderAsc(PxTableInfoDao.Properties.Type, PxTableInfoDao.Properties.SortNo)
        .list();

    mServeTableList = new ArrayList<>();
    for (PxTableInfo pxTableInfo : allTableList) {
      ServeTable serveTable = new ServeTable();
      UserTableRel rel = DaoServiceUtil.getUserTableRelService()
          .queryBuilder()
          .where(UserTableRelDao.Properties.TableId.eq(pxTableInfo.getId()))
          .where(UserTableRelDao.Properties.UserId.eq(mUser.getId()))
          .unique();
      serveTable.setTableInfo(pxTableInfo);
      if (rel == null) {
        serveTable.setIsSelected(false);
      } else {
        serveTable.setIsSelected(true);
      }
      mServeTableList.add(serveTable);
    }
    mServeTableAdapter.setData(mServeTableList);
  }

  /**
   * 桌台变更
   */
  @Override public void onTableSelected(int pos, boolean selected) {
    User user = DaoServiceUtil.getUserService()
        .queryBuilder()
        .where(UserDao.Properties.ObjectId.eq(mUser.getObjectId()))
        .unique();
    PxTableInfo tableInfo = mServeTableList.get(pos).getTableInfo();
    UserTableRel rel = DaoServiceUtil.getUserTableRelService()
        .queryBuilder()
        .where(UserTableRelDao.Properties.UserId.eq(user.getId()))
        .where(UserTableRelDao.Properties.TableId.eq(tableInfo.getId()))
        .unique();
    if (selected) {
      if (rel == null) {
        UserTableRel userTableRel = new UserTableRel();
        userTableRel.setDbUser(user);
        userTableRel.setDbTable(tableInfo);
        DaoServiceUtil.getUserTableRelService().saveOrUpdate(userTableRel);
      }
    } else {
      if (rel != null) {
        DaoServiceUtil.getUserTableRelService().delete(rel);
      }
    }
    mServeTableList.get(pos).setIsSelected(selected);
    mServeTableAdapter.notifyItemChanged(pos);
  }

  @Override public void onBackPressed() {
    //更新桌台
    EventBus.getDefault().post(new UpdateServeTableEvent());
    super.onBackPressed();
  }

  /**
   * 全选按钮
   */
  @OnClick(R.id.btn_all_or_none) public void allOrNone() {
    if (isAll) {
      mBtnAllOrNone.setText("全选");
      selectedALL(false);
      isAll = false;
    } else {
      mBtnAllOrNone.setText("全不选");
      selectedALL(true);
      isAll = true;
    }
  }

  private void selectedALL(boolean sel) {
    for (int i = 0; i < mServeTableList.size(); i++) {
      onTableSelected(i, sel);
    }
  }
}
