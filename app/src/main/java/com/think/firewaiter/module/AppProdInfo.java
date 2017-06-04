package com.think.firewaiter.module;

import java.io.Serializable;

/**
 * Created by zjq on 2016/5/4.
 */
public class AppProdInfo implements Serializable {
  private PxProductInfo mProductInfo;//商品信息
  private Double mMultipleUnitNumber;//双单位数量
  private Double mNumber;//数量
  private boolean isDelay;//是否等待
  private boolean isMultipleUnit;//是否为双单位

  public PxProductInfo getProductInfo() {
    return mProductInfo;
  }

  public void setProductInfo(PxProductInfo productInfo) {
    mProductInfo = productInfo;
  }

  public Double getMultipleUnitNumber() {
    return mMultipleUnitNumber;
  }

  public void setMultipleUnitNumber(Double multipleUnitNumber) {
    mMultipleUnitNumber = multipleUnitNumber;
  }

  public Double getNumber() {
    return mNumber;
  }

  public void setNumber(Double number) {
    mNumber = number;
  }

  public boolean isDelay() {
    return isDelay;
  }

  public void setIsDelay(boolean isDelay) {
    this.isDelay = isDelay;
  }

  public boolean isMultipleUnit() {
    return isMultipleUnit;
  }

  public void setIsMultipleUnit(boolean isMultipleUnit) {
    this.isMultipleUnit = isMultipleUnit;
  }
}
