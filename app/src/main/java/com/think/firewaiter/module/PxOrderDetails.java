package com.think.firewaiter.module;

/**
 * Entity mapped to table "OrderDetails".
 */
public class PxOrderDetails implements java.io.Serializable {

  private boolean isChecked;//标记选中,用于删除
  private Double mRefundNum;//用于批量修改的数量
  private Double mRefundCheckOutNum;//用于批量修改数量中的多单位

  public static final String STATUS_ORIDINARY = "0";
  public static final String STATUS_REFUND = "1";
  public static final String STATUS_DELAY = "2";

  private boolean isDiscountChecked;//标记选中，用于打折选择

  /**
   * 数据库主键id
   */
  private long id;
  /**
   * 对应服务器id
   */
  private String objectId;
  /**
   * 价格
   */
  private Double price;
  /**
   * 数量
   */
  private Double num;
  /**
   * 商品状态(0:正常 1：退菜 2：延迟)
   */
  private String status;
  /**
   * 折扣率(0-100)
   */
  private Double discountRate;
  /**
   * 是否打折（0：是 1：否）
   */
  private String isDiscount;
  /**
   * 已下单
   */
  private Boolean isOrdered;
  /**
   * 已结账数量
   */
  private Double payNum;
  /**
   * 该订单详情是否完结
   */
  private Boolean isFinish;
  /**
   * 结账单位数量 一般为重量
   */
  private Double checkOutNumber;
  /**
   * 所在订单
   */
  private PxOrderInfo dbOrder;
  /**
   * 商品信息
   */
  private PxProductInfo mProductInfo;//用于向服务生发送
  /**
   * 已上菜
   */
  private Boolean isServing;

  /**
   * Get and Set
   */
  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public boolean isDiscountChecked() {
    return isDiscountChecked;
  }

  public void setIsDiscountChecked(boolean isDiscountChecked) {
    this.isDiscountChecked = isDiscountChecked;
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  public Double getPrice() {
    return price;
  }

  public void setPrice(Double price) {
    this.price = price;
  }

  public Double getNum() {
    return num;
  }

  public void setNum(Double num) {
    this.num = num;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Double getDiscountRate() {
    return discountRate;
  }

  public void setDiscountRate(Double discountRate) {
    this.discountRate = discountRate;
  }

  public String getIsDiscount() {
    return isDiscount;
  }

  public void setIsDiscount(String isDiscount) {
    this.isDiscount = isDiscount;
  }

  public Boolean getIsOrdered() {
    return isOrdered;
  }

  public void setIsOrdered(Boolean isOrdered) {
    this.isOrdered = isOrdered;
  }

  public Double getPayNum() {
    return payNum;
  }

  public void setPayNum(Double payNum) {
    this.payNum = payNum;
  }

  public Boolean getIsFinish() {
    return isFinish;
  }

  public void setIsFinish(Boolean isFinish) {
    this.isFinish = isFinish;
  }

  public Double getCheckOutNumber() {
    return checkOutNumber;
  }

  public void setCheckOutNumber(Double checkOutNumber) {
    this.checkOutNumber = checkOutNumber;
  }

  public PxOrderInfo getDbOrder() {
    return dbOrder;
  }

  public void setDbOrder(PxOrderInfo dbOrder) {
    this.dbOrder = dbOrder;
  }

  public PxProductInfo getProductInfo() {
    return mProductInfo;
  }

  public void setProductInfo(PxProductInfo productInfo) {
    mProductInfo = productInfo;
  }

  public Double getRefundCheckOutNum() {
    return mRefundCheckOutNum;
  }

  public void setRefundCheckOutNum(Double refundCheckOutNum) {
    mRefundCheckOutNum = refundCheckOutNum;
  }

  public Double getRefundNum() {
    return mRefundNum;
  }

  public void setRefundNum(Double refundNum) {
    mRefundNum = refundNum;
  }

  public boolean isChecked() {
    return isChecked;
  }

  public void setIsChecked(boolean isChecked) {
    this.isChecked = isChecked;
  }

  public Boolean getIsServing() {
    return isServing;
  }

  public void setIsServing(Boolean isServing) {
    this.isServing = isServing;
  }
}
