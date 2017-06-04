package com.think.firewaiter.event;

/**
 * Created by dorado on 2016/8/15.
 */
public class ChatConnectEvent {
  public static final String CONNECTED = "连接成功";
  public static final String CONNECTION_CLOSED = "连接断开";
  public static final String CONNECTION_CLOSED_ON_ERROR = "连接错误或已断开";
  public static final String RECONNECTION_SUCCESSFUL = "重连成功";
  public static final String RECONNECTION_FAILED = "重连失败";
  public static final String CONNECTING = "连接中...";
  private String mConnectStatus;

  public ChatConnectEvent(String connectStatus) {
    mConnectStatus = connectStatus;
  }

  public String getConnectStatus() {
    return mConnectStatus;
  }
}
