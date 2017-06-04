package com.think.firewaiter.utils;

/**
 * Created by zjq on 2015/10/10.
 * 当浮点型数据位数超过10位之后，数据变成科学计数法显示。用此方法可以使其正常显示。
 */
public class NumberFormatUtils {

  public static String formatFloatNumber(double value) {
    if (value != 0.00) {
      boolean b = value < 1;
      java.text.DecimalFormat df = new java.text.DecimalFormat("########.00");
      return b ? String.valueOf(new Double(df.format(value))) : df.format(value);
    } else {
      return "0.00";
    }
  }

  public static String formatFloatNumber(Double value) {
    if (value != null) {
      if (value.doubleValue() != 0.00) {
        boolean b = value < 1;
        java.text.DecimalFormat df = new java.text.DecimalFormat("########.00");
        return b ? String.valueOf(new Double(df.format(value.doubleValue()))) : df.format(value.doubleValue());
      } else {
        return "0.00";
      }
    }
    return "";
  }
}
