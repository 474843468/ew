package com.think.firewaiter.utils;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * 屏幕分辨率计算工具类
 * Created by liuxin on 15/4/1.
 */
public class ScreenUtils {

    /**
     * 将dip/dp转换为px
     * @param context
     * @param dip
     * @return
     */
    public static int dp2px(Context context,float dip){
        return (int)(dip * getScale(context) + 0.5f);
    }

    /**
     * 将px转换为dip
     * @param context
     * @param px
     * @return
     */
    public static int px2dp(Context context,float px){
        return (int)(px / getScale(context) + 0.5f);
    }

    /**
     * 将sp转化为px
     * @param context
     * @param sp
     * @return
     */
    public static int sp2px(Context context,float sp){
        return (int)(sp * getFontScale(context) + 0.5f);
    }

    /**
     * 将px转换为sp
     * @param context
     * @param px
     * @return
     */
    public static int px2sp(Context context,float px){
        return (int)(px / getFontScale(context) + 0.5f);
    }

    public static DisplayMetrics getDm(Context context){
        return context.getResources().getDisplayMetrics();
    }

    public static float getScale(Context context){
        return getDm(context).density;
    }

    public static float getFontScale(Context context){
        return getDm(context).scaledDensity;
    }

}
