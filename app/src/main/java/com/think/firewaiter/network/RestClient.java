package com.think.firewaiter.network;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;
import com.orhanobut.logger.Logger;
import com.think.firewaiter.config.Setting;
import com.think.firewaiter.utils.DigestsUtils;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

/**
 * User: ylw
 * Date: 2016-06-01
 * Time: 17:50
 * FIXME
 */
//@formatter:off
public abstract class RestClient {

  private final AsyncHttpClient mClient;
  private static Gson sGsonWithBuilder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
      .setDateFormat("yyyy-MM-dd HH:mm:ss")
      .create();
  public static String DEFAULT_ENCODING = "utf-8";

  public RestClient() {
    start();
    mClient = new AsyncHttpClient();
    mClient.setConnectTimeout(5000);
    mClient.setTimeout(10000);
    mClient.setMaxRetriesAndTimeout(1, 5000);
  }

  public RestClient(int retries, int timeout, int connectTimeOut, int responseTimeOut) {
    mClient = new AsyncHttpClient();
    mClient.setMaxRetriesAndTimeout(retries, timeout);
    mClient.setConnectTimeout(connectTimeOut);
    mClient.setResponseTimeout(responseTimeOut);
  }

  public void post(Context context, String url, HttpReq req) {
    try {
      long timestamp = System.currentTimeMillis();
      String json = new Gson().toJson(req);
      String sign = DigestsUtils.md5(json + Setting.SALT + timestamp);
      StringEntity stringEntity = new StringEntity(json, DEFAULT_ENCODING);
      mClient.post(context, url + "?timestamp=" + timestamp + "&sign=" + sign, stringEntity, "application/json;charset=utf-8", new TextHttpResponseHandler() {
            @Override public void onFailure(int statusCode, Header[] headers, String responseString,
                Throwable throwable) {
              finish();
              failure(responseString, throwable);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
              success(responseString);
            }

            @Override public void onProgress(long bytesWritten, long totalSize) {
              super.onProgress(bytesWritten, totalSize);
            }
          });
    } catch (Exception e) {
      failure(null, null);
    }
  }

  protected abstract void start();

  protected abstract void finish();

  protected abstract void failure(String responseString, Throwable throwable);

  protected abstract void success(String responseString);

  public static Gson getGsonWithBuilder() {
    return sGsonWithBuilder;
  }
}