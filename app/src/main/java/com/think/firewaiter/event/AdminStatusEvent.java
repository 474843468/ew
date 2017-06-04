package com.think.firewaiter.event;

/**
 * User: ylw
 * Date: 2017-02-17
 * Time: 14:31
 * 收银机admin 状态
 */
public class AdminStatusEvent {
  public static final String ADMIN_ONLINE = "收银端在线";//在线
  public static final String ADMIN_OFFLINE = "收银端已下线";//下线
  public static final String ADMIN_NOT_ONLINE = "收银端不在线";//离线
  private String mStatus = ADMIN_NOT_ONLINE;

  public AdminStatusEvent(String status) {
    mStatus = status;
  }

  public String getStatus() {
    return mStatus;
  }
}